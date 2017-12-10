package com.ld.model;

public class QueryObject {

	private String subjectName;
	private double score;
	private String query;
	private String limitName;
	private String filterStr;
	private Tamplate tamplate;
	
	public QueryObject() {
		// TODO Auto-generated constructor stub
	}

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getLimitName() {
		return limitName;
	}

	public void setLimitName(String limitName) {
		this.limitName = limitName;
	}

	public String getFilterStr() {
		return filterStr;
	}

	public void setFilterStr(String filterStr) {
		this.filterStr = filterStr;
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
