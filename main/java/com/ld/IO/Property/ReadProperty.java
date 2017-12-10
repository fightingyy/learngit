package com.ld.IO.Property;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.hankcs.hanlp.corpus.tag.Nature;
import com.ld.Parser.WordParser;
import com.ld.model.Label;

public class ReadProperty {
	

	/**
	* 从excel中读取学科属性
	* @param fileName
	* @return
	* @throws IOException
	* @throws ClassNotFoundException
	* @throws SQLException
	*/
	public static Map<String,String> readPropertyByExcel(String fileName) throws IOException, ClassNotFoundException, SQLException{
		
		Map<String,String> map=new HashMap<String,String>();
        @SuppressWarnings("resource")
		XSSFWorkbook xwb = new XSSFWorkbook(fileName);
        
        XSSFSheet sheet = xwb.getSheetAt(0);
        
        XSSFRow row;
        String label;
        String uri;
        String course;
        
        for (int i = 28; i<sheet.getPhysicalNumberOfRows(); i++) {
            
        	    row = sheet.getRow(i);
                label = row.getCell(1).toString();
                course=row.getCell(3).toString().split("#")[0];
                uri=row.getCell(3).toString().split("#")[1].toLowerCase();
//                System.out.println(label);
                map.put(uri, label);
        }
        
        return map;
        
	}
	
	/**
	* 从excel中读取公共属性
	* @param fileName
	* @return
	* @throws IOException
	* @throws ClassNotFoundException
	* @throws SQLException
	*/
	public static Map<String,String> readCommon(String fileName) throws IOException, ClassNotFoundException, SQLException{
		
		Map<String,String> map=new HashMap<String,String>();
        @SuppressWarnings("resource")
		XSSFWorkbook xwb = new XSSFWorkbook(fileName);
        
        XSSFSheet sheet = xwb.getSheetAt(0);
        
        XSSFRow row;
        String label;
        String uri;
        String course;
        
        for (int i = 1; i<28; i++) {
            
        	    row = sheet.getRow(i);
                label = row.getCell(1).toString();
                course=row.getCell(3).toString().split("#")[0];
                uri=row.getCell(3).toString().split("#")[1].toLowerCase();
//                System.out.println(label);
                map.put(uri, label);
        }
        
        return map;
        
	}
	
	
	/**
	* 从学科属性表中查询出所有的属性
	* @param course
	* @return
	* @throws IOException
	* @throws ClassNotFoundException
	* @throws SQLException
	*/
	public static Map<String,String> selectProperty(String course) throws IOException, ClassNotFoundException, SQLException{
		
		Map<String,String> map=new HashMap<String,String>();
		Class.forName("com.mysql.jdbc.Driver");		
		Connection cn= DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
		
		Statement stmt=cn.createStatement();
		
		String sql="select * from "+course+"_property;";
		ResultSet results=stmt.executeQuery(sql);
        
        while(results.next()) {

                String label = results.getString("label");
                String uri=results.getString("uri");
                if(uri.equals("wordSource")&&course.equals("chinese")) uri="wordSoure";
                map.put(uri, label);
        }
        stmt.close();
        cn.close();
        return map;
        
	}
	
	/**
	* 根据uri查询属性的label
	* @param course
	* @param uriList
	* @return
	*/
	public static List<String> selectLabelList(String course, List<String> uriList){
		
		List<String> labelList=new ArrayList<String>();
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
			Connection cn= DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			
			Statement stmt=cn.createStatement();
			
			for(String uri:uriList){
			
				String sql="select * from "+course+"_property where uri='"+uri+"';";
				ResultSet results=stmt.executeQuery(sql);
				String label ="";
		        
		        while(results.next()) {
		                label = results.getString("label");   
		                labelList.add(label);
		        }
			}
			stmt.close();
	        cn.close();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

		return labelList;
	}

	/**
	 * 确定属性是不是关系属性
	 * @param course
	 * @param type
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static boolean isObject(String course,String type) throws IOException, ClassNotFoundException, SQLException{
			
			boolean isObject=false;
			Class.forName("com.mysql.jdbc.Driver");		
			Connection cn= DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			
			Statement stmt=cn.createStatement();
			
			String sql="select isobject from "+course+"_property where uri='"+type+"';";
			ResultSet results=stmt.executeQuery(sql);
	        
	        while(results.next()) {
	
	                int objectint = results.getInt("isobject");
	                if(objectint==1)
	                	isObject=true;
	                else isObject=false;
	        }
	        stmt.close();
	        cn.close();
	        
	        return isObject;
	}
	
	/**
	* 根据uri查询属性的label
	* @param course
	* @param uriList
	* @return
	*/
	public static String selectLabelByUri(String course, String uri){
		
		String label="";
		String temp="";
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
			Connection cn= DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			
			Statement stmt=cn.createStatement();
			
			String sql="select * from "+course+"_property where uri='"+uri+"';";
			ResultSet results=stmt.executeQuery(sql);
	        
	        while(results.next()) {
	        	temp=results.getString("uri");
	        	if(temp.equals(uri))
	        		label = results.getString("label");  	                
	        }
	        if(label==null||label.equals("")){
	        	sql="select * from Common_property where uri='"+uri+"';";
				results=stmt.executeQuery(sql);				
		        
		        while(results.next()) {
		        	temp=results.getString("uri");
		        	if(temp.equals(uri))
		        		label = results.getString("label");  
	                
		        }
	        }
	        stmt.close();
	        cn.close();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		return label;
	}

}
