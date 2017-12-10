package com.ld.Util;

import java.util.*;
import java.util.Map.Entry;

import com.ld.model.AnswerObject;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：Sort   
* 类描述：  对map进行排序
* 创建人：ludan   
* 创建时间：2017年7月17日 下午4:00:43   
* @version        
*/
public class Sort {

	 public static Map<String, Integer> sortMapByIntValue(Map<String, Integer> oriMap) {  
	        if (oriMap == null || oriMap.isEmpty()) {  
	            return null;  
	        }  
	        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();  
	        List<Map.Entry<String, Integer>> entryList = new ArrayList<Map.Entry<String, Integer>>(  
	                oriMap.entrySet());  
	        Collections.sort(entryList, new MapValueComparator());  
	  
	        Iterator<Map.Entry<String, Integer>> iter = entryList.iterator();  
	        Map.Entry<String, Integer> tmpEntry = null;  
	        while (iter.hasNext()) {  
	            tmpEntry = iter.next();  
	            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());  
	        }  
	        return sortedMap;  
     }  

	 
	 public static Map<String, Double> sortMapByDoubleValue(Map<String, Double> oriMap) {  
		 
		 DoubleComparator bvc =  new DoubleComparator(oriMap);  
		 TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);  
		 sorted_map.putAll(oriMap);
        
		 return sorted_map;  
	 }
	
	 
	 public static Map<String, Double> addMap(Map<String, Double> oriMap,Map<String, Integer> countMap) {  
		 
		 Map<String, Double> map=new HashMap<String, Double>();
		 
		 for (Entry<String, Double> entry : oriMap.entrySet()) { 
			 String item=entry.getKey();
			 double score=entry.getValue();
			 int count=countMap.get(item);
			 score+=(double)count/10;
			 map.put(item, score);
		 }
        
		 return map;  
	 }
	 
}

class MapValueComparator implements Comparator<Map.Entry<String, Integer>> {  
	  
    @Override  
    public int compare(Entry<String, Integer> me1, Entry<String, Integer> me2) {  
  
        return me2.getValue().compareTo(me1.getValue());  
    }  
}   

class DoubleComparator implements Comparator<String> {  
	  
    Map<String, Double> base;  
    public DoubleComparator(Map<String, Double> base) {  
        this.base = base;  
    }  
  
    // Note: this comparator imposes orderings that are inconsistent with equals.      
    public int compare(String a, String b) {  
        if (base.get(a) >= base.get(b)) {  
            return -1;  
        } else {  
            return 1;  
        } // returning 0 would merge keys  
    }  
}  
