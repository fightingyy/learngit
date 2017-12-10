package com.ld.IO;

import java.util.List;
import java.util.Map;

public class StringProcess {

	public StringProcess() {
		// TODO Auto-generated constructor stub
	}

	/**
	* 添加唯一的元素
	* @param list
	* @param a
	* @return
	*/
	public static List<String> array_unique(List<String> list, String a) {
        if (!list.contains(a)) {
            list.add(a);
        }
        return list;
    }
	
	
	/**
	* 计算字符串中某个字符出现的次数
	* @param item
	* @param mark
	* @return
	*/
	public static int CountNumber(String item,String mark){
		int count=0;
		int index=0;
		while (true) {  
		    index = item.indexOf(mark, index + 1);  
		    if (index > 0) {  
		    	count++;  
		    } 
		    else {  
		    	break;  
		    }  
	    }  
		return count;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	/**
	* 将属性的uri对应到label
	* @param courseMap
	* @param commonMap
	* @param property
	* @return
	*/
	public static String toLabel(Map<?, ?> courseMap, Map<?, ?> commonMap, String property){
		
    	if((String) courseMap.get(property)!=null){
        	property=(String) courseMap.get(property);
        }
        if((String) commonMap.get(property)!=null){
        	property=(String) commonMap.get(property);
        }
        if(property.equals("consistedof")) 
        	property="组成";
        else if (property.equals("consistedof")) 
        	property="内容";
        else if(property.equals("type"))
        	property="类型";
        
		return property;
	}

	/**
	* 根据问题中的句号问号，确定是否将答案截断
	* @param question
	* @return
	*/
	public static int getSentenceCount(String question){
		int count=0;
		
		int count1=StringProcess.CountNumber(question, "。");
		int count2=StringProcess.CountNumber(question, "？");
		
		if(count1>1||count2>1)
			count=2;
		else if(count1+count2>1)
			count=2;
		else if(count1==1&&!question.endsWith("。"))
			count=2;
		else if(count2==1&&!question.endsWith("？"))
			count=2;
		
		return count;
	}
}
