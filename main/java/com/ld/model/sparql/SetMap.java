package com.ld.model.sparql;

import java.util.Map;

public class SetMap {

	private String name;
	private String prefix_name;
	private boolean isVariable;
	
	Map<String,Map<String,Boolean>> map;
	
	public SetMap(String name,String prefix_name,boolean isVariable) {
		this.name=name;
		this.prefix_name=prefix_name;
		this.isVariable=isVariable;
		
		Map<String,Boolean> map2=null;
		map2.put(prefix_name,isVariable);
		this.map.put(name, map2);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrefix_name() {
		return prefix_name;
	}

	public void setPrefix_name(String prefix_name) {
		this.prefix_name = prefix_name;
	}

	public boolean isVariable() {
		return isVariable;
	}

	public void setVariable(boolean isVariable) {
		this.isVariable = isVariable;
	}

	public Map<String,Map<String,Boolean>> getMap(){
		return  map;
	}
}
