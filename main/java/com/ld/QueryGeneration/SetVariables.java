package com.ld.QueryGeneration;

import java.util.*;
import java.util.Map.Entry;

import com.ld.model.sparql.*;

public class SetVariables {

	private Map<String,String> prefixMap;
	private Value subject;
	private Value predicate;
	private Value value;
	private Triple triple;
	private String having;

	private String OrderBy;
	private String limit;
	private String question;
	private String queryType;
	private boolean optional;
	private Filter filter;
	private String filterStr;
	
	private List<Prefix> prefixes=new ArrayList<Prefix>();
	private List<Term> terms=new ArrayList<Term>();
	private List<Triple> conditions=new ArrayList<Triple>();
	private List<String> filterList=new ArrayList<String>();

	public String getHaving() {
		return having;
	}
	public void setHaving(String having) {
		this.having = having;
	}

	public String getOrderBy() {
		return OrderBy;
	}
	public void setOrderBy(String orderBy) {
		OrderBy = orderBy;
	}
	public String getLimit() {
		return limit;
	}
	public void setLimit(String limit) {
		this.limit = limit;
	}
	
	public SetVariables(Map<String,String> prefix_map,Value subject_new,Value predicate_new,Value value_new){
		
		for(Entry<String, String> entry:prefix_map.entrySet()){
			String url=entry.getKey();
			String name=entry.getValue();
			Prefix prefix=new Prefix(name,url);
			this.prefixes.add(prefix);
		}
		
		this.setSubject(subject_new);
		this.setPredicate(predicate_new);
		this.setValue(value_new);	

		this.triple=new Triple(subject,predicate,value);
		
		if(triple.getSubject().isSelect()){
			Term term=new Term(triple.getSubject().getName());
			this.terms.add(term);
		}
		
		if(triple.getPredicate().isSelect()){
			Term term=new Term(triple.getPredicate().getName());
			this.terms.add(term);
		}
		
		if(triple.getValue().isSelect()){
			Term term=new Term(triple.getValue().getName());
			this.terms.add(term);
		}

		this.conditions.add(triple);	
	}

//	public SetVariables(Map<String,String> prefix_map,Value subject_new,Value predicate_new,Value value_new){
//		
//		this(prefix_map,subject_new,predicate_new,value_new);
//		this.optional=optional;		
//
//		triple.setOptional(this.optional);
//		
//	}
	
	
	public SetVariables(Map<String,String> prefix_map,Value subject_new,Value predicate_new,Value value_new,boolean optional,Filter filter){
		
		this(prefix_map, subject_new, predicate_new, value_new);
		this.setFilter(filter);
	}
	
	public SetVariables(Map<String,String> prefix_map,Value subject_new,Value predicate_new,Value value_new,boolean optional,String filterStr){
		
		this(prefix_map, subject_new, predicate_new, value_new);
		this.setFilterStr(filterStr);
	}
	
//	public SetVariables(Map<String,String> prefix_map,Value subject_new,Predicate predicate_new,Value value_new,boolean optional,List<String> filters){
//		
//		this(prefix_map, subject_new, predicate_new, value_new, optional);
//		this.setFilterList(filters);
//	}
	


	public SetVariables(Map<String,String> prefix_map,Value subject_new,Value predicate_new,Value value_new,boolean optional,String queryType_new, String having_new, Filter filter_new,  String OrderBy_new, String limit_new, String question_new){
		
		this(prefix_map,subject_new,predicate_new,value_new);
		this.setHaving(having_new);
		this.setFilter(filter_new);
		this.setOrderBy(OrderBy_new);
		this.setLimit(limit_new);		
		this.setQuestion(question_new);
		this.setQueryType(queryType_new);
	}
	

	public String getQuestion() {
		return question;
	}
	public void setQuestion(String question) {
		this.question = question;
	}

	public String getQueryType() {
		return queryType;
	}
	public void setQueryType(String queryType) {
		this.queryType = queryType;
	}
	
	public List<Prefix> getPrefixes() {
		return prefixes;
	}
	public void setPrefixes(List<Prefix> prefixes) {
		this.prefixes = prefixes;
	}
	public List<Term> getTerms() {
		return terms;
	}
	public void setTerms(List<Term> terms) {
		this.terms = terms;
	}
	public List<Triple> getConditions() {
		return conditions;
	}
	public void setConditions(List<Triple> conditions) {
		this.conditions = conditions;
	}
	public Value getPredicate() {
		return predicate;
	}
	public void setPredicate(Value predicate) {
		this.predicate = predicate;
	}
	public Value getValue() {
		return value;
	}
	public void setValue(Value value) {
		this.value = value;
	}
	public Map<String,String> getPrefixMap() {
		return prefixMap;
	}
	public void setPrefixMap(Map<String,String> prefixMap) {
		this.prefixMap = prefixMap;
	}
	public Value getSubject() {
		return subject;
	}
	public void setSubject(Value subject) {
		this.subject = subject;
	}
	public Triple getTriple() {
		return triple;
	}
	public void setTriple(Triple triple) {
		this.triple = triple;
	}
	public boolean isOptional() {
		return optional;
	}
	public void setOptional(boolean optional) {
		this.optional = optional;
	}
	public List<String> getFilterList() {
		return filterList;
	}
	public void setFilterList(List<String> filterList) {
		this.filterList = filterList;
	}
	public Filter getFilter() {
		return filter;
	}
	public void setFilter(Filter filter) {
		this.filter = filter;
	}
	public String getFilterStr() {
		return filterStr;
	}
	public void setFilterStr(String filterStr) {
		this.filterStr = filterStr;
	}
}
