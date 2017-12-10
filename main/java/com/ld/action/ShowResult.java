package com.ld.action;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.struts2.ServletActionContext;
import org.jsoup.select.Elements;

import net.sf.json.JSONObject;

import com.ld.IO.TestCompare;
import com.ld.IO.File.GetFiles;
import com.ld.IO.Term.AddTerm;
import com.opensymphony.xwork2.ActionSupport;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：ShowResult   
* 类描述：  在页面上显示测试结果列表
* 创建人：ludan   
* 创建时间：2017年6月2日 上午11:17:07   
* @version        
*/
public class ShowResult extends ActionSupport {

	private String course;
	
	private String list;
	
	public String execute(){
		
		String course=getCourse();
		String path=ServletActionContext.getRequest().getRealPath("/");
		
		if(path.contains("\\.metadata"))
			path=path.split("\\.metadata")[0]+"KnowledgeQA/";
		else if(path.contains("WEB-INF")){
			path=path.split("WEB-INF")[0];
		}
		String path1 = path.split("testResult.jsp")[0];
		path1 = path1.substring(0, path1.length()-1);
		System.out.println(path1);
		path=path.split("testResult.jsp")[0]+"resources/TestResults/"+course;
		
		//获取测试结果列表
		List<String> fileList=GetFiles.getFileList(path);
		Collections.sort(fileList);
	    
		JSONObject result=new JSONObject();
	    result.put("list", fileList);
	    result.put("path",path1);
	     
	    list=result.toString();
	    
		return SUCCESS;
	} 

	
	
	public String getCourse() {
		return course;
	}
	public void setCourse(String course) {
		this.course = course;
	}
	public String getList() {
		return list;
	}
	public void setList(String list) {
		this.list = list;
	}
	
}
