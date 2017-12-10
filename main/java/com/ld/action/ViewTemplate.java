package com.ld.action;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.json.JSONObject;

import org.apache.struts2.ServletActionContext;
import org.w3c.dom.Document;

import com.opensymphony.xwork2.ActionSupport;

/** 
 * 项目名称:  KnowledgeQA  
 * 类名称:   ViewTemplate
 * 类描述:   根据属性筛选模板
 * 创建人:   ludan
 * 创建时间:  2017年8月28日 下午12:36:22
 *      
 */  
public class ViewTemplate extends ActionSupport {
	
	private static final long serialVersionUID = 1L;
	private String result;
	private String course;
	private String type;
	Connection cn;
	
	HttpServletRequest request = ServletActionContext.getRequest();

	/**
	* 筛选出属性对应的所有模板
	* @return
	* @throws Exception
	*/
	public String ViewTemplate() throws Exception{
		
		String course=getCourse();
		
		JSONObject resultObject=new JSONObject(); 
		
		Class.forName("com.mysql.jdbc.Driver");		
		cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
		
		Statement stmt=cn.createStatement();
		String sql="select * from "+course+"_template where type='"+type+"';";
		
		ResultSet results=stmt.executeQuery(sql);
		int i=0;
		while (results.next()){
			
			String content=results.getString("content");
			resultObject.put(i, content);
			i++;
		}
		this.result=resultObject.toString();
		System.out.println(result);
		cn.close();
		return SUCCESS;
	}
	

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getCourse() {
		return course;
	}

	public void setCourse(String course) {
		this.course = course;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
