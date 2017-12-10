package com.ld.model.sparql;


public class Subject extends Value{
	
	private Prefix prefix = null;
	private boolean isVariable = false;
	
	public Subject(String name, Prefix prefix){
		super(name);
		this.prefix = prefix;
	}

	public Subject(String name) {
		super(name);
	}
	
	@Override
	public Subject clone()
	{
		Subject subject = new Subject(name);
		subject.setIsVariable(isVariable());
		return subject;
	}

	public String toString() {
		if (isVariable) {
			return "?"+name.toLowerCase();
		} 
		else {
			if(prefix != null) {
				return prefix.getName()+":"+name;
			}
		return name;
	   }
	}
	
	public boolean isVariable() {
		return isVariable;
	}

	public void setVariable(boolean isVariable) {
		this.isVariable = isVariable;
	}
	
}

