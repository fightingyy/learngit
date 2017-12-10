package com.ld.search;


import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.struts2.ServletActionContext;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.ld.IO.MaxString;
import com.ld.IO.StringProcess;
import com.ld.IO.FirstAndLast;
import com.ld.IO.Property.ReadProperty;
import com.ld.Parser.WordParser;
import com.ld.answer.DecideAnswer;
import com.ld.model.AnswerObject;
import com.ld.model.QueryObject;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：VirtuosoSearch   
* 类描述： Virtuoso查询  
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:11:28   
* @version        
*/
public class VirtuosoSearch {
	
	public static String url="jdbc:virtuoso://166.111.68.66:1111";
	Map<?, ?> chineseMap=new HashMap<Object, Object>();
	Map<?, ?> commonMap=new HashMap<Object, Object>();
	Map<?, ?> courseMap=new HashMap<Object, Object>();
	
	public String subjectName="";
	static String fromString="";
	public boolean isContinue=true;
	private int rycount=0;
//	SemanticSimilarity semanticSimilarity=SemanticSimilarity.getInstance();
	
	private Logger logger = LoggerFactory.getLogger(VirtuosoSearch.class);
	
	public VirtuosoSearch(String course){
		switch (course) {
		case "chinese": fromString="from <http://edukb.org/chinese> from <http://edukb.org/chinese_chengyu> from <http://edukb.org/chinese_zidian> from <http://edukb.org/chinese_cidian> from <http://edukb.org/chinese_gushiwen> from <http://edukb.org/chinese_zuopin>";break;
		case "geo": fromString="from <http://edukb.org/geo> from <http://edukb.org/geo_textbook> from <http://edukb.org/geo_ad_baidu> from <http://edukb.org/geo_ad_wiki> from <http://edukb.org/geo_baidu> from <http://edukb.org/geo_wiki> from <http://edukb.org/geo_china_pedia> from <http://edukb.org/geo_geonames> from <http://edukb.org/geo_china_administrative_divisions> from <http://edukb.org/geo_resort> from <http://edukb.org/geo_resort_baidu> ";break;
		case "history":fromString="from <http://edukb.org/history> from <http://edukb.org/history_pedia> from <http://edukb.org/history_baidu> from <http://edukb.org/history_baidu_infobox>";break;
		case "english":fromString="from <http://edukb.org/english> from <http://edukb.org/english_cidian>";break;
		default: fromString="from <http://edukb.org/"+course+">";
			break;
		}
	}
	
