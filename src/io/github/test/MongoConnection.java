package io.github.test;

import io.github.sqlconnection.BaseConnection;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoConnection {
	static HashSet<String> pos;
	static HashSet<String> neg;
	static HashMap<String, Integer> vocab; //index of word in tf_idf array
	static HashMap<String, Integer> dfMap;
	static int n = 500;
	static int V = 12007;
	
	public static void main(String[] args){
		
		BaseConnection bc = new BaseConnection();
		bc.connect();
			
		bc.setDBAndCollection("cs336", "unlabel_review_after_splitting");
		DBCursor cursor1 = bc.getCursor();
		
		bc.setDBAndCollection("cs336", "selected");
		DBCursor cursor2 = bc.getCursor();
		
		vocab = new HashMap<String, Integer>();
		dfMap = new HashMap<String, Integer>();
		initVocab(cursor1);

		//set R to hold the six reviews
		ArrayList<BasicDBList> setR = new ArrayList<BasicDBList>();
		while(cursor2.hasNext()){
			DBObject line = cursor2.next();
			setR.add((BasicDBList) line.get("review"));
		}
		
		double[] rStar = tf_idf(setR.get(0));
		double[] r1 = tf_idf(setR.get(1));
		double[] r2 = tf_idf(setR.get(2));
		double[] r3 = tf_idf(setR.get(3));
		double[] r4 = tf_idf(setR.get(4));
		double[] r5 = tf_idf(setR.get(5));
		
		double[] query = new double[V];
		//good movie
		String word1 = "good";
		String word2 = "movie";
		query[vocab.get(word1)] = idf(dfMap.get(word1)) * logFreq(1);
		System.out.println(logFreq(1) + "---" + idf(dfMap.get(word1)) + "-----" + query[vocab.get(word1)]);
		query[vocab.get(word2)] = idf(dfMap.get(word2)) * logFreq(1);
		System.out.println(logFreq(1) + "---" + idf(dfMap.get(word2)) + "-----" + query[vocab.get(word2)]);

		System.out.println(cosSimilarity(rStar, r1));
		System.out.println(cosSimilarity(rStar, r2));
		System.out.println(cosSimilarity(rStar, r3));
		System.out.println(cosSimilarity(rStar, r4));
		System.out.println(cosSimilarity(rStar, r5));
		System.out.println("----------");
		System.out.println(cosSimilarity(query, rStar));
		System.out.println(cosSimilarity(query, r1));
		System.out.println(cosSimilarity(query, r2));
		System.out.println(cosSimilarity(query, r3));
		System.out.println(cosSimilarity(query, r4));
		System.out.println(cosSimilarity(query, r5));
		bc.close();
	}
	
	//compute the cosine similarity of query and document
	private static double cosSimilarity(double[] q, double d[]){
		double result = 0;
		//to hard to name them for the formula
		double a = 0, b = 0, c = 0;
		for(int i = 0; i < V; i++){
			a += q[i] * d[i];
			b += q[i] * q[i];
			c += d[i] * d[i];
		}
		result = a / (Math.sqrt(b) * Math.sqrt(c));
		return result;
	}
	
	//construct the arr with corresponding tf_idf weight
	private static double[] tf_idf(BasicDBList reviews){
		double[] arr = new double[V];
		for(int i = 0; i < reviews.size(); i++){
			BasicDBObject review = (BasicDBObject) reviews.get(i);
			String word = review.getString("word");
			double idf = idf(dfMap.get(word));
			double logFreq = logFreq(Integer.parseInt(review.getString("count")));
			//System.out.println(word + dfMap.get(word) +"++++"+ idf + "---" + logFreq + "----" + idf * logFreq);
			arr[vocab.get(word)] = idf * logFreq;
		}
		return arr;
	}
	
	//idf
	private static double idf(double df){
		if(df <= 0)
			return 0;
		else
			return Math.log10(n / df);
	}
	
	//log-frequency weighting
	private static double logFreq(int tf){
		if(tf <= 0)
			return 0;
		else{
			return Math.log10(tf) + 1;
		}
	}
	
	//number of reviews contain term t
	private static int df(String t, DBCursor cursor1){
		int count = 0;
		DBCursor cursor = cursor1.copy();
		while(cursor.hasNext()){

			DBObject line = cursor.next();

			BasicDBList reviews = (BasicDBList) line.get("review");
			
			for(int i = 0; i < reviews.size(); i++){
				BasicDBObject review = (BasicDBObject) reviews.get(i);
				String word = review.getString("word");
				if(t.equals(word)){
					count++;
					break;
				}
			}
		}
		return count;
	}
	private static void initVocab(DBCursor cursor){
		int count = 0;
		DBCursor cursor1 =cursor.copy();
		while(cursor.hasNext()){
			DBObject line = cursor.next();
			BasicDBList reviews = (BasicDBList) line.get("review");
			
			for(int i = 0; i < reviews.size(); i++){
				BasicDBObject review = (BasicDBObject) reviews.get(i);
				String word = review.getString("word");
				
				if(!vocab.containsKey(word)){
					vocab.put(word, count);
					dfMap.put(word, df(word, cursor1));
					//System.out.println(word + df(word, cursor1));
					count++;
				}
			}
		}

		

	}
	
	
	//put the pos & neg words into HashSet
	private static void initWords() throws IOException{
		pos = new HashSet<String>();
		neg = new HashSet<String>();
		
		BufferedReader br = new BufferedReader(new FileReader("positive words.txt"));
		String line;
		while((line = br.readLine()) != null){
			pos.add(line);
		}
		br.close();
		
		br = new BufferedReader(new FileReader("negative words.txt"));
		while((line = br.readLine()) != null){
			neg.add(line);
		}
		br.close();
	}
}
