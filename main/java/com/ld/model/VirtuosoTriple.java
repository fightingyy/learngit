package com.ld.model;

import java.sql.Timestamp;

public class VirtuosoTriple {

    private String subject;  
    private String predicate;  
    private String value; 

	public VirtuosoTriple() {
		
	}

	public VirtuosoTriple(String subject,String predicate,String value) {
		
		this.subject=subject;
		this.predicate=predicate;
		this.value=value;
	}
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}

