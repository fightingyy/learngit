package com.ld.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import ruc.irm.similarity.sentence.morphology.SemanticSimilarity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jspsmart.upload.File;
import com.ld.IO.MaxString;
import com.ld.IO.remove.RemoveDuplicate;
import com.ld.answer.AnswerQuestion;
import com.ld.model.AnswerObject;

public class TestQuestion {

	int process;
	private String answer = "";

	public TestQuestion() {
		// TODO Auto-generated constructor stub
	}

	public void TestExcel(String course,File excel){
		
	}
	
	public boolean isRight(String course,JSONArray result, AnswerQuestion answerQuestion, String ExcelAnswer,SemanticSimilarity semanticSimilarity){
		
		boolean flag=false;
		
		String items="";
    	String title="";
    	
    	int tag=0;
    	int count=0;
    	ObjectMapper mapper=new ObjectMapper();
    	int arrayCount=ExcelAnswer.split("、").length;
    	List<String> answerSplitList=Arrays.asList(ExcelAnswer.split("、"));
    	List<String> ExcelanswerList=Arrays.asList(ExcelAnswer.split("\\|"));
    	String answerList="";
    	String[] array=null;
    	String[] array1=null;
    	for(int i=0;i<result.size();i++) {
        	
			AnswerObject ansObj=new AnswerObject();
    		JSONObject jsonObject=(JSONObject) result.get(i);
    		
			try {
				ansObj=mapper.readValue(jsonObject.toString(), AnswerObject.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}   
        	items=ansObj.getValue();
        	title=ansObj.getSubject();
        	String predicate=ansObj.getPredicate();
        	String message=ansObj.getMessage();
        	String triple=title+predicate+items;
        	if(triple.equals("")) {
        		if(!message.equals(""))setAnswer(message);
        		continue;
        	}
        	
        	if(items.equals("此问题在知识库中没有直接的答案，以下是可能的答案")) continue;
        	array=null;
        	array1=null;
        	boolean flag1=false;
        	String answerTemp=items.replaceAll("\\s*", "").replaceAll(" +", "").replaceAll("\t", "").replaceAll("<br>", "");
        	title=title.replaceAll(" ", "").replace("（非直接查询出的结果，经后续处理得到的答案）", "");
        	if(answerTemp.contains("返回▲"))
        		answerTemp=answerTemp.substring(0, answerTemp.indexOf("返回▲")+3);
        	answerTemp=answerTemp.replace("（非直接查询出的结果，经后续处理得到的答案）", "").replace("(节选)", "");
        	
        	ExcelAnswer=ExcelAnswer.replaceAll("\\s*|\t|\r|\n", "").replaceAll(" +", "").replaceAll("<br>", "");
        	answerList+=items;
        	if(course.equals("math")){
        		answerTemp=answerTemp.replaceAll("&lt;", "<").replaceAll("&gt;", ">");
        		array=ExcelAnswer.split(";");       	
        		
    			for(int j=0;j<array.length;j++){
    				
    					
					String[] brs=items.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("\\s*", "").split("<br>");
					
					brs=RemoveDuplicate.CheckNull(brs);
					for(int t=0;t<brs.length;t++){     			
						if(!Arrays.asList(array).contains(brs[t])){
    						flag=false;
    						break;
    					}
						else if(array[j].equals(brs[t].replaceAll("\\s*", "")))
		    				flag1=true;
					}
    			}
    			
//        		if(items.contains("<br>")){
//        			String[] brs=items.split("<br>");
//					brs=RemoveDuplicate.CheckNull(brs);
//					for(int t=0;t<brs.length;t++){     			
//		    			
//		    			if(ExcelAnswer.equals(brs[t].replaceAll("\\s*", "")))
//		    				flag1=true;
//					}
//        		}
        	}
        	else{
        		String commonStr=MaxString.getMaxString(ExcelAnswer, answerTemp);
        		if(answerTemp.equals(triple.replaceAll("<br>", "")))
        			flag=true;
        		else if (commonStr.length()>9&&Math.abs(commonStr.length()-ExcelAnswer.length())<2&&answerTemp.length()>6) {
        			flag=true;
				}
        		else if(answerTemp.equals(ExcelAnswer)||title.equals(ExcelAnswer)||ExcelanswerList.contains(title))
	        		flag1=true;	
	        	else if(ExcelAnswer.contains("、")&&answerSplitList.contains(title)){
	        		count++;
	        	}	   
	        	else if(items.indexOf("<br>")!=items.lastIndexOf("<br>")){

	        		String[] brs=items.split("<br>");
	        		brs=RemoveDuplicate.CheckNull(brs);
	        		for(int j=0;j<brs.length;j++){     			
		    			
		    			if(ExcelAnswer.equals(brs[j].replaceAll("\\s*", "")))
		    				flag1=true;

		    		}
	        	}
	        	if(course.equals("english")&&!ExcelAnswer.contains("、")&&!flag1){
	        		List<String> splitAns=Arrays.asList(items.split(":|：|；|，|<br>"));
	        		if(splitAns.contains(ExcelAnswer))
	        			flag1=true;	
	        	}
	        	if(ExcelAnswer.contains("、")&&!flag1) {
	        		array=ExcelAnswer.split("、");
	        	}
	        	else if(ExcelAnswer.contains("|")&&!flag1) 
	        		array1=ExcelAnswer.split("\\|");
		    	if(array!=null){
		    		for(int j=0;j<array.length;j++){
		    			if(j==10)break;
		    			if(array[j].contains("|")) array[j]=array[j].substring(array[j].indexOf("|")+1);
		    			if(items.contains(array[j])||triple.contains(array[j]))
		    				flag1=true;
		    			else{
		    				flag1=false;
		    				break;
		    			}
		    		}	
		    	}
		    	if(array1!=null)
					for(int j=0;j<array1.length;j++){
		    			if(answerTemp.equals(array1[j]))
		    				flag1=true;
		    		}
        	}
        	if(flag1)
    	    	flag=true;
	    	if(!flag&&count>0&&count==arrayCount)
	    		flag=true;
	    	
	    	if(items!=null){
	    		if(ansObj.getPredicate().equals("")&&!items.equals(""))
	    			setAnswer(getAnswer()+items+"<br>");
	    		else if(items.equals("")&&!title.equals("")){
	    			setAnswer(getAnswer()+ansObj.getSubject()+"<br>");
	    		}
	    		else setAnswer(getAnswer() + ansObj.getSubject()+"--"+ansObj.getPredicate()+":"+items+"<br>");
    	    	if(items.contains("（非直接查询出的结果，经后续处理得到的答案）")&&flag)
    	    		process++;
    	    	tag++;
    	    }
        }
    	if(!answerList.equals("")&&array!=null&&array.length>1&&!flag){
    		boolean flag2=true;
    		for(int j=0;j<array.length;j++){
    			if(!answerList.replaceAll(" ", "").contains(array[j])){
    				flag2=false;
    				break;
    			}
    		}
    		flag=flag2;
    	}
        
	    return flag;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

}
