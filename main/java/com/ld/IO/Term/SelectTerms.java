package com.ld.IO.Term;

import java.io.*;
import java.sql.*;
import java.util.*;

/** 
 * 项目名称:  KnowledgeQA  
 * 类名称:   SelectTerms
 * 类描述:   查询实例表
 * 创建人:   ludan
 * 创建时间:  2017年8月29日 上午10:10:07
 *      
 */  
public class SelectTerms {
	

	/**
	* 获取文件中的所有学科实例
	* @param fileName
	* @return
	*/
	public static Map<String,String> getMap(String fileName){

		Map<String,String> term=new HashMap<String,String>();  
        try {
        	File file = new File(fileName);   
            InputStreamReader reader = new InputStreamReader(new FileInputStream(file),"UTF-8"); 
            BufferedReader br = new BufferedReader(reader); 
            String line = ""; 
//			line = br.readLine();
			while ((line = br.readLine()) != null) {  

	            String[] array=null;
	            if(line.contains("##")){
		            array=line.split("##");
		            if(array.length>1)
		            	term.put(array[0], array[1]);
		            else if(array.length==1)
		            	term.put(array[0], null);
	            }
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return term;
  
	}
	
	/**
	* 获取数据库中的学科实例表
	* @param course
	* @return
	*/
	public static Map<String,String> getMapBySql(String course){

		Map<String,String> term=new HashMap<String,String>();  
        try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection cn= DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			
			Statement stmt=cn.createStatement();
			String sql="select * from "+course+"_terms;";
			
			ResultSet results=stmt.executeQuery(sql);
			while (results.next()) {  

			    String termStr=results.getString("term");
			    String regex=results.getString("regex");
			    term.put(termStr,regex);
			}
			stmt.close();
			cn.close();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		 
		return term;
  
	}
	
	
	/**
	 * 查询属于关系属性的属性
	 * @param course
	 * @return
	 */
	public static List<String> getTypeBySql(String course){

		List<String> termList=new ArrayList<String>();  
        try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection cn= DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			
			Statement stmt=cn.createStatement();
			String sql="select * from "+course+"_terms where isType=1;";
			
			ResultSet results=stmt.executeQuery(sql);
			while (results.next()) {  

			    String termStr=results.getString("term");
			    termList.add(termStr);
			}
			stmt.close();
			cn.close();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		 
		return termList;
  
	}
	
	/**
	 * 从数据库实例表查询实例的别名
	 * @param course
	 * @return
	 */
	public static Map<String,String> getAltLabel(String course){

		Map<String,String> term=new HashMap<String,String>();  
        try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection cn= DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			
			Statement stmt=cn.createStatement();
			String sql="select * from "+course+"_terms;";
			
			ResultSet results=stmt.executeQuery(sql);
			while (results.next()) {  

			    String termStr=results.getString("term");
			    String altlabel=results.getString("altlabel");
			    if(altlabel!=null&&!altlabel.equals(""))
			    	term.put(termStr,altlabel);
			}
			stmt.close();
			cn.close();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		 
		return term;
  
	}
}
