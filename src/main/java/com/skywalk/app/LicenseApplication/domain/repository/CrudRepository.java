package main.java.com.skywalk.app.LicenseApplication.domain.repository;

import java.net.UnknownHostException;

import org.jetbrains.annotations.NotNull;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import com.mongodb.MongoClient;

public enum CrudRepository {
	INSTANCE;
	private MongoClient mongoClient;
	
	private CrudRepository(){
		try { 
			if (mongoClient == null) 
				mongoClient = getClient(); 
		} catch (Exception e){ 
			e.printStackTrace(); 
		} 
	}
	
	private MongoClient getClient(){
		try { 
			return new MongoClient( "127.0.0.1", 27017); 
		}catch (UnknownHostException uh) { 
			uh.printStackTrace(); 
		} 
		return null; 
	}
	
	public Datastore getDatastore(@NotNull String dbName) {
		Morphia mapping = new Morphia();
		mapping.mapPackage("main.java.com.skywalk.app.domain.models");
	    return mapping.createDatastore(mongoClient,dbName);
	}
}
