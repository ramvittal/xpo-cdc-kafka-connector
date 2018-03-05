package com.xpo.ltl.confluent.kafka;

import java.io.Serializable;

public class FieldDef implements Serializable {
	
	
	private static final long serialVersionUID = 2612602960994829931L;
	
	private String fieldName;
	private String fieldType;
	private String fieldValue;
	
	public FieldDef() {
		
	}
	
	public FieldDef(String fieldName, String fieldType, String fieldValue) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.fieldValue = fieldValue;
		
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldType() {
		return fieldType;
	}

	public void setFieldType(String fieldType) {
		this.fieldType = fieldType;
	}

	public String getFieldValue() {
		return fieldValue;
	}

	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}

}
