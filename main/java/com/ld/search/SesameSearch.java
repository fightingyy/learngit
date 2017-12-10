//package com.ld.search;
//
//import java.io.IOException;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//import javax.servlet.ServletContext;
//
//import net.sf.json.JSONObject;
//
//import org.apache.struts2.ServletActionContext;
//import org.openrdf.model.Statement;
//import org.openrdf.model.Value;
//import org.openrdf.model.ValueFactory;
//import org.openrdf.query.BindingSet;
//import org.openrdf.query.BooleanQuery;
//import org.openrdf.query.GraphQuery;
//import org.openrdf.query.GraphQueryResult;
//import org.openrdf.query.MalformedQueryException;
//import org.openrdf.query.QueryEvaluationException;
//import org.openrdf.query.QueryLanguage;
//import org.openrdf.query.TupleQuery;
//import org.openrdf.query.TupleQueryResult;
//import org.openrdf.repository.RepositoryConnection;
//import org.openrdf.repository.RepositoryException;
//import org.openrdf.repository.http.HTTPRepository;
//
//import com.hankcs.hanlp.corpus.tag.Nature;
//import com.hankcs.hanlp.seg.common.Term;
//import com.ld.IO.MaxString;
//import com.ld.IO.StringProcess;
//import com.ld.IO.subjectSort;
//import com.ld.IO.Property.ReadProperty;
//import com.ld.IO.remove.RemoveDuplicate;
//import com.ld.MainPartExtractor.DecideSubject;
//import com.ld.Parser.WordParser;
//import com.ld.model.Label;
//import com.ld.model.Tamplate;
//
///**   
//*    
//* 项目名称：KnowledgeQA   
//* 类名称：Search   
//* 类描述： sesame查询
//* 创建人：ludan   
//* 创建时间：2017年7月17日 下午3:58:10   
//* @version        
//*/
//public class SesameSearch {
//
//	private static String SERVER = "http://kb.cs.tsinghua.edu.cn:18080/openrdf-sesame/";
////    public List<String> REPOSITORY = new ArrayList<String>();
//    public String REPOSITORY = "chinese_facts";
//
//    private HTTPRepository repository;//Repository，ValueFactory和RepositoryConnection
//    private ValueFactory valueFactory;
//    private RepositoryConnection connection;
//    public String subjectName="";
//    Map chineseMap=new HashMap();
//	Map commonMap=new HashMap();
//	Map courseMap=new HashMap();
//	public boolean isContinue=true;
//	private int rycount=0;
//	
//	public SesameSearch(){
//
//	}
//	
//    /**
//    * 初始化连接
//    * @throws RepositoryException
//    */
//    public void initConnection() throws RepositoryException {
//        repository = new HTTPRepository(SERVER, REPOSITORY);
//        repository.initialize();
//        repository.setUsernameAndPassword("sesame863", "KEG#$*205");
//        connection = repository.getConnection();
//        valueFactory = connection.getValueFactory();
//    }
//
//    /**
//    * 关闭连接
//    * @throws RepositoryException
//    */
//    public void closeConnection() throws RepositoryException {
//        connection.close();
//        repository.shutDown();
//    }
//    
//    public JSONObject result(String query,String course,List<String> usageList) throws RepositoryException, MalformedQueryException, QueryEvaluationException, ClassNotFoundException, SQLException, IOException{
//    	
//    	JSONObject results=new JSONObject();
//    	if(course.equals("chinese"))
//    		this.REPOSITORY="chinese_facts";
//    	else this.REPOSITORY=course;
//    	results=resultReturn(query, course,usageList);
//    	
////    	if(results.isEmpty()){
////    		this.REPOSITORY="history";
////    		results=new JSONObject();
////    		results=resultReturn(query, course,usageList);
////    	}
////    	
////    	if(results.isEmpty()){
////    		this.REPOSITORY="geo";
////    		results=new JSONObject();
////    		results=resultReturn(query, course,usageList);
////    	}
//    	return results;
//    }
//    
//    /**根据sparql查询语句进行查询
//     * @param queryString
//     * @param course
//     * @param usageList
//     * @return
//     * @throws RepositoryException
//     * @throws MalformedQueryException
//     * @throws QueryEvaluationException
//     * @throws ClassNotFoundException
//     * @throws SQLException
//     * @throws IOException
//     */
//    @SuppressWarnings("unchecked")
//	public JSONObject resultReturn(String queryString, String course,List<String> usageList) throws RepositoryException, MalformedQueryException, QueryEvaluationException, ClassNotFoundException, SQLException, IOException { 	
//		
//        //打开连接
//        HTTPRepository repository = new HTTPRepository(SERVER, REPOSITORY);
//        System.out.println("REPOSITORY:"+REPOSITORY);
//        repository.setUsernameAndPassword("sesame863", "KEG#$*205");
//        repository.initialize();
//        RepositoryConnection connection = repository.getConnection();
//        ServletContext sct= ServletActionContext.getServletContext();
//        
//		commonMap=ReadProperty.selectProperty("Common");
//		chineseMap=(Map) sct.getAttribute("chineseMap");
//		
//        courseMap=ReadProperty.selectProperty(course);
//
//        TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
//        TupleQueryResult result = tupleQuery.evaluate();
//
//        int numHeader = result.getBindingNames().size();
//        List arrHeader = new ArrayList();
//        List listItem = new ArrayList();
//        List arrItem = new ArrayList();
//
//        List<String> sList=new ArrayList<String>();
//        
//        JSONObject jsonObject = new JSONObject();
//
//        int acount=9;
//        if(usageList.contains("status")) acount=30;
//        boolean subject=false;
//        subjectSort subjects=new subjectSort();
//        String subjectTemp="";
//        
//        for (int i = 0; i < numHeader; i++) {
//        	String property=result.getBindingNames().get(i);
//        	
//        	if(property.equals("subjectlabel")){
//        		subject=true;
//        		continue;
//        	}
//        	
//        	if(!queryString.contains("subjectlabel")){
//        		String stemp="";
//        		if(queryString.contains("shitixianzhi"))
//        			stemp=queryString.substring(queryString.indexOf("?limit rdfs:label '")+19,queryString.lastIndexOf("'."))+"的";
//        		if(queryString.indexOf("?subject rdfs:label '")+21<queryString.indexOf("'."))
//        			subjectTemp=stemp+queryString.substring(queryString.indexOf("?subject rdfs:label '")+21,queryString.indexOf("'."));
//    		}
//            if((String) courseMap.get(property)!=null){
//            	property=(String) courseMap.get(property);
//            }
//            if((String) commonMap.get(property)!=null){
//            	property=(String) commonMap.get(property);
//            }
//            if(property.equals("type"))
//            	property="类型";
//            arrHeader.add(subjectTemp+"--"+property);
//        }
//        
//        int count = 0;
//        if(subject&&numHeader>1){
//        	
//        	List<subjectSort> subjectList=new ArrayList<subjectSort>();
//        	
//        	arrHeader = new ArrayList();
//              	 
//        	int tag=0;
//            while (result.hasNext()) {
//
//            	if(tag==acount)break;
//            	tag++;
//                BindingSet bindingSet = result.next();
//                String property=result.getBindingNames().get(0);
////                arrHeader.add(bindingSet.getValue(propery).stringValue());
//                arrHeader.clear();
//                if(property.equals("subjectlabel")) 
//                	subjectName+=bindingSet.getValue(property).stringValue()+"|";
//                
//                String object=null;
//                String subjectlabel="";
//                
//                if(usageList.contains("first")||usageList.contains("last")){
//                	
//                	String subjectLabel=bindingSet.getValue(result.getBindingNames().get(0)).stringValue();
//                	String bindingName = result.getBindingNames().get(1);
//                	if(bindingName.equals("begintime")&&bindingSet.getValue(bindingName)!=null){
//                		tag=0;
//	                	String begintime=bindingSet.getValue(bindingName).stringValue();
//	                	subjectSort subjectsort=new subjectSort(subjectLabel,begintime);
//	                	
//	                	subjectList.add(subjectsort);
//                	}
//                	if(result.getBindingNames().size()>2){
//                	bindingName = result.getBindingNames().get(2);
//	                	if(bindingName.equals("shijian")&&bindingSet.getValue(bindingName)!=null){
//	                		tag=0;
//		                	String begintime=bindingSet.getValue(bindingName).stringValue();
//		                	subjectSort subjectsort=new subjectSort(subjectLabel,begintime);
//		                	
//		                	subjectList.add(subjectsort);
//	                	}
//                	}
//                }
//                
//                if(subjectList.isEmpty()){
//                	
//                	for (int i = 1; i < numHeader; i++) {
//                
//                	
//	                String bindingName = result.getBindingNames().get(i);
//	                
//	                if(bindingSet.getValue(bindingName)!=null){
//	                	if(bindingSet.getValue(property)!=null)
//	                		subjectlabel=bindingSet.getValue(property).stringValue();
//	                	object=bindingSet.getValue(bindingName).stringValue();
//	                	if(object==null||object.equals("")) continue;
//	                	property=bindingName;
//	                	if((String) courseMap.get(property)!=null){
//	                    	property=(String) courseMap.get(property);
//	                    }
//	                    if((String) commonMap.get(property)!=null){
//	                    	property=(String) commonMap.get(property);
//	                    }
//	                    
//	                    object=subjectlabel+"--"+property+":<br>"+object;
//	                	if(isUrl(object)){
//	                		if(object.contains("instance")||object.contains("class")){
//	                			sList.add(object);
//	                		}
//	                		else if(object.contains("getjpg")||object.contains("getpng")){
//		                		object=object.replaceAll("&nbsp;", "").replaceAll("<br>", "");
//		                		listItem.add(object);
//		                	}
//	                	}
//	                	else if(object!=null&&!object.equals("")) listItem.add(object);
//	                }
//
//	            }
//            }
//                if((object==null||object.equals(""))&&!arrHeader.isEmpty()){
//                	arrHeader.remove(count);
//                	count=count-1;
//                }
//	            
//	            count = count + 1;
//	        }
//            if(subjectName!=null&&!subjectName.equals("")){
//            	subjectName=RemoveDuplicate.removeSameStr(subjectName);
//            }
//            if(!subjectList.isEmpty()){
//            	String usage="";
//            	if(usageList.contains("first")) 
//            		usage="first";
//            	else if(usageList.contains("last")) 
//            		usage="last";
//            	
//            	
////            	subjects=subjects.getSort(subjectList,usage);
//            }
//            if(!sList.isEmpty()){
//		        List<String> LabelsList=searchLabels(sList);
//
//	            listItem.addAll(LabelsList);
//	        }
//            listItem=RemoveDuplicate.remove(listItem);
//            arrItem.add(listItem);
//
//        }
//
//        else{
//	        while (result.hasNext()) {
//	
//	            BindingSet bindingSet = result.next();
//	            
//	            if(bindingSet.size()>0){
//	            
//			        for (int i = 0; i < numHeader; i++) {
//		
//		                String bindingName = result.getBindingNames().get(i); 
//		                
//		                String object="";
//		                if(bindingSet.getValue(bindingName)!=null){
//		                	object=bindingSet.getValue(bindingName).stringValue();
//		                }
//		                
//		                if(isUrl(object)){
//	                		if(object.contains("instance")||(object.contains("class")&&!object.contains("Category")&&!object.contains("NamedIndividual")&&!object.contains("owl#Class"))){
//	                			sList.add(object);
//	                		}
//	                		else if(object.contains("getjpg")||object.contains("getpng")){
//		                		object=object.replaceAll("&nbsp;", "").replaceAll("<br>", "");
//		                		listItem.add(object);
//		                	}
//	                	}
//	                	else if(object!=null&&!object.equals(""))
//	                			listItem.add(object);
//		            }
//			        count = count + 1;
//	            }
//		     }
//	        if(!sList.isEmpty()){
//		        List<String> LabelsList=searchLabels(sList);
//
//	            listItem.addAll(LabelsList);
//	        }
//	        
//	        listItem=RemoveDuplicate.remove(listItem);
//	        arrItem.add(listItem);
//        }
//        String item="";
//        String list="";
//        String temp="";
//        String image="";
//        arrHeader=RemoveDuplicate.remove(arrHeader);
//        
//        if(subjects.getSubject()==null){
//        	int t=0;
//        	
//	        for(int i=0;i<arrHeader.size();i++){
//	        	
//	        	if(!listItem.isEmpty()){
//	        		item=null;
//	        		image="";
//	        		for(int j=0;j<listItem.size();j++){
//	        			
//	        			if(temp.replaceAll(" ", "").contains(listItem.get(j).toString().replaceAll(" ", ""))||temp.replaceAll(" ", "").equals(listItem.get(j).toString().replaceAll(" ", ""))) continue;
//	        			else if(listItem.get(j).toString().contains(temp)&&!temp.equals("")) 
//	        				list=list.replace(temp, listItem.get(j).toString());
//	        			else {
//	        				if(isUrl(listItem.get(j).toString()))
//	        					image=arrHeader.get(i)+":"+listItem.get(j);
//	        				else list=list+"<br>"+listItem.get(j)+"<br>";
//	        			}
//		        		temp=listItem.get(j).toString();
//	        		}
//	        		if(!arrHeader.isEmpty()&&image.equals(""))
//	        			item=arrHeader.get(i)+":"+list;
//	        		else item=list;
//	        	}
//	
//		        if(item!=null&&!item.equals("")){
//		        	jsonObject.put(t,item);
//		        	t++;
//		        }
//		        if(!image.equals("")&&image!=null){
//		        	jsonObject.put(t,image);
//			        t++;
//		        }
//	        }
//	        
//	        Collections.sort(listItem);
//	        if(arrHeader.isEmpty()&&!listItem.isEmpty()){
//	        	String itemStr="";
//	        	String belongStr="";
//	        	if(queryString.contains("belongsToO")){
//	        		belongStr=queryString.substring(queryString.indexOf("?belongsToO rdfs:label '")+24, queryString.lastIndexOf("'."));
//	        	}
//	        	for(int j=0;j<listItem.size();j++){
//	        		if(!belongStr.equals("")&&listItem.get(j).toString().equals(belongStr)) continue;
//	        		if(listItem.get(j).toString().contains(":"))
//	        			jsonObject.put(j,listItem.get(j).toString());
//	        		else if((itemStr==null||itemStr.equals(""))&&jsonObject.isEmpty())
//	        			itemStr+=listItem.get(j).toString();
//        			else itemStr+="、"+listItem.get(j).toString();	        		
//        		}
//	        	if(itemStr!=null&&!itemStr.equals(""))
//	        		jsonObject.put(0,itemStr);
//	        }
//        }
//        else {
//        	jsonObject.put(0,subjects.getSubject());
//        	subjectName=subjects.getSubject();
//        }
//        result.close();
//        if(!jsonObject.isEmpty()&&jsonObject.getString("0").equals("")) jsonObject=new JSONObject();
//        return jsonObject;
//
//    }
//    
//    //查询实例
//    public void searchInstance(String str) throws Exception {
//        initConnection();
//
////        String queryString = "SELECT ?x WHERE { ?x <http://edukb.org/knowledge/0.1/property/common#label> \"" + str + "\" } ";
//        String queryString = "SELECT ?s WHERE { ?x <http://www.w3.org/2000/01/rdf-schema#label> ?s } ";
//        System.out.println(queryString);
//        TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
//        TupleQueryResult result = tupleQuery.evaluate();
//        while (result.hasNext()) { // iterate over the result
//            BindingSet bindingSet = result.next();
//            Value valueOfX = bindingSet.getValue("s");
//            System.out.println(valueOfX.stringValue());
//        }
//        result.close();
//
//        closeConnection();
//    }
//    
//    public String getLabel(String subject) throws RepositoryException, QueryEvaluationException, MalformedQueryException{
//    	
//    	String label=null;
//   
//    	initConnection();
//    	subject=subject.trim();
//
////    	String queryString = "SELECT ?label WHERE { <"+subject+"> <http://www.w3.org/2000/01/rdf-schema#label> ?label .} ";
//    	String queryString = "SELECT ?label ?des ?limitlabel WHERE { <"+subject+"> <http://www.w3.org/2000/01/rdf-schema#label> ?label ."
//    			+ "OPTIONAL {<"+subject+"> <http://purl.org/dc/elements/1.1/description> ?des.}"
//    			+"OPTIONAL {?limit <http://edukb.org/knowledge/0.1/property/geo#shitixianzhi> <"+subject+">."
//    			+ "?limit <http://www.w3.org/2000/01/rdf-schema#label> ?limitlabel.}"
//    			+ "} ";
//    	
//    	TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
//        TupleQueryResult result = tupleQuery.evaluate();
//        while (result.hasNext()) { // iterate over the result
//            BindingSet bindingSet = result.next();
//            
//            Value valueOfD = bindingSet.getValue("des");
//            if(valueOfD!=null) label=valueOfD.stringValue();
//
//	        if(label==null||label.equals("")){
//	            Value valueOfX = bindingSet.getValue("label");
//	            label=valueOfX.stringValue();
//	        }
//	        
//	        Value valueOfL = bindingSet.getValue("limitlabel");
//            if(valueOfL!=null&&!label.contains("中国的")) label=valueOfL.stringValue()+"的"+label;
//            
//        }
//        result.close();
//
//        closeConnection();
//    	
//    	return label;
//    }
//    
//    public String searchLabel(String subject) throws RepositoryException, QueryEvaluationException, MalformedQueryException{
//    	
//    	String label=null;
//    	
//    	initConnection();
//    	
//    	String queryString = "SELECT ?label WHERE { <"+subject+"> ?o ?label .} ";
//    	
//    	TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
//        TupleQueryResult result = tupleQuery.evaluate();
//        while (result.hasNext()) { 
//            BindingSet bindingSet = result.next();
//            Value valueOfX = bindingSet.getValue("label");
//            label=valueOfX.stringValue();
//        }
//        result.close();
//
//        closeConnection();
//    	
//    	return label;
//    }
//    
//    public List<String> searchLabels(List<String> sList) throws RepositoryException, QueryEvaluationException, MalformedQueryException{
//    	
//		List<String> labels=new ArrayList<String>();
//    	String label=null;
//    	String temp="";
//    	
//    	initConnection();
//    	
//    	int count=sList.size();
//    	
//    	String queryString = "SELECT * WHERE { ";
//    	if(count==1){
//    		String subject=sList.get(0);
//    		if(subject.contains(":")&&!subject.startsWith("http:"))
//    			temp=subject.substring(0, subject.indexOf(":")+1);
//    	}
//    	
//    	for(int i=0;i<count;i++){
//    		
//    		String subject=sList.get(i);
//    		subject=subject.replaceAll("<br>", "").replaceAll("&nbsp;", "");
//    		if(!subject.startsWith("http"))
//    			subject=subject.substring(subject.indexOf("http"));
//    	
//	    	queryString+="\t"+"<"+subject+"> <http://www.w3.org/2000/01/rdf-schema#label> ?label"+i+".\n"
//	    	+"\t"+"OPTIONAL {<"+subject+"> <http://purl.org/dc/elements/1.1/description> ?des"+i+".}\n";
//    	}
//    	
//    	queryString+="} ";
//    	
////    	System.out.println(queryString);
//	    	
//	    	TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
//	        TupleQueryResult result = tupleQuery.evaluate();
//	        while (result.hasNext()) {
//	            BindingSet bindingSet = result.next();
//	            for(int i=0;i<count;i++){
//	            	label="";
//	            	if(bindingSet.hasBinding("des"+i)){
//		            	Value valueOfD = bindingSet.getValue("des"+i);
//			            label=valueOfD.stringValue();
//	            	}
//		            if(label==null||label.equals("")){
//			            Value valueOfX = bindingSet.getValue("label"+i);
//			            label=valueOfX.stringValue();
//			            if(count==1)label=temp+label;
//		            }
//	
//		            labels.add(label);
//	            }
//	        }
//        
//	    result.close();    
//        closeConnection();
//    	
//    	return labels;
//    }
//    
//	public boolean SubjectAndProperty(String course,String subjectName,String propertyName) throws RepositoryException, QueryEvaluationException, MalformedQueryException{
//	    	
//	    	REPOSITORY=course;
//	    	initConnection();
//	
//	    	String query = "ask {\n ?subject <http://www.w3.org/2000/01/rdf-schema#label> '"+subjectName+"' .\n"
//	    			+ "?property <http://www.w3.org/2000/01/rdf-schema#label> '"+propertyName+"'.\n"
//	    			+ "?subject ?property ?value.\n"
//	    			+ "} ";
//	    	
//	    	BooleanQuery booleanQuery = connection.prepareBooleanQuery(QueryLanguage.SPARQL, query);
//	    	
//	    	boolean flag = booleanQuery.evaluate();
//	       
//	        return flag;
//    }
//	
//		
//
//    public List<String> searchLabels(Map<String,String> map) throws RepositoryException, QueryEvaluationException, MalformedQueryException{
//    	
//		List<String> labels=new ArrayList<String>();
//    	String label=null;
//    	List<String> valueList=new ArrayList<String>();
//    	
//    	
//    	initConnection();
//    	
//    	String queryString = "SELECT * WHERE { ";
//    	
//    	int i=0;
//    	for(Entry<String,String> entry:map.entrySet()){
//    		
//    		String subject=entry.getKey();    	
//	    	queryString+="\t"+"<"+subject+"> <http://www.w3.org/2000/01/rdf-schema#label> ?label"+i+" .\n";
//	    	i++;
//    	}
//    	
//    	queryString+="} ";
//	    	
//    	TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
//        TupleQueryResult result = tupleQuery.evaluate();
//        valueList.addAll(map.values());
//        while (result.hasNext()) {
//            BindingSet bindingSet = result.next();
//            for(int j=0;j<valueList.size();j++){
//	            Value valueOfX = bindingSet.getValue("label"+j);
//	            label=valueOfX.stringValue();
//	            String title=valueList.get(j).toString();
//		        labels.add(title+label);
//            }
//        }
//        
//	    result.close();
//	    closeConnection();
//    	
//    	return labels;
//	}
//
//
//     /**查询子图
//     * @param subjectName
//     * @param filterStr
//     * @param isSelect
//     * @param course
//     * @return
//     * @throws Exception
//     */
//    public JSONObject searchGraph(String subjectName,String filterStr,boolean isSelect,String course) throws Exception {
//    	
//        JSONObject resultObject=new JSONObject();
//		
//		if(course.equals("chinese"))
//			REPOSITORY="chinese_facts";
//		else REPOSITORY=course;
//		
//		initConnection();
//		
//		ServletContext sct= ServletActionContext.getServletContext();
//		commonMap=ReadProperty.selectProperty("Common");
//		
//		courseMap=ReadProperty.selectProperty(course);
//    	
//        String queryString ="";
//        String queryValue="";
//        
//        if(!isSelect){
//        	queryString= "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o .?s <http://www.w3.org/2000/01/rdf-schema#label> '" +subjectName+ "'.}";
//        queryValue="CONSTRUCT { ?s ?p ?o} \n WHERE { \n"
//    			+ "?s ?p ?o .\n"
//    			+ "?o <http://www.w3.org/2000/01/rdf-schema#label> '"+subjectName+"'.\n"
//				+ "}";
//        }
//        else {
//        	
//        	queryString="CONSTRUCT { ?s ?p ?o} \n WHERE { \n"
//        			+ "?s ?p ?o .\n"
//        			+ "?s <http://www.w3.org/2000/01/rdf-schema#label> ?subjectlabel.\n"
//        			+ "FILTER CONTAINS(str(?subjectlabel),'"+filterStr+"','i').\n"
//        					+ "}";
//        	queryValue="CONSTRUCT { ?s ?p ?o} \n WHERE { \n"
//        			+ "?s ?p ?o .\n"
//        			+ "?o <http://www.w3.org/2000/01/rdf-schema#label> ?subjectlabel.\n"
//        			+ "FILTECONTAINSex("+"?subjectlabel,'"+filterStr+"','i').\n"
//        					+ "}";
//        }
//        
//        System.out.println(queryString);
//        GraphQuery graphQuery = connection.prepareGraphQuery(QueryLanguage.SPARQL, queryString); 
//        GraphQueryResult result = graphQuery.evaluate();
//        
//        graphQuery = connection.prepareGraphQuery(QueryLanguage.SPARQL, queryValue); 
//        GraphQueryResult result2 = graphQuery.evaluate();
//        //Model resultModel = QueryResults.asModel(result);//GraphQueryResult to Model
//        Label label=new Label();
//        int i=0;
//        String temp="";
//        String title="";
//        String tempSubject="";
//        Map<String,String> map=new HashMap<String,String>();
//        List<String> labelList=new ArrayList<String>();
//        String uri="";
//        String tempuri=null;
//       
//        while (result.hasNext()) {
//            Statement statement = result.next();
//            uri=statement.getSubject().stringValue();
//            if(!uri.equals(tempuri)) {
//            	tempSubject=getLabel(uri);
//            	this.subjectName+=tempSubject+"|";
//            	tempuri=uri;
//            }
//            String predicate=statement.getPredicate().stringValue();
//            
//            String object=statement.getObject().stringValue();
//        	if(isUrl(object)){
//        		if(object.contains("instance")||(predicate.contains("type")&&!object.contains("NamedIndividual")&&!object.contains("Category")&&!object.contains("owl#Class"))){
//        			predicate=predicate.split("#")[1];
//	            	if(courseMap.get(predicate)!=null)
//	            		predicate=(String) courseMap.get(predicate);
//	            	if(commonMap.get(predicate)!=null)
//	            		predicate=(String) commonMap.get(predicate);
//	            	if(predicate.equals("type"))
//	            		predicate="类型";
//        			map.put(object,tempSubject+"--"+predicate+":");
//        			continue;
//        		}
//        		else if(!object.contains("getpng")&&!object.contains("getjpg")&&!predicate.contains("image")) 
//        			continue;
//        	}
//        	if(predicate.contains("#label")||predicate.contains("description")) continue;
//        	if(predicate.contains("#")){
//        		predicate=predicate.split("#")[1];
//            	if(courseMap.get(predicate)!=null)
//            		predicate=(String) courseMap.get(predicate);
//            	if(commonMap.get(predicate)!=null)
//            		predicate=(String) commonMap.get(predicate);
//            	
//            	if(predicate==null||predicate.equals("分类编号"))continue;
//        	}
//        	
//        	String items="";
//        	if(predicate!=null&&!object.equals("")&&object!=null){
//        		items=tempSubject+"--"+predicate+":"+object+"<br>";
//            	if(!temp.equals(items)&&!items.equals("")){
//	            	temp=items;
//	            	resultObject.put(i, items);	
//	            	i++;
//            	}
//        	}
//        }
//        
//        while (result2.hasNext()) {
//            Statement statement = result2.next();
//            uri=statement.getSubject().stringValue();
//            if(!uri.equals(tempuri)) {
//            	tempSubject=getLabel(uri);
//            	this.subjectName+=tempSubject+"|";
//            	tempuri=uri;
//            }
//            String predicate=statement.getPredicate().stringValue();
//            
//            String object=statement.getObject().stringValue();
//        	if(isUrl(object)){
//        		if(object.contains("instance")||(predicate.contains("type")&&!object.contains("NamedIndividual")&&!object.contains("Category")&&!object.contains("owl#Class"))){
//        			predicate=predicate.split("#")[1];
//	            	if(courseMap.get(predicate)!=null)
//	            		predicate=(String) courseMap.get(predicate);
//	            	if(commonMap.get(predicate)!=null)
//	            		predicate=(String) commonMap.get(predicate);
//	            	if(predicate.equals("type"))
//	            		predicate="类型";
//        			map.put(object,tempSubject+"--"+predicate+":");
//        			continue;
//        		}
//        		else if(!object.contains("getpng")&&!object.contains("getjpg")&&!predicate.contains("image")) 
//        			continue;
//        	}
//        	if(predicate.contains("#label")||predicate.contains("description")) continue;
//        	if(predicate.contains("#")){
//        		predicate=predicate.split("#")[1];
//            	if(courseMap.get(predicate)!=null)
//            		predicate=(String) courseMap.get(predicate);
//            	if(commonMap.get(predicate)!=null)
//            		predicate=(String) commonMap.get(predicate);
//            	
//            	if(predicate==null||predicate.equals("分类编号"))continue;
//        	}
//        	
//        	String items="";
//        	if(predicate!=null&&!object.equals("")&&object!=null){
//        		items=tempSubject+"--"+predicate+":"+object+"<br>";
//            	if(!temp.equals(items)&&!items.equals("")){
//	            	temp=items;
//	            	resultObject.put(i, items);	
//	            	i++;
//            	}
//        	}
//        }
//        
//        if(this.subjectName!=null&&this.subjectName.equals("|")){
//    		this.subjectName=RemoveDuplicate.removeSameStr(this.subjectName);
//    		resultObject.put("subject", this.subjectName);
//        }
//        
//        labelList=searchLabels(map);
//        if(!labelList.isEmpty()){
//        	for(int j=0;j<labelList.size();j++){
//        		resultObject.put(i, labelList.get(j));
//        		i++;
//        	} 		
//        }
//        closeConnection();
//        result.close();
//        
//        return resultObject;
//     
//    }
//    
//	public JSONObject subjectFilterSearch(String course,String question,Map<String,String> courseTerms) throws Exception {
//	        
//			if(course.equals("chinese"))
//	    		this.REPOSITORY="chinese_facts";
//	    	else this.REPOSITORY=course;
//		
//	    	initConnection();
//	        
//	        JSONObject resultObject=new JSONObject();
//	        
//	        ServletContext sct= ServletActionContext.getServletContext();
//			commonMap=ReadProperty.selectProperty("Common");
//			chineseMap=(Map) sct.getAttribute("chineseMap");
//			
//	        courseMap=ReadProperty.selectProperty(course);
//	        
//	        List<Nature> natureList=new ArrayList<Nature>();
////	        natureList.add(Nature.n);
//	        natureList.add(Nature.nz);
//	        natureList.add(Nature.nr);
//	        natureList.add(Nature.ns);
//	        natureList.add(Nature.a);
//	        natureList.add(Nature.v);
//	        natureList.add(Nature.vn);
//	        natureList.add(Nature.vi);
//	        natureList.add(Nature.g);
//	        natureList.add(Nature.gb);
//	        natureList.add(Nature.gbc);
//	        natureList.add(Nature.gc);
//	        natureList.add(Nature.gg);
//	        natureList.add(Nature.gi);
//	        natureList.add(Nature.gm);
//	        natureList.add(Nature.gp);
//	        natureList.add(Nature.nm);
//	        natureList.add(Nature.nmc);
//	        natureList.add(Nature.nb);
//	        natureList.add(Nature.nba);
//	        natureList.add(Nature.nbc);
//	        natureList.add(Nature.nbp);
//	        natureList.add(Nature.nhm);
//	        
//	        String queryString = "construct { ?subject ?predicate ?value } where {\n"
//	        		+ " ?subject ?predicate ?value.\n"
//	        		+ "?subject <http://www.w3.org/2000/01/rdf-schema#label> ?subjectlabel. \n";
//	        if(question.contains("等")) question=question.substring(question.indexOf("等")+1);
//        	question=question.replaceAll("这(.*)?(是|叫)什么", "");
//        	
//	        List<Term> cutWords=WordParser.CutWord(question);
//	        List<String> filterList=new ArrayList<String>();
//	        int t=0;
//	        for(int i=0;i<cutWords.size();i++){
//	        	Term word=cutWords.get(i);
//	        	if(word.nature==Nature.n&&i==0){
//	        		filterList.add(word.word);
//	        		continue;
//	        	}
//	        		
//	        	if(word.word.equals("起着")||word.word.equals("叫")) continue;
////	        	if(word.word.length()>1&&word.nature!=Nature.d&&word.nature!=Nature.ry&&word.nature!=Nature.rys&&word.nature!=Nature.ryt&&word.nature!=Nature.ryv&&word.nature!=Nature.r&&word.nature!=Nature.w&&word.nature!=Nature.ude1&&word.nature!=Nature.cc&&word.nature!=Nature.vshi){
//	        	if(word.word.length()>1&&natureList.contains(word.nature))	{
//        			filterList.add(word.word);
//	        	}
//	        	else if(word.nature==Nature.n&&t==0){
//	        		filterList.add(word.word);
//	        		t++;
//	        	}
//	        	else if(word.nature==Nature.n&&word.word.length()>2)
//	        		filterList.add(word.word);
//	        	else if(word.nature==Nature.a&&i+1<cutWords.size()&&cutWords.get(i+1).nature==Nature.n){
//	        		filterList.add(word.word+cutWords.get(i+1).word);
//	        		i++;
//	        	}
//	        }
//	        List<String> termList=new ArrayList<String>();
//			termList.addAll(courseTerms.keySet());
//			DecideSubject ds=new DecideSubject();
//	        List<String> list=ds.DecideByMap(termList, question, course,"");
//	        list=RemoveDuplicate.removeContians(list, question, courseTerms, course, "");
////	        filterList.addAll(list);
//	        filterList=RemoveDuplicate.remove(filterList);
//	        
//	        for(int i=0;i<filterList.size();i++){
//	        	if(!filterList.get(i).equals("")&&!list.contains(filterList.get(i)))
//	        		queryString+="FILTER CONTAINS(str(?value),'"+filterList.get(i)+"').\n";
//	        }
//	        if(list.size()==1) queryString+="FILTER CONTAINS(str(?subjectlabel),'"+list.get(0)+"').\n";
//	        
//	        
//	        queryString+="}";
//	        if(queryString.contains("FILTER")&&filterList.size()>2&&list.size()==1){
//		        System.out.println(queryString);
//		   
//		        GraphQuery graphQuery = connection.prepareGraphQuery(QueryLanguage.SPARQL, queryString); 
//		        GraphQueryResult result = graphQuery.evaluate();
//		        int j=0;
//		        while (result.hasNext()) { // iterate over the result
//		        	Statement statement = result.next();
//		            String subject=statement.getSubject().stringValue();
//		            String predicate=statement.getPredicate().stringValue();
//		            String value=statement.getObject().stringValue();
//		            if(predicate.contains("label")||predicate.contains("description")||predicate.contains("common#source")) continue;
//		            
//		            subject=getLabel(subject);
//		            if(predicate.contains("description")) continue;
//		            predicate=predicate.split("#")[1];
//		            if(courseMap.get(predicate)!=null)
//	            		predicate=(String) courseMap.get(predicate);
//	            	if(commonMap.get(predicate)!=null)
//	            		predicate=(String) commonMap.get(predicate);
//		            resultObject.put(j, subject+"--"+predicate+":<br>"+value);
//		            j++;
//		        }
//		        result.close();
//	
//		        closeConnection();
//	        }
//	        
//	        return resultObject;
//	}
//	
//	
//
//	public String ExctratAnswer(String course,String question,String maxCommonStr,String maxStr){
//		
//		Map<String, Nature> wordMap=WordParser.splitWordandNature(WordParser.CutWord(question));
//		String title="";
//    	if(StringProcess.CountNumber(maxStr, "\n")>5||maxStr.length()>1000||maxStr.contains("相关链接")||maxStr!=null&&!maxStr.equals("")&&((maxCommonStr.length()==4&&!maxCommonStr.endsWith("的")&&!maxCommonStr.startsWith("的"))||maxCommonStr.length()>4)){
//        	
//    		title=maxStr.substring(0,maxStr.indexOf(":")+1);
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
//        		String[] arraySplit=maxStr.split("。");
//        		if((maxCommonStr.length()>10||maxStr.length()>400)&&arraySplit.length>1){
//	        		for(int i=0;i<arraySplit.length;i++){
//	        			if(arraySplit[i].contains(maxCommonStr)){
//	        				maxStr=arraySplit[i]+"（非直接查询出的结果，经后续处理得到的答案）";
//	        				break;
//	        			}
//	        		}
//        		}
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
//        	maxStr=maxStr.replaceAll("（如图\\d－\\d+）", "").replaceAll("（\\d）", "").replaceAll("\\d）", "");
//        }
//        else if(!wordMap.containsValue(Nature.cc))
//        	maxStr="";
//    	if(maxStr!=null&&!maxStr.equals("")&&!maxStr.contains(title))
//    		maxStr=title+maxStr;
//    	
//    	return maxStr;
//    }
//	
//    
//    public String searchByChemicalFormula(String ChemicalFormula){
//    	
//    	String label="";
//    	REPOSITORY="chemistry";
//    	try {
//			initConnection();
//			String queryString = "SELECT ?label WHERE { ?subject <http://www.w3.org/2000/01/rdf-schema#label> ?label ."
//					+ "?subject <http://edukb.org/knowledge/0.1/property/chemistry#ChemicalFormula> '"+ChemicalFormula+"'.} ";
//	    	
//	    	TupleQuery tupleQuery = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
//	        TupleQueryResult result = tupleQuery.evaluate();
//	        while (result.hasNext()) { 
//	            BindingSet bindingSet = result.next();
//	            Value valueOfX = bindingSet.getValue("label");
//	            label=valueOfX.stringValue();
//	        }
//	        result.close();
//
//	        closeConnection();
//	        
//		} catch (RepositoryException | MalformedQueryException | QueryEvaluationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//    	return label;
//    }
//    
//
//    public static boolean isUrl(String str){
//    	
//    	boolean isurl=false;
//    	
//    	String regex = "http://(.*)?";
//    	
//    	 Pattern pattern = Pattern.compile(regex);
//         Matcher Url = pattern.matcher(str);
//         if(Url.find()){
//        	 isurl=true;
//         }
//    	return isurl;
//    }
//
//}
