package com.ld.IO.Property;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.ld.IO.Term.AddTerm;


/*根据每个学科属性的excel生成json文件*/
/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：PropertyJson   
* 类描述： 属性json文件的生成读取
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:41:39   
* @version        
*/
public class PropertyJson {

	public static String url="jdbc:virtuoso://166.111.68.66:1111/charset=GB2312/log_enable=2";
	
	/**
	* 读取Excel文件中的数据只json数组
	* @param fileName
	* @return
	* @throws IOException
	*/
	public static JSONArray getFileJson(String fileName) throws IOException {  

        XSSFWorkbook xwb = new XSSFWorkbook(fileName);
        
        XSSFSheet sheet = xwb.getSheetAt(0);
        
        XSSFRow row;
       
        String key="";
        String value="";
        JSONArray jsonArray = new JSONArray();
        
        for (int i = 28; i<sheet.getPhysicalNumberOfRows(); i++) {

        	JSONObject celljson=new JSONObject();
    	    row = sheet.getRow(i);
    	    key = row.getCell(3).toString().split("#")[1];
    	    value=row.getCell(1).toString();
    	    
    	    celljson.put("text", value); 
    	    celljson.put("val", key);
    	    jsonArray.add(celljson);
    	    
        }
        xwb.close();
  
        return jsonArray;  
    }  
	
