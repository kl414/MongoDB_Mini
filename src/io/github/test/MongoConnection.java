package io.github.test;

import io.github.sqlconnection.BaseConnection;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

public class MongoConnection {
	static HashSet<String> pos;
	static HashSet<String> neg;
	
	public static void main(String[] args){
		try {
			initWords();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		BaseConnection bc = new BaseConnection();
		bc.connect();
		bc.setDBAndCollection("cs336", "unlabel_review_after_splitting");
		
		int value = 0;
		
		//get each review out of the review list of splitted words
		DBCursor cursor = bc.getCursor();
		while(cursor.hasNext()){
			DBObject line = cursor.next();
			BasicDBList reviews = (BasicDBList) line.get("review");
			
			for(int i = 0; i < reviews.size(); i++){
				BasicDBObject review = (BasicDBObject) reviews.get(i);
				value += evaluateReview(review);
			}
			
			if(value >= 0){
				//positive review
			}else{
				//negative review
			}
		}
		
		bc.close();
	}
	
	private static int evaluateReview(BasicDBObject review){
		int value = 0;
		
		String word = review.getString("word");
		int count = review.getInt("count");
		if(pos.contains(word)){
			value += count;
		}else if(neg.contains(word)){
			value -= count;
		}else{
			//do nothing for word that is not positive or negative
		}
		
		return value;
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
