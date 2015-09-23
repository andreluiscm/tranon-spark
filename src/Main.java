import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import scala.Tuple2;

public class Main {

	public static void main(String[] args) {

		String input = args[0];
		String output = args[1];
		String output2 = args[2];
		int m = Integer.valueOf(args[3]);
		int k = Integer.valueOf(args[4]);
		
		SparkConf conf = new SparkConf();
		JavaSparkContext sc = new JavaSparkContext(conf);
		
		JavaRDD<String> rddInput = sc.textFile("hdfs://ldiag-master:9000/user/tranon" + input + "/*").cache();
		
		// x1x2x3x4 \n x1x2x4x5 ...
		JavaRDD<String> rddCombinations = rddInput.flatMap(
				new FlatMapFunction<String, String>() {
					public Iterable<String> call(String x) {
						if (Splitter.on(", ").trimResults().omitEmptyStrings().splitToList(x).size() >= m) {
							
							List<String> alSubtrajectory = new ArrayList<String>();
							Combinations<String> combinations = new Combinations<String>(Splitter.on(", ").trimResults().omitEmptyStrings().splitToList(x), m);
							
							for (Iterator<List<String>> iterator = combinations.iterator(); iterator.hasNext();) {
								
								List<String> subtrajectory = iterator.next();
								Collections.sort(subtrajectory);
								alSubtrajectory.add(Util.listToString(subtrajectory));
								
							}
							
							return alSubtrajectory;
							
						}
						
						return Arrays.asList();
						
					}
				}).mapToPair(
						new PairFunction<String, String, Integer>() {
							public Tuple2<String, Integer> call(String x) {
								return new Tuple2(x, 1);
							}
						}).reduceByKey(
								new Function2<Integer, Integer, Integer>() {
									public Integer call(Integer x, Integer y) throws Exception {
										return x + y;
									}
								}).flatMap(
										new FlatMapFunction<Tuple2<String,Integer>, String>() {
											@Override
											public Iterable<String> call(Tuple2<String, Integer> tuple) throws Exception {
												if (tuple._2() < k)
													return Arrays.asList(tuple._1());
												else
													return Arrays.asList();
											}
										});
		
		JavaRDD<String> rddTempCombinations = rddCombinations;
		List<String> alHittingSet = new ArrayList<String>();
		
		while (rddTempCombinations.count() > 0) {
			
			// x1 \t 5 \n x2 \t 4 ...
			JavaRDD<String> rddPoiCount = rddTempCombinations.flatMap(
					new FlatMapFunction<String, String>() {
						public Iterable<String> call(String x) {
							
							List<String> alPois = new ArrayList<String>();
							Combinations<String> combinations = new Combinations<String>(Splitter.on("x").trimResults().omitEmptyStrings().splitToList(x), 1);
							
							for (Iterator<List<String>> iterator = combinations.iterator(); iterator.hasNext();) {
								
								List<String> subset = iterator.next();
								alPois.add("x" + Util.listToString(subset));

							}
							
							return alPois;
							
						}
					}).mapToPair(
							new PairFunction<String, String, Integer>() {
								public Tuple2<String, Integer> call(String x) {
									return new Tuple2(x, 1);
								}
							}).reduceByKey(
									new Function2<Integer, Integer, Integer>() {
										public Integer call(Integer x, Integer y) throws Exception {
											return x + y;
										}
									}).mapToPair(
											new PairFunction<Tuple2<String,Integer>, Integer, String>() {
												public Tuple2<Integer, String> call(Tuple2<String, Integer> tuple) throws Exception {
													return tuple.swap();
												}
											}).sortByKey(false).mapToPair(
													new PairFunction<Tuple2<Integer,String>, String, Integer>() {
														public Tuple2<String, Integer> call(Tuple2<Integer, String> tuple) throws Exception {
															return tuple.swap();
														}
													}).flatMap(
															new FlatMapFunction<Tuple2<String,Integer>, String>() {
																@Override
																public Iterable<String> call(Tuple2<String, Integer> tuple) throws Exception {
																	return Arrays.asList(tuple._1() + "\t" + String.valueOf(tuple._2()));
																}
															});
			
			String poi = Splitter.on("\t").trimResults().omitEmptyStrings().splitToList(rddPoiCount.first()).get(0);

			alHittingSet.add(poi);

			rddTempCombinations = rddTempCombinations.filter(s -> !s.contains(poi));
			
		}
		
		JavaRDD<String> rddHittingSet = sc.parallelize(alHittingSet);
		
		JavaRDD<String> rddOutput = rddInput.map(
				new Function<String, String>() {
					public String call(String trajectory) throws Exception {
						List<String> alTrajectoryPois = new ArrayList<String>(Splitter.on(", ").trimResults().omitEmptyStrings().splitToList(trajectory));
						alTrajectoryPois.removeAll(alHittingSet);

						return Joiner.on(", ").join(alTrajectoryPois);
					};
				});
		
		rddOutput.saveAsTextFile("hdfs://ldiag-master:9000/user/tranon" + output);
		rddHittingSet.saveAsTextFile("hdfs://ldiag-master:9000/user/tranon" + output2);
		
		sc.close();
		
	}
	
}