	/**
	 * 生成json文件
	 * @param course
	 * @param path
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static void createJsonFile(String course,String path) throws IOException, ClassNotFoundException, SQLException{
//		String path="D:\\Documents\\Workspace\\KnowledgeQA\\";
		StringBuffer buffer = new StringBuffer(); 		

    	buffer = new StringBuffer(); 
		
		JSONArray cell=getJsonData(course);
    	buffer.append(cell.toString());
//		write = new FileWriter(new File(path+"js//"+course+".json"));  
//    	OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(path+"//WebContent//js//"+course+".json", true),"UTF-8"); 
    	Writer write = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path+"src/main/webapp/js/"+course+".json"), "UTF-8")); 
        write.write(buffer.toString()); 
        write.flush(); 

        write.close();
	}
	
	/**
	 * 读取属性的json文件
	 * @param course
	 * @param path
	 * @return
	 */
	@SuppressWarnings("resource")
	public static JSONObject readJsonFile(String course,String path){
		
		JSONObject jsonObject=new JSONObject();
		JSONArray jsonArray=new JSONArray();
		
		StringBuffer stringBuffer = new StringBuffer();
		String line = null ;

		BufferedReader br;
		try {
			File file=new File(path+"src/main/webapp/js/"+course+".json");
			if(file.exists()){
				br =new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8")); 
			}
			else {
				file=new File(path+"src/main/webapp/js/"+course+".json");
				br =new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8")); 
			}
					
			while( (line = br.readLine())!= null ){
				stringBuffer.append(line);
				
			} 
			
			jsonArray=JSONArray.fromObject(stringBuffer.toString());
			
			for(int i=0;i<jsonArray.size();i++){
				JSONObject cell=(JSONObject) jsonArray.get(i);
				jsonObject.put(cell.getString("val"), cell.getString("text"));
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return jsonObject;
	}
	
	/**
	 * 从知识库中查询学科属性并生成json文件,用于前端数据的展示
	 * @param course
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static JSONArray getJsonData(String course) throws IOException, ClassNotFoundException, SQLException {  

		String fromString="";
		switch (course) {
		case "chinese": fromString="from <http://edukb.org/chinese> from <http://edukb.org/chinese_chengyu> from <http://edukb.org/chinese_zidian> from <http://edukb.org/chinese_cidian> from <http://edukb.org/chinese_gushiwen> from <http://edukb.org/chinese_zuopin>";break;
		case "geo": fromString="from <http://edukb.org/geo> from <http://edukb.org/geo_textbook> from <http://edukb.org/geo_ad_baidu> from <http://edukb.org/geo_ad_wiki> from <http://edukb.org/geo_baidu> from <http://edukb.org/geo_wiki> from <http://edukb.org/geo_china_pedia> from <http://edukb.org/geo_geonames> from <http://edukb.org/geo_china_administrative_divisions> from <http://edukb.org/geo_resort> from <http://edukb.org/geo_resort_baidu> ";break;
		case "history":fromString="from <http://edukb.org/history> from <http://edukb.org/history_pedia> from <http://edukb.org/history_baidu> from <http://edukb.org/history_baidu_infobox>";break;		case "Common": fromString="from <http://edukb.org/chinese>";break;
		default: fromString="from <http://edukb.org/"+course+">";
			break;
		}
		VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");
		String queryString = "SELECT distinct ?subject ?label ?description "+fromString+" WHERE {"
				+ "?subject<http://www.w3.org/2000/01/rdf-schema#label> ?label."
				+ "?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DatatypeProperty>."
				+ "?subject <http://purl.org/dc/elements/1.1/description> ?description"
				+ "}";
		String queryString2 = "SELECT distinct ?subject ?label ?description "+fromString+" WHERE {"
				+ "?subject<http://www.w3.org/2000/01/rdf-schema#label> ?label."
				+ "?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty>."
				+ "?subject <http://purl.org/dc/elements/1.1/description> ?description"
				+ "}";
        
        
        JSONArray jsonArray = new JSONArray();
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (queryString, set);
		ResultSet result = vqe.execSelect();
		
		vqe = VirtuosoQueryExecutionFactory.create (queryString2, set);
		ResultSet result2 = vqe.execSelect();
		
		Class.forName("com.mysql.jdbc.Driver");		
		Connection cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
		
		Statement stmt=cn.createStatement();
		
		int i=0;
		String temp="";
		List<String> uniqList=new ArrayList<String>();
		while (result.hasNext()) { 
		    QuerySolution rs = (QuerySolution) result.next();
		    
		    String subject=rs.get("subject").toString();
		    String type="";
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
		    
		    String description=rs.get("description").toString();
		    
		    if(type.equals("Content")) label="内容";
		    
		    temp=type+label;
            if(uniqList.contains(type+label)) continue;
            else uniqList.add(temp);
		    
		    System.out.println(subject+"   "+label+"   "+description);
		    if(subject.contains(label)&&description!=null&&!description.equals("")) label=description.replace("geonames-", "");

		    if(subject.contains(course)||subject.contains("common")||subject.contains("demo")){
		    	
		    	JSONObject celljson=new JSONObject();
//		    	if(course.equals("chinese")&&type.equals("wordSource")) type="wordSoure";
			    celljson.put("text", label); 
			    celljson.put("val", type);
			    String sql="insert into "+course+"_property (uri,label,isobject) values ('"+type+"','"+label+"',0);";
			    stmt.addBatch(sql);
			    if(type.replaceAll("-\\d+", "").equals("")||!type.contains("-")||course.equals("biology"))
	    	    	jsonArray.add(celljson);
		    }
      }
		
		while (result2.hasNext()) { 
		    QuerySolution rs = (QuerySolution) result2.next();
		   
		    String subject=rs.get("subject").toString();
		    String type="";
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
		    
		    String description=rs.get("description").toString();
		    
		    System.out.println(subject+"   "+label+"   "+description);

	    	if(subject.contains("common")||subject.contains(course)||subject.contains("demo")){
	        	JSONObject celljson=new JSONObject();
	    	    
	    	    celljson.put("text", label); 
	    	    celljson.put("val",type );
	    	    if(type.replaceAll("-\\d+", "").equals("")||!type.contains("-")||course.equals("biology"))
	    	    	jsonArray.add(celljson);
	    	    
	    	    if(subject.contains("topicOf"))
	    	    	System.out.println(subject);
	    	    i++;
	    	    String sql="insert into "+course+"_property (uri,label,isobject) values ('"+type+"','"+label+"',1);";
	    	    stmt.addBatch(sql);
	    	}

      }
		//先删除已有的学科属性表
		String sql="truncate table "+course+"_property;";
		stmt.execute(sql);
   
		//将属性插入至mysql数据库的学科属性表
		stmt.executeBatch();
		vqe.close();
		set.close();
		
		cn.close();
  
        return jsonArray;  
    }
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException {
		String path="D:\\Documents\\Workspace\\KnowledgeQA";
		createJsonFile("Common",path);
	}

}
