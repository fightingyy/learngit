package com.ld.model;

public class Filter {

	private String name;
	private String pattern;

	public Filter(String name,String pattern){
		
		this.name=name;
		this.pattern=pattern;
	}
	
	public String toString(){

		return  "FILTER regex(?"+name+",'" +pattern+"')";
	}
	
	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	

}
