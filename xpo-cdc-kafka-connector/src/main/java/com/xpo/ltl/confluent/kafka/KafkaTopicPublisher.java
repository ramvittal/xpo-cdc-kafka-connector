package com.xpo.ltl.confluent.kafka;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaTopicPublisher  {
	
	private static KafkaTopicPublisher publisher=null;
	private  Producer<String, GenericRecord> producer=null;
	
	
	
	private KafkaTopicPublisher() {
		
	}
	
	private static KafkaTopicPublisher getInstance(Properties props,boolean isNew) {
		
		//create a singleton publisher 
		if(publisher == null || isNew) {
			publisher = new KafkaTopicPublisher();
			publisher.producer = new KafkaProducer<String, GenericRecord>(props);
		}
		
		return publisher;
	}
	
	private static KafkaTopicPublisher getDefaultInstance(String kafkaBootStrapServer, String schemaRegistryUrl) {
		
		
	
		//create a singleton default avro publisher 
		if(publisher == null) {
			publisher = new KafkaTopicPublisher();
			
			Properties props = new Properties();
		    props.put("bootstrap.servers", kafkaBootStrapServer);
		    props.put("acks", "all");
		    props.put("retries", 0);
		    props.put("key.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
		    props.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
		    props.put("schema.registry.url", schemaRegistryUrl);
			
			publisher.producer = new KafkaProducer<String, GenericRecord>(props);
		}
		
		return publisher;
	}
	
	
	//send generic avro message to kafka topic
	
	public boolean writeGenericAvroMessage(String topic, String partitionKey, GenericRecord message) {
		
		
		 
		this.producer.send(new ProducerRecord<String, GenericRecord>(topic, partitionKey, message) );
		return true;

	}
	
	//send table change as  avro message to kafka topic
	
		public boolean sendTableChangeToKafka(String source, TableDef tableDef) {
			
			String avroSchema =  SchemaRegistry.getSchema(tableDef.getTableName());
			
			if(avroSchema == null) {
				tableDef.buildAvroSchema();
				SchemaRegistry.addEntry(tableDef.getTableName(), tableDef.getTableAvroSchema());
			} else {
				tableDef.setTableAvroSchema(avroSchema);
			}
			
			System.out.println(tableDef.getTableAvroSchema());
			
			Schema.Parser parser = new Schema.Parser();
		    Schema schema = parser.parse(tableDef.getTableAvroSchema());
		    String topicName = source + "_" + tableDef.getTableName();
		    
		    GenericRecord tableChangeRecord = new GenericData.Record(schema);
		    
		    for(FieldDef field : tableDef.getFields()) {
		    	
		    		Object fieldValue = field.getFieldValue() ;
		    	
		    		if("long".equals(field.getFieldType())) {
		    			fieldValue = Long.parseLong(field.getFieldValue());
		    		} else if("int".equals(field.getFieldType())) {
		    			fieldValue = Integer.parseInt(field.getFieldValue());
		    		} else if("double".equals(field.getFieldType())) {
		    			fieldValue = Double.parseDouble(field.getFieldValue());
		    		}
		    	
		    		tableChangeRecord.put(field.getFieldName(), fieldValue);
		    }
		    
		    System.out.println(tableChangeRecord);
		     
			this.producer.send(new ProducerRecord<String, GenericRecord>(topicName, tableDef.getPartitionKey(), tableChangeRecord) );
			return true;

		}
	
	
	/**
	 * close the producer and nullify it
	 *
	 * @param  none
	 * @return boolean true or false indicates the status of close operation
	 *
	 */

	public  boolean  closeProducer() {
		publisher.producer.close(30L, TimeUnit.MILLISECONDS);
		publisher.producer=null;
		return true;
	}
	
	
	/* running from command line 
		   mvn clean package
		   mvn exec:java -Dexec.mainClass="com.xpo.ltl.confluent.kafka.KafkaTopicPublisher"
	 */
	 

	public static void main(String[] args) {
		
		
		//basicKafkaPubTest();
		
		//Setup Inputs to Java code transformation
		
		String bootstrapServer =  "localhost:9092";   // this should be integration service level configuration parameter
		String schemaRegistryUrl = "http://localhost:8081";
		
		if(args.length > 0 && "sandbox".equals(args[0])) {
			System.out.println("Using Kafka server:" + args[0]);
			bootstrapServer = "cdcxvd1461.con-way.com:19092";
			schemaRegistryUrl = "http://" + "cdcxvd1461.con-way.com:18081";
		} else  if(args.length > 0 && "aws".equals(args[0])) {
			System.out.println("Using Kafka server:" + args[0]);
			bootstrapServer = "34.218.6.3:9092";
			schemaRegistryUrl = "http://" + "34.218.6.3:8081";
		}
			
			
			
		
		
		String sourceTable = "ops_trailer_load";   // this is based on source/target mapping
		String partitionKey = "eqpId";   // this is based on source/target mapping. default to primary key of the source table
		
		
		//column names from source table
		String columnNames = "db2TxnLogOccuredTimestamp,infCdcProcessingTimestamp,eqpId,eqpIdPfxTxt,eqpIdSfxNbr,origSic,currStat,statUpdtTmst"; 
		
		// column types need to be mapped to avro data type - see avro types here - https://avro.apache.org/docs/1.8.2/spec.html#schema_primitive
		String columnTypes = "long,long,long,string,long,string,string,long";
		
		// convert source timestamp field to long value. curr tmst for testing only
		String db2TxnLogOccuredTimestamp = System.currentTimeMillis()+"";
		String infCdcProcessingTimestamp = System.currentTimeMillis()+"";
		String statusTimestamp = System.currentTimeMillis()+"";
		
		// need better way to send values as value can have comma in it.  may be try byte[] in next iteration..?
		String columnValues = db2TxnLogOccuredTimestamp + "," +infCdcProcessingTimestamp + ",3031011,303,1011,UPO,LDDK," + statusTimestamp;
		String action = "Insert";
		
		publishCDCRecord2Kafka( bootstrapServer, schemaRegistryUrl, sourceTable, partitionKey, columnNames, columnTypes, columnValues, action );
		
	  }

	private static void basicKafkaPubTest() {
		Properties props = new Properties();
	    props.put("bootstrap.servers", "localhost:9092");
	    props.put("acks", "all");
	    props.put("retries", 0);
	    props.put("key.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
	    props.put("value.serializer", "io.confluent.kafka.serializers.KafkaAvroSerializer");
	    props.put("schema.registry.url", "http://localhost:8081");

	    String schemaString = "{\"namespace\": \"example.avro\", \"type\": \"record\", " +
	                           "\"name\": \"page_visit\"," +
	                           "\"fields\": [" +
	                            "{\"name\": \"time\", \"type\": \"long\"}," +
	                            "{\"name\": \"site\", \"type\": \"string\"}," +
	                            "{\"name\": \"ip\", \"type\": \"string\"}" +
	                           "]}";
	    
	    
	    KafkaTopicPublisher publisher =  KafkaTopicPublisher.getInstance(props, false);
	    
	   // Producer<String, GenericRecord> producer = new KafkaProducer<String, GenericRecord>(props);

	    Schema.Parser parser = new Schema.Parser();
	    Schema schema = parser.parse(schemaString);

	    Random rnd = new Random();
	    for (long nEvents = 0; nEvents < 5; nEvents++) {
	      long runtime = new Date().getTime();
	      String site = "www.example.com";
	      String ip = "192.168.2." + rnd.nextInt(255);

	      GenericRecord page_visit = new GenericData.Record(schema);
	      page_visit.put("time", runtime);
	      page_visit.put("site", site);
	      page_visit.put("ip", ip);
	      
	      System.out.println(schemaString);
	      System.out.println(page_visit);

	    //  ProducerRecord<String, GenericRecord> data = new ProducerRecord<String, GenericRecord>(
	      //    "page_visits", ip, page_visit);
	      publisher.writeGenericAvroMessage("test3", ip, page_visit);
	    }

	    publisher.closeProducer();
	}
		
		
	private static void publishCDCRecord2Kafka(String kafkabootStrapServer, String schemaRegistryUrl, String sourceTable, String partitionKeyName,
			String columnNames, String columnTypes, String columnValues, String action )  {
		
		
		try {
			
		// 1. setup default kafka singleton avro publisher  
		  KafkaTopicPublisher publisher =  KafkaTopicPublisher.getDefaultInstance(kafkabootStrapServer, schemaRegistryUrl);
		  
		 //2. setup table def instance
		  TableDef tableDef =  new TableDef("ltl.xpo.com.db2.cdc." + sourceTable, sourceTable, partitionKeyName);
		  tableDef.setTableAction(action);   
		 
		  //3. add fields to tableDef  -  extract these fields from source table; 
		 
		  
		  List<String> columnsNameList = Arrays.asList(columnNames.split("\\s*,\\s*"));
		  List<String> columnsTypeList = Arrays.asList(columnTypes.split("\\s*,\\s*"));
		  List<String> columnsValueList = Arrays.asList(columnValues.split("\\s*,\\s*"));
		  
		  int index=0;
		  String columnnType="";
		  String columnValue="";


		  for( String columnName : columnsNameList)  {
			  columnnType  = columnsTypeList.get(index);
			  columnValue  = columnsValueList.get(index++);
			  tableDef.addField(columnName, columnnType, columnValue);
		  }
		  
		  //4. send the table change to kafka
		  publisher.sendTableChangeToKafka("db2_cdc", tableDef);
		  
		  //5. close producer before JVM dies
		  publisher.closeProducer();
		   
		}
		catch(Exception e) {
			//handle exception  - need better logging and exception handling
			e.printStackTrace();
			
		}
		  
		  
		
		
		
	}
		
		

	
	
	

}

