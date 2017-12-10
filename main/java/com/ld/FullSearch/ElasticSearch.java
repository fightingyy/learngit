package com.ld.FullSearch;

import java.io.*;
import java.net.*;
import java.util.*;

import net.sf.json.JSONArray;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.*;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.*;
import org.elasticsearch.client.transport.*;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.*;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.action.delete.*;

import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import com.ld.IO.StringProcess;
import com.ld.IO.remove.RemoveDuplicate;
import com.ld.Parser.WordParser;
import com.ld.search.VirtuosoSearch;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：ElasticSearch   
* 类描述： 对ElasticSearch索引的操作  
* 创建人：ludan   
* 创建时间：2017年4月26日 下午1:04:26   
* @version        
*/

public class ElasticSearch {
	
	TransportClient client=null;
	private IndicesAdminClient adminClient=null;
	List<String> graphList=new ArrayList<String>();
	static List<String> predicateList=new ArrayList<String>();
	
	static{
		predicateList.add("原因");
		predicateList.add("方法");
		predicateList.add("作用");
		predicateList.add("意义");
		predicateList.add("关系");
		predicateList.add("联系");
	}

	public ElasticSearch() {
		
		try {
			//连接Elasticsearch
			Settings settings = Settings.settingsBuilder().put("client.transport.sniff", true).put("cluster.name","es_knowledge").build(); 
			client =  TransportClient.builder().build().addTransportAddresses(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
//			client = TransportClient.builder().build().addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("10.1.1.119"), 9200));

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** 
     * 判断集群中{index}是否存在 
     */  
	public boolean isIndexExists(String indexName) {
        boolean flag = false;
        
        IndicesExistsRequest inExistsRequest = new IndicesExistsRequest(indexName);
 
		IndicesExistsResponse inExistsResponse = client.admin().indices()
		        .exists(inExistsRequest).actionGet();
 
		if (inExistsResponse.isExists()) {
		    flag = true;
		} else {
		    flag = false;
		}
 
        return flag;
    }  
    
    /**
     * 建立mapping
     * @param indices
     * @param mappingType
     */
    public void createMapping(String indices,String mappingType){
        
        XContentBuilder builder;
		try {
			builder = XContentFactory.jsonBuilder()
			        .startObject()
			        .startObject(indices)
			        .startObject("properties")
			        .startObject("subject").field("type", "string").field("analyzer", "index_ansj").endObject()
			        .startObject("predicate").field("type", "string").field("analyzer", "index_ansj").endObject()
			        .startObject("value").field("type", "string").field("analyzer", "index_ansj").endObject()
			        .endObject()
			        .endObject()
			        .endObject();
			PutMappingRequest mapping = Requests.putMappingRequest(indices).type(mappingType).source(builder);
	        client.admin().indices().putMapping(mapping).actionGet();
//	        client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    /**
     * 新建并插入索引
     * @param course
     */
    public void InsertIndex(String course){  
 
        VirtuosoSearch vSearch=new VirtuosoSearch(course);
        
        //各学科添加子图
        switch (course) {
		case "chinese":{
			graphList.add("chinese");
//			graphList.add("chinese_cidian");
			graphList.add("chinese_gushiwen");
//			graphList.add("chinese_zidian");
//			graphList.add("chinese_chengyu");
			graphList.add("chinese_zuopin");
		}break;
		case "geo":{
			graphList.add("geo");
			graphList.add("geo_baidu");
			graphList.add("geo_wiki");
			graphList.add("geo_ad_baidu");
			graphList.add("geo_ad_wiki");
			graphList.add("geo_china_pedia");
//			graphList.add("geo_textbook");
			graphList.add("geo_china_administrative_divisions");
			graphList.add("geo_resort");
			graphList.add("geo_resort_baidu");
		}break;
		case "history":{
			graphList.add("history");
			graphList.add("history_pedia");
			graphList.add("history_baidu");
			graphList.add("history_baidu_infobox");
		}break;

		default:graphList.add(course);
			break;
		}

        BulkRequestBuilder bulkRequest = client.prepareBulk();
        int count=0;
        for(String graphName:graphList){
        	//查询出子图中的所有三元组
	        JSONArray dataJsonArray=vSearch.SearchValue(course,graphName); 	        

	        for(int i=0;i<dataJsonArray.size();i++){  
	        	String json=dataJsonArray.getJSONObject(i).toString();
	        	
	        	//开启批量插入
                bulkRequest.add(client.prepareIndex(course, graphName).setSource(json));
		        //每五千条提交一次
                if (i% 5000==0) {                	
                    bulkRequest.execute().actionGet();
                    bulkRequest = client.prepareBulk();
                }
                count++;
	        }
	        if(dataJsonArray.size()%5000!=0){
	        	bulkRequest.execute().actionGet();
	        	bulkRequest = client.prepareBulk();
	        }
        }
        System.out.println(course+"索引建立完成，共建立"+count+"条");
        client.close();
    }
    
    /**
    * 删除索引
    * @param indexName
    * @return
    */
    public boolean deleteIndex(String indexName){
    	
    	DeleteIndexResponse dResponse=null;
    	
    	dResponse = client.admin().indices().prepareDelete(indexName)
		             .execute().actionGet();
		     if (dResponse.isAcknowledged()) {
		         System.out.println("delete index "+indexName+"  successfully!");
		 }else{
		     System.out.println("Fail to delete index "+indexName);
		 }
    	 return dResponse.isAcknowledged();
    }
    
    
    /**
    * 更新所有学科的索引
    * @param course
    * @throws
    */
    public void updateIndex(String course){
    	ElasticSearch elasticSearch=new ElasticSearch();
    	
    	DeleteIndexResponse dResponse =null;
    	if(elasticSearch.isIndexExists(course)){
	    	dResponse= client.admin().indices().prepareDelete(course).execute().actionGet();
		    
	    	//删除索引
	    	if (dResponse.isAcknowledged()) {
	    		System.out.println("delete index "+course+"  successfully!");
	    		elasticSearch.InsertIndex(course);
	    	}else{
	    		System.out.println("Fail to delete index "+course);
		    }
    	}
    	//建立索引
    	else{
    		elasticSearch.InsertIndex(course);
    	}
    }
    
    /**
     * 根据问题查询索引
     * @param course
     * @param question
     * @return
     * @throws Error
     */
    public List<String> SearchIndex(String course,String question) throws Error{
    	
    	
    	List<String> resultList=new ArrayList<String>();
    	BoolQueryBuilder booleanQuery = new BoolQueryBuilder();
    	SearchRequestBuilder builder = client.prepareSearch(course).setSearchType(SearchType.DEFAULT).setFrom(0).setSize(50); 
    	
    	List<String> keyList=new ArrayList<String>();
    	List<Term> cutWords=WordParser.CutWord(question);
    	int count=0;
    	
    	//抽取问题中的关键词
    	for(Term word:cutWords){
    		if(word.nature==Nature.g){
    			keyList.add(word.word);
    		}
    		else if(course.equals("geo")&&question.contains("最")){
    			if(word.nature==Nature.gm||word.nature==Nature.ns||word.word.contains("最")){
    				keyList.add(word.word);
        		}
    		}
    		//去除问题中的疑问词
    		if(word.nature.toString().startsWith("ry")){
    			question=question.replace(word.word, "");
    			count++;
    		}
    	}
    	
    	//查询
    	QueryStringQueryBuilder mustqueryBuilderb=null;
		if(!keyList.isEmpty()){
			for(String key:keyList){
				mustqueryBuilderb=new QueryStringQueryBuilder("\""+key+"\"").field("value");
//				mustqueryBuilderb=new QueryStringQueryBuilder(keyword).field("value").defaultOperator(Operator.AND);
				booleanQuery.must(mustqueryBuilderb);
			}
		}
 
//		float tieBreak=(float) 0.2;
    	MultiMatchQueryBuilder  multiMatchQuery =QueryBuilders.multiMatchQuery(question,"value");
    	booleanQuery.must(multiMatchQuery);

    	builder.setQuery(booleanQuery);  

		SearchResponse response = builder.execute().actionGet();   
		System.out.println("共查询到"+response.getHits().getTotalHits()+"条结果");
		List<String> tempList=new ArrayList<String>();

		//对结果进行逐句分割
		for(SearchHit hit:response.getHits()){
			Map<String, Object> source = hit.getSource();
			if (!source.isEmpty()) {
				
				String subject=source.get("subject").toString().replaceAll("\n", "").replaceAll("\\s", "").replaceAll("\\t", "");
				String predicate=source.get("predicate").toString().replaceAll("\n", "").replaceAll("\\s", "").replaceAll("\\t", "");
				String value=source.get("value").toString();
				if(value.equals("")) continue;
				if(predicate.equals("实体限制")||predicate.equals("中文名"))continue;
				String[] arrayStrings=null;
				
				//政治中的一些特定属性不进行分割
				if(course.equals("politics")&&predicateList.contains(predicate)||(course.equals("biology")&&count>2)){
					resultList=StringProcess.array_unique(resultList, subject+"-"+predicate+":"+value);
					continue;
				}
				if(!tempList.contains(subject+predicate+value)){
					tempList.add(subject+predicate+value);
				}
				else continue;
				if(!value.contains("原因相同")){
					if(StringProcess.CountNumber(value, "\n")>2&&value.length()>200)
						arrayStrings=value.split("。|  \n  \n|\n");
					else
						arrayStrings=value.split("。|  \n  \n");
					value=value.replaceAll("\n", "").replaceAll(" ", "");
				}
				else arrayStrings=value.split("\n");
				
				arrayStrings=RemoveDuplicate.CheckNull(arrayStrings);
				
				if(value.length()>300&&arrayStrings.length==1)
					arrayStrings=value.split("\n");
				if(value.length()<40&&arrayStrings.length==2&&!course.equals("geo")){
					arrayStrings=new String[1];
					arrayStrings[0]=value;
				}
				for(String str:arrayStrings){
					if((course.equals("geo")&&str.length()>300)||(course.equals("history")&&str.contains("③"))){
						String[] arrays=str.split("\n");
						for(String s:arrays){
							if(s.length()<300)
								resultList=StringProcess.array_unique(resultList, subject+"-"+predicate+":"+s);
						}
					}
					else {
						resultList=StringProcess.array_unique(resultList, subject+"-"+predicate+":"+str);
						
					}
						
				}
			}
		}		
		client.close();
		//去重
		resultList=RemoveDuplicate.remove2(resultList);
    	return resultList;
    }
    
    /**
    * 删除索引
    */
    public void delete(){  
    	
    	DeleteResponse response = client.prepareDelete("history", "", "")   
    	        .execute()   
    	        .actionGet();  
    	
        System.out.println(response.getType());  
    } 

	public static void main(String[] args) {
		
		ElasticSearch elasticSearch=new ElasticSearch();

		List<String> courseList=new ArrayList<String>();

		//courseList.add("chinese");
		//courseList.add("history");
		//courseList.add("english");
		//courseList.add("geo");
		courseList.add("chemistry");
		//courseList.add("biology");
		//courseList.add("politics");
		//courseList.add("physics");
		
		String course="";
		for(int i=0;i<courseList.size();i++){
			
			course=courseList.get(i);
			elasticSearch.updateIndex(course);
		}	  
	}

}
