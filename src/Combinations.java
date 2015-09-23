import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.math.BigIntegerMath;

public class Combinations<T> implements Iterable<List<T>>{

	private static final int THREAD_NUMBER = 4;
	private final T[] elements;
	private final int[] combinationIndices;
	private long remainingCombinations;
	private long totalCombinations;
	
	private static int arraylength;
	private static int totallength;
	
	private static List<Integer> list;
	private static int length;
	private static int j;

	public static void main(String[] args) {
		
		list = new ArrayList<Integer>();
		length = 5;
		
		for(int i=0; i < 150; i++){
			list.add(i);
		}

		totallength = Combinations.calculateTotalCombinations(list.size(), length).intValue();
		arraylength = (int) Math.ceil((double) totallength / THREAD_NUMBER);
		
		for (int i = 0; i < THREAD_NUMBER; i++) {
			new Thread("" + i) {
				
				int coun = 0;
				
				public void run() {
					
					System.out.println("Thread: " + getName() + " running");
					j = Integer.parseInt(getName());
					
					long start = System.currentTimeMillis();
					
					Combinations<Integer> c = new Combinations<Integer>(list, length, arraylength*j + 1, arraylength*(j+1));
					for (List<Integer> l : c) {
						coun++;
						
						/** 
						 * map 
						 */
						
					}
					
					long elapsed = System.currentTimeMillis() - start;
					System.out.println("tempo total:" + elapsed + " milisegundos");
					
					System.out.println("total de combinacoes: " + coun);
				}
			}.start();
		}
		
	}
	
	/**
	 * Generates all combinations of a specified length from the given set of size k
	 * @param elements
	 * @param length
	 * @param length k
	 */
	public Combinations(T[] elements, int length, int from, int to){
		
		if(from > to){
			throw new IllegalArgumentException("Combination from cannot be greater than combination to.");
		}
		
		if(length > elements.length){
			throw new IllegalArgumentException("Combination length cannot be greater than set size.");
		}

		this.elements = elements.clone(); 
		this.combinationIndices = new int[length];

		BigInteger total = calculateTotalCombinations(elements.length, length);
		
		if(from <= 0){
			from = 1;
		}
		
		if(to > total.intValue()){
			to = total.intValue();
		}
		
		
		if(total.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0){
			throw new IllegalArgumentException("Total number of combinations must not be more than 2^63."); 
		}

		totalCombinations = total.longValue();
		reset();
		
		if(from > 0){
			generateNextCombinationIndices(from - 1);
		}
		
		totalCombinations = to + 1 - from ;
		remainingCombinations = totalCombinations;
	}
	
	/**
	 * Generates all combinations of a specified length from the given set.
	 * @param elements
	 * @param length
	 */
	public Combinations(T[] elements, int length){
		
		if (length > elements.length){
			throw new IllegalArgumentException("Combination length cannot be greater than set size.");
		}

		this.elements = elements.clone(); 
		this.combinationIndices = new int[length];

		BigInteger total = calculateTotalCombinations(elements.length, length);

		if (total.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0){
			throw new IllegalArgumentException("Total number of combinations must not be more than 2^63."); 
		}

		totalCombinations = total.longValue();
		reset();
	}
	
	
	
	/**
	 * Calculate total of combinations
	 * @param n (total elements)
	 * @param k (chosen elements)
	 * @return
	 */
	public static BigInteger calculateTotalCombinations(int n, int k) {
		if(n >= k){
			BigInteger sizeFactorial = BigIntegerMath.factorial(n);
			BigInteger lengthFactorial = BigIntegerMath.factorial(k);
			BigInteger differenceFactorial;
			differenceFactorial = BigIntegerMath.factorial(n - k);
			return sizeFactorial.divide(differenceFactorial.multiply(lengthFactorial));
		}else{
			return BigInteger.ZERO;
		}
	}

	/**
	 * Generates all combinations of a specified length from the given set.
	 * @param elements
	 * @param length
	 */
	@SuppressWarnings("unchecked")
	public Combinations(Collection<T> elements, int length){
		this(elements.toArray((T[]) new Object[elements.size()]), length);
	}
	
	/**
	 * Generates all combinations of a specified length from the given set, omitting k
	 * @param elements
	 * @param length
	 * @param k
	 */
	@SuppressWarnings("unchecked")
	public Combinations(Collection<T> elements, int length, int k, int to){
		this(elements.toArray((T[]) new Object[elements.size()]), length, k, to);
	}

	/**
	 * Reset combinations.
	 */
	public final void reset()
	{
		for (int i = 0; i < combinationIndices.length; i++){
			combinationIndices[i] = i;
		}
		remainingCombinations = totalCombinations;
	}

	/**
	 * @return The number of combinations not yet generated.
	 */
	public long getRemainingCombinations(){
		return remainingCombinations;
	}

	/**
	 * Are there more combinations?
	 * @return true if there are more combinations available, false otherwise.
	 */
	public boolean hasMore(){
		return remainingCombinations > 0;
	}

	/**
	 * @return The total number of combinations.
	 */
	public long getTotalCombinations(){
		return totalCombinations;
	}

	/**
	 * Generate the next combination and return a list containing the
	 * appropriate elements.
	 * @return
	 */
	public List<T> nextCombinationAsList(){
		return nextCombinationAsList(new ArrayList<T>(elements.length));
	}

	/**
	 * Generate the next combination as List
	 * @param destination
	 * @return
	 */
	public List<T> nextCombinationAsList(List<T> destination){
		generateNextCombinationIndices(0);
		// Generate actual combination.
		destination.clear();
		for (int i : combinationIndices)
		{
			destination.add(elements[i]);
		}
		return destination;
	}

	/**
	 * Generate the indices into the elements array for the next combination. 
	 * @param k (number of combination indices omitted)
	 */
	private void generateNextCombinationIndices(int k){
		for(int o=0; o <= k; o++){
			if (remainingCombinations == 0){
				throw new IllegalStateException("There are no combinations remaining.  " +
						"Generator must be reset to continue using.");
			}
			else if (remainingCombinations < totalCombinations){
				int i = combinationIndices.length - 1;
				while (combinationIndices[i] == elements.length - combinationIndices.length + i){
					i--;
				}
				++combinationIndices[i];
				for (int j = i + 1; j < combinationIndices.length; j++){
					combinationIndices[j] = combinationIndices[i] + j - i;
				}
			}
			--remainingCombinations;
		}
	}

	/**
	 * Re-implements Iterator.
	 */
	public Iterator<List<T>> iterator(){
		return new Iterator<List<T>>(){

			public boolean hasNext(){
				return hasMore();
			}

			public List<T> next(){
				return nextCombinationAsList();
			}

			public void remove(){
				throw new UnsupportedOperationException("Iterator does not support removal.");
			}
		};
	}

}