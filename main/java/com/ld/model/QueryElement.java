package com.ld.model;

import java.util.ArrayList;
import java.util.List;

import com.ld.QueryGeneration.SetVariables;

public class QueryElement {

	private double score;
	private String subjectName;
	private String filterStr;
	private List<SetVariables> variablesList=new ArrayList<SetVariables>();
	private String queryStr;
	private String limitName;
	private Tamplate tamplate;
	
	public QueryElement() {
		// TODO Auto-generated constructor stub
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public String getFilterStr() {
		return filterStr;
	}

	public void setFilterStr(String filterStr) {
		this.filterStr = filterStr;
	}

	public List<SetVariables> getVariablesList() {
		return variablesList;
	}

	public void setVariablesList(List<SetVariables> variablesList) {
		this.variablesList = variablesList;
	}

	public String getQueryStr() {
		return queryStr;
	}

	public void setQueryStr(String queryStr) {
		this.queryStr = queryStr;
	}

	public String getLimitName() {
		return limitName;
	}

	public void setLimitName(String limitName) {
		this.limitName = limitName;
	}

	/**
	 * @return the tamplate
	 */
	public Tamplate getTamplate() {
		return tamplate;
	}

	/**
	 * @param tamplate the tamplate to set
	 */
	public void setTamplate(Tamplate tamplate) {
		this.tamplate = tamplate;
	}
	
	

}
