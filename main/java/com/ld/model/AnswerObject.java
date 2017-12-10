package com.ld.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ld.Util.MapUtil;
import com.ld.Util.Sort;

import net.sf.json.JSONArray;

public class AnswerObject {

	private String subject;
	private String predicate;
	private String value;
	private double score;
	private String subjectUri;
	private String message="";
	private String filterStr;
	private String attention;
	private String tamplateContent;
	private String fsanswer;//全文搜索得到的答案和分数
	private int fs;//是否为全文搜索得到的答案
	
	JSONArray anwerArray=new  JSONArray();
	
	public AnswerObject() {
		// TODO Auto-generated constructor stub
	}

	public  String toString(){
		return  subject+predicate+value;
	}
	public boolean equals(Object obj) {
		if (!(obj instanceof AnswerObject)) return false;

		AnswerObject f = (AnswerObject) obj;
		return f.toString().equals(this.toString());
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

	public double getScore() {
		return score;
	}

	public void setScore(double d) {
		this.score = d;
	}

	public String getSubjectUri() {
		return subjectUri;
	}

	public void setSubjectUri(String subjectUri) {
		this.subjectUri = subjectUri;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getFilterStr() {
		return filterStr;
	}

	public void setFilterStr(String filterStr) {
		this.filterStr = filterStr;
	}

	public String getAttention() {
		return attention;
	}

	public void setAttention(String attention) {
		this.attention = attention;
	}

	/**
	 * @return the tamplateContent
	 */
	public String getTamplateContent() {
		return tamplateContent;
	}

	/**
	 * @param tamplateContent the tamplateContent to set
	 */
	public void setTamplateContent(String tamplateContent) {
		this.tamplateContent = tamplateContent;
	}

	/**
	 * @return the fsanswer
	 */
	public String getFsanswer() {
		return fsanswer;
	}

	/**
	 * @param fsanswer the fsanswer to set
	 */
	public void setFsanswer(String fsanswer) {
		this.fsanswer = fsanswer;
	}

	/**
	 * @return the fs
	 */
	public int getFs() {
		return fs;
	}

	/**
	 * @param fs the fs to set
	 */
	public void setFs(int fs) {
		this.fs = fs;
	}

}
