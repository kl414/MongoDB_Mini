package io.github.sqlconnection;

import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class BaseConnection {
	private String ip = "localhost";
	private int port = 27017;
	
	MongoClient mongo = null;	
	private DB currentDB = null;
	protected DBCollection currentCollection = null;
	

	/**
	 * construction function with different parameters
	 */
	public BaseConnection(){};
	public BaseConnection(String ip){
		this.ip = ip;
	}
	public BaseConnection(int port){
		this.port = port;
	}
	public BaseConnection(String ip, int port){
		this.ip = ip; this.port = port;
	}
	
	
	
	public void connect(){
		mongo = new MongoClient(this.ip,this.port);
	}
	
	public void setDBAndCollection(String dbName, String collectionName){
		this.currentDB = mongo.getDB(dbName);
		this.currentCollection = this.currentDB.getCollection(collectionName);
	}
	
	
	/**
	 * print all dbs in mongo
	 */
	public void showDBs(){
		List<String> dbs = mongo.getDatabaseNames();
		for(String db:dbs){
			System.out.println(db);
		}
		
	}
	
	public DBCursor getCursor(){
		return this.currentCollection.find();
	}
	
	public void close(){
		if(this.mongo !=null){
			this.mongo.close();
		}
		this.mongo = null;
	
	}
}
