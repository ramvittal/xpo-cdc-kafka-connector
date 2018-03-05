package com.xpo.ltl.confluent.kafka;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableDef {
	
	private String tableNamespace;
	private String tableName;
	private String tableAction;
	private long db2TxnLogOccuredTimestamp;
	private long infCdcProcessingTimestamp;
	private List<FieldDef> fields; 
	private Map<String, FieldDef> fieldsMap; 
	private String tableAvroSchema;
	private String partitionKeyName;
	
	public TableDef() {
		
	}
	
	public TableDef(String tableSpaceName, String tableName, String partitionKeyName) {
		
		this.tableNamespace = tableSpaceName;
		this.tableName = tableName;
		this.fields = new ArrayList<FieldDef>();
		this.partitionKeyName =  partitionKeyName;
		this.fieldsMap = new HashMap<String, FieldDef>();
		this.addField("tableName", "string", tableName);
		
		
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
		this.addField("tableName", "string", tableName);
	}

	public List<FieldDef> getFields() {
		return fields;
	}

	public void setFields(List<FieldDef> fields) {
		this.fields = fields;
	}

	public String getTableNamespace() {
		return tableNamespace;
	}

	public void setTableNamespace(String tableNamespace) {
		this.tableNamespace = tableNamespace;
	}
	
	public void addField(FieldDef field) {
		
		this.fields.add(field);
		
	}
	
	public FieldDef addField(String fieldName, String fieldType, String fieldValue) {
		
		FieldDef field = new FieldDef(fieldName, fieldType, fieldValue);
		
		//TO DO: eval if fields ArrayList can be removed and map can be used instead?
		this.fields.add(field);
		this.fieldsMap.put(fieldName, field);
		
		return field;
		
	}

	public String getTableAction() {
		return tableAction;
	}

	public void setTableAction(String tableAction) {
		this.tableAction = tableAction;
		this.addField("action", "string", tableAction);
		
	}

	public long getDb2TxnLogOccuredTimestamp() {
		return db2TxnLogOccuredTimestamp;
	}

	public void setDb2TxnLogOccuredTimestamp(long db2TxnLogOccuredTimestamp) {
		this.db2TxnLogOccuredTimestamp = db2TxnLogOccuredTimestamp;
		this.addField("db2TxnLogOccuredTimestamp", "long", db2TxnLogOccuredTimestamp+"");
	}

	public long getInfCdcProcessingTimestamp() {
		return infCdcProcessingTimestamp;
	}

	public void setInfCdcProcessingTimestamp(long infCdcProcessingTimestamp) {
		this.infCdcProcessingTimestamp = infCdcProcessingTimestamp;
		this.addField("infCdcProcessingTimestamp", "long", infCdcProcessingTimestamp+"");
	}

	public String getTableAvroSchema() {
		return tableAvroSchema;
	}

	public void setTableAvroSchema(String tableAvroSchema) {
		this.tableAvroSchema = tableAvroSchema;
	}
	
	
	
	public void buildAvroSchema() {
		
		if(this.tableAvroSchema != null || this.fields.size() == 0) {
			return;
		}
		
		StringBuilder sb =  new StringBuilder();
		
		sb.append("{\"namespace\":\"" ).append(this.tableNamespace).append("\",");
		sb.append("\"type\":\"record\",\n");
		sb.append("\"name\":\"" ).append(this.tableName).append("\",\n");
		sb.append("\"fields\": [\n");
		
		for(FieldDef field : this.getFields()) {
			sb.append("{\"name\":\"").append(field.getFieldName()).append("\",");
			sb.append("\"type\":\"").append(field.getFieldType()).append("\"},\n");
		}
		sb.append("]}");
		sb.deleteCharAt(sb.indexOf(",\n]}"));
		
		this.tableAvroSchema = sb.toString();
		
		
		
	}

	public String getPartitionKeyName() {
		return partitionKeyName;
	}
	
	public String getPartitionKey() {
		
		///this.fields.
		
		FieldDef partitionKey =  this.fieldsMap.get(this.getPartitionKeyName());
		
		return partitionKey.getFieldValue();
	}

	public void setPartitionKeyName(String partitionKey) {
		this.partitionKeyName = partitionKey;
	}

}
