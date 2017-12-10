package com.ld.model.sparql;

public class Term_deprecated extends Value {

	OrderBy orderBy = OrderBy.NONE;
	Aggregate aggregate = Aggregate.NONE;
	Term_deprecated as = null;

	public Term_deprecated(String name) {
		super(name);
		this.name = name.replace("?","").replace("!","");
	}
	public Term_deprecated(String name,boolean b) {
		super(name);
		this.name = name.replace("?","").replace("!","");
		setIsVariable(b);
	}

	public Term_deprecated(String name, Aggregate aggregate) {
		super(name);
		this.aggregate = aggregate;
	}
	public Term_deprecated(String name, Aggregate aggregate,boolean b,Term_deprecated t) {
		super(name);
		this.aggregate = aggregate;
		setIsVariable(b);
		as = t;
	}

	public Term_deprecated(String name, OrderBy orderBy) {
		super(name);
		this.orderBy = orderBy;
	}
	public Term_deprecated(String name, OrderBy orderBy,boolean b,Term_deprecated t) {
		super(name);
		this.orderBy = orderBy;
		setIsVariable(b);
		as = t;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Term_deprecated)) return false;

		Term_deprecated f = (Term_deprecated) obj;
		return f.getName().toLowerCase().equals(this.getName().toLowerCase()) && f.getAggregate() == aggregate && f.getOrderBy() == orderBy;
	}

	public OrderBy getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(OrderBy orderBy) {
		this.orderBy = orderBy;
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

	@Override
	public String toString() {
		if (aggregate != Aggregate.NONE) {
			if (as != null) {
				return aggregate+"(?"+name.toLowerCase()+") AS " + as.toString();
			}
			else {
				return aggregate+"(?"+name.toLowerCase()+")";
			}
		}
		if (orderBy != OrderBy.NONE) {
			String n;
			if (as != null) { n = as.name; } else { n = name; }
			if (orderBy == OrderBy.ASC)
				return "ASC(?"+n.toLowerCase()+")";
			else
				return "DESC(?"+n.toLowerCase()+")";
		}
		if (isVariable() && !isString()) {
			return "?"+name.toLowerCase();
		}
		else {
			return name;
		}
	}



}
