//package br.ufc.tranon.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
//import java.util.PriorityQueue;
import java.util.Set;
import java.util.StringTokenizer;

//import br.ufc.tranon.infrequent.Element;

public class Util {

	public Util(String inputPath) {

	}

	public static HashSet<String> stringToSet(String string){
		HashSet<String> myHashSet = new HashSet<String>();

		StringTokenizer st = new StringTokenizer(string.trim(), ", ");

		while(st.hasMoreTokens()){
			String s = st.nextToken();
			myHashSet.add(s);
		}
		
		return myHashSet;
	}

	public static String listToString(List<String> list){
		String string = "";

		for (String s : list) {
			string = string + s;
		}

		return string;
	}

	public static String setToString(Set<String> set){
		String string = "";

		if (set.isEmpty()) {
			return string;
		}

		for (String s : set) {
			string = string + s + ", ";
		}

		string = string.substring(0, string.length() - 2);

		return string;
	}

	public static void listToFile(List<String> list) throws IOException{
		Files.write(Paths.get("infrequent.txt"), Util.listToString(list).getBytes());
	}

	public static Set<String> fileToSet(File f) throws IOException{
		List<String> lines = Files.readAllLines(Paths.get(f.getAbsolutePath()), Charset.forName("UTF-8"));
		String[] s = lines.get(0).split(",");

		Set<String> result = new HashSet<String>();

		for (int i = 0; i < s.length; i++) {
			result.add(s[i].trim());
		}

		return result;
	}
	
	public static Set<String> fileToSet2(File f) throws IOException{
		Set<String> result = new HashSet<String>();
		
		for (String string : Files.readAllLines(Paths.get(f.getAbsolutePath()), Charset.forName("UTF-8"))) {
			String[] s = string.split("\t");
			result.add(s[0].trim());
		}

		return result;
	}
	
	public static List<String> fileToList(File f) throws IOException{
		List<String> lines = Files.readAllLines(Paths.get(f.getAbsolutePath()), Charset.forName("UTF-8"));
		String[] s = lines.get(0).split(",");

		List<String> result = new ArrayList<String>();

		for (int i = 0; i < s.length; i++) {
			result.add(s[i].trim());
		}

		return result;
	}
	
//	public static PriorityQueue<Element> fileToQueue(File f) throws IOException{
//		List<String> lines = Files.readAllLines(Paths.get(f.getAbsolutePath()), Charset.forName("UTF-8"));
//		
//		PriorityQueue<Element> result = new PriorityQueue<Element>();
//
//		for (String line : lines) {
//			result.add(new Element(line.trim().split("\t")[0], Integer.parseInt(line.trim().split("\t")[1])));
//		}
//		
//		return result;
//	}
	
	public static void copyFile(File source, File dest) throws IOException {
	    InputStream is = null;
	    OutputStream os = null;
	    try {
	        is = new FileInputStream(source);
	        os = new FileOutputStream(dest);
	        byte[] buffer = new byte[1024];
	        int length;
	        while ((length = is.read(buffer)) > 0) {
	            os.write(buffer, 0, length);
	        }
	    } finally {
	        is.close();
	        os.close();
	    }
	}
}