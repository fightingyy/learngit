package com.ld.Util;

import java.util.*;

import com.ld.model.AnswerObject;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：MapUtil   
* 类描述： 对map进行排序
* 创建人：ludan   
* 创建时间：2017年7月17日 下午4:00:01   
* @version        
*/
public class MapUtil {

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map )  {  
	    
		List<Map.Entry<K, V>> list =  new LinkedList<Map.Entry<K, V>>( map.entrySet() );  
	    Collections.sort( list, new Comparator<Map.Entry<K, V>>(){  
	        public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )  
	        {  
	            return (o1.getValue()).compareTo( o2.getValue() );  
	        }  
	    } );  
	
	    Map<K, V> result = new LinkedHashMap<K, V>();  
	    for (Map.Entry<K, V> entry : list)  
	    {  
	        result.put( entry.getKey(), entry.getValue() );  
	    }  
	    return result;  
	} 
	
	public static <K, V> Map<K, V> sortByKey( Map<K, V> map )  {  
	    
		List<Map.Entry<K, V>> list =  new LinkedList<Map.Entry<K, V>>( map.entrySet() );  
	    Collections.sort( list, new Comparator<Map.Entry<K, V>>(){  
	        public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )  
	        {  
	            return ((String) o1.getKey()).compareTo( (String) o2.getKey() );  
	        }  
	    } );  
	
	    Map<K, V> result = new LinkedHashMap<K, V>();  
	    for (Map.Entry<K, V> entry : list)  
	    {  
	        result.put( entry.getKey(), entry.getValue() );  
	    }  
	    return result;  
	}
	
	public static List<AnswerObject> sortObject(List<AnswerObject> anObjList){
		
		List<AnswerObject> sortList=new ArrayList<AnswerObject>();
		
		Map<Integer, String> sortMap=new HashMap<Integer, String>();
		for(int i=0;i<anObjList.size();i++){
			sortMap.put(i, anObjList.get(i).getValue());
		}
		sortMap=MapUtil.sortByValue(sortMap);
		for(int key:sortMap.keySet()){
			sortList.add(anObjList.get(key));
		}
		
		return sortList;
	}

	
}
