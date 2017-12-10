package com.ld.QueryGeneration;

import java.util.*;

import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.vocabulary.*;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：SetPrefix   
* 类描述： 确定学科前缀的缩写
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:56:15   
* @version        
*/
public class SetPrefix {

	private static Map<String, String> prefixes = new HashMap<String, String>();

	public SetPrefix(String course) {
		prefixes.put(RDF.getURI(), "rdf");
		prefixes.put(RDFS.getURI(), "rdfs");
		prefixes.put("http://edukb.org/knowledge/0.1/property/common#", "cop");
//		prefixes.put("http://edukb.org/knowledge/0.1/property/chinese#", "chp");		
//		prefixes.put("http://edukb.org/knowledge/0.1/instance/chinese#", "chi");	
//		prefixes.put("http://edukb.org/knowledge/0.1/property/geo#", "geop");
//		prefixes.put("http://edukb.org/knowledge/0.1/instance/geo#", "geoi");
//		prefixes.put("http://edukb.org/knowledge/0.1/class/geo#", "geoc");
		
		switch(course){
		case "chinese" : prefixes.put("http://edukb.org/knowledge/0.1/property/chinese#", "chp");break;
		case "geo": prefixes.put("http://edukb.org/knowledge/0.1/property/geo#", "geop"); break;
		case "math": prefixes.put("http://edukb.org/knowledge/0.1/property/math#", "mp");
		case "english": prefixes.put("http://edukb.org/knowledge/0.1/property/english#", "ep");
		case "chemistry": prefixes.put("http://edukb.org/knowledge/0.1/property/chemistry#", "cyp"); break;
		case "history": {
			prefixes.put("http://edukb.org/knowledge/0.1/property/history#", "hp"); 
			prefixes.put("http://edukb.org/knowledge/0.1/class/history#", "hc"); 
		} break;
		case "physics": prefixes.put("http://edukb.org/knowledge/0.1/property/physics#", "php"); break;
		case "biology": prefixes.put("http://edukb.org/knowledge/0.1/property/biology#", "bp"); break;
		case "politics": prefixes.put("http://edukb.org/knowledge/0.1/property/politics#", "pp"); break;
		default :prefixes.put("", "all");
		}
	}

	public static Map<String, String> getPrefixes(){
		return prefixes;
	}

}
