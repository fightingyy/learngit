package com.ld.model.sparql;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.hp.hpl.jena.vocabulary.RDF;

public class Query implements Serializable {

	private static final long serialVersionUID = 6040368736352575802L;
	List<Term> selectTerms; // SELECT ?x ?y
	List<Prefix> prefixes;
	List<Triple> conditions;
	List<Term> orderBy;
	Filter filter;
    List<Having> having;
    List<Union> unions;
    private String fromStr;
    List<String> filterList=new ArrayList<String>();
	QueryType queryType = QueryType.SELECT;
	

	int limit;
	int offset;

	public Query()
	{
		super();
		selectTerms = new ArrayList<Term>();
		prefixes = new ArrayList<Prefix>();
		conditions = new ArrayList<Triple>();
		orderBy = new ArrayList<Term>();
                having  = new ArrayList<Having>();
                unions = new ArrayList<Union>();
	}

	public Query(List<Prefix> prefixes,List<Term> selectTerms, List<Triple> conditions)
	{
		super();
		this.selectTerms = selectTerms;
		this.prefixes = prefixes;
		this.conditions = conditions;
                having = new ArrayList<Having>();
                unions = new ArrayList<Union>();
	}
	
	public Query(List<Prefix> prefixes,List<Term> selectTerms, List<Triple> conditions,Filter filter)
	{
		this(prefixes,selectTerms,conditions);
		this.filter=filter;
		
	}
//	
//	public Query(List<Prefix> prefixes,List<Term> selectTerms, List<Triple> conditions,String fromString){
//		
//		this(prefixes,selectTerms,conditions);
//		this.fromStr=fromString;
//	}
	
	public Query(List<Prefix> prefixes,List<Term> selectTerms, List<Triple> conditions,Filter filter,String fromString){
		
		this(prefixes,selectTerms,conditions,filter);
		this.fromStr=fromString;
	}
	
	public Query(List<Prefix> prefixes,List<Term> selectTerms, List<Triple> conditions,List<String> filters){
		
		this(prefixes,selectTerms,conditions);
		this.filterList=filters;
	}
	public Query(List<Prefix> prefixes,List<Term> selectTerms, List<Triple> conditions,String fromString){
		
		this(prefixes,selectTerms,conditions);
		this.fromStr=fromString;
	}

	public Query(List<Term> selectTerms, List<Triple> conditions)
	{
		this(new ArrayList<Prefix>(),selectTerms, conditions);
	}

	public Query(List<Prefix> prefixes, List<Term> selectTerms, List<Triple> conditions, List<Term> orderBy, int limit, int offset)
	{
		super();
		this.selectTerms = selectTerms;
		this.prefixes = prefixes;
		this.conditions = conditions;
		this.orderBy = orderBy;
		this.limit = limit;
		this.offset = offset;
                having = new ArrayList<Having>();
                unions = new ArrayList<Union>();
	}

