package com.ld.IO;

import java.util.List;

import com.ld.model.AnswerObject;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：subjectSort   
* 类描述： 
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:34:02   
* @version        
*/
public class FirstAndLast {
	
	private String subject;
	private String predicate;
	private String value;
	private String begintime;
	
	public FirstAndLast(){
		
	}
	
	public FirstAndLast(String subject,String begintime){
		
		this.subject=subject;
		this.begintime=begintime;
	}
	
	
	/**
	* 历史根据时间回答最早最晚这类问题
	* @param subjectList
	* @param usage
	* @return
	*/
	public AnswerObject getSort(List<FirstAndLast> subjectList,String usage){
		
		AnswerObject answerObject=new AnswerObject();
		FirstAndLast subjects=new FirstAndLast();
		
		int max=-100;
		
		for(FirstAndLast s:subjectList){
			
			int time=0;
			String begintime=s.getBegintime();
			if(begintime.contains("公元"))
				begintime=begintime.substring(begintime.indexOf("公元")+2);
			if(begintime.contains("年"))
				begintime=begintime.substring(0,begintime.indexOf("年"));
			if(begintime.contains("前")){
				begintime=begintime.substring(begintime.indexOf("前")+1);
				time=-Integer.parseInt(begintime);
			}
			else time=Integer.parseInt(begintime);
			if(usage.equals("first")){
				if(time<max){
					max=time;
					subjects=s;
				}
			}
			else if(usage.equals("last")){
				
				if(time>max){
					max=time;
					subjects=s;
				}
			}
		}
		answerObject.setSubject(subjects.getSubject()+"（非直接查询出的结果，经后续处理得到的答案）");
		answerObject.setScore(10);
		
		return answerObject;
		
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getBegintime() {
		return begintime;
	}
	public void setBegintime(String begintime) {
		this.begintime = begintime;
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
