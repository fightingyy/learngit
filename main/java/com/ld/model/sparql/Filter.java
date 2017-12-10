package com.ld.model.sparql;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

public class Filter implements Serializable, Cloneable
{
	private static final long serialVersionUID = -6243542586884540703L;

	private boolean and = true;
	private HashSet<Pair> pairs=new HashSet<Pair>();
	
	public Filter(HashSet<Pair> pairSet){
		
		this.pairs=pairSet;
	}
	
//	public String toString(){
//		
//		String filter="";
//		String temp="";
//		for(Pair pair:pairs){
//			filter=pair.toString();
//			if(filter!=null&&!filter.equals(""))
//				temp+="\t"+filter+"\n";
//		}
//		
//		return temp;
//	}
//	
	public String toString(){
		
		String filter="";
		String temp="";
		String type="";
		for (Pair pair:pairs) {  
			  
			String str=pair.getFilterStr();
			type=pair.getFilterName();
			if(temp.equals(""))
				temp="\""+str+"\"";
			else{
				temp+=" and \""+str+"\"";
			}
		}  
		if(!type.equals("")&&!temp.equals(""))
			filter="\t?"+type+" bif:contains '"+temp+"'";
		return filter;
	}

	//uses && if set true, otherwise ||
	public void setAnd(boolean and)
	{
		this.and = and;
	}

	public boolean isAnd(){
		return and;
	}

	public HashSet<Pair> getPairs() {
		return pairs;
	}


	public void setPairs(HashSet<Pair> pairs) {
		this.pairs = pairs;
	}
}
