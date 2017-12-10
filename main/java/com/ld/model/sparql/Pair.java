package com.ld.model.sparql;

import java.io.Serializable;

public class Pair implements Serializable, Cloneable
{
	private static final long serialVersionUID = -1255754209857823420L;

	private String filterName;
	private String filterStr;
	
	public Pair(String filterName,String filterStr){
		
		this.filterName=filterName;
		this.filterStr=filterStr;
	}
	
	public String toString(){
		
		String filter="";
		if(filterStr!=null&&!filterStr.equals(""))
//			filter="FILTER CONTAINS(str(?"+filterName+"),'"+filterStr+"')";
			filter="?"+filterName+" bif:contains '\""+filterStr+"\"'";
		return filter;
	}

	public String getFilterName() {
		return filterName;
	}

	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}

	public String getFilterStr() {
		return filterStr;
	}

	public void setFilterStr(String filterStr) {
		this.filterStr = filterStr;
	}
}