	/** copy constructor **/
	public Query(Query query){
		this.queryType = query.getQt();
		List<Term> selectTerms = new ArrayList<Term>();
		for(Term term : query.getselectTerms()){
			Term newTerm = new Term(term.getName());
			newTerm.setIsVariable(term.isVariable());
			newTerm.setIsURI(newTerm.isURI);
			newTerm.setAggregate(term.getAggregate());
			newTerm.setOrderBy(term.getOrderBy());
			newTerm.setAlias(term.getAlias());
			selectTerms.add(newTerm);
		}
		this.selectTerms = selectTerms;
		List<Prefix> prefixes = new ArrayList<Prefix>();
		for(Prefix prefix : query.getPrefixes()){
			Prefix newPrefix = new Prefix(prefix.getName(), prefix.getUrl());
			prefixes.add(newPrefix);
		}
		this.prefixes = prefixes;
		List<Triple> conditions = new ArrayList<Triple>();
		for(Triple condition : query.getConditions()){
			Term variable = new Term(condition.getSubject().getName());
			variable.setIsVariable(condition.getSubject().isVariable());
//			variable.setIsURI(condition.getSubject().isURI);
			Value predicate = new Value(condition.getPredicate().getName());
			predicate.setIsVariable(condition.getPredicate().isVariable());
			predicate.setPrefix(condition.getPredicate().getPrefix());
			Term value = new Term(condition.getValue().getName());
			if(condition.getValue() instanceof Term){
				value.setIsURI(((Term)condition.getValue()).isURI);
			}
			value.setIsVariable(condition.getValue().isVariable());
			Triple newCondition = new Triple(variable, predicate, value);
			conditions.add(newCondition);
		}
		this.conditions = conditions;
		List<Term> orderBy = new ArrayList<Term>();
		for(Term order : query.getOrderBy()){
			Term newTerm = new Term(order.getName());
			newTerm.setIsVariable(order.isVariable());
			newTerm.setAggregate(order.getAggregate());
			newTerm.setOrderBy(order.getOrderBy());
			orderBy.add(newTerm);
		}
		this.orderBy = orderBy;
		//TODO add copy for filters
        this.having = query.having;
        this.unions = query.unions; // TODO copy unions

		this.limit = query.getLimit();
		this.offset = query.getOffset();
	}

	public List<Integer> getSlotInts() {

		List<Integer> result = new ArrayList<Integer>();

		String name;
		int i;

		for (Triple triple : conditions) {

			name = triple.subject.getName();
			if (name.matches("s[0-9]+")) {
				i = Integer.parseInt(name.substring(name.indexOf("s") + 1));
				result.add(i);
			}

			name = triple.value.getName();
			if (name.matches("s[0-9]+")) {
				i = Integer.parseInt(name.substring(name.indexOf("s") + 1));
				result.add(i);
			}

			name = triple.predicate.getName();
			if (name.matches("s[0-9]+")) {
				i = Integer.parseInt(name.substring(name.indexOf("s") + 1));
				result.add(i);
			}
		}



		return result;
	}

	@Override
	public String toString()
	{

		String groupBy = null;

		String retVal = "";
		for (Prefix prefix : prefixes)
		{
			retVal += prefix.toString() + "\n";
		}

		if (queryType == QueryType.SELECT){
			retVal += "\nSELECT distinct ";

            boolean group = false;
			for (Term term : selectTerms){
				if(!retVal.contains(term.toString()))
					retVal += term.toString() + " ";
				if(selectTerms.size() > 1 && term.toString().contains("COUNT")){
                                    group = true;
				}
			}
            if (group) {
                groupBy = "";
                for (Term t : selectTerms) {
                    if (!t.toString().contains("COUNT"))
                        groupBy += t.toString() + " ";
                }
            }
		}
		else retVal += "\nASK ";

		if(fromStr!=null){
			retVal+=fromStr+" \n";
		}
		retVal += "WHERE {\n";

		if (conditions != null)  {
                    for (Triple condition : conditions) {
			if (condition != null) retVal += "\t" + condition.toString() + "\n";
                    }
                }
                for (Union u : unions) retVal += u.toString() + "\n";
                if(filter!=null&&!filter.getPairs().isEmpty())
                	retVal += filter.toString()+".\n";
		retVal += "}\n";

		if(groupBy != null){
			retVal += "GROUP BY " + groupBy + "\n";
		}

                if (!having.isEmpty()) {
                    for (Having h : having) retVal += h.toString() + "\n";
                }

		if (orderBy != null && !orderBy.isEmpty())
		{
			retVal += "ORDER BY ";
			for (Term term : orderBy)
			{
				retVal += term.toString() + " ";
			}
			retVal += "\n";
		}

		if (limit != 0 || offset != 0)
		{
			retVal += "LIMIT " + limit + " OFFSET " + offset + "\n";
		}

		return retVal;

	}

