package com.xpo.ltl.confluent.kafka;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class SchemaRegistry implements Serializable {
	
	
	private static final long serialVersionUID = 8176175240252736253L;
	
	static private Map<String, String> registry = new HashMap<String, String>();
	
	
	public static void  addEntry(String tableName, String avroSchema) {
		if(registry.get(tableName) == null) {
			registry.put(tableName, avroSchema);
		}
	}
	
	
	public static String getSchema(String tableName) {
		
		return registry.get(tableName);
	}
	

}
