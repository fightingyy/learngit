package com.ld.answer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonFormat.Value;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hankcs.hanlp.HanLP;
import com.ld.FullSearch.FullSearch;
import com.ld.IO.CourseChange;
import com.ld.IO.remove.RemoveDuplicate;
import com.ld.Util.MapUtil;
import com.ld.Util.Sort;
import com.ld.model.AnswerObject;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：PreciseAnswer   
* 类描述： 对结果的后处理去重、排序
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:29:00   
* @version        
*/
public class PreciseAnswer {
	private static Logger logger = LoggerFactory.getLogger(PreciseAnswer.class);

	/**
	 * 答案的进一步抽取
	 * @param firstResult
	 * @param question
	 * @param course
	 * @param usageList
	 * @param path
	 * @return
	 */
	public static JSONArray Precise(JSONArray firstResult,String question,String course,List<String> usageList,String path){
		
		JSONArray resultSet=new JSONArray();
		FullSearch fullSearch=new FullSearch();
		
		ObjectMapper mapper=new ObjectMapper();
		
		boolean isContinue=true;
		for(int i=0;i<firstResult.size();i++) {
        	
			AnswerObject ansObj=new AnswerObject();
    		JSONObject jsonObject=(JSONObject) firstResult.get(i);
    		
			try {
				ansObj=mapper.readValue(jsonObject.toString(), AnswerObject.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    
			AnswerObject answerObject=new AnswerObject();
        	String item=ansObj.getValue();
        	answerObject=ansObj;
        	String predicate=ansObj.getPredicate();

        	//抽取英语单词词性
        	if(course.equals("english")&&usageList.contains("cixing")&&predicate.equals("解释")){
        		
        		String cixing="";
        		if(item.contains(".:"))
        			cixing=item.substring(0,item.indexOf(".:"));
        		else continue;
        		switch(cixing){
        		case "prep":cixing="介词";break;
        		case "pron":cixing="代名词";break;
        		case "n":cixing="名词";break;
        		case "v":cixing="动词";break;
        		case "conj":cixing="连接词";break;
        		case "s":cixing="主词";break;
        		case "sc":cixing="主词补语";break;
        		case "o":cixing="受词";break;
        		case "oc":cixing="受词补语";break;
        		case "vi ":cixing="不及物动词";break;
        		case "vt ":cixing="及物动词";break;
        		case "aux.v":cixing="助动词";break;
        		case "a":cixing="形容词";break;
        		case "adv":cixing="副词";break;
        		case "art":cixing="冠词";break;
        		case "num":cixing="数词";break;
        		case "int":cixing="感叹词";break;
        		case "u":cixing="不可数名词";break;
        		case "c":cixing="可数名词";break;
        		case "pl":cixing="复数";break;
        		case "int.":cixing="语气词";break;
        		case "abbr.":cixing="缩写词";break;
        		}
        		answerObject.setPredicate("词性");
        		
        		answerObject.setValue(cixing);
        		resultSet.add(answerObject);
        		i++;
        	}
        	//根据全文检索在百度正文中抽取结果
        	else if(usageList.contains("text")&&predicate.equals("百度正文")&&isContinue){
        		List<String> list=Arrays.asList(RemoveDuplicate.CheckNull(item.split("。")));
        		fullSearch.getSimilarAnswer(course, question, path, list);
            	AnswerObject ansObject=fullSearch.getAnswer();
            	ansObj.setValue(ansObject.getValue());
            	ansObj.setScore(ansObject.getScore()-5);
            	if(ansObj.getScore()>=5){
            		resultSet.add(ansObj);
            		isContinue = false;
            	}
        	}
        	else if (usageList.contains("text")&&predicate.equals("百度正文")&&!isContinue) {
				
			}
        	else{
        		resultSet.add(ansObj);
        	}
        }
		
		if(resultSet.isEmpty())
			resultSet=firstResult;
		
		return resultSet;
	} 
	
	/**
	 * 对最终结果去重和排序
	 * @param resultArray
	 * @return
	 */
	public static JSONArray  removeObject(JSONArray resultArray,boolean flag)   { 
	    
		JSONArray result=new JSONArray();
		List<String> uniList=new ArrayList<String>();
		
		Map<String, List<AnswerObject>> map=new HashMap<String, List<AnswerObject>>();
		Map<String,Double> sortMap=new HashMap<String,Double> ();
		ObjectMapper mapper=new ObjectMapper();

		List<AnswerObject> uniqueList=new ArrayList<AnswerObject>();

		for(int i=0;i<resultArray.size();i++) {
        	
			AnswerObject ansObj=new AnswerObject();
    		JSONObject jsonObject=(JSONObject) resultArray.get(i);
    		
			try {
				ansObj=mapper.readValue(jsonObject.toString(), AnswerObject.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}           	
        	String subject=ansObj.getSubject();
        	String predicate=ansObj.getPredicate();
        	String subjectUri=ansObj.getSubjectUri();
        	
        	String temp=subject+predicate+subjectUri;
        	if(predicate.equals("人口")&&ansObj.getValue().equals("0")) continue;
        	
        	//合并主语谓语相同的三元组
        	if(map.isEmpty()||map.get(temp)==null){
        		uniqueList.add(ansObj);
        		
        		map.put(temp, uniqueList);
        		uniqueList=new ArrayList<AnswerObject>();
        	}
        	else if(map.get(temp)!=null){
        		uniqueList=map.get(temp);
        		uniqueList.add(ansObj);
        		
        		map.put(temp, uniqueList);
        		uniqueList=new ArrayList<AnswerObject>();
        	}
		}
		double maxScore=0;
		AnswerObject newAnsObject=new AnswerObject();
		int i=0;
		if(flag) map=MapUtil.sortByKey(map);
		
		int count=0;
		//对结果进行去重
		for(List<AnswerObject> ansList:map.values()){
			String value="";
			maxScore=0;
			ansList=MapUtil.sortObject(ansList);
			for(AnswerObject answerObject:ansList){
				newAnsObject=answerObject;
				if(!answerObject.getValue().equals("")&&!value.contains(answerObject.getValue())&&!answerObject.getValue().startsWith("http://")){
					
					value+="<br>"+answerObject.getValue()+"<br>";
				}
				else if((!answerObject.getValue().equals("")&&!value.contains(answerObject.getValue())&&answerObject.getValue().startsWith("http://"))||answerObject.getSubject().contains("非直接查询出的结果，经后续处理得到的答案")){
					result.add(newAnsObject);
					sortMap.put(i+"", answerObject.getScore());
					i++;
					continue;
				}
				
				double score=answerObject.getScore();
				String filterStr=answerObject.getFilterStr();
				if(!filterStr.equals("")&&answerObject.getValue().equals(filterStr)&&!answerObject.getPredicate().equals("解释")&&!answerObject.getPredicate().equals("中文意思")){
					score=filterStr.length()+score;
				}
				if(maxScore<score) maxScore=score;
			}
			if(!value.equals("")){
				newAnsObject.setValue(value);
				newAnsObject.setScore(maxScore);
				sortMap.put(i+"", maxScore);
				result.add(newAnsObject);
				i++;
			}
		}
		
		//对答案进行排序
		if(!flag)
			sortMap=Sort.sortMapByDoubleValue(sortMap);
		
		resultArray=new JSONArray();

		maxScore=0;
		JSONArray jsonArray=new JSONArray();
		String tempValue="";
		String tempUri="";
		for(String keyStr:sortMap.keySet()){
			JSONObject object=(JSONObject) result.get(Integer.parseInt(keyStr));
			double score=object.getDouble("score");
			String temp=object.getString("subject")+object.getString("predicate")+object.getString("value");
			jsonArray.add(object);
			
			int n=1;
			//筛选出得分为topN的答案

			if(count<=n){
				if(!uniList.contains(temp)){
					if(object.getString("predicate").equals("强相关于")&&sortMap.size()>1) continue;
					if(maxScore==0){
						count++;
						maxScore=score;
					}
					else if (score<maxScore) {
						count++;
					}
					if(tempUri.equals("")&&!tempValue.equals("")&&object.getString("value").replaceAll("<br>", "").contains(tempValue)){
						uniList.add(temp);
						resultArray.add(object);
						tempValue=object.getString("value");
					}
					if(count>n) continue;
					uniList.add(temp);
					resultArray.add(object);
					tempValue=object.getString("value").replaceAll("<br>", "");
					tempUri=object.getString("subjectUri");
				}
				else continue;
			}
			
		}
		//logger.info("All result:"+jsonArray.toString());
		if(flag)resultArray=jsonArray;
		
		return resultArray;
	}
	public static void main(String[] args){
		String sen="我国陆地大部分位于什么带?";
		System.out.println(HanLP.segment(sen));
		List<String> keywordList = HanLP.extractKeyword(sen, 3);
		System.out.println(keywordList.toString());
		
	}
}
