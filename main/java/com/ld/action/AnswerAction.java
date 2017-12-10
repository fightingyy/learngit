package com.ld.action;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.python.antlr.PythonParser.elif_clause_return;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ld.FullSearch.FullSearch;
import com.ld.answer.AnswerQuestion;
import com.ld.model.AnswerObject;
import com.opensymphony.xwork2.ActionSupport;

/**   
*    
* 椤圭洰鍚嶇О锛欿nowledgeQA   
* 绫诲悕绉帮細AnswerAction   
* 绫绘弿杩帮細 涓庡墠绔繘琛屼氦浜掞紝灏嗛棶棰樼殑绛旀杩斿洖缁欏墠绔� 
* 鍒涘缓浜猴細ludan   
* 鍒涘缓鏃堕棿锛�2017骞�6鏈�2鏃� 涓婂崍11:12:00   
* @version        
*/
public class AnswerAction extends ActionSupport implements ServletRequestAware{

	private static final long serialVersionUID = 1L;
	private String inputQuestion;
	private String result;
	private String course;
	
	HttpServletRequest request = ServletActionContext.getRequest();
	HttpServletResponse response=ServletActionContext.getResponse();
	
	private static Logger logger = LoggerFactory.getLogger(AnswerAction.class);
	
	public AnswerAction(){
		
	}
	
	public String getInputQuestion() {
		return inputQuestion;
	}
	public void setInputQuestion(String inputQuestion) {
		this.inputQuestion = inputQuestion;
	}
	
	public String execute() throws Exception{

		JSONArray resultSet=new JSONArray();
		AnswerObject answerObject=new AnswerObject();
		
		String inputQuestion=getInputQuestion();
		String course=getCourse();
//		String subjectName="";
//		Search s=new Search();
		
		if(!inputQuestion.equals("")&&inputQuestion!=null){

			AnswerQuestion answerQuestion=new AnswerQuestion(inputQuestion,course);
	    	resultSet=answerQuestion.getResultArray();
	    	
//	    	subjectName=answerQuestion.subjectName;
//	    	subjectName=RemoveDuplicate.removeSameStr(subjectName);
		}
		else{ 
			answerObject.setMessage("请输入问题！");
			resultSet.add(answerObject);
		}
		
		String uuid = UUID.randomUUID().toString();
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		String time = df.format(new Date());//得到当前时间
		JSONObject jObject = new JSONObject();
		boolean fs = true;
		if (resultSet.size() >= 1) {
			jObject = resultSet.getJSONObject(resultSet.size()-1);
			if ((int)jObject.get("fs") == 0) {
				fs = false;
			}
		}
		if (fs && resultSet.size() > 1) {
			for (int i = 0; i < resultSet.size()-1; i++) {
				JSONObject jsonObject = resultSet.getJSONObject(i);
				String subject = (String) jsonObject.get("subject");
				String predicate = (String) jsonObject.get("predicate");
				double score = (double) jsonObject.get("score");
				String value = (String) jsonObject.get("value");
				if (value.indexOf("\n") != -1) {
					value = value.replaceAll("\n", "");
				}
				String tamplate = (String) jsonObject.get("tamplateContent");	
				String fsanswer = (String) jObject.get("fsanswer");
					
				logger.debug("id:"+uuid+" time:"+time+" question:"+inputQuestion+" value:"+value+" subject:"+subject+" predicate:"+predicate+" score:"+score+" template:"+tamplate+" fsanswer:"+fsanswer);
			}
			
		}
		else if (fs && resultSet.size() == 1) {
			JSONObject jsonObject = resultSet.getJSONObject(0);
			String subject = "";
			String predicate = "";
			double score = 0.0;
			String value = (String) jsonObject.get("value");
			if (value.indexOf("\n") != -1) {
				value = value.replaceAll("\n", "");
			}
			String tamplate = (String) jsonObject.get("tamplateContent");	
			String fsanswer = (String) jObject.get("fsanswer");
				
			logger.debug("id:"+uuid+" time:"+time+" question:"+inputQuestion+" value:"+value+" subject:"+subject+" predicate:"+predicate+" score:"+score+" template:"+tamplate+" fsanswer:"+fsanswer);
		}
		else if (!fs) {
			for (int i = 0; i < resultSet.size(); i++) {
				JSONObject jsonObject = resultSet.getJSONObject(i);
				String subject = (String) jsonObject.get("subject");
				String predicate = (String) jsonObject.get("predicate");
				double score = (double) jsonObject.get("score");
				String value = (String) jsonObject.get("value");
				if (value.indexOf("\n") != -1) {
					value = value.replaceAll("\n", "");
				}
				String tamplate = (String) jsonObject.get("tamplateContent");
				String fsanswer = "";
					
				logger.debug("id:"+uuid+" time:"+time+" question:"+inputQuestion+" value:"+value+" subject:"+subject+" predicate:"+predicate+" score:"+score+" template:"+tamplate+" fsanswer:"+fsanswer);
			}
			
		}
		
		result=resultSet.toString();
		System.out.println(result);
		//logger.info("top N result:"+resultSet.toString());

		return SUCCESS;
	}
	
	public String getResult() {
		return result;
	}


	public void setResult(String result) {
		this.result = result;
	}

	@Override
	public void setServletRequest(HttpServletRequest arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public String getCourse() {
		return course;
	}


	public void setCourse(String course) {
		this.course = course;
	}
}
