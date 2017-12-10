package com.ld.answer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.ld.IO.Convert;
import com.ld.IO.FirstAndLast;
import com.ld.IO.Property.ReadProperty;
import com.ld.IO.remove.RemoveDuplicate;
import com.ld.Parser.WordParser;
import com.ld.model.AnswerObject;

/** 
 * 项目名称:  KnowledgeQA  
 * 类名称:   DecideAnswer
 * 类描述:   答案选取
 * 创建人:   ludan
 * 创建时间:  2017年8月22日 下午5:08:41
 *      
 */  
public class DecideAnswer {
	
	Map<String, Double> map=new HashMap<String, Double>();
	Map<String, Integer> itemCountMap=new HashMap<String, Integer>();
	static List<Nature> natureList=new ArrayList<Nature>();
	JSONArray decideArray=new JSONArray();
	
	public DecideAnswer(){
		
	}
	static{

      natureList.add(Nature.rys);
      natureList.add(Nature.ry);
      natureList.add(Nature.ryt);
      natureList.add(Nature.ryv);
      natureList.add(Nature.ude1);
	}

	/**
	* 从3种查询方式的结果中选取出最佳的一个结果
	* @param course
	* @param subjectFilterArray
	* @param subjectGraphArray
	* @param subjectToValueArray
	* @param question
	* @param typeList
	* @return
	*/
	public JSONArray decide(String course,JSONArray subjectFilterArray,JSONArray subjectGraphArray,JSONArray subjectToValueArray,String question,List<String> typeList){
		
		JSONArray finalResult=new JSONArray();
	 
		typeList=ReadProperty.selectLabelList(course, typeList);
		
		if(!subjectFilterArray.isEmpty()){
			decideByRegex(subjectFilterArray, question,typeList,course);
		}
		
		if(!subjectToValueArray.isEmpty()){
			decideByRegex(subjectToValueArray, question,typeList,course);
		}
		
		if(!subjectGraphArray.isEmpty()){
			decideByRegex(subjectGraphArray, question,typeList,course);
		}
		finalResult=decideArray;
		
		return finalResult;
	}

