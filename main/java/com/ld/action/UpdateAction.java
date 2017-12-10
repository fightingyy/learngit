package com.ld.action;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.servlet.ServletContext;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts2.ServletActionContext;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import net.sf.json.JSONObject;

import com.ld.IO.File.ReadFile;
import com.ld.IO.Property.PropertyJson;
import com.ld.IO.Term.AddTerm;
import com.ld.pattern.AddPattern;
import com.ld.search.VirtuosoSearch;
import com.opensymphony.xwork2.ActionSupport;

/** 
 * 项目名称:  KnowledgeQA  
 * 类名称:   UpdateAction
 * 类描述:   更新数据
 * 创建人:   ludan
 * 创建时间:  2017年8月30日 上午10:57:22
 *      
 */  
public class UpdateAction extends ActionSupport {

	private String data;
	List<String> courseList=new ArrayList<String>();
	private String course;
	
	Connection cn;
	
	public UpdateAction(){
		
		courseList.add("chinese");
		courseList.add("history");
		 courseList.add("geo");
		 courseList.add("english");
//		 courseList.add("math");
		 courseList.add("chemistry");
		 courseList.add("biology");
		 courseList.add("politics");
		 courseList.add("physics");
		 courseList.add("Common");
	}
	
	/**
	* 查询各学科的术语和模板数量
	* @return
	*/
	public String show(){
		
		JSONObject resultObject=new JSONObject();
		
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			Statement stmt=cn.createStatement();
			
			for(int i=0;i<courseList.size();i++){
				
				JSONObject courseObject=new JSONObject();
				String course=courseList.get(i);
				if(course.equals("chinese")){
					
				}
				String sql1="select count(pattern_id) from "+course+"_template;";
				ResultSet result1=stmt.executeQuery(sql1);
				int patternCount=0;
				while(result1.next()){
					patternCount=result1.getInt(1);
					courseObject.put("patternCount", patternCount);
				}
				
				if(course.equals("Common"))
					courseObject.put("termCount", "\\");
				else if(course.equals("chinese")){
					int termCount=0;

					String path=ServletActionContext.getRequest().getSession().getServletContext().getRealPath("");
					if(path.contains("\\.metadata"))
						path=path.split("\\.metadata")[0]+"KnowledgeQA/";
					else if(path.contains("WEB-INF")){
						path=path.split("WEB-INF")[0];
					}
					
					String file=path+"resources/Terms/chineseTerms.txt";
					
					termCount=ReadFile.readLineCount(file);
					courseObject.put("termCount", termCount);
				}
				else{
					String sql2="select count(id) from "+course+"_terms;";
					ResultSet result2=stmt.executeQuery(sql2);
					int termCount=0;
					while(result2.next()){
						termCount=result2.getInt(1);
						courseObject.put("termCount", termCount);
					}
				}
				String temp="";
				switch(course){
				case "chinese": temp="语文";break;
				case "geo": temp="地理";break;
				case "math": temp="数学";break;
				case "english": temp="英语";break;
				case "chemistry": temp="化学";break;
				case "history": temp="历史";break;
				case "biology": temp="生物";break;
				case "politics": temp="政治";break;
				case "physics":temp="物理";break;
				default:temp="通用";break;
				}
				resultObject.put(temp, courseObject);
			}
			data=resultObject.toString();			
			cn.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		
		return SUCCESS;
	}
	/**
	* 学科中英文名转换
	* @param item
	* @return
	*/
	public static String change(String item) {
		String course="";
		
		switch(item){
		case "语文": course="chinese";break;
		case "地理": course="geo";break;
		case "数学": course="math";break;
		case "英语": course="english";break;
		case "化学": course="chemistry";break;
		case "历史": course="history";break;
		case "生物": course="biology";break;
		case "政治": course="politics";break;
		case "物理":course="physics";break;	
		case "通用":course="Common";break;	
		}
		
		return course;
		
	}
	
	
	/**
	* 更新术语表
	* @return
	*/
	public String updateTerm(){
		
		String course=change(getCourse());
		ServletContext sct= ServletActionContext.getServletContext();
		String path=sct.getRealPath("/");
		
		if(path.contains("\\.metadata"))
			path=path.split("\\.metadata")[0]+"KnowledgeQA/";
		else if(path.contains("WEB-INF")){
			path=path.split("WEB-INF")[0];
		}
		
		try {
			if(course.equals("chinese")){
				AddTerm.WriteTermtoTxt(path,course);
				AddTerm.AddPoetryName();
			}
			else{
				//先删除已有的术语表
				Class.forName("com.mysql.jdbc.Driver");
				cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
				Statement stmt=cn.createStatement();
				String sql="truncate table "+course+"_terms;";
				stmt.execute(sql);
				
				//插入新的术语表
				AddTerm.searchInstance(course);
				VirtuosoSearch vSearch=new VirtuosoSearch(course);
				//添加实例的别称
				Map<String, String> map=vSearch.selectOtherName(course);
				AddTerm.addAltLabel(course, map);
				cn.close();
			}
			AddTerm.addCourseAlt();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		return SUCCESS;
	}
	/**
	* 更新模板表
	* @return
	*/
	public String updateTemplate(){
		
		//先将模板导出
		export();
		
		String course=change(getCourse());
		ServletContext sct= ServletActionContext.getServletContext();
		String path=sct.getRealPath("/");
		
		if(path.contains("\\.metadata"))
			path=path.split("\\.metadata")[0]+"KnowledgeQA/";
		else if(path.contains("WEB-INF")){
			path=path.split("WEB-INF")[0];
		}
		
		try {
//			if(!course.equals("Common")){
				
				AddPattern.insertTemplate(course,path);
				//生成新的属性json文件，用于前端展示
				PropertyJson.createJsonFile(course, path);
			
		} catch (RepositoryException | MalformedQueryException
				| QueryEvaluationException | IOException | ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return SUCCESS;
	}

	/**
	* 将模板表导出备份至Excel表
	*/
	public void export(){
		String course=change(getCourse());
		String path=ServletActionContext.getRequest().getRealPath("/");
		
		if(path.contains("\\.metadata"))
			path=path.split("\\.metadata")[0]+"KnowledgeQA/";
		else if(path.contains("WEB-INF")){
			path=path.split("WEB-INF")[0];
		}
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			Statement stmt=cn.createStatement();
			
			String sql="select * from "+course+"_template;";
			
			ResultSet results=stmt.executeQuery(sql);
			
			XSSFWorkbook wb = null;
			Sheet sheet =null;
			String ExcelPath=path+"resources/pattern/"+course+".xlsx";
			

			wb = new XSSFWorkbook();
	           
            //创建sheet对象   
            sheet = (Sheet) wb.createSheet("sheet1");  
	           
	         //创建sheet对象   
	        if (sheet==null) {
	            sheet = (Sheet) wb.createSheet("sheet1");  
	        }
	        
	        //添加表头  
	        Row row = sheet.createRow(0);
	        row.setHeight((short) 540); 
	        row.createCell(0).setCellValue("id");
            row.createCell(1).setCellValue("content");
            row.createCell(2).setCellValue("subject");
            row.createCell(3).setCellValue("value");
            row.createCell(4).setCellValue("type");
            row.createCell(5).setCellValue("class");
            row.createCell(6).setCellValue("usage");
            row.createCell(7).setCellValue("priority");
	        


	        //循环写入行数据    
	        int i=1;
	        while(results.next()){
				int id=results.getInt("pattern_id");
				String content=results.getString("content");
				int subjectInt=results.getInt("subject");
				int valueInt=results.getInt("value");
				String type=results.getString("type");
				String myclass=results.getString("class");
				String usage=results.getString("usage");
				int priority=results.getInt("priority");
				
				boolean subject,value;
				if(subjectInt==0)subject=false;
				else subject=true;
				
				if(valueInt==0)value=false;
				else value=true;
				
				row = (Row) sheet.createRow(i);  
	            row.setHeight((short) 500); 
	            row.createCell(0).setCellValue(String.valueOf(id));
	            row.createCell(1).setCellValue(content);
	            row.createCell(2).setCellValue(subject);
	            row.createCell(3).setCellValue(value);
	            row.createCell(4).setCellValue(type);
	            row.createCell(5).setCellValue(myclass);
	            row.createCell(6).setCellValue(usage);
	            row.createCell(7).setCellValue(String.valueOf(priority));
				
	            i++;
			}
	        
	        //创建文件流   
	        FileOutputStream stream = new FileOutputStream(ExcelPath);  
	        //写入数据   
	        wb.write(stream);  
	        //关闭文件流   
	        stream.flush();
	        stream.close(); 
			
	        wb.close();
			cn.close();
			
		} catch (ClassNotFoundException | SQLException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getCourse() {
		return course;
	}

	public void setCourse(String course) {
		this.course = course;
	}
}