	public List<String> getVariablesAsStringList()
	{
		List<String> result = new ArrayList<String>();
		for (Term term : selectTerms)
		{
			result.add(term.toString());
		}
		return result;
	}

	public List<Term> getselectTerms()
	{
		return selectTerms;
	}

	public void setselectTerms(List<Term> selectTerms)
	{
		this.selectTerms = selectTerms;
	}

	public List<Prefix> getPrefixes()
	{
		return prefixes;
	}

	public void setPrefixes(List<Prefix> prefixes)
	{
		this.prefixes = prefixes;
	}

	public List<Triple> getConditions()
	{
		return conditions;
	}

	public void setConditions(List<Triple> conditions)
	{
		this.conditions = conditions;
	}

	public void addCondition(Triple triple)
	{
		conditions.add(triple);
	}

        public void addUnion(Union union) {
            unions.add(union);
        }

        public void addHaving(Having h)
	{
		this.having.add(h);
	}

	public List<Term> getOrderBy()
	{
		return orderBy;
	}

	public void addOrderBy(Term term)
	{
		if (term.orderBy == OrderBy.NONE)
			term.orderBy = OrderBy.ASC;

		orderBy.add(term);
	}

	public void addPrefix(Prefix prefix)
	{
		prefixes.add(prefix);
	}

	public void addSelectTerm(Term term)
	{
		for (int i = 0; i < selectTerms.size(); ++i)
			if (term.equals(selectTerms.toArray()[i])) return;

		selectTerms.add(term);
	}

	public boolean isSelectTerm(Term term)
	{
		for (int i = 0; i < selectTerms.size(); ++i) // TODO: have to figure out
													// while .remove doesn't
													// call .equals
		{
			if (term.equals(selectTerms.toArray()[i])) return true;
		}
		return false;
	}

	public void removeSelTerm(Term term)
	{
		List<Term> newselectTerms = new ArrayList<Term>();
		for (int i = 0; i < selectTerms.size(); ++i) // TODO: have to figure out
													// while .remove doesn't
													// call .equals
		{
			if (!term.equals(selectTerms.toArray()[i])) newselectTerms.add((Term) selectTerms.toArray()[i]);
		}
		selectTerms = newselectTerms;
	}

	public int getLimit()
	{
		return limit;
	}

	public void setLimit(int limit)
	{
		this.limit = limit;
	}

	public int getOffset()
	{
		return offset;
	}

	public void setOffset(int offset)
	{
		this.offset = offset;
	}

	public QueryType getQt()
	{
		return queryType;
	}

	public void setQt(QueryType queryType)
	{
		this.queryType = queryType;
	}

	public void replaceVarWithURI(String var, String uri){
		Value subject;
		Value predicate;
		Value object;
		uri = "<" + uri + ">";

		for(Triple triple : conditions){
			subject = triple.getSubject();
			predicate = triple.getPredicate();
			object = triple.getValue();
			if(subject.isVariable()){
				if(subject.getName().equals(var)){
					subject.setName(uri);
					subject.setIsVariable(false);
//					subject.setIsURI(true);
				}
			}
			if(predicate.isVariable()){
				if(predicate.getName().equals(var)){
					predicate.setName(uri);
					predicate.setIsVariable(false);
				}
			}
			if(object.isVariable()){
				if(object.getName().equals(var)){
					object.setName(uri);
					object.setIsVariable(false);
					if(object instanceof Term){
						((Term) object).setIsURI(true);
					} else if(object instanceof Value){
						((Value) object).setIsVariable(false);
					}
				}
			}

		}
	}

