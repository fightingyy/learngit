/**  
* @Title: ShowAction.java  
* @Package com.ld.action  
* @Description: TODO(用一句话描述该文件做什么)  
* @author abc  
* @date 2017年11月26日  
* @version V1.0  
*/ 
package com.ld.action;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionSupport;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/** 
 * 项目名称:  KnowledgeQA  
 * 类名称:   ShowAction
 * 类描述:   
 * 创建人:   abc
 * 创建时间:  2017年11月26日 下午11:27:57
 *      
 */

public class ShowAction extends ActionSupport implements ServletRequestAware {
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LoggerFactory.getLogger(AnswerAction.class);
	
	HttpServletRequest request = ServletActionContext.getRequest();
	HttpServletResponse response=ServletActionContext.getResponse();

	public String execute() throws Exception{
        request.setCharacterEncoding("UTF-8");//服务器上编码;
		String path = "F:/logs";
		File file=new File(path);
		File[] fileList = file.listFiles();
		JSONArray jsonArray = new JSONArray();
		for (int i = 0; i < fileList.length; i++) {
			if (fileList[i].isFile()) {
				FileReader fr=new FileReader(fileList[i]);
		        BufferedReader br=new BufferedReader(fr);
		        String inputLine;
				StringBuffer content = new StringBuffer();
				while ((inputLine = br.readLine()) != null) {
					if (inputLine.contains("used hownet wordsimilarity")) {
						inputLine = br.readLine();
						if (inputLine == null) {
							break;
						}
					}
					String id = inputLine.substring(inputLine.indexOf("id")+3, inputLine.indexOf("time")-1);
					String time = inputLine.substring(inputLine.indexOf("time")+5, inputLine.indexOf("question")-1);
					String question = inputLine.substring(inputLine.indexOf("question")+9, inputLine.indexOf("value")-1);
					String value = inputLine.substring(inputLine.indexOf("value")+6, inputLine.indexOf("subject")-1);
					value = value.replaceFirst("<br>", "");
					//value = value.replaceAll("<br>", " ");
					String subject = inputLine.substring(inputLine.indexOf("subject")+8, inputLine.indexOf("predicate")-1);
					String predicate = inputLine.substring(inputLine.indexOf("predicate")+10, inputLine.indexOf("score")-1);
					String score = inputLine.substring(inputLine.indexOf("score")+6, inputLine.indexOf("template")-1);
					String template = inputLine.substring(inputLine.indexOf("template")+9, inputLine.indexOf("fsanswer")-1);
					String fsanswer = inputLine.substring(inputLine.indexOf("fsanswer")+9);
					//String str = template.replace("<", "&lt;");
					//str = str.replace(">", "&gt;");
					//template = str;
					//template = str.replace("&", "&amp;");
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("id", id);
					jsonObject.put("time", time);
					jsonObject.put("question", question);
					jsonObject.put("value", value);
					jsonObject.put("subject", subject);
					jsonObject.put("predicate", predicate);
					jsonObject.put("score", score);
					jsonObject.put("template", template);
					jsonObject.put("fsanswer", fsanswer);
					jsonArray.add(jsonObject);
				}
			}
		}
		
	    request.setAttribute("result", jsonArray); 
		return SUCCESS;
	
	}
	/* (non-Javadoc)
	 * @see org.apache.struts2.interceptor.ServletRequestAware#setServletRequest(javax.servlet.http.HttpServletRequest)
	 */
	@Override
	public void setServletRequest(HttpServletRequest request) {
		// TODO Auto-generated method stub

	}

}
