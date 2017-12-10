/**  
* @Title: AddPattern.java  
* @Package com.ld.pattern  
* @Description: TODO(用一句话描述该文件做什么)  
* @author ludan  
* @date 2017年8月29日  
* @version V1.0  
*/ 
package com.ld.pattern;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.ld.IO.Property.PropertyJson;
import com.ld.IO.Term.AddTerm;
import com.ld.search.VirtuosoSearch;

/** 
 * 项目名称:  KnowledgeQA  
 * 类名称:   AddPattern
 * 类描述:   添加属性模板
 * 创建人:   ludan
 * 创建时间:  2017年8月29日 上午10:11:04
 *      
 */

public class AddPattern {
	
	public static String url="jdbc:virtuoso://166.111.68.66:1111/charset=GB2312/log_enable=2";

	/**
	 * 根据查询出的学科数据自动生成学科模板，插入至xml文件
	 * @param course
	 * @param wordsEncode
	 * @param encodeWords
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 * @throws IOException
	 */
	public static void insertPatternToXml(String course,Map<String, ArrayList<String>> wordsEncode,Map<String, ArrayList<String>> encodeWords) throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException{
    	
		String fromString="";
		switch (course) {
		case "chinese": fromString="from <http://edukb.org/chinese> from <http://edukb.org/chinese_chengyu> from <http://edukb.org/chinese_zidian> from <http://edukb.org/chinese_cidian> from <http://edukb.org/chinese_gushiwen> from <http://edukb.org/chinese_zuopin>";break;
		case "geo": fromString="from <http://edukb.org/geo> from <http://edukb.org/geo_textbook> from <http://edukb.org/geo_ad_baidu> from <http://edukb.org/geo_ad_wiki> from <http://edukb.org/geo_baidu> from <http://edukb.org/geo_wiki> from <http://edukb.org/geo_china_pedia> from <http://edukb.org/geo_geonames> from <http://edukb.org/geo_china_administrative_divisions> from <http://edukb.org/geo_resort> from <http://edukb.org/geo_resort_baidu> ";break;
		case "history":fromString="from <http://edukb.org/history> from <http://edukb.org/history_pedia> from <http://edukb.org/history_baidu> from <http://edukb.org/history_baidu_infobox>";break;		case "Common": fromString="from <http://edukb.org/chinese>";break;
		default: fromString="from <http://edukb.org/"+course+">";
			break;
		}
		VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");
		String queryString = "SELECT ?subject ?label ?description "+fromString+" WHERE {"
				+ "?subject <http://www.w3.org/2000/01/rdf-schema#label> ?label."
				+ "?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DatatypeProperty>."
				+ "?subject <http://purl.org/dc/elements/1.1/description> ?description"
				+ "}";
		
        
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (queryString, set);
		ResultSet result = vqe.execSelect();

        String Filewriter = "./resources/pattern/"+course+"1.xml";
	    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(Filewriter)));
	    int id=1;
        bufferedWriter.write("<?xml version='1.0' encoding='UTF-8'?>");
        bufferedWriter.newLine();
	    bufferedWriter.write("<patterns>");
	    bufferedWriter.newLine();
	    String temp="";
        while (result.hasNext()) { 
            QuerySolution rs = (QuerySolution) result.next();
            
            String subject=rs.get("subject").toString();
            
            String type=subject.split("#")[1];
            
            String label=rs.get("label").toString();
            
            String description=rs.get("description").toString();
            
            System.out.println(subject+"   "+label+"   "+description);
            
            if(subject.contains(course)){
            	String regex=AddTerm.writeCutTerm(course,label);
	    	  	
	    	  	//对属性进行分词后生成模板
	    	  	if(regex!=null&&!regex.equals("")){
	    	  		String pattern=appendPattern(id,regex,type);
	    	  		bufferedWriter.write(pattern);
		    	  	bufferedWriter.newLine(); 
		    	  	id++;
	    	  	}
	    	  	else{
	    	  		//直接根据属性生成模板
	    	  		String pattern=appendPattern(id,label,type);
	    	  		bufferedWriter.write(pattern);
		    	  	bufferedWriter.newLine();
		    	  	id++;
	    	  	}
	    	  	
	    	  	//时间、人物、地点等特殊问法的模板
	    	  	List<String > replaceList=new ArrayList<String>();
	    	  	replaceList=process(label);
	    	  	
	    	  	for(int i=0;i<replaceList.size();i++){
	    	  		String pattern=appendPattern(id,replaceList.get(i),type);
	    	  		bufferedWriter.write(pattern);
		    	  	bufferedWriter.newLine();
		    	  	id++;
	    	  	}
            }
        }
        bufferedWriter.write("</patterns>");
        bufferedWriter.close();
        vqe.close();
        set.close();
    }
	
	
	
	
	public static String appendPattern(int id,String label,String type){
		String pattern="";
		pattern="    <pattern>\n"
				+ "        <pattern_id>"+id+"</pattern_id>\n"
				+ "        <content>(?&lt;title&gt;(.*)?)"+label+"(.*)?</content>\n"
				+ "        <subject>true</subject>\n"
				+ "        <value>false</value>\n"
				+"        <type>"+type+"</type>\n"
				+"        <class>null</class>\n"
				+"        <usage>data</usage>\n"
				+"    </pattern>\n";
		return pattern;
	}
	public static List<String> process(String label){
		String replaceStr="";
		String temp="";
		List<String > replaceList=new ArrayList<String>();
		
		if(label.endsWith("时间")){
			replaceStr="(何时|什么时候|哪(一)?年|几年|哪(一)?天|哪(个|一)?朝(代)?|什么时期|哪个时期)";
	
			if(!label.equals("时间"))
	  			temp=label.split("时间")[0];
	  		if(!temp.equals("")){
	  			replaceList.add(temp+"(.*)?"+replaceStr);
	  			replaceList.add(replaceStr+"(.*)?"+temp);
	  		}
	  		else 
	  			replaceList.add(replaceStr);
		}
		if(label.endsWith("者")||label.endsWith("员")){

			replaceStr="(谁|哪(一)?位|哪(一)?个)";
			if(label.endsWith("者"))
				temp=label.split("者")[0];
			if(label.endsWith("员"))
	  			temp=label.split("员")[0];
	  		if(!temp.equals("")){
	  			replaceList.add(temp+"(.*)?"+replaceStr);
	  			replaceList.add(replaceStr+"(.*)?"+temp);
	  		}
	  		else 
	  			replaceList.add(replaceStr);
		}
		
		if(label.endsWith("地点")||label.contains("地址")){
			replaceStr="";
			replaceStr="(什么地方|何处|何地|哪个省|哪个(城)?市|哪里|哪儿|哪处)";
			if(!label.equals("地点"))
	  			temp=label.split("地点")[0];
			if(!label.equals("地址"))
	  			temp=label.split("地址")[0];
	  		if(!temp.equals("")){
	  			replaceList.add(temp+"(.*)?"+replaceStr);
	  			replaceList.add(replaceStr+"(.*)?"+temp);
	  		}
	  		else 
	  			replaceList.add(replaceStr);
		}

		return replaceList;
	}
	
	/**
	 * 根据属性生成模板
	 * @param course
	 * @param path
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 * @throws IOException
	 */
	public static void insertTemplate(String course,String path) throws RepositoryException, MalformedQueryException, QueryEvaluationException, IOException{

		String fromString="";
		switch (course) {
		case "chinese": fromString="from <http://edukb.org/chinese> from <http://edukb.org/chinese_chengyu> from <http://edukb.org/chinese_zidian> from <http://edukb.org/chinese_cidian> from <http://edukb.org/chinese_gushiwen> from <http://edukb.org/chinese_zuopin>";break;
		case "geo": fromString="from <http://edukb.org/geo> from <http://edukb.org/geo_textbook> from <http://edukb.org/geo_ad_baidu> from <http://edukb.org/geo_ad_wiki> from <http://edukb.org/geo_baidu> from <http://edukb.org/geo_wiki> from <http://edukb.org/geo_china_pedia> from <http://edukb.org/geo_geonames> from <http://edukb.org/geo_china_administrative_divisions> from <http://edukb.org/geo_resort> from <http://edukb.org/geo_resort_baidu> ";break;
		case "history":fromString="from <http://edukb.org/history> from <http://edukb.org/history_pedia> from <http://edukb.org/history_baidu> from <http://edukb.org/history_baidu_infobox>";break;		case "Common": fromString="from <http://edukb.org/chinese>";break;
		default: fromString="from <http://edukb.org/"+course+">";
			break;
		}
		
		String queryString = "SELECT distinct ?subject ?label ?description "+fromString+" WHERE {"
				+ "?subject <http://www.w3.org/2000/01/rdf-schema#label> ?label."
				+ "?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DatatypeProperty>."
				+ "?subject <http://purl.org/dc/elements/1.1/description> ?description"
				+ "}";
		String queryString2 = "SELECT distinct ?subject ?label ?description "+fromString+" WHERE {"
				+ "?subject <http://www.w3.org/2000/01/rdf-schema#label> ?label."
				+ "?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty>."
				+ "?subject <http://purl.org/dc/elements/1.1/description> ?description"
				+ "}";
		
		VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");

		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (queryString, set);
		ResultSet result = vqe.execSelect();
       
		vqe = VirtuosoQueryExecutionFactory.create (queryString2, set);
		ResultSet result2 = vqe.execSelect();

        try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			Statement stmt=cn.createStatement();
			
			JSONObject firstJson=PropertyJson.readJsonFile(course,path);
			
			insert(stmt,result,course,firstJson);
			insert(stmt,result2,course,firstJson);
			cn.close();
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
        set.close();
		vqe.close();
    }
	
	public static void insert(Statement stmt,ResultSet result,String course,JSONObject firstJson) throws QueryEvaluationException, SQLException{
		String temp="";
		List<String> uniqList=new ArrayList<String>();
		VirtuosoSearch vSearch=new VirtuosoSearch(course);
		
		while (result.hasNext()) {
            QuerySolution rs=result.nextSolution();
            
            String subject=rs.get("subject").toString();
            
            String type=null;
            if(subject.endsWith("biology#")){
            	type="biology";           			
            }
            else if (subject.split("#")[1].startsWith("-")) {
            	type=subject.substring(subject.indexOf("biology")).replaceAll("\\-", "").replaceAll("#", "");
			}
            else if((subject.contains("biology")||subject.contains("common"))&&course.equals("biology")&&subject.contains("-"))
            	type=subject.substring(subject.indexOf("#")).replaceAll("\\-", "").replaceAll("#", "");
            else if(subject.contains("-")&&subject.contains(course)&&subject.contains("#")){
            	type=subject.split("#")[1].replaceAll("\\-\\d+", "").replaceAll("\\-", "");
            }
            else if(subject.split("#").length>1)
            	type=subject.split("#")[1];
            
            String label=rs.get("label").toString();
            if(type.equals("Content")) label="内容";

            String description=rs.get("description").toString();
            if(subject.contains(label)&&description!=null&&!description.equals("")) label=description.replace("geonames-", "");
            
//            System.out.println(subject+"   "+label+"   "+description);
            
            temp=type+label;
            if(uniqList.contains(type+label)) continue;
            else uniqList.add(temp);
            int priority=1;
            if(label.length()<3&&subject.contains(course)){
	            int count=vSearch.SearchCount(subject);
	            
	            if(count>100||count<10){
	            	priority=2;
	            }
            }
            String sql="";
            if(firstJson.containsKey(type)) continue;
            if(subject.contains(course)||subject.contains("common")||subject.contains("demo")){
            	String regex=AddTerm.writeCutTerm(course,label);
	    	  	
	    	  	//对属性进行分词后生成模板
	    	  	if(regex!=null&&!regex.equals("")){
	    	  		sql="insert ignore into "+course+"_template (content,type,priority) values ('(?<title>(.*)?)"+regex+"(.*)?','"+type+"',"+priority+");";
	    	  		stmt.execute(sql);
	    	  	}
	    	  	else{
	    	  		//直接根据属性生成模板
	    	  		sql="insert ignore into "+course+"_template (content,type,priority) values ('(?<title>(.*)?)"+label+"(.*)?','"+type+"',"+priority+");";
	    	  		stmt.execute(sql);
	    	  	}
	    	  	
	    	  	//时间、人物、地点等特殊问法的模板
	    	  	List<String > replaceList=new ArrayList<String>();
	    	  	replaceList=AddPattern.process(label);
	    	  	
	    	  	for(int i=0;i<replaceList.size();i++){
	    	  		sql="insert ignore into "+course+"_template (content,type,priority) values ('(?<title>(.*)?)"+replaceList.get(i)+"(.*)?','"+type+"',"+priority+");";
	    	  		stmt.execute(sql);
	    	  	}
            }
        }
	}
}