	/**
	 * 根据查询语句进行查询
	 * @param queryObject
	 * @param course
	 * @param usageList
	 * @param question
	 * @return
	 */
	public JSONArray searchQuery(QueryObject queryObject, String course,List<String> usageList,String question){
		
		JSONArray jsonArray = new JSONArray();
		JSONArray filterJson = new JSONArray();
		JSONArray MatchJson = new JSONArray();
		
		ServletContext sct= ServletActionContext.getServletContext();
		try {
			VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");
			commonMap=ReadProperty.selectProperty("Common");
			courseMap=ReadProperty.selectProperty(course);

			double score=0;
			String query=queryObject.getQuery();
			String filterStr=queryObject.getFilterStr();
			
			VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (query, set);
			ResultSet results = vqe.execSelect();
			
			int numHeader = results.getResultVars().size();
			JSONArray arrHeader = new JSONArray();

	        List<String> sList=new ArrayList<String>();

	        int acount=9;
	        if(usageList.contains("status")) acount=30;
	        boolean hasSubLabel=false;
	        FirstAndLast subjects=new FirstAndLast();

        	List<FirstAndLast> subjectList=new ArrayList<FirstAndLast>();
              	 
        	int tag=1;
            while (results.hasNext()) {
            	if(tag==acount)break;
            	int t=1;
            	QuerySolution rs = results.nextSolution();
                String property="";
                AnswerObject answerObject=new AnswerObject();
                //add 11.26
                if (queryObject.getTamplate() != null) {
                	answerObject.setTamplateContent(queryObject.getTamplate().getContent());
				}
                
                if(filterStr!=null&&!filterStr.equals("")){
                	answerObject.setFilterStr(filterStr);
                }
                score=queryObject.getScore();
                String subjectlabel="";
                if(queryObject.getSubjectName()!=null){
                	answerObject.setSubject(queryObject.getSubjectName());
                }
                
                if(query.contains("subjectlabel")){ 
                	hasSubLabel=true;
                	subjectlabel=rs.get("subjectlabel").toString();
                	answerObject.setSubject(subjectlabel);     	
                }
                if(!query.contains("subjectlabel")&&queryObject.getLimitName()!=null&&!queryObject.getLimitName().equals("")){
	        		answerObject.setSubject(queryObject.getLimitName()+"的"+queryObject.getSubjectName());
	    		}
	        	else if(!query.contains("subjectlabel")&&queryObject.getLimitName()!=null&&queryObject.getLimitName().equals("")){
	        		answerObject.setSubject(queryObject.getSubjectName());
	        	}
                
                RDFNode subUriNode=rs.get("subject");
                if(subUriNode!=null) answerObject.setSubjectUri(subUriNode.toString());
                
                String object=null;

                if(usageList.contains("first")||usageList.contains("last")){
                	
                	String subjectLabel=rs.get(results.getResultVars().get(1).toString()).toString();
                	String bindingName = results.getResultVars().get(2).toString();
                	subjectList.addAll(DecideAnswer.processTime(bindingName, subjectLabel, rs, results));
                }
                if(course.equals("english")&&results.getResultVars().size()>2&&usageList.contains("translation")){
                	String bindingName = results.getResultVars().get(2).toString();
            		object=rs.get(bindingName).toString();
            		if(object.startsWith("\"")&&object.endsWith("\""))
                		object=object.replaceAll("\"", "");
            		property=StringProcess.toLabel(courseMap, commonMap, bindingName);
            		answerObject.setPredicate(property);
            		answerObject.setValue(object);
            		String common=MaxString.getMaxString(question, object);
            		
            		answerObject.setScore(score+common.length());
            		
            		answerObject=DecideAnswer.decideEnlishAnswer(filterStr, results, answerObject);
            		if(answerObject==null) continue;
            		if(answerObject!=null) jsonArray.add(answerObject);
            	}
                else if(subjectList.isEmpty()){
                	
                	if(hasSubLabel)
                		t=2;
                	
                	for (int i = t; i < numHeader; i++) {

		                String bindingName = results.getResultVars().get(i).toString();
		                
		                if(rs.get(bindingName)!=null){
		                	
		                	object=rs.get(bindingName).toString();
		                	if(object.startsWith("\"")&&object.endsWith("\"")||(object.endsWith("\"")&&StringProcess.CountNumber(object, "\"")==1))
		                		object=object.replaceAll("\"", "");
		                	
		                	if(object==null||object.equals("")) continue;
		                	if(hasSubLabel&&!course.equals("english")){
		                		String commonStr=MaxString.longestCommonSubsequence(question.replaceAll("的", ""), object);
		                		if(object.length()==1) commonStr=object;
		                		if(commonStr.length()>2)score+=Math.sqrt((double)commonStr.length()/object.length());
		                	}
		                	answerObject.setScore(score);
		                	property=bindingName;
		                	property=StringProcess.toLabel(courseMap, commonMap, property);
		                	
		                	answerObject.setPredicate(property);
		                	
		                    if(course.equals("chinese")&&property.equals("内容")&&object.length()>1000&&(!usageList.contains("nextContent")&&!usageList.contains("lastContent")&&StringProcess.CountNumber(query, "FILTER")<1))
		                    	continue;
//		                    object=subjectlabel+"--"+property+":<br>"+object;
		                    if((subjectlabel.contains("戏文·")||subjectlabel.contains("杂剧·"))&&property.equals("内容"))continue;
		                    boolean isURI=false;
		                	if(VirtuosoSearch.isUrl(object)){
		                		if(object.contains("instance")||object.contains("class")){
		                			answerObject.setValue(object);
		                			arrHeader.add(answerObject);
		                			isURI=true;
//		                			answerObject=null;
		                		}
		                		else if(object.contains("getjpg")||object.contains("getpng")){
			                		answerObject.setValue(object);
			                	}
		                	}
		                	else if(object!=null&&!object.equals("")) answerObject.setValue(object);
		                	
		                	if(filterStr!=null&&!filterStr.equals("")&&filterStr.equals(object)){
		                		MatchJson.add(answerObject);
		                	}
		                	else if(filterStr!=null&&!filterStr.equals("")&&!filterStr.equals(object)&&!isURI){
		                		filterJson.add(answerObject);
		                	}
		                	else if(answerObject!=null&&!isURI) 
		                		jsonArray.add(answerObject);
		                }
                	}
                }
	        }
            
            if(!subjectList.isEmpty()){
            	String usage="";
            	if(usageList.contains("first")) 
            		usage="first";
            	else if(usageList.contains("last")) 
            		usage="last";	            	
            	
            	AnswerObject ansObj=subjects.getSort(subjectList,usage);
            	jsonArray.add(ansObj);
            }
            if(!arrHeader.isEmpty()){
		        JSONArray LabelArray=VirtuosoSearch.searchLabels(arrHeader);
		        jsonArray.addAll(LabelArray);
	        }
            if(!MatchJson.isEmpty()) jsonArray.addAll(MatchJson);
            else if(!filterJson.isEmpty()) jsonArray.addAll(filterJson);
	        vqe.close();
	        set.close();
		} catch (ClassNotFoundException | IOException | SQLException | RepositoryException | QueryEvaluationException | MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		return jsonArray;
	}
	
	 /**
	  * 判断字符串是不是uri链接
	 * @param str
	 * @return
	 */
	public static boolean isUrl(String str){
	    	
	    	boolean isurl=false;
	    	
	    	String regex = "http://(.*)?";
	    	
	    	 Pattern pattern = Pattern.compile(regex);
	         Matcher Url = pattern.matcher(str);
	         if(Url.find()){
	        	 isurl=true;
	         }
	    	return isurl;
    }
	
//	 public static List<String> searchLabels(List<String> sList) throws RepositoryException, QueryEvaluationException, MalformedQueryException{
//	    	
//			List<String> labels=new ArrayList<String>();
//	    	String label=null;
//	    	String temp="";
//	    	
//	    	VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");
//	    	
//	    	int count=sList.size();
//	    	int cyele=0;
//	    	int acount=0;
//	    	List<String> tempList=new ArrayList<String>();
//	    	String query = "SELECT distinct * "+fromString+" WHERE { ";
//	    	int t=0;
//	    	for(int i=0;i<count;i++){
//	    		
//	    		String subject=sList.get(i);
//	    		subject=subject.replaceAll("<br>", "").replaceAll("&nbsp;", "");
//	    		if(!subject.startsWith("http")){
//	    			if(subject.contains("--")&&subject.contains(":"))
//	    				temp=subject.substring(0, subject.indexOf(":")+1);
//	    			subject=subject.substring(subject.indexOf("http"));	  
//	    			tempList.add(temp);
//	    		}
//	    	
//		    	query+="\t"+"<"+subject+"> <http://www.w3.org/2000/01/rdf-schema#label> ?label"+i+".\n";
////		    	+"\t"+"OPTIONAL {<"+subject+"> <http://purl.org/dc/elements/1.1/description> ?des"+i+".}\n";
//		    	acount++;
//		    	if(acount==10){
//		    		acount=0;
//		    		query+="} ";
//			    	
//		        	VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (query, set);
//		    		ResultSet result = vqe.execSelect();
//		            
//		            while (result.hasNext()) {
//		            	QuerySolution rs = result.nextSolution();
//		                for(int j=cyele*10;j<10*(cyele+1);j++){
//
//		                	label=rs.get("label"+j).toString();
//		                	if(tempList.size()>t){
//		                		label=tempList.get(t)+label;
//		                	}
//		    		        labels=StringProcess.array_unique(labels, label);
//		    		        t++;
//		                }
//		            }
//		            query = "SELECT * "+fromString+" WHERE  { ";
//		            cyele++;
//		    	}
//	    	}
//	    	
//	    	query+="} ";
//
//	    	VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (query, set);
//			ResultSet result = vqe.execSelect();
//		    	
//	        while (result.hasNext()) {
//	        	if(result.getRowNumber()>3) break;
//	        	QuerySolution rs = result.nextSolution();
//	            for(int i=cyele*10;i<count;i++){
//	            	label="";
//	            	label=rs.get("label"+i).toString();
//	            	if(tempList.size()>t){
//                		label=tempList.get(t)+label;
//                	}
//	            	labels=StringProcess.array_unique(labels, label);
//    		        t++;
//	            }
//	        }
//	        vqe.close();
//	        set.close();
//
//	    	return labels;
//    }
    
	 /**
	  * 查询uri的label
	 * @param ansList
	 * @return
	 * @throws RepositoryException
	 * @throws QueryEvaluationException
	 * @throws MalformedQueryException
	 */
	public static JSONArray searchLabels(JSONArray ansList) throws RepositoryException, QueryEvaluationException, MalformedQueryException{
	    	
		 JSONArray resultList=new JSONArray();
	    	String label=null;

	    	ObjectMapper mapper=new ObjectMapper();
	    	VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");
	    	
	    	int count=ansList.size();
	    	int cyele=0;
	    	int acount=0;

	    	String query = "SELECT distinct * "+fromString+" WHERE { ";

	    	for(int i=0;i<count;i++){
	    		
	    		AnswerObject ansObject=new AnswerObject();
	    		JSONObject jsonObject=(JSONObject) ansList.get(i);
	    		
				try {
					ansObject=mapper.readValue(jsonObject.toString(), AnswerObject.class);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		
	    		String subject=ansObject.getValue();
	    		subject=subject.replaceAll("\\t", "").replaceAll(" ", "").replaceAll("\\n", "");
	    	
		    	query+="\t"+"OPTIONAL {<"+subject+"> <http://www.w3.org/2000/01/rdf-schema#label> ?label"+i+".}\n";
//		    	+"\t"+"OPTIONAL {<"+subject+"> <http://purl.org/dc/elements/1.1/description> ?des"+i+".}\n";
		    	acount++;
		    	if(acount==10){
		    		acount=0;
		    		query+="} ";
			    	
		        	VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (query, set);
		    		ResultSet result = vqe.execSelect();
		            
		            while (result.hasNext()) {
		            	QuerySolution rs = result.nextSolution();
		                for(int j=cyele*10;j<10*(cyele+1);j++){
		                	if(rs.get("label"+j)!=null)
		                		label=rs.get("label"+j).toString();

		    		        ansObject.setValue(label);
		    		        resultList.add(ansObject);
		                }
		            }
		            query = "SELECT * "+fromString+" WHERE  { ";
		            cyele++;
		    	}
	    	}
	    	
	    	query+="} ";

	    	VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (query, set);
			ResultSet result = vqe.execSelect();
		    	
	        while (result.hasNext()) {
	        	if(result.getRowNumber()>3) break;
	        	
	        	QuerySolution rs = result.nextSolution();
	            for(int i=cyele*10;i<count;i++){
	            	
	            	AnswerObject ansObject=new AnswerObject();
		    		JSONObject jsonObject=(JSONObject) ansList.get(i);
		    		
					try {
						ansObject=mapper.readValue(jsonObject.toString(), AnswerObject.class);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		    		
	            	label="";
	            	if(rs.get("label"+i)!=null)
	            		label=rs.get("label"+i).toString();

	            	ansObject.setValue(label);
	            	resultList.add(ansObject);
	            }
	        }
	        vqe.close();
	        set.close();

	    	return resultList;
	 }
	 
	/**
	 * 查询子图
	 * @param subjectName
	 * @param filterStr
	 * @param isSelect
	 * @param course
	 * @return
	 */
	public JSONArray searchGraph(String subjectName,String filterStr,boolean isSelect,String course){
		
		JSONArray resultArray=new JSONArray();
		JSONArray valueUriArray=new JSONArray();
		
		VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");
		
		ServletContext sct= ServletActionContext.getServletContext();
		try {
			commonMap=ReadProperty.selectProperty("Common");
			courseMap=ReadProperty.selectProperty(course);
	    	
	        String queryString ="";
	        String queryValue="";

	        if(!isSelect){
	        	queryString= "CONSTRUCT { ?s ?p ?o } "+fromString+" WHERE { ?s ?p ?o .?s <http://www.w3.org/2000/01/rdf-schema#label> '" +subjectName+ "'.}";
	        queryValue="CONSTRUCT { ?s ?p ?o} \n "+fromString+" WHERE { \n"
	    			+ "?s ?p ?o .\n"
	    			+ "?o <http://www.w3.org/2000/01/rdf-schema#label> '"+subjectName+"'.\n"
					+ "}";
	        }
	        else {
	        	
	        	queryString="CONSTRUCT { ?s ?p ?o} \n "+fromString+" WHERE { \n"
	        			+ "?s ?p ?o .\n"
	        			+ "?s <http://www.w3.org/2000/01/rdf-schema#label> ?subjectlabel.\n"
	        			+ "?subjectlabel bif:contains '\""+filterStr+"\"'.\n"
	        					+ "}";
	        	queryValue="CONSTRUCT { ?s ?p ?o} \n "+fromString+" WHERE { \n"
	        			+ "?s ?p ?o .\n"
	        			+ "?o <http://www.w3.org/2000/01/rdf-schema#label> '"+filterStr+"'.\n"
	        					+ "}";
	        }
	        
	        System.out.println(queryString);
	        
	        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (queryString, set);

			Model model = vqe.execConstruct();
	        Graph g = model.getGraph();
	        
	        vqe = VirtuosoQueryExecutionFactory.create (queryValue, set);

			Model model2 = vqe.execConstruct();
	        Graph g2 = model2.getGraph();

	        String tempSubject="";
	        String uri="";
	        String tempuri=null;
	        
	        for (Iterator<?> it = g.find(Node.ANY, Node.ANY, Node.ANY); it.hasNext();){
	        	AnswerObject answerObject=new AnswerObject();
	        	Triple t = (Triple)it.next();
	        	uri=t.getSubject().toString();
	        	if(!uri.equals(tempuri)){

					tempSubject=getLabel(uri);
	            	tempuri=uri;
	        	}
	        	String predicate=t.getPredicate().toString();
	        	if(predicate.contains("#label")||predicate.contains("description")||predicate.contains("categoryId")||predicate.contains("source")||predicate.contains("type")) continue;
	        	String predicate_prefix=predicate.split("#")[0];
	        	if(predicate.contains("biology#-")&&course.equals("biology")) predicate="biology"+predicate.split("#")[1].replaceAll("\\-", "");
	        	else if(predicate.contains("-")&&course.equals("biology")) predicate=predicate.split("#")[1].replaceAll("\\-", "");
	        	else if(predicate.contains("#")&&!predicate.endsWith("#"))predicate=predicate.split("#")[1];
	        	else if (predicate.endsWith("#")&&course.equals("biology")) predicate="biology";
	        	predicate=predicate.replaceAll("\\-\\d+", "");
	        	
	        	predicate=StringProcess.toLabel(courseMap,commonMap,predicate);
	        	String object=t.getObject().toString();
	        	answerObject.setSubject(tempSubject);
	        	answerObject.setPredicate(predicate);
	        	answerObject.setValue(object);
	        	
	        	if(isUrl(object)){
	        		
	        		if(!predicate.contains("shitixianzhi")&&object.contains("instance")){
	            	
	        			valueUriArray.add(answerObject);
	        			continue;
	        		}
	        		else if(!object.contains("getpng")&&!object.contains("getjpg")&&!predicate.contains("image")) 
	        			continue;
	        	}

	        	if(predicate!=null&&!object.equals("")&&object!=null){
	        		if(object.startsWith("\"")&&object.endsWith("\""))
	            		object=object.replaceAll("\"", "");
	        		answerObject.setValue(object);
	            	resultArray.add(answerObject);
	        	}
	        }
	        
	        if(!valueUriArray.isEmpty()){
	        	valueUriArray=searchLabels(valueUriArray);
	        	resultArray.addAll(valueUriArray);
	        }
	        valueUriArray.clear();
	        for (Iterator<?> it = g2.find(Node.ANY, Node.ANY, Node.ANY); it.hasNext();){
	        	AnswerObject answerObject=new AnswerObject();
	        	Triple t = (Triple)it.next();
	        	uri=t.getSubject().toString();
	        	if(!uri.equals(tempuri)){

					tempSubject=getLabel(uri);
	            	tempuri=uri;
	        	}
	        	String predicate=t.getPredicate().toString();
	        	if(predicate.contains("#label")||predicate.contains("description")||predicate.contains("categoryId")||predicate.contains("source")||predicate.contains("type")) continue;
	        	String predicate_prefix=predicate.split("#")[0];
	        	if(predicate.contains("biology#-")&&course.equals("biology")) predicate="biology"+predicate.split("#")[1].replaceAll("\\-", "");
	        	else if(predicate.contains("-")&&course.equals("biology")) predicate=predicate.split("#")[1].replaceAll("\\-", "");
	        	else if(predicate.contains("#")&&!predicate.endsWith("#"))predicate=predicate.split("#")[1];
	        	else if (predicate.endsWith("#")&&course.equals("biology")) predicate="biology";
	        	
	        	predicate=StringProcess.toLabel(courseMap,commonMap,predicate);
	        	String object=t.getObject().toString();
	        	answerObject.setSubject(tempSubject);
	        	answerObject.setPredicate(predicate);
	        	answerObject.setValue(object);
	        	
	        	if(isUrl(object)){
	        		
	        		if(!predicate.contains("shitixianzhi")&&object.contains("instance")){	            	
	        			valueUriArray.add(answerObject);
	        			continue;
	        		}
	        		else if(!object.contains("getpng")&&!object.contains("getjpg")&&!predicate.contains("image")) 
	        			continue;
	        	}
	        	if(predicate.contains("#label")||predicate.contains("description")||predicate.contains("categoryId")||predicate.contains("source")||predicate.contains("type")) continue;

	        	if(predicate!=null&&!object.equals("")&&object!=null){
	        		if(object.startsWith("\"")&&object.endsWith("\""))
	            		object=object.replaceAll("\"", "");
	        		answerObject.setValue(object);
	            	resultArray.add(answerObject);
	        	}
	        }
	        
	        if(!valueUriArray.isEmpty()){
	        	valueUriArray=searchLabels(valueUriArray);
	        	resultArray.addAll(valueUriArray);
	        }
	        vqe.close();
	        set.close();
		} catch (ClassNotFoundException | IOException | SQLException | RepositoryException | QueryEvaluationException | MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
        return resultArray;
	}
	
    public String getLabel(String subject){
    	
    	String label=null;
    	VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");
    	subject=subject.trim();

//    	String queryString = "SELECT ?label WHERE { <"+subject+"> <http://www.w3.org/2000/01/rdf-schema#label> ?label .} ";
    	String queryString = "SELECT distinct ?label ?limitlabel "+fromString+" WHERE { <"+subject+"> <http://www.w3.org/2000/01/rdf-schema#label> ?label ."
//    			+ "OPTIONAL {<"+subject+"> <http://purl.org/dc/elements/1.1/description> ?des.}"
    			+"OPTIONAL {?limit <http://edukb.org/knowledge/0.1/property/geo#shitixianzhi> <"+subject+">."
    			+ "?limit <http://www.w3.org/2000/01/rdf-schema#label> ?limitlabel.}"
    			+ "} ";
    	
    	VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (queryString, set);
		ResultSet result = vqe.execSelect();
		
        while (result.hasNext()) { 

        	QuerySolution rs = result.nextSolution();
        	
            label=rs.get("label").toString();
            
	        RDFNode valueOfL = rs.get("limitlabel");
            if(valueOfL!=null&&!label.contains("中国的")) label=valueOfL.toString()+"的"+label;
            
        }
        vqe.close();
    	set.close();
    	return label;
    }
    
    public List<String> search(String query){
    	
    	List<String> labelList=new ArrayList<String>();
    	String label=null;
    	VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");
    	
    	VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (query, set);
		ResultSet result = vqe.execSelect();
		
        while (result.hasNext()) { 

        	QuerySolution rs = result.nextSolution();
        	
            label=rs.get("subjectlabel").toString();
            
            labelList.add(label);
           
        }
        vqe.close();
    	set.close();
    	return labelList;
    }
    
    public List<String> searchLabels(Map<String,String> map) throws RepositoryException, QueryEvaluationException, MalformedQueryException{
    	
		List<String> labels=new ArrayList<String>();
    	String label=null;
    	List<String> valueList=new ArrayList<String>();
    	
    	VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");
    	
    	String queryString = "SELECT distinct * "+fromString+" WHERE  { ";
    	
    	int i=0;
    	int count=0;
    	int cyele=0;
    	valueList.addAll(map.values());
    	for(Entry<String,String> entry:map.entrySet()){
    		
    		String subject=entry.getKey();    	
	    	queryString+="<"+subject.trim()+"> <http://www.w3.org/2000/01/rdf-schema#label> ?label"+i+".\n";
	    	i++;
	    	count++;
	    	if(count==52){
	    		count=0;
	    		queryString+="} ";
		    	
	        	VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (queryString, set);
	    		ResultSet result = vqe.execSelect();
	            
	            while (result.hasNext()) {
	            	QuerySolution rs = result.nextSolution();
	                for(int j=cyele*52;j<52*(cyele+1);j++){

	    	            label=rs.get("label"+j).toString();
	    	            String title=valueList.get(j).toString();
	    		        labels.add(title+label);
	                }
	            }
	            queryString = "SELECT * "+fromString+" WHERE  { ";
	            cyele++;
	    	}
    	}
    	
    	queryString+="} ";
	    	
    	VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (queryString, set);
		ResultSet result = vqe.execSelect();
        
        while (result.hasNext()) {
        	QuerySolution rs = result.nextSolution();
            for(int j=cyele*52;j<valueList.size();j++){

	            label=rs.get("label"+j).toString();
	            String title=valueList.get(j).toString();
		        labels.add(title+label);
            }
        }
        vqe.close();
        set.close();
    	return labels;
	}
    
    /**
     * 查询实例的别名
     * @param course
     * @return
     * @throws RepositoryException
     * @throws QueryEvaluationException
     * @throws MalformedQueryException
     */
    public Map<String, String> selectOtherName(String course) throws RepositoryException, QueryEvaluationException, MalformedQueryException{
		
		Map<String, String> labelMap=new HashMap<String, String>();
		String label=null;
		String otherName=null;
		String[] array=null;
		VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");
		String prefix="<http://edukb.org/knowledge/0.1/property/"+course+"#";
	
		String queryString = "SELECT ?label ?othername "+fromString+" WHERE { "
				+ "?subject <http://www.w3.org/2000/01/rdf-schema#label> ?label."
				+ "?subject <http://edukb.org/knowledge/0.1/property/common#altLabel> ?othername."
				+ "} ";
		
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (queryString, set);
		ResultSet result = vqe.execSelect();
		
	    while (result.hasNext()) { 
	
	    	QuerySolution rs = result.nextSolution();
	    	
	        label=rs.get("label").toString();
	        otherName=rs.get("othername").toString();
	
	        otherName=otherName.replace(" ", "").replaceAll("，", "、");
	    	if(otherName.startsWith("又称")) otherName=otherName.replace("又称", "");
	    	if(otherName.endsWith("又称")) otherName=otherName.replace("等", "");
	
			labelMap.put(otherName,label);
	    }
	    List<String> pList=new ArrayList<String>();

	    switch (course) {
		case "chinese":pList.add("nickname");break;
//		case "english":pList.add("");break;
		case "geo":pList.add("jiancheng");break;
		case "history":{
			pList.add("nianhao");
			pList.add("nianhao1");
			pList.add("miaohao");
			pList.add("benming");
			pList.add("bieming");
			pList.add("shihao");
			pList.add("waihao");
			break;
		}
		case "chemistry":pList.add("OtherNames");break;
//		case "physics":pList.add("");break;
//		case "biology":pList.add("");break;
//		case "politics":pList.add("");break;

		}
	    for(String property:pList){
	    	if(pList.equals("")) continue;
	        queryString= "SELECT ?label ?"+property+" "+fromString+" WHERE { "
	    			+ "?subject <http://www.w3.org/2000/01/rdf-schema#label> ?label."
	    			+ "?subject "+prefix+property+"> ?"+property+"."
	    			+ "} ";
	        vqe = VirtuosoQueryExecutionFactory.create (queryString, set);
			result = vqe.execSelect();
			
	        while (result.hasNext()) { 
	
	        	QuerySolution rs = result.nextSolution();
	
	            label=rs.get("label").toString();
	            otherName=rs.get(property).toString();
	            otherName=otherName.replace(" ", "").replaceAll("，", "、");
	        	if(otherName.startsWith("又称")) otherName=otherName.replace("又称", "");
	        	if(otherName.endsWith("又称")) otherName=otherName.replace("等", "");
	
	    		labelMap.put(otherName,label);
	
	        }
	    }
	    vqe.close();
	    set.close();
		return labelMap;
	}
    
//	public JSONObject FullSearch(String course,String question,Map<String,String> courseTerms,List<String> typeList) throws Exception {
//        
//        JSONObject result=new JSONObject();
//        VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");
//        
//        ServletContext sct= ServletActionContext.getServletContext();
//		commonMap=ReadProperty.selectProperty("Common");
//		
//		courseMap=ReadProperty.selectProperty(course);
//        String Changedcourse=CourseChange.change(course, true);
//       
//        List<Nature> natureList=new ArrayList<Nature>();
//        natureList.add(Nature.n);
//        natureList.add(Nature.nis);
//        natureList.add(Nature.nnt);
//        natureList.add(Nature.nt);
//	    natureList.add(Nature.nz);
//	    natureList.add(Nature.ng);
//	    natureList.add(Nature.ns);
//	    natureList.add(Nature.g);
//	    natureList.add(Nature.gb);
//	    natureList.add(Nature.gbc);
//	    natureList.add(Nature.gc);
//	    natureList.add(Nature.gg);
//	    natureList.add(Nature.gi);
//	    natureList.add(Nature.gm);
//	    natureList.add(Nature.gp);
//	    natureList.add(Nature.nm);
//	    natureList.add(Nature.nmc);
//	    natureList.add(Nature.nb);
//	    natureList.add(Nature.nba);
//	    natureList.add(Nature.nbc);
//	    natureList.add(Nature.nbp);
//	    natureList.add(Nature.nhm);
//	    natureList.add(Nature.nhd);
//	    natureList.add(Nature.nx);
//	    
//	    List<String> removeList=new ArrayList<String>();
//	    removeList.add("事件");
//	    removeList.add("元素符号");
//	    removeList.add("城市");
//	    removeList.add("动物");
//	    removeList.add("年代");
//	    removeList.add("第二次世界大战");
//	    removeList.add("第二次");
//	    removeList.add("基础上");
//	    removeList.add("时期");
//	    removeList.add("人们");
//	    removeList.add("宋朝");
//	    removeList.add("政府");
//	    removeList.add("组织");
//	    removeList.add("人");
//	    removeList.add("人体");
//	    removeList.add("月");
//	    removeList.add("初中化学");
//	    removeList.add("观点");
//	    removeList.add("前后左右");
//	    removeList.add("条约");
//	    removeList.add("日常生活");
//	    removeList.add("实验室");
//	    removeList.add("科学家");
//	    removeList.add("宏观");
//	    if(course.equals("geo"))
//	    	fromString="from <http://edukb.org/geo>";
//	    
//	    String queryString = "construct { ?subject ?predicate ?value } "+fromString+" where { ?subject ?predicate ?value.\n";
//	    if(question.lastIndexOf("等")<question.length()-10&&!question.contains("等级")&&!question.contains("什么等")){
//	    	question=question.substring(question.indexOf("等")+1);
//	    }
//	    if(course.equals("physics")&&question.contains("因而"))question=question.substring(question.indexOf("因而")+1);
//	    
//	    List<Term> cutWords=WordParser.CutWord(question);
//	    List<String> filterList=new ArrayList<String>();
//	    List<String> nList=new ArrayList<String>();
//	    List<String> nsList=new ArrayList<String>();
//	    List<String> leftList=new ArrayList<String>();
//	    List<String> tList=new ArrayList<String>();
//	    List<String> addList=new ArrayList<String>();
//
//	    List<String> termList=new ArrayList<String>();
//	    termList.addAll(courseTerms.keySet());
//	    List<String> subjectList=DecideSubject.DecideByMap(termList, question, course);
//	    
//	    for(int i=0;i<cutWords.size();i++){
//	    	Term word=cutWords.get(i);
//	    	if(word.word.equals(Changedcourse)) continue;
//	    	if(word.nature==Nature.ry||word.nature==Nature.rys||word.nature==Nature.ryv||word.nature==Nature.ryt)
//	    		rycount++;
//	    	else if(word.nature==Nature.nr||word.nature==Nature.nrf||word.nature==Nature.nrj||word.nature==Nature.j)
//	    		addList=StringProcess.array_unique(addList,word.word);
//	    	
//	    	if(natureList.contains(word.nature)&&!removeList.contains(word.word)&&!word.word.contains("哪")){
//	    		if(word.word.endsWith("国家")&&!word.word.equals("国家")) word.word=word.word.replace("国家", "");
//	    		if(word.word.contains(",")||word.word.contains("，")){
//	    			String[] array=word.word.split("，");
//	    			String[] array1=word.word.split(",");
//	    			if(array.length==1) array=array1;
//	    			for(int j=0;j<array.length;j++){
//	    				array[j]=array[j].replace("。", "").replace("？", "");
//	    				filterList=StringProcess.array_unique(filterList, array[j]);
//	    			}
//	    		}
//	    		else 
//	    			filterList=StringProcess.array_unique(filterList, word.word.replace("？", "").replace("。", ""));
//	    		
//	    	}
//	    	else if(word.nature==Nature.t||word.nature==Nature.tg){
//	    		tList=StringProcess.array_unique(tList, word.word);
//	    	}
//	    	else if(word.length()==1&&word.nature==Nature.b){
//	    		filterList=StringProcess.array_unique(filterList, word.word);
//	    	}
//	    	if((word.word.equals("后")||word.word.equals("以后")||word.word.equals("中"))&&i>0){
//	    		if(cutWords.get(i-1).nature==Nature.n||cutWords.get(i-1).nature==Nature.nz){
//	    			nList.add(cutWords.get(i-1).word);
//	    		}
//	    	}
//	    	
//	    	if(word.nature==Nature.n||word.nature==Nature.nnt||word.nature==Nature.nis||word.nature==Nature.nrf||word.nature==Nature.ns){
//	    		nList=StringProcess.array_unique(nList, word.word);
//	    		if(word.nature==Nature.n&&word.word.length()>2)
//	    			leftList=StringProcess.array_unique(leftList, word.word);
//	    	}
//	    	else if(word.nature==Nature.ns)
//	    		nsList=StringProcess.array_unique(nsList, word.word); 
//	    	else if(word.nature==Nature.nz&&(word.word.endsWith("者")||word.word.endsWith("内")||word.word.endsWith("上")||word.word.length()<4)){
//	    		nList=StringProcess.array_unique(nList, word.word);
//	    	}
//	    }
//	    if(filterList.size()==1&&filterList.get(0).length()<3){
//	    	filterList.addAll(addList);
//	    }
//	    
//	    subjectList.removeAll(removeList);
//	    subjectList=RemoveDuplicate.removeContians2(subjectList, question, courseTerms, Changedcourse, "");
//	    filterList.addAll(subjectList);
//	    filterList=RemoveDuplicate.remove(filterList);
//	    
//	    if(filterList.size()>1){
//		    for(String subjectStr:subjectList){
//		    	if(subjectStr.length()>4){
//		    		List<Term> words=WordParser.CutWord(subjectStr);
//		    		if(words.size()>1&&filterList.contains(subjectStr))
//		    			filterList.remove(subjectStr);
//		    	}
//		    }
//	    }
//	    if(filterList.size()==1&&!tList.isEmpty()){
//	    	filterList.addAll(tList);
//	    }
//	    queryString+="?subject <http://www.w3.org/2000/01/rdf-schema#label> ?subjectlabel.\n";
//	    if(filterList.size()==1&&filterList.get(0).length()==1) filterList.clear();
//	    for(int i=0;i<filterList.size();i++){
//	    	String filterStr=filterList.get(i);
//	    	if(!filterStr.equals(""))
//	    		queryString+="?subjectlabel bif:contains \"'"+filterList.get(i)+"'\".\n";
//	    } 
//	    queryString+="}";
//
//	    Graph graph=null;
//	    VirtuosoQueryExecution vqe=null;
//	    if(filterList.size()>1){
//	    	System.out.println("====================主语的全文检索====================");
//	    	System.out.println(queryString);	    	
//	    	long start = System.currentTimeMillis();
//	    	
//	    	vqe = VirtuosoQueryExecutionFactory.create (queryString, set);	    	
//	    	Model model = vqe.execConstruct();
// 	        graph = model.getGraph();
//	    	
//	    	long end = System.currentTimeMillis();	        
//	        System.out.println("主语全文检索所用时间为："+Time.computeTime(start, end));
//	    }
//        
//	    int j=0;
//	    String maxStr="";
//		while(isContinue&&maxStr.equals("")&&!filterList.isEmpty()){
//        	maxStr=ValueFilterSearch(graph, course, question, typeList, filterList, nList, nsList, leftList,subjectList);
//        	graph=null;
//        }
//		
//		if(maxStr!=null&&!maxStr.equals("")){
//			resultObject.put(j, maxStr);
//		}
//		vqe.close();
//		set.close();
//        return resultObject;
//	}
    
//    /**
//     * 模糊查询
//     * @param result
//     * @param course
//     * @param question
//     * @param typeList
//     * @param filterList
//     * @param nList
//     * @param nsList
//     * @param leftList
//     * @return
//     * @throws Exception
//     */
//    public String ValueFilterSearch(ResultSet result,String course,String question,List<String> typeList,List<String> filterList,List<String> nList,List<String> nsList,List<String> leftList) throws Exception {
//        
//    	String queryString="";
//    	VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");
//    	VirtuosoQueryExecution vqe =null;
//	    
////    	 if(graph==null||graph.isEmpty()){
////         	if(filterList.size()>0){	
////         		queryString = "construct { ?subject ?predicate ?value } "+fromString+" where { ?subject ?predicate ?value.\n";
////         		for(int i=0;i<filterList.size();i++){
////         			if(filterList.get(i).length()>4)
////         				queryString+="FILTER CONTAINS(str(?subjectlabel),'"+filterList.get(i)+"')\n";
////         			else if(!filterList.get(i).equals(""))
////         	    		queryString+="FILTER CONTAINS(str(?value),'"+filterList.get(i)+"')\n";
////         	    }
////         	    queryString+="}";
////         	    System.out.println("=============================主语+主语的全文检索============================");
////         	    System.out.println(queryString);
////         	    VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (queryString, set);
////     	    	Model model = vqe.execConstruct();
////     	        g2 = model.getGraph();
////         	}
////         }
//	    if(result==null||!result.hasNext()){
//        	if(filterList.size()>=2||(filterList.size()==1&&filterList.get(0).length()>3)){	
//        		queryString = "select ?subject ?predicate ?value  "+fromString+" where { \n"
//        				+ "?subject ?predicate ?value.\n";
//        		for(int i=0;i<filterList.size();i++){
//        	    	if(!filterList.get(i).equals(""))
//        	    		queryString+="filter CONTAINS(str(?value),'"+filterList.get(i)+"').\n";
//        	    }
//        	    queryString+="}";
//        	    System.out.println("=============================宾语的全文检索============================");
//        	    System.out.println(queryString);
//        	    
//        	    long start = System.currentTimeMillis(); 		
//        	
////        	    Query sparql= QueryFactory.create(queryString);
//        	    vqe = VirtuosoQueryExecutionFactory.create (queryString, set);
//
//        	    result=vqe.execSelect();
//    	        
//        	    isContinue=false;
//        	    
//        	    long end = System.currentTimeMillis();
//		        
//		        System.out.println("宾语全文检索所用时间为："+Time.computeTime(start, end));
//        	}
//        	else isContinue=false;
//        }
//	    QuerySolution rs=null;
//
//      //当多个检索关键字未查询到结果时，将词性为名词的词去除后再次进行查询
//	    if(result==null||!result.hasNext()){
//        	if(!nList.isEmpty()&&filterList.size()>1){
//        		filterList.removeAll(nList);
//        		if(filterList.size()>nsList.size()&&!nsList.isEmpty())
//        			filterList.removeAll(nsList);
//        		if(!leftList.isEmpty()) filterList.addAll(leftList);
//        		filterList=RemoveDuplicate.remove(filterList);
//        		queryString = "select ?subject ?predicate ?value  "+fromString+" where { \n"
//        				+ "?subject ?predicate ?value.\n";
//        		if(filterList.size()==1&&filterList.get(0).length()==1) filterList.clear();
//        		if(!filterList.isEmpty()){
//	        		for(int i=0;i<filterList.size();i++){
//	        	    	if(!filterList.get(i).equals(""))
//	        	    		queryString+="FILTER CONTAINS(str(?value),'"+filterList.get(i)+"').\n";
//	        	    }
//	        		queryString+="}";
//	        	    System.out.println("=============================减少查询关键词==========================");
//	        	    System.out.println(queryString);
//	        	    vqe = VirtuosoQueryExecutionFactory.create (queryString, set);
//	        	    result=vqe.execSelect();
//        		}
//        	}
//        }
//        String maxStr="";
//        String tempStr="";
//        String maxCommonStr="";
//        double maxScore=0;
//        
//        if(result!=null){
//        	boolean isAnswer=false;
//        	while (result.hasNext()){
//        	
//    			rs = result.nextSolution();
//        		
//	            String subject=rs.get("subject").toString();
//	            String predicate=rs.get("predicate").toString();
//	            String value=rs.get("value").toString();
//	            if(predicate.contains("Topic_analysis")||predicate.contains("Topic_content")||predicate.contains("label")||predicate.contains("description")||predicate.contains("category")||predicate.contains("common#source")||predicate.contains("type")||predicate.contains("categoryId")||predicate.contains("annotation")||predicate.contains("image")) continue;
//	            
//	            if(value.startsWith("\"")&&value.endsWith("\""))
//	            	value=value.replaceAll("\"", "");
//	            subject=getLabel(subject);
//	            if(SesameSearch.isUrl(value)&&value.startsWith("http://")) value=getLabel(value);
//	            boolean isPredicate=false;
//	            predicate=predicate.split("#")[1];
//	            if(typeList.contains(predicate)) isPredicate=true;
//	            if(courseMap.get(predicate)!=null)
//	        		predicate=(String) courseMap.get(predicate);
//	        	if(commonMap.get(predicate)!=null)
//	        		predicate=(String) commonMap.get(predicate);
//	            tempStr=subject+"--"+predicate+":<br>"+value;
//	//	            tempStr=tempStr.replaceAll(" ", "");
//	            
//	            String commonStr=MaxString.getMaxString(question, tempStr);
//	            if(commonStr.length()<4) continue;
//	            if(commonStr.length()>maxCommonStr.length()){
//	            	maxCommonStr=commonStr;
//	            	maxStr=tempStr;
//	            	subjectName=subject;
//	            }
//	            else if(commonStr.length()==maxCommonStr.length()){
//	            	if(isPredicate){
//	            		maxCommonStr=commonStr;
//		            	maxStr=tempStr;
//		            	subjectName=subject;
//		            	isAnswer=true;
//	            	}
//	            	else if(!isAnswer&&maxCommonStr.length()>3){
//	            		double score1=0;
//	            		double score2=0;
//	            		if(maxScore==0)
//	            			score1=semanticSimilarity.getSimilarity(question, maxStr);
//	            		score2=semanticSimilarity.getSimilarity(question, tempStr);
//	            		if((score2>score1&&score1>0)||(score2>maxScore&&maxScore>0)){
//	            		maxCommonStr=commonStr;
//		            	maxStr=tempStr;
//		            	subjectName=subject;
//		            	maxScore=score2;
//	            		}
//	            		else if(score1>0) maxScore=score1;
//	            	}
//	            }
//	//	            j++;
//	        }
//        }
//        
//        if(maxStr!=null&&!maxStr.equals("")&&((maxCommonStr.length()==4&&!maxCommonStr.endsWith("的")&&!maxCommonStr.startsWith("的"))||maxCommonStr.length()>4)){
//        	System.out.println("最长公共子串："+maxCommonStr);
//        	String[] array=maxStr.split("。");
//        	String[] arrays=maxStr.split("\n");
//        	if(arrays.length>2&&array.length<=2&&!course.equals("politics")){
//        		for(int i=0;i<arrays.length;i++){
//	        		if((arrays[i].contains(maxCommonStr)&&!arrays[i].contains(":")&&!isContinue)||(arrays[i].contains(maxCommonStr)&&isContinue&&arrays[i].contains(":"))){
//	        			maxStr=MaxString.subString("\n", arrays[i], maxCommonStr);
////	        			maxStr=MaxString.subString(" ", maxStr, maxCommonStr);
//	        			if(i-1>-1&&arrays[i-1].replaceAll(" ", "").length()==2)
//	        				maxStr=arrays[i-1]+maxStr;
//	        			maxStr+="（非直接查询出的结果，经后续处理得到的答案）";
//	        		}
//	        	}
//        	}
//        	if((course.equals("politics")||course.equals("physics"))&&maxStr.length()<600||(rycount>1&&(question.contains("，")||question.contains(",")))){
//        		
//        	}
//        	else if(array.length>=2&&course.equals("biology")){
//	        	for(int i=0;i<array.length;i++){
//	        		if(array[i].contains(maxCommonStr)){
//	        			int index=array[i].lastIndexOf("\n", array[i].indexOf(maxCommonStr));
//	        			int index1=array[i].indexOf("\n", array[i].indexOf(maxCommonStr));		 
//	        			
//	        			if(index>0){
//	        				maxStr=array[i].substring(index+1);
//	        				continue;
//	        			}
//	        			else if(index1>0){
//	        				maxStr=array[i].substring(0,index1);
//	        				continue;
//	        			}
//	        			String[] array1=array[i].split("；");
//	        			for(int t=0;t<array1.length;t++){
//	        				if(array1[t].contains(maxCommonStr))
//	        					maxStr=array1[t];
//	        			}
//	        		}
//	        	}
//	        	maxStr+="（非直接查询出的结果，经后续处理得到的答案）";
//        	}
//        	else if(array.length>2){
//        		maxCommonStr=maxCommonStr.replace("。", "");
//        		for(int i=0;i<array.length;i++){
//	        		if(array[i].contains(maxCommonStr)){
//	        			maxStr=MaxString.subString("\n", array[i], maxCommonStr);
////	        			maxStr=MaxString.subString(" ", maxStr, maxCommonStr);
//	        			maxStr+="（非直接查询出的结果，经后续处理得到的答案）";
//	        		}
//	        	}
//        	}
//        	else if(maxStr.contains("\n")){
//        		arrays=maxStr.split("\n");
//        		for(int i=0;i<arrays.length;i++){
//	        		if(arrays[i].contains(maxCommonStr)){
//	        			maxStr=arrays[i]+"（非直接查询出的结果，经后续处理得到的答案）";
//	        		}
//	        	}
//        	}
//        	maxStr=maxStr.replaceAll("（\\d）", "").replaceAll("\\d）", "");
//        }
//        else maxStr="";
//        vqe.close();
//        set.close();
//        return maxStr;
//	}
//    
//    public String ValueFilterSearch(Graph graph,String course,String question,List<String> typeList,List<String> filterList,List<String> nList,List<String> nsList,List<String> leftList,List<String> subjectList){
//        
//    	String queryString="";
//    	String query="";
//    	VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");
//    	VirtuosoQueryExecution vqe=null;
//	    
//    	Graph graph2=null;
//    	 if(graph==null||graph.isEmpty()){
//
//         	if(filterList.size()>0){
//	         	if(filterList.size()>1){	
//	         		query = "construct { ?subject ?predicate ?value } "+fromString+" where { ?subject ?predicate ?value.\n";
//	         		queryString="construct { ?subject ?predicate ?value } "+fromString+" where { ?subject ?predicate ?value.\n";
//	         		boolean flag=true;
//	         		for(int i=0;i<filterList.size();i++){
//	         			if(subjectList.contains(filterList.get(i))&&filterList.get(i).length()>2&&flag){
//	         				queryString+="FILTER CONTAINS(str(?value),'"+filterList.get(i)+"').\n";
//	         				
//	         				query+="?subject <http://www.w3.org/2000/01/rdf-schema#label> ?subjectlabel.\n"
//	         						+ "?subjectlabel bif:contains \"'"+filterList.get(i)+"'\".\n";		
//	         				flag=false;
//	         			}
//	         			else if(!filterList.get(i).equals("")){
//	         	    		query+="?value bif:contains \"'"+filterList.get(i)+"'\".\n";
//	         	    		queryString+="?value bif:contains \"'"+filterList.get(i)+"'\".\n";
//	         			}
//	         	    }
//	         		queryString+="}";
//	         	}
//	         	else if(filterList.size()==1){
//	         		query = "construct { ?subject ?predicate ?value } "+fromString+" where { ?subject ?predicate ?value.\n"
//	         				+ "?value bif:contains \"'"+filterList.get(0)+"'\".\n";
//	         		queryString="";
//	         	}
//	         	query+="}";
//         	    
//         	    System.out.println("=============================主语+宾语的全文检索============================");
//         	    System.out.println(query);
//         	    
//         	   vqe = VirtuosoQueryExecutionFactory.create (query, set);
//    	    	Model model = vqe.execConstruct();
//    	    	graph = model.getGraph();
//	       	    
//	       	    if(!query.equals(queryString)&&!queryString.equals("")){
//	       	    	System.out.println("=============================宾语的全文检索============================");
//	       	    	System.out.println(queryString);
//	       	    	vqe = VirtuosoQueryExecutionFactory.create (queryString, set);
//	     	    	model = vqe.execConstruct();
//	     	    	graph2 = model.getGraph();
//	       	    }
//	       	    
//	       	    isContinue=false;
//	       	    if(graph!=null&&graph.isEmpty()&&graph2!=null&&!graph2.isEmpty()){
//	       	    	graph=graph2;
//	       	    	graph2=null;
//	       	    }
//	    	}
//         }
//
//      //当多个检索关键字未查询到结果时，将词性为名词的词去除后再次进行查询
//	    if(graph==null||graph.isEmpty()){
//	    	int size=filterList.size();
//        	if(course.equals("history")&&!subjectList.isEmpty()&&subjectList.get(0).length()>4){
//        		filterList.removeAll(subjectList);
//        	}
//        	if(!nList.isEmpty()){
//        		
//        		String keyWord=ExtractKeyWord.extractKeyByLocation(question);
//        		
//    			filterList.removeAll(nList);
//        		if(filterList.isEmpty()){
//        			if(nList.contains(keyWord)){
//        				nList.remove(keyWord);
//        				filterList=nList;
//        			}
//        		}
//        		if(!leftList.isEmpty()) 
//        			filterList.addAll(leftList);
//        		
//        		filterList=RemoveDuplicate.remove(filterList);
//        		
//        		queryString = "construct { ?subject ?predicate ?value } "+fromString+" where { \n"
//        				+ "?subject ?predicate ?value.\n";
//        		if(filterList.size()==1&&filterList.get(0).length()==1) filterList.clear();
//        		if(!filterList.isEmpty()&&filterList.size()<size){
//	        		for(int i=0;i<filterList.size();i++){
//	        	    	if(!filterList.get(i).equals(""))
//	        	    		queryString+="?value bif:contains \"'"+filterList.get(i)+"'\"\n";
//	        	    }
//	        		
//	        		queryString+="}";
//	        	    System.out.println("=============================减少查询关键词==========================");
//	        	    System.out.println(queryString);
//	        	    vqe = VirtuosoQueryExecutionFactory.create (queryString, set);
//	        	    Model model = vqe.execConstruct();
//	     	    	graph = model.getGraph();
//        		}
//        	}
//        	isContinue=false;
//        }
//	    String maxStr="";
//        String maxCommonStr="";
//        MaxString max=new MaxString();
//        if(graph!=null){	
//        	max.getMaxCommon(graph, courseMap, commonMap, question,course);
//        	
//	        if(graph2!=null){
//	        	max.getMaxCommon(graph2, courseMap, commonMap, question,course);
//	        }
//	        maxStr=max.maxStr;
//	        maxCommonStr=max.maxCommonStr;
//        }
//        if(maxStr.contains("--内容:")&&(question.contains("哪些")||question.contains("怎样"))){
//        	
//        }
//        else if(maxStr.contains(":")&&maxStr.indexOf(maxCommonStr)>maxStr.indexOf(":")||!maxStr.contains(":"))
//        	maxStr=ExctratAnswer(course, question, maxCommonStr, maxStr);
//        if(maxCommonStr.length()<4) maxStr="";
//        if(maxStr!=null&&!maxStr.equals("")) System.out.println("最长公共子串："+maxCommonStr);
//        vqe.close(); 
//        set.close();
//        return maxStr;
//	}
    
    public String ExctratAnswer(String course,String question,String maxCommonStr,String maxStr){
		
		Map<String, Nature> wordMap=WordParser.splitWordandNature(WordParser.CutWord(question));
		String title="";
    	if(StringProcess.CountNumber(maxStr, "\n")>5||maxStr.length()>1000||maxStr.contains("相关链接")||maxStr!=null&&!maxStr.equals("")&&((maxCommonStr.length()==4&&!maxCommonStr.endsWith("的")&&!maxCommonStr.startsWith("的"))||maxCommonStr.length()>4)){
        	
    		title=maxStr.substring(0,maxStr.indexOf(":")+1);
        	String[] array=maxStr.split("。");
        	String[] arrays=maxStr.split("\n");
        	if(arrays.length>2&&array.length<=2&&!course.equals("politics")){
        		for(int i=0;i<arrays.length;i++){
	        		if((arrays[i].contains(maxCommonStr)&&!arrays[i].contains(":")&&!isContinue)||(arrays[i].contains(maxCommonStr)&&isContinue&&arrays[i].contains(":"))){
	        			maxStr=MaxString.subString("\n", arrays[i], maxCommonStr);
//	        			maxStr=MaxString.subString(" ", maxStr, maxCommonStr);
	        			if(i-1>-1&&arrays[i-1].replaceAll(" ", "").length()==2)
	        				maxStr=arrays[i-1]+maxStr;
	        			maxStr+="（非直接查询出的结果，经后续处理得到的答案）";
	        		}
	        	}
        	}
        	if((course.equals("politics")||course.equals("physics"))&&maxStr.length()<600||(rycount>1&&(question.contains("，")||question.contains(",")))){
        		String[] arraySplit=maxStr.split("。");
        		if(maxCommonStr.length()>10&&arraySplit.length>1){
	        		for(int i=0;i<arraySplit.length;i++){
	        			if(arraySplit[i].contains(maxCommonStr)){
	        				maxStr=arraySplit[i]+"（非直接查询出的结果，经后续处理得到的答案）";
	        				break;
	        			}
	        		}
        		}
        	}
        	else if(array.length>=2&&course.equals("biology")){
	        	for(int i=0;i<array.length;i++){
	        		if(array[i].contains(maxCommonStr)){
	        			int index=array[i].lastIndexOf("\n", array[i].indexOf(maxCommonStr));
	        			int index1=array[i].indexOf("\n", array[i].indexOf(maxCommonStr));		 
	        			
	        			if(index>0){
	        				maxStr=array[i].substring(index+1);
	        				continue;
	        			}
	        			else if(index1>0){
	        				maxStr=array[i].substring(0,index1);
	        				continue;
	        			}
	        			String[] array1=array[i].split("；");
	        			for(int t=0;t<array1.length;t++){
	        				if(array1[t].contains(maxCommonStr))
	        					maxStr=array1[t];
	        			}
	        		}
	        	}
	        	maxStr+="（非直接查询出的结果，经后续处理得到的答案）";
        	}
        	else if(array.length>2){
        		maxCommonStr=maxCommonStr.replace("。", "");
        		for(int i=0;i<array.length;i++){
	        		if(array[i].contains(maxCommonStr)){
	        			maxStr=MaxString.subString("\n", array[i], maxCommonStr);
//	        			maxStr=MaxString.subString(" ", maxStr, maxCommonStr);
	        			maxStr+="（非直接查询出的结果，经后续处理得到的答案）";
	        		}
	        	}
        	}
        	else if(maxStr.contains("\n")){
        		arrays=maxStr.split("\n");
        		for(int i=0;i<arrays.length;i++){
	        		if(arrays[i].contains(maxCommonStr)){
	        			maxStr=arrays[i]+"（非直接查询出的结果，经后续处理得到的答案）";
	        		}
	        	}
        	}
        	maxStr=maxStr.replaceAll("（如图\\d－\\d+）", "").replaceAll("（\\d）", "").replaceAll("\\d）", "");
        }
        else if(!wordMap.containsValue(Nature.cc))
        	maxStr="";
    	if(maxStr!=null&&!maxStr.equals("")&&!maxStr.contains(title))
    		maxStr=title+maxStr;
    	
    	return maxStr;
    }
    
    /**
     * 根据化学式查询实例名称
     * @param ChemicalFormula
     * @return
     */
    public static String searchByChemicalFormula(String ChemicalFormula){
    	
    	String label="";

    	VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");
    	String queryString = "SELECT ?label from <http://edukb.org/chemistry> WHERE { ?subject <http://www.w3.org/2000/01/rdf-schema#label> ?label ."
				+ "?subject <http://edukb.org/knowledge/0.1/property/chemistry#ChemicalFormula> '"+ChemicalFormula+"'.} ";
		
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (queryString, set);
		ResultSet result = vqe.execSelect();
		int count=0;
		while (result.hasNext()) { 
			QuerySolution rs = result.nextSolution();
		    
			if(count>0){
				String temp=rs.get("label").toString();
				Map<String, Nature> wordMap=WordParser.splitWordandNature(WordParser.CutWord(temp));
				if(wordMap.containsValue(Nature.gbc))
					label=temp;				
			}
			else label=rs.get("label").toString();
		    count++;
		}
		vqe.close();
		set.close();
    	return label;
    }
    
//    public String searchWikiInfobox(List<String> subjectList,List<Tamplate> TamplateList,String question){
//    	
//    	AnswerObject answerObj=new AnswerObject();
//    	String info="";
//    	String subjectName="";
//    	String type="";
//    	if(subjectList.size()==1) subjectName=subjectList.get(0);
//    	else{
//	    	for(int i=0;i<subjectList.size();i++){      		
//	    		List<Term> cutWords=WordParser.CutWord(subjectList.get(i));
//	    		if(cutWords.size()==1&&cutWords.get(0).nature==Nature.ns)
//	    			subjectName=subjectList.get(0);
//	    	}
//    	}
//    	if(subjectName.equals("")) return answer;
//    	for(Tamplate tamplate:TamplateList){
//    		String property=(String) courseMap.get(tamplate.getType());
//    		if(property!=null&&tamplate.getPriority()==1&&type.equals("")){
//    			type=property;
//    		}
//    		else if(property!=null&&tamplate.getPriority()==1&&!type.equals("")){
//    			if(!question.contains(type)&&question.contains(property)){
//    				type=property;
//    			}
//    		}
//    	}
//    	
//    	VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");
//    	String queryString = "SELECT ?wikiinfo from <http://edukb.org/geonamse> WHERE { ?subject <http://www.w3.org/2000/01/rdf-schema#label> '"+subjectName+"'."
//				+ "?subject <http://edukb.org/knowledge/0.1/property/geo#wiki_infobox> ?wikiinfo.} ";
//		
//		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (queryString, set);
//		ResultSet result = vqe.execSelect();
//		while (result.hasNext()) { 
//			QuerySolution rs = result.nextSolution();
//		    
//		    info=rs.get("wikiinfo").toString();
//		}
//		
//		vqe.close();
//		set.close();
//		if(info!=null&&info.equals("")){
//			info=MaxString.ExtractAnswerFromWiki(info, type);
//			answer=subjectName+info;
//		}
//		
//    	return answer;
//    }
    
    /**
     * 确定三元组是否存在
     * @param course
     * @param subjectName
     * @param propertyName
     * @return
     * @throws RepositoryException
     * @throws QueryEvaluationException
     * @throws MalformedQueryException
     */
    public boolean SubjectAndProperty(String course,String subjectName,String propertyName) throws RepositoryException, QueryEvaluationException, MalformedQueryException{
    	
    	VirtGraph set=new VirtGraph (url, "dba", "dba");  

    	String query = "ask from<"+fromString+"> WHERE {\n ?subject <http://www.w3.org/2000/01/rdf-schema#label> '"+subjectName+"' .\n"
    			+ "?property <http://www.w3.org/2000/01/rdf-schema#label> '"+propertyName+"'.\n"
    			+ "?subject ?property ?value.\n"
    			+ "} ";
    	
    	VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (query, set);
    	
    	boolean flag = vqe.execAsk();
        
        return flag;
    }
    
    /**
     * 查询出graphName图中的所有的三元组
     * @param course
     * @param graphName
     * @param path
     * @return
     */
    public JSONArray SearchValue(String course,String graphName){
    	
    	JSONArray dataJsonArray=new JSONArray();
		
    	StringBuffer buffer = new StringBuffer(); 		
    	buffer = new StringBuffer();
  
    	List<String> remainList=new ArrayList<String>();
    	remainList.add("http://edukb.org/knowledge/0.1/property/common#definition");
    	remainList.add("http://edukb.org/knowledge/0.1/property/common#altLabel");
    	remainList.add("http://edukb.org/knowledge/0.1/property/common#content");
    	remainList.add("http://edukb.org/knowledge/0.1/property/common#example");
    	remainList.add("http://edukb.org/knowledge/0.1/property/common#includes");
    	remainList.add("http://edukb.org/knowledge/0.1/property/common#includes");

		VirtuosoSearch vSearch=new VirtuosoSearch(course);
		
		VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");	

		fromString="from <http://edukb.org/"+graphName+">";
		if(graphName.equals("geo_china_pedia"))
			fromString="from <http://edukb.org/geo>";
		
		String queryProperty="SELECT distinct ?property ?label "+fromString+" WHERE {"
				+ "?property <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DatatypeProperty>."
				+ "?property <http://www.w3.org/2000/01/rdf-schema#label> ?label."
				+ "}";
		String queryObject="SELECT distinct ?property ?label "+fromString+" WHERE {"
				+ "?property <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty>."
				+ "?property <http://www.w3.org/2000/01/rdf-schema#label> ?label."
				+ "}";
		
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (queryProperty, set);
		ResultSet propertySet=vqe.execSelect();
		
		VirtuosoQueryExecution vqe2 = VirtuosoQueryExecutionFactory.create (queryObject, set);
		ResultSet ObjectSet=vqe2.execSelect();
		
		String predicate="";
		List<String> distincList=new ArrayList<String>();
		fromString="from <http://edukb.org/"+graphName+">";
		if(graphName.contains("geo_ad_")) fromString+=" from <http://edukb.org/geo_china_administrative_divisions>";
		List<String> uniqueList=new ArrayList<String>();

		if(!propertySet.hasNext()) propertySet=ObjectSet;
		while(propertySet.hasNext()){
			QuerySolution rSolution=propertySet.nextSolution();
			String propertyUri = rSolution.get("property").toString();
			predicate=rSolution.get("label").toString();
			
			if(!propertyUri.contains("shitixianzhi ")&&!distincList.contains(propertyUri)&&propertyUri.contains(course)||(propertyUri.contains("common")&&remainList.contains(propertyUri))){
				
				distincList.add(propertyUri);
				if(propertyUri.contains("\\")) continue;
				String query="select distinct ?subjectlabel ?value "+fromString+" WHERE { ?subject <"+propertyUri+"> ?value ."
						+ "?subject <http://www.w3.org/2000/01/rdf-schema#label> ?subjectlabel."
						+ "}";
	
				vqe = VirtuosoQueryExecutionFactory.create (query, set);
				ResultSet resultSet=vqe.execSelect();
		        String subject="";
		        
		        String value="";
//		        int id=1;
		        while(resultSet.hasNext()){
		        	
		        	QuerySolution rs=resultSet.nextSolution();
		        	if(rs.get("subjectlabel")!=null)
		        		subject=rs.get("subjectlabel").toString();
		        	value=rs.get("value").toString();
		        	if(value.contains("getjpg")||value.contains("getpng")) continue;
		        	if(value!=null&&!value.contains(" ")&&value.startsWith("http://"))
		        		value=vSearch.getLabel(value);
		        	String predicate_prefix=predicate.split("#")[0];
		        	if(predicate.contains("#"))
		        		predicate=predicate.split("#")[1];
	
		        	if(courseMap.get(predicate)!=null&&predicate_prefix.contains(course))
		        		predicate=(String) courseMap.get(predicate);
		        	if(commonMap.get(predicate)!=null&&predicate_prefix.contains("common"))
		        		predicate=(String) commonMap.get(predicate); 
		        	
		        	if(value==null||course.equals("geo")&&value.replaceAll("\\d+", "").replace("[a-z]","").length()<5&&!predicate.contains("别称")&&!predicate.contains("别名")) continue;
		        	else if(value==null||(value.length()<6&&!predicate.contains("别称")&&!predicate.contains("别名"))||(course.equals("chinese")&&value.equals("示例"))) continue;
		        	if(predicate.contains("terms/created")||predicate.contains("label")||predicate.contains("description")||predicate.contains("category")||predicate.contains("categoryId")||predicate.contains("source")||predicate.contains("type")||predicate.contains("annotation")) continue;
		        	
		        	JSONObject celljson=new JSONObject();
		        	
		        	if(subject==null) subject="";

		        	if(value!=null&&value.startsWith("\"")&&value.endsWith("\""))
		        		value=value.replaceAll("\"", "");
		        	
		        	if(!uniqueList.contains(subject+predicate+value)){
		        		if((graphName.contains("baidu")||graphName.contains("wiki")||graphName.contains("pedia"))&&(predicate.contains("正文")||predicate.contains("百度"))){
		        			String[] array=value.split("。");
		        			for(String item:array){
		        				item=item.replaceAll("\\[[0-9]*\\]", "");
		        				if(!item.equals("")){
			        				celljson=new JSONObject();
			        				celljson.put("subject", subject);
						        	celljson.put("predicate", predicate);
						        	celljson.put("value", item);	
						        	dataJsonArray.add(celljson);
		        				}
		        			}
		        		}
		        		else {
		        			celljson.put("subject", subject);
				        	celljson.put("predicate", predicate);
				        	celljson.put("value", value);	
				        	dataJsonArray.add(celljson);
						}
		        		
		        	}
		        	else continue;
		        }
			}
			if(!propertySet.hasNext()&&isContinue){ 
				propertySet=ObjectSet;
				isContinue=false;
			}
		}	
//		TextJson.createJson(course, path, dataJsonArray);
		vqe.close();
		vqe2.close();
		set.close();
		
		return dataJsonArray;
	}
    
    public static List<String> SearchRelate(String subject){
    	
    	List<String> relateList=new ArrayList<String>();
    	VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");	
    	
    	String query="PREFIX pp: <http://edukb.org/knowledge/0.1/property/politics#>"
    			+ "PREFIX cop: <http://edukb.org/knowledge/0.1/property/common#>"
    			+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
    			+ "SELECT distinct ?subject ?subjectlabel from <http://edukb.org/politics> "
    			+ "WHERE {"
    			+ "?subject rdfs:label ?subjectlabel."
    			+ "?subject pp:qiangxiangguanyu ?relateTo."
    			+ "?relateTo rdfs:label '"+subject+"'."
    			+ "}";
    	VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (query, set);
		ResultSet resultSet=vqe.execSelect();

		while(resultSet.hasNext()){
			QuerySolution rSolution=resultSet.nextSolution();
			RDFNode labelNode = rSolution.get("subjectlabel");
			
			if(labelNode!=null&&!subject.equals(labelNode.toString())){
				relateList.add(labelNode.toString());
			}
		}
    	
    	vqe.close();
		set.close();
    	return relateList;
    }
    public static boolean isExist(String query){
    	
    	boolean isExist=false;
    	VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");	
    	VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (query, set);

    	isExist= vqe.execAsk();
    	vqe.close();
    	set.close();
    	return isExist;
    }
    
    public int SearchCount(String predicate){
    	int count=0;
    	VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");	
    	
    	String query="SELECT (count(distinct ?s) as ?count)"+fromString+"WHERE {"
				+ "?s <"+predicate+"> ?o."
				+ "}";
    	
    	VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (query, set);
		ResultSet resultSet=vqe.execSelect();
    	while (resultSet.hasNext()) {
			QuerySolution querySolution = (QuerySolution) resultSet.next();
			Literal countStr=querySolution.getLiteral("count");
			count=countStr.getInt();
		}
    	
    	vqe.close();
    	set.close();
    	
    	return count;
    }
	public static void main(String[] args) throws RepositoryException, QueryEvaluationException, MalformedQueryException, ClassNotFoundException, IOException, SQLException {
		
//		String qString="SELECT (count(?s) as ?count)"
//				+ "from <geonames_chinese> WHERE {"
//				+ "?s ?p ?o."
//				+ "}";

		VirtuosoSearch search=new VirtuosoSearch("");
		
		search.SearchValue("geo","geo_ad_baidu");
	}
}
