//package com.ld.model.sparql;
//
//public class Predicate extends Value implements Cloneable{
//
//	private String prefix;
//	protected String name;
//	private boolean isVariable = false;
//
//	@Override 
//	public Predicate clone()
//	{
//		Predicate p = new Predicate(name, prefix,isVariable);
//		p.setIsVariable(isVariable());
//		return p;
//	}
//
//	public Predicate(String name) {
//		super();
//		this.prefix = null;
//		this.name = name;
//	}
//	public Predicate (String prefix,String name,boolean isVariable) {
//	
//		this.name=name;
//		this.prefix=prefix;
//		this.isVariable=isVariable;
//	}
//
//	public String getPrefix() {
//		return prefix;
//	}
//
//	public void setPrefix(String prefix) {
//		this.prefix = prefix;
//	}
//
////	@Override
//	public String toString() {
//		
//		if (isVariable()) {
//			return "?" + name;
//		}
//		else{
//			if (prefix == null||prefix=="") {
//				return name;
//			}
//			return prefix+":"+name;
//		}	
//	}
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = super.hashCode();
//		result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
//		return result;
//	}
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (!super.equals(obj))
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		Predicate other = (Predicate) obj;
//		if (prefix == null) {
//			if (other.prefix != null)
//				return false;
//		} else if (!prefix.equals(other.prefix))
//			return false;
//		return true;
//	}
//
//
//
//}
