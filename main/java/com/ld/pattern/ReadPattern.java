package com.ld.pattern;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.*;

import org.python.antlr.ast.keyword;
import org.w3c.dom.*;
import org.xml.sax.*;

import com.ld.*;
import com.ld.IO.Property.ReadProperty;
import com.ld.IO.Term.SelectTerms;
import com.ld.model.Tamplate;

/** 
 * 项目名称:  KnowledgeQA  
 * 类名称:   ReadPattern
 * 类描述:   读取学科所有模板
 * 创建人:   ludan
 * 创建时间:  2017年8月21日 下午2:58:47
 *      
 */  
public class ReadPattern {
		
	private static Document doc = null;
	
	public static void init(String xmlFile) throws Exception {   
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();   
		DocumentBuilder db = dbf.newDocumentBuilder();   
		doc = db.parse(new File(xmlFile));   
	}
	
	/**从数据库的表coursetemplate中读取course学科的所有模板
	 * @param course
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static List<Tamplate> readPattern(String course) throws ClassNotFoundException, SQLException{
		
		List<Tamplate> tamplateList=new ArrayList<Tamplate>();
		
		Connection cn;  
		Class.forName("com.mysql.jdbc.Driver");
		cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
		
	  	Statement stmt=cn.createStatement();
		String sql="select * from "+course+"_template order by pattern_id;";
		System.out.println(sql);

		ResultSet results=stmt.executeQuery(sql);
			
		while(results.next()){
			Tamplate t=new Tamplate();
			boolean subject;
			boolean value;
			
			int id = results.getInt("pattern_id"); 
			String content=results.getString("content");			
			int subjectInt=results.getInt("subject");
			if(subjectInt==0)subject=false;
			else subject=true;
			int valueInt=results.getInt("value");
			if(valueInt==0)value=false;
			else value=true;
			String type=results.getString("type");
			String myclass=results.getString("class");
			String usage=results.getString("usage");
			int priority=results.getInt("priority");
			
			t.setPatternID(id);
			t.setContent(content);
			t.setSubject(subject);
			t.setValue(value);
			t.setType(type);
			t.setMyclass(myclass);
			t.setUsage(usage);
			t.setPriority(priority);
			
			tamplateList.add(t);
		}
		cn.close();
		
		return tamplateList;
	}

	/**
	 * 将模板插入至数据库的模板表中
	 * @param content
	 * @param course
	 * @param group
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static boolean insert(String content,String course,String group) throws ClassNotFoundException, SQLException{
		
		  Connection cn;  
		  Class.forName("com.mysql.jdbc.Driver");
		  cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
		  
			Statement stmt=cn.createStatement();
			String sql="insert into chinese_pattern (`content`, `course`, `group`) values ('"+content+"','"+course+"','"+group+"');";
			System.out.println(sql);

			boolean results=stmt.execute(sql);
			cn.close();
			
			return results;
	}
	
	public static void delete(String course){
		
		  Connection cn;  
		  try {
			Class.forName("com.mysql.jdbc.Driver");
				cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			  
				Statement stmt=cn.createStatement();
				String selSql="select * from "+course+"_template;";		
				
				ResultSet results=stmt.executeQuery(selSql);
				
				String type="";
				String sql;
		        
		        while(results.next()) {
//		            label = results.getString("label");  
		            type=results.getString("type"); 
		            int id=results.getInt("pattern_id");
		            if(id>=8304	&&id<=8431		){
		            	sql="delete from "+course+"_template where type='"+type+"';";
		            	stmt.addBatch(sql);
		            }
		        }
		        stmt.executeBatch();
		        
		        cn.close();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	}
	
	public static void update(String course){
		
		  Connection cn;  
		  try {
			Class.forName("com.mysql.jdbc.Driver");
				cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
				Map<String, String> terms=SelectTerms.getMapBySql(course);
				Statement stmt=cn.createStatement();
				String selSql="select * from "+course+"_template;";		
				
				ResultSet results=stmt.executeQuery(selSql);
				
				String type="";
				String sql;
				String label="";
		        int id;
		        while(results.next()) {
//		            label = results.getString("label");  
		            type=results.getString("type");  
		            id=results.getInt("pattern_id");
		            label=ReadProperty.selectLabelByUri(course, type);
		            if(id>1000&&label.length()==2){
		            	sql="update "+course+"_template set priority=3 where pattern_id="+id+";";
		            	stmt.addBatch(sql);
		            }
//		            else if(id>1000&&label.length()>1){
//		            	sql="update "+course+"_template set priority=2 where pattern_id="+id+";";
//		            	stmt.addBatch(sql);
//		            }
		        }
		        stmt.executeBatch();
		        
		        cn.close();
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	}
	
	public static void level1(String xmlFile) throws Exception{
		
		List<Tamplate> list=new ArrayList<Tamplate>();
		init(xmlFile);
		
		NodeList patternList = doc.getElementsByTagName("pattern"); 
		
		
			for (int i = 0; i < patternList.getLength(); i++) {
			   
			   Element pattern = (Element) patternList.item(i);
			   
			   Tamplate tamplate = new Tamplate();
			   
			   NodeList contentNode = pattern.getElementsByTagName("content");
			   Element e = (Element) contentNode.item(0);
			   Node t = e.getFirstChild();
			   String content=t.getNodeValue();
			   
			   NodeList courseNode = pattern.getElementsByTagName("course");
			   e = (Element) courseNode.item(0);
			   t = e.getFirstChild();
			   String course=t.getNodeValue();
			   
			   NodeList groupNode = pattern.getElementsByTagName("group");
			   e = (Element) groupNode.item(0);
			   t = e.getFirstChild();
			   String group=t.getNodeValue();
			   
			   boolean flag=insert(content,course,group);
			   if(flag){
				   System.out.println("插入成功");
			   }
			   
			}
	}
	
	public static void main(String[] args) throws Exception{
			
//		String path="D:\\Documents\\Workspace\\KnowledgeQA\\resources\\pattern\\Chinese.xml";
//		level1(path);
		
		delete("geo");
	}
	
}
