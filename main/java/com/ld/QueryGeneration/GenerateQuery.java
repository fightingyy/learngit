package com.ld.QueryGeneration;

import java.util.*;

import net.sf.json.JSONObject;

import org.eclipse.jdt.internal.compiler.ast.ThisReference;
import org.python.antlr.PythonParser.else_clause_return;

import com.ld.model.QueryElement;
import com.ld.model.QueryObject;
import com.ld.model.sparql.*;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：GenerateQuery   
* 类描述：根据三元组生成查询语句   
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:01:55   
* @version        
*/
public class GenerateQuery {
	
//	private List<Term> terms=new ArrayList<Term>();
//	private List<Triple> conditions=new ArrayList<Triple>();
	public List<QueryObject> queryList=new ArrayList<QueryObject>();
	Map<String,JSONObject> queryMap=new HashMap<String,JSONObject>();
	int i=0;

	/**
	 * Generate Queries
	 * @param t
	 * @return
	 */
	
	public GenerateQuery(){}
	
	public void generateQuery(List<QueryElement> queryElementList){
		List<Term> terms=new ArrayList<Term>();
		List<Triple> conditions=new ArrayList<Triple>();
		
		List<Prefix> prefixes=new ArrayList<Prefix>();
		for(QueryElement queryElement:queryElementList){

			QueryObject queryObject=new QueryObject();
			//add 11.26
			queryObject.setTamplate(queryElement.getTamplate());
			Filter filter = null;
			terms.clear();
			conditions.clear();
			List<SetVariables> Variables=queryElement.getVariablesList();
			if(Variables.size()==1) continue;
			for(SetVariables sv:Variables){
				
				conditions.add(sv.getTriple());
				for(Term term:sv.getTerms()){
					terms.add(term);
				}
				prefixes=sv.getPrefixes();
				if(sv.getFilter()!=null){
					filter=sv.getFilter();
				}
				
			}
			Query query=null;
			if(filter!=null&&!filter.getPairs().isEmpty()){
				query=new Query(prefixes,terms,conditions,filter);
			}
			else 
				query=new Query(prefixes,terms,conditions);
			if(query!=null){
				queryObject.setScore(queryElement.getScore());
				queryObject.setSubjectName(queryElement.getSubjectName());
				queryObject.setQuery(query.toString());	
				queryObject.setFilterStr(queryElement.getFilterStr());
				queryList.add(queryObject);
			}
		}
	}
	
	/**
	 * 生成sparql查询语句
	 * @param queryElementList
	 * @param course
	 */
	public void generateQuery(List<QueryElement> queryElementList,String course){
		List<Term> terms=new ArrayList<Term>();
		List<Triple> conditions=new ArrayList<Triple>();
		List<String> removeList=new ArrayList<String>();
		List<Prefix> prefixes=new ArrayList<Prefix>();
		String fromString="";
		switch (course) {
		case "chinese": fromString="from <http://edukb.org/chinese> from <http://edukb.org/chinese_chengyu> from <http://edukb.org/chinese_zidian> from <http://edukb.org/chinese_cidian> from <http://edukb.org/chinese_gushiwen> from <http://edukb.org/chinese_zuopin>";break;
		case "geo": fromString="from <http://edukb.org/geo> from <http://edukb.org/geo_textbook> from <http://edukb.org/geo_ad_baidu> from <http://edukb.org/geo_ad_wiki> from <http://edukb.org/geo_baidu> from <http://edukb.org/geo_wiki> from <http://edukb.org/geo_china_pedia> from <http://edukb.org/geo_geonames> from <http://edukb.org/geo_china_administrative_divisions> from <http://edukb.org/geo_resort> from <http://edukb.org/geo_resort_baidu> ";break;
//		case "geo": fromString="from <http://edukb.org/geo> from <http://edukb.org/geo_textbook> from <http://edukb.org/geo_china_pedia>";break;
		case "history":fromString="from <http://edukb.org/history> from <http://edukb.org/history_pedia> from <http://edukb.org/history_baidu> from <http://edukb.org/history_baidu_infobox>";break;		case "english":fromString="from <http://edukb.org/english> from <http://edukb.org/english_cidian>";break;
		default: fromString="from <http://edukb.org/"+course+">";
			break;
		}
		
		for(QueryElement queryElement:queryElementList){

			QueryObject queryObject=new QueryObject();
			//add 11.26
			queryObject.setTamplate(queryElement.getTamplate());
			List<SetVariables> Variables=queryElement.getVariablesList();
			Filter filter = null;
			terms.clear();
			conditions.clear();
			
			if(Variables.size()==1) continue;
			for(SetVariables sv:Variables){
				
				conditions.add(sv.getTriple());
				for(Term term:sv.getTerms()){
					terms.add(term);
				}
				prefixes=sv.getPrefixes();
				if(sv.getFilter()!=null){
					filter=sv.getFilter();
				}
				
			}
			Query query=null;
			if(filter!=null&&!filter.getPairs().isEmpty()){
//				if(course.equals("geo")){
//	//				fromString="from <http://edukb.org/geo> from <http://edukb.org/geo_baidu> from <http://edukb.org/geo_wiki> from <http://edukb.org/geo_china_pedia>";
//					fromString="from <http://edukb.org/geo> from <http://edukb.org/geo_baidu> from <http://edukb.org/geo_wiki>";
//				}
				query=new Query(prefixes,terms,conditions,filter,fromString);
			}
			else 
				query=new Query(prefixes,terms,conditions,fromString);
			if(query!=null&&!queryMap.containsKey(query.toString())){
				JSONObject cell=new JSONObject();
				cell.put("index", i);
				cell.put("score", queryElement.getScore());
				
				queryMap.put(query.toString(), cell);
			}
			else {
				JSONObject object=queryMap.get(query.toString());
				if(queryElement.getScore()>object.getDouble("score")){
					int index=object.getInt("index");
					queryList.remove(index);
					queryObject.setScore(queryElement.getScore());
					queryObject.setSubjectName(queryElement.getSubjectName());
					queryObject.setQuery(query.toString());
					queryObject.setFilterStr(queryElement.getFilterStr());
					queryObject.setLimitName(queryElement.getLimitName());
					queryList.add(object.getInt("index"),queryObject);
					object=new JSONObject();
					object.put("index",index);
					object.put("score", queryElement.getScore());
					queryMap.put(query.toString(), object);
				}
				continue;
			}
			if(query!=null){
				queryObject.setScore(queryElement.getScore());
				queryObject.setSubjectName(queryElement.getSubjectName());
				queryObject.setQuery(query.toString());
				queryObject.setFilterStr(queryElement.getFilterStr());
				queryObject.setLimitName(queryElement.getLimitName());
				queryList.add(queryObject);
				i++;
			}
		}
	}	
}