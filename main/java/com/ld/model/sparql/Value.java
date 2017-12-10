package com.ld.model.sparql;

import java.io.Serializable;

public class Value implements Serializable,Cloneable{

	private static final long serialVersionUID = -3733491240975566183L;

	private String prefix;
	protected String name;
	private boolean isVariable = false;
	private boolean isSelect=false;
	private boolean isURI;

	@Override 
	public Value clone()
	{
		Value value = new Value(name);
		value.setIsVariable(isVariable());
		return value;
	}
	
	public void setIsURI(boolean isURI){
		this.isURI = isURI;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setIsVariable(boolean b) {
		isVariable = b;
	}

	public boolean isVariable(){
		return isVariable;
	}

	public Value(String name) {
		super();
		this.name = name;
	}

	public Value(){
		
	}
	public Value(String prefix,String name) {
		
		this(name);
		this.prefix=prefix;
	}
	public Value(String prefix,String name,boolean isVariable) {
		
		this(prefix,name);
		this.setIsVariable(isVariable);
	}

	public Value(String prefix,String name,boolean isVariable,boolean isSelect){
		
		this(prefix,name,isVariable);
		this.isSelect=isSelect;
	}
	public String toString() {
		if (isVariable()) {
			return "?" + name;
		}
		else{
			if (prefix == null||prefix=="") {
				return name;
			}
			return prefix+":"+name;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isVariable ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Value other = (Value) obj;
		if (isVariable != other.isVariable)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public boolean isSelect() {
		return isSelect;
	}

	public void setSelect(boolean isSelect) {
		this.isSelect = isSelect;
	}



}
