package com.ld.model.sparql;

import java.io.Serializable;

public class Triple implements Serializable, Cloneable{

	private static final long serialVersionUID = -1681019680404287955L;

	Value subject = new Value();
	Value predicate = new Value();
	Value value = new Value();
	boolean optional=false;

	@Override public Triple clone()
	{
		return new Triple(subject.clone(),predicate.clone(),value.clone(),optional);
	}


	public boolean isOptional() {
		return optional;
	}
	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	public Value getSubject() {
		return subject;
	}
	public void setSubject(Value subject) {
		this.subject = subject;
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

//	public void reverse(){
//		Term newVariable = new Term(this.value.getName());
//		newVariable.setIsVariable(this.value.isVariable());
//		if(value instanceof Term){
//			newVariable.setIsURI(((Term)value).isURI);
//		}
//
//		Term newValue = new Term(this.subject.getName());
//		newValue.setIsVariable(this.subject.isVariable());
//		newValue.setIsURI(subject.isURI);
//
//		this.variable = newVariable;
//		this.value = newValue;
//
//	}

	@Override
	public String toString() {
		if (optional) {
			return "OPTIONAL {"+subject.toString()+" "+predicate.toString()+" "+value.toString()+".}";

		}
		return subject.toString()+" "+predicate.toString()+" "+value.toString()+".";
	}

	public Triple(Value subject, Value predicate,Value value) {
		super();
		this.subject = subject;
		this.predicate = predicate;
		this.value = value;
	}
	

	public Triple(Value subject, Value predicate,Value value, boolean optional) {
		super();
		this.subject = subject;
		this.predicate = predicate;
		this.value = value;
		this.optional = optional;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (optional ? 1231 : 1237);
		result = prime * result
				+ ((predicate == null) ? 0 : predicate.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		result = prime * result
				+ ((subject == null) ? 0 : subject.hashCode());
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
		Triple other = (Triple) obj;
		if (optional != other.optional)
			return false;
		if (predicate == null) {
			if (other.predicate != null)
				return false;
		} else if (!predicate.equals(other.predicate))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		return true;
	}

}
