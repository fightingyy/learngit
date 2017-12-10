package com.ld.model.sparql;


public class Term extends Value implements Cloneable
{
	@Override 
	public Term clone() {return new Term(name,isURI,isVariable());}

	OrderBy orderBy = OrderBy.NONE; 
	Aggregate aggregate = Aggregate.NONE;
	boolean isURI = false;
	String alias;
	
	public Term(){
		
	}

	public Term(String name) {
		super(name);
//		this.name = name.replace("?","").replace("!","");
//		alias = name;
	}
	public Term(String name, boolean uri) {
		super(name);
		this.name = name.replace("?","").replace("!","");
		isURI = uri;
		setIsVariable(true);
		alias = name;
	}

	public Term(String name, boolean uri, boolean variable) {
		super(name);
		this.name = name.replace("?","").replace("!","");
		isURI = uri;
		alias = name;
		setIsVariable(variable);
	}

	public Term(String name, Aggregate aggregate) {
		super(name);
		this.aggregate = aggregate;
		alias = name;
	}
	public Term(String name, Aggregate aggregate, String as) {
		super(name);
		this.aggregate = aggregate;
		alias = as;
	}

	public Term(String name, OrderBy ob) {
		super(name);
		orderBy = ob;
		alias = name;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Term)) return false;

		Term f = (Term) obj;
		return f.getName().equals(this.getName()) && f.getAggregate() == aggregate && f.getOrderBy() == orderBy;
	}

	public OrderBy getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(OrderBy ob) {
		orderBy = ob;
	}

	public Aggregate getAggregate() {
		return aggregate;
	}

	public void setAggregate(Aggregate aggregate) {
		this.aggregate = aggregate;
	}

	public boolean isString()
	{
		return name.startsWith("'");
	}

	public void setIsURI(boolean isURI){
		this.isURI = isURI;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	@Override
	public String toString() {
//		System.err.println("SPARQL_Term: name="+name+",alias="+alias+",agg="+aggregate+",orderBy="+orderBy); // DEBUG
//		if (aggregate != Aggregate.NONE) {
//			if (alias != null && !alias.equals(name))
//				return "(" + aggregate+"(?"+name.toLowerCase()+") AS ?" + alias + ")";
//			else
//				return aggregate+"(?"+name.toLowerCase()+")";
//		}
//		if (orderBy != OrderBy.NONE) {
//			if (orderBy == OrderBy.ASC)
//				return "ASC(?"+alias.toLowerCase()+")";
//			else
//				return "DESC(?"+alias.toLowerCase()+")";
//		}
//		if (isString()) {
//			return name.replaceAll("_"," ");
//		}
//		else if (isURI || !isVariable()) {
//			return name;
//		}
//		else 
			return "?"+name;
	}


}
