package com.ld.QuestionGeneration;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.http.HTTPRepository;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.ld.IO.Term.AddTerm;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class GetInstanceProperty {
	private static String SERVER = "http://166.111.7.170:18080/openrdf-sesame/";

    public static String url="jdbc:virtuoso://10.1.1.11:1111/charset=GB2312/log_enable=2";


    public static JSONArray getInstanceandPropertyofClass(String server, String course) throws IOException, RepositoryException, MalformedQueryException, QueryEvaluationException {
       
    	VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");
    	String fromStr="";
    	switch (course) {
		case "chinese": fromStr="from <http://edukb.org/chinese> from <http://edukb.org/chinese_chengyu> from <http://edukb.org/chinese_zidian> from <http://edukb.org/chinese_cidian> from <http://edukb.org/chinese_gushiwen>";break;
		case "geo": fromStr="from <http://edukb.org/geo> from <http://edukb.org/geo_baidu> from <http://edukb.org/geo_wiki> from <http://edukb.org/geo_china_pedia> from <http://edukb.org/geonames>";break;
		default: fromStr="from <http://edukb.org/"+course+">";
			break;
		}
    	
        String querybyClass = "select distinct ?classUri ?className "+fromStr+"  where " +
                "{" +
                "?classUri <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Class>." +
                "?classUri <http://www.w3.org/2000/01/rdf-schema#label> ?className}";
        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (querybyClass, set);
		ResultSet resultClass = vqe.execSelect();

        String classUri, className;
        JSONArray jsonArray = new JSONArray();//总的array
        int i=0;
        while (resultClass.hasNext()) {
        	
            JSONObject jsonObject = new JSONObject();//每一个Class的object

            QuerySolution rsc = resultClass.nextSolution();
            className = rsc.get("className").toString();
            classUri = rsc.get("classUri").toString();
            jsonObject.put("class", className);
            
//            array_unique((LinkedList<String>) listInstance, subInstance);

            String queryforInstancebyClass = "select distinct ?subInstance ?instanceName ?propertyName from <"+fromStr+"> where" +
                    "{" +
                    "?subInstance <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#NamedIndividual>." +
                    "?subInstance <http://www.w3.org/2000/01/rdf-schema#label> ?instanceName." +
                    "?subInstance <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" +
                    classUri +
                    ">." +
                    "?subInstance ?property ?value."+
                    "?property <http://www.w3.org/2000/01/rdf-schema#label> ?propertyName"+
                    "} ";
            vqe = VirtuosoQueryExecutionFactory.create (queryforInstancebyClass, set);
    		ResultSet resultforInstancebyClass = vqe.execSelect();

            String subInstance, instanceName,propertyName;
            List<String> listInstance = new LinkedList<String>();
            List<String> listProperty = new LinkedList<String>();
            
            while (resultforInstancebyClass.hasNext()) {
            	
                QuerySolution qs = resultforInstancebyClass.nextSolution();
                subInstance = qs.get("subInstance").toString();
                instanceName = qs.get("instanceName").toString();
                if(subInstance.contains("，")||subInstance.contains(" ")){
                	System.out.println(subInstance);
                	continue;
                }
                propertyName = qs.get("propertyName").toString();
                if(instanceName.equals(className)) continue;
                array_unique((LinkedList<String>) listInstance, instanceName);
                
                String queryforObjectPropertybyInstance = "select distinct ?propertyName from <"+fromStr+"> WHERE { <"+subInstance+"> ?property ?o ."
//                		+ "?s <http://www.w3.org/2000/01/rdf-schema#label> \"" + instanceName + "\" ."
                		+ "?property <http://www.w3.org/2000/01/rdf-schema#label> ?propertyName."
                		+ " }";
                vqe = VirtuosoQueryExecutionFactory.create (queryforObjectPropertybyInstance, set);
        		ResultSet resultforObjectPropertybyInstance = vqe.execSelect();
                i++;
                String PropertyName;
                while (resultforObjectPropertybyInstance.hasNext()) {

                    QuerySolution rsp = resultforObjectPropertybyInstance.nextSolution();
                    PropertyName = rsp.get("propertyName").toString();
                    if(PropertyName.equals("标注")||PropertyName.equals("分类")||PropertyName.equals("等同")||PropertyName.equals("部分于")||PropertyName.equals("出处")||PropertyName.equals("类型")||PropertyName.contains("相关于")||PropertyName.equals("下属于")) continue;
                    if(className.equals("人物")&&PropertyName.equals("包含")) continue;
                    array_unique((LinkedList<String>) listProperty, PropertyName);
                }
//
//                String queryforDatatypePropertybyInstance = "select distinct ?datatypePropertyName where" +
//                        "{" +
//                        "?datatypeProperty <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DatatypeProperty>." +
//                        "?datatypeProperty <http://www.w3.org/2000/01/rdf-schema#label> ?datatypePropertyName." +
//                        "<" +
//                        subInstance +
//                        "> ?objectProperty ?obj." +
//                        "} ";
//                TupleQuery tupleQueryforDatatypePropertybyInstance = connection.prepareTupleQuery(QueryLanguage.SPARQL, queryforDatatypePropertybyInstance);
//                TupleQueryResult resultfordatatypePropertybyInstance = tupleQueryforDatatypePropertybyInstance.evaluate();
//
//                String  datatypePropertyName;
//                while (resultfordatatypePropertybyInstance.hasNext()) {
//
//                    BindingSet bindingSetProperty = resultfordatatypePropertybyInstance.next();
//                    datatypePropertyName = bindingSetProperty.getValue("datatypePropertyName").stringValue();
//                    array_unique((LinkedList<String>) listDatatypeProperty, datatypePropertyName);
//                }

            }
            jsonObject.put("className", className);
            jsonObject.put("instance", listInstance);
            jsonObject.put("property", listProperty);

            jsonArray.add(jsonObject);
        }
        System.out.println(jsonArray.toString());
        
        StringBuffer buffer = new StringBuffer(); 		

    	buffer = new StringBuffer(); 

    	buffer.append(jsonArray.toString());
    	//生成每个类型下的所有实例和属性的json文件
    	Writer write = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("D:\\TestSet\\"+course+"\\"+course+".json"), "UTF-8")); 
        write.write(buffer.toString()); 
        write.flush(); 

        write.close();
        
        return jsonArray;
    }


    //去除数组中重复的记录
    public static void array_unique(LinkedList<String> list, String a) {
        if (!list.contains(a)) {
            list.add(a);
        }
    }

    /**
     * 将毫秒数转成具体的时间值
     */
    public static String formatTime(long mss) {
        long days = mss / (1000 * 60 * 60 * 24);
        long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (mss % (1000 * 60)) / 1000;
        return days + " days " + hours + " hours " + minutes + " minutes " + seconds + " seconds ";
    }

    public static void main(String[] args) throws RepositoryException, QueryEvaluationException, MalformedQueryException, IOException {
        long startTime = System.currentTimeMillis();
       
        List<String> courseList=new ArrayList<String>();
//		courseList.add("chinese");
//		courseList.add("history");
//		courseList.add("english");		
//		courseList.add("physics");
//		courseList.add("chemistry");
//		courseList.add("biology");
//		courseList.add("politics");
		courseList.add("geo");
		
		String course="";
		for(int i=0;i<courseList.size();i++){
			course=courseList.get(i);
	        String repo = course;
	        getInstanceandPropertyofClass(SERVER, repo);
		}

        long endTime = System.currentTimeMillis();
        System.out.println("=======================================================");
        System.out.println("Time used: " + formatTime(endTime - startTime));
        System.out.println("=======================================================");
    }
}