	/**
	* 根据问题的正则表达式给答案评分
	* @param jsonArray
	* @param question
	* @param typeList
	* @param course
	*/
	public void decideByRegex(JSONArray jsonArray,String question,List<String> typeList,String course){

		Pattern pattern=null;
		java.util.regex.Matcher matcher;
		
		Convert.convertLabel(question,course);
		String cLabel=Convert.clabel;
		String template=QuestionToRegex(question);
		List<Term> CutWord=WordParser.CutWord(question);
		ObjectMapper mapper=new ObjectMapper();
		
		for(int i=0;i<jsonArray.size();i++) {
        	
			AnswerObject ansObj=new AnswerObject();
    		JSONObject jsonObject=(JSONObject) jsonArray.get(i);
    		
			try {
				ansObj=mapper.readValue(jsonObject.toString(), AnswerObject.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
        	
        	String item=ansObj.getValue();
        	String predicate=ansObj.getPredicate();
        	
        	if(predicate.equals("类型")||predicate.equals("相关于")||predicate.equals("下属于")) continue;
        	double score=0;
        	int icount=0;
        	
        	//根据答案出现的次数评分
        	if(itemCountMap.containsKey(item)){
        		icount=itemCountMap.get(item);
        	}
        	icount++;
        	if(item.length()>1000) icount--;
        	if(icount>=3)
        		icount=3;
        	itemCountMap.put(item, icount);
        	
        	if(template!=null&&!template.equals("")&&template.contains("(.*)?")){
        		pattern=Pattern.compile(template);
            	matcher=pattern.matcher(item);
            	if(matcher.find()){
            		score=1;
            		map.put(item, score);
            	}
        	}
        	
        	
    		int count=0;
        	String title2=ansObj.getSubject();
        	
        	if(cLabel!=null&&!cLabel.equals("")&&title2.equals(cLabel))
        		count=count+2;
        	
        	//根据模板识别的谓语评分
        	for(int t=0;t<typeList.size();t++){
        		if(predicate.equals(typeList.get(t))&&item.length()<600)
        			count++;
        	}
        	
        	score+=(double)count/10;
        	if(item.length()>600&&score>=0.2) score=score-0.2;
        	
        	ansObj.setScore(score);
        	decideArray.add(ansObj);
        
        }
	}
	
	/**
	* 根据时间确定最早最晚这类问题的答案
	* @param bindingName
	* @param subjectLabel
	* @param rs
	* @param results
	* @return
	*/
	public static List<FirstAndLast> processTime(String bindingName,String subjectLabel,QuerySolution rs,ResultSet results){
		
		List<FirstAndLast> subjectList=new ArrayList<FirstAndLast>();
		
		//获取开始实例的开始时间
		if(bindingName.equals("begintime")&&rs.get(bindingName)!=null){
    	
        	String begintime=rs.get(bindingName).toString();
        	FirstAndLast subjectsort=new FirstAndLast(subjectLabel,begintime);
        	
        	subjectList.add(subjectsort);
    	}
		
		
    	if(results.getResultVars().size()>2){
    		bindingName = results.getResultVars().get(2).toString();
        	if(bindingName.equals("shijian")&&rs.get(bindingName)!=null){
            	String begintime=rs.get(bindingName).toString();
            	FirstAndLast subjectsort=new FirstAndLast(subjectLabel,begintime);
            	
            	subjectList.add(subjectsort);
        	}
    	}
    	
		return subjectList;
	}

	/**
	* 英语汉译英确定准确答案
	* @param filterStr
	* @param results
	* @param ansObj
	* @return
	*/
	public static AnswerObject decideEnlishAnswer(String filterStr,ResultSet results,AnswerObject ansObj){
		
		boolean isAnswer=false;	
		boolean flag=false;
		
		//每个单词可能有多个解释，将每个解释分割开
		String[] array=ansObj.getValue().replaceAll("（(.*?)）", "").split("；|:|【|】|；|（|）|\\(|\\)");
		String[] filterArray=filterStr.split("\\|");
		if(array.length==1) array=ansObj.getValue().split("，");
		array=RemoveDuplicate.CheckNull(array);
		List<String> splistList=Arrays.asList(array);
		
		splistList=RemoveDuplicate.removeCharacter(splistList);
		
		//若单词中的某个解释与问题所问的一致，就认为是正确答案
		if(filterArray.length==1){		
			if(!splistList.contains(filterArray[0])&&!splistList.contains(filterArray[0]+"的")){
				
	    		if(splistList.get(0).contains(":")){
	    			String item=splistList.get(0).substring(splistList.get(0).indexOf(":")+1);	            			
	    			if(item.equals(filterArray[0])||item.equals(filterArray[0]+"的"))
	    				isAnswer=true;
	    		}			
			}
			else isAnswer=true;
		}
		else if (filterArray.length>1) {
			for(String answer:splistList){
				if(flag) break;
				for(String item:filterArray){
					if(!answer.contains(item)){
						flag=false;
						break;
					}
					else flag=true;
				}
			}
			isAnswer=flag;
		}
		
		
		if(isAnswer)
			return ansObj;
		else 
			return null;
	}
	
	/**
	* 将问题转化为正则表达式
	* @param question
	* @return
	*/
	public String QuestionToRegex(String question){
		
		String regex="";
		List<Term> CutWord=WordParser.CutWord(question);
		
		String template=question.replace("?", "").replace("？", "");
		for(Term word:CutWord){
			if(word.nature==Nature.ry||word.nature==Nature.rys||word.nature==Nature.ryt||word.nature==Nature.ry){
				template=template.replace(word.word, "(.*)?");	
			}
			else if(word.word.equals("，")||word.word.equals(",")||word.word.equals("、")||word.nature==Nature.ude1)
				template=template.replaceAll(word.word, ".");
		}
		return regex;
	}

}

