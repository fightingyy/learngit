package com.ld.action;

import java.io.PrintWriter;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.struts2.ServletActionContext;

import com.ld.answer.AnswerQuestion;
import com.opensymphony.xwork2.ActionSupport;

/** 
 * 项目名称:  KnowledgeQA  
 * 类名称:   AnswerService
 * 类描述:   提供问答接口
 * 创建人:   ludan
 * 创建时间:  2017年8月29日 下午5:00:10
 *      
 */  
public class AnswerService extends ActionSupport {

	private String inputQuestion;
	private String course;
	private JSONArray resultArray;
	
	
	HttpServletRequest request = ServletActionContext.getRequest();
	HttpServletResponse response=ServletActionContext.getResponse();

	public String execute() throws Exception{
 
		String inputQuestion=getInputQuestion();
		String course=getCourse();

		
		if(!inputQuestion.equals("")&&inputQuestion!=null){

			AnswerQuestion answerQuestion=new AnswerQuestion(inputQuestion,course);
	    	resultArray=answerQuestion.getResultArray();
		}
		
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		
        out.println(resultArray);
		System.out.println("result:"+resultArray.toString());

		return 	null;
	}
	
	public JSONObject TransObject(JSONObject resultSet){
		
		JSONArray resultArray=new JSONArray();
		JSONObject resultObject=new JSONObject();

		Iterator iterator=resultSet.keys();
        
		while (iterator.hasNext()) {
        	String key = (String) iterator.next();
        	if(key.equals("attention")) continue;
        	String item =resultSet.getString(key);
        	
        	if(item.contains("--")&&item.contains(":")){
        		JSONObject cellJsonObject=new JSONObject();
        		item=item.replaceAll("<br>", "");
        		String subject=item.substring(0,item.indexOf("--"));
        		String predicate=item.substring(item.indexOf("--")+2,item.indexOf(":"));
        		String value=item.substring(item.indexOf(":")+1);
        		
        		cellJsonObject.put("subject", subject);
        		cellJsonObject.put("predicate", predicate);
        		cellJsonObject.put("value", value);
        		resultArray.add( cellJsonObject);
        	}
        	if(key.equals("subject")){
        		String[] array=item.split("\\|");
        		for(String ref:array){
        			JSONObject cell=new JSONObject();
        			cell.put("label", ref);
        			cell.put("uri", "");
        			resultObject.put("referenceSubject", cell);
        		}
        	}
        }
        resultObject.put("result", resultArray);
		return resultObject;
	}
	
	public String getCourse() {
		return course;
	}

	public String getInputQuestion() {
		return inputQuestion;
	}
	public void setInputQuestion(String inputQuestion) {
		this.inputQuestion = inputQuestion;
	}

	public void setCourse(String course) {
		this.course = course;
	}

	/**
	 * @return the result
	 */
	public JSONArray getResult() {
		return resultArray;
	}

}