	public void replaceVarWithPrefixedURI(String var, String uri){
		Value subject;
		Value predicate;
		Value object;

		for(Triple triple : conditions){
			subject = triple.getSubject();
			predicate = triple.getPredicate();
			object = triple.getValue();
			if(subject.isVariable()){
				if(subject.getName().equals(var)){
					subject.setName(uri);
					subject.setIsVariable(false);
					subject.setIsURI(true);
				}
			}
			if(predicate.isVariable()){
				if(predicate.getName().equals(var)){
					predicate.setName(uri);
					predicate.setIsVariable(false);
				}
			}
			if(object.isVariable()){
				if(object.getName().equals(var)){
					object.setName(uri);
					object.setIsVariable(false);
					if(object instanceof Term){
						((Term) object).setIsURI(true);
					}
				}
			}

		}
	}

	public List<Triple> getTriplesWithVar(String var){
		List<Triple> triples = new ArrayList<Triple>();

		Value subject;
		Value predicate;
		Value value;
		for(Triple triple : conditions){
			subject = triple.getSubject();
			predicate = triple.getPredicate();
			value = triple.getValue();

			if(subject.isVariable() && subject.getName().equals(var)){
				triples.add(triple);
			} else if(predicate.isVariable() && predicate.getName().equals(var)){
				triples.add(triple);
			} else if(value.isVariable() && value.getName().equals(var)){
				triples.add(triple);
			}
		}
		return triples;
	}

	public List<Triple> getRDFTypeTriples(){
		List<Triple> triples = new ArrayList<Triple>();

		for(Triple triple : conditions){
			if(triple.getPredicate().toString().equals("rdf:type")||triple.getPredicate().toString().equals(RDF.type.getURI())){
				triples.add(triple);
			}
		}
		return triples;
	}

	public List<Triple> getRDFTypeTriples(String var){
		List<Triple> triples = new ArrayList<Triple>();

		for(Triple triple : conditions){
			if(triple.getPredicate().toString().equals("rdf:type") && triple.getSubject().getName().equals(var)){
				triples.add(triple);
			}
		}
		return triples;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((conditions == null) ? 0 : conditions.hashCode());
		result = prime * result + ((filter == null) ? 0 : filter.hashCode());
		result = prime * result + limit;
		result = prime * result + offset;
		result = prime * result + ((orderBy == null) ? 0 : orderBy.hashCode());
		result = prime * result
				+ ((prefixes == null) ? 0 : prefixes.hashCode());
		result = prime * result + ((queryType == null) ? 0 : queryType.hashCode());
		result = prime * result
				+ ((selectTerms == null) ? 0 : selectTerms.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Query other = (Query) obj;
		if (conditions == null) {
			if (other.conditions != null)
				return false;
		} else if (!conditions.equals(other.conditions))
			return false;
		if (filter == null) {
			if (other.filter != null)
				return false;
		} else if (!filter.equals(other.filter))
			return false;
		if (limit != other.limit)
			return false;
		if (offset != other.offset)
			return false;
		if (orderBy == null) {
			if (other.orderBy != null)
				return false;
		} else if (!orderBy.equals(other.orderBy))
			return false;
		if (prefixes == null) {
			if (other.prefixes != null)
				return false;
		} else if (!prefixes.equals(other.prefixes))
			return false;
		if (queryType == null) {
			if (other.queryType != null)
				return false;
		} else if (!queryType.equals(other.queryType))
			return false;
		if (selectTerms == null) {
			if (other.selectTerms != null)
				return false;
		} else if (!selectTerms.equals(other.selectTerms))
			return false;
		return true;
	}

	/**
	 * Returns the variable in the SPARQL query, which determines the type of the answer
	 * by an rdf:type Predicate.
	 * @return
	 */
	public String getAnswerTypeVariable(){
		Term selection = selectTerms.iterator().next();
		for(Triple t : conditions){
			if(t.getSubject().equals(selection) && t.getPredicate().getName().equals("type")){
				return t.getValue().getName();
			}
		}
		return null;
	}

	public String getFromStr() {
		return fromStr;
	}

	public void setFromStr(String fromStr) {
		this.fromStr = fromStr;
	}

}
