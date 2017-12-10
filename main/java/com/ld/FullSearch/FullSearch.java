package com.ld.FullSearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.python.core.PyFunction;
import org.python.core.PyList;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import com.ld.IO.MaxString;
import com.ld.IO.File.PythonFile;
import com.ld.Parser.WordParser;
import com.ld.Util.Sort;
import com.ld.model.AnswerObject;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：FullSearch   
* 类描述： 全文检索
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:04:29   
* @version        
*/
public class FullSearch {
	
	public static String subject="";
	private AnswerObject ansObj=new AnswerObject();

	private static Logger logger = LoggerFactory.getLogger(FullSearch.class);
	public FullSearch() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 根据最长公共子序列得出最可能的问句主语
	 * @param course
	 * @param question
	 * @param path
	 * @param resultList
	 * @return
	 */
	public static List<String> getSimilarSubject(String course,String question,String path,List<String> resultList){
		
		List<String> subjectList=new ArrayList<String>();
		if(course.equals("history")&&resultList.size()==2) return resultList;
		
		if(resultList.size()>0){
			
			path+="resources/lcs.py";	
			
			//读取python文件
	        PyFunction func = PythonFile.getPyFuntion(path, "question_parse");

	        List<Term> wordsList=WordParser.CutWord(question);

        	Map<String, Nature> wordMap = WordParser.splitWordandNature(WordParser.CutWord(question));
        	for(Term word:wordsList){
        		if(word.nature.toString().startsWith("ry"))
        			question=question.replaceAll(word.word, "");
        	}
        	if(question.length()>2){
        		//根据最长公共子序列对每个主语进行评分
		        PyObject pyobj = func.__call__(new PyString(question), new PyList(resultList)); 

				PyObject iterObject=pyobj.__iter__();
				PyObject nextbject=null;
				int count=0;
				int size=1;
				double maxScore=0;
//				if(course.equals("history")) size=2;
				Map<String, Double> sortMap=new HashMap<String, Double>();
				//对每个主语的分值再次进行修改
				while ((nextbject=iterObject.__iternext__())!=null&&count<5) {
					
					PyObject iter=nextbject.__iter__();
					String value=iter.__iternext__().toString();					
					double score=iter.__iternext__().asDouble();
					String temp=value.replaceAll("　", "");
					String commonStr=MaxString.longestCommonSubsequence(temp.replaceAll("的", ""), question.replaceAll("的", ""));
					score=score*Math.sqrt(commonStr.length()/(double)(Math.abs(temp.length()-commonStr.length()+1)));
					sortMap.put(value, score);
				}
				
				//根据评分对结果进行排序，选出得分最高的主语
				sortMap=Sort.sortMapByDoubleValue(sortMap);
				for(Entry<String, Double> entry:sortMap.entrySet()){
					if(count>size)break;
					double score=entry.getValue();
					if(maxScore-score>0.2){
						count++;
						continue;	
					}
					if(score>=1){
						subjectList.add(entry.getKey());
						if(count==0)maxScore=score;
						count++;
					}
				}
        	}        
		}
		return subjectList;
	}

	/**
	 * 根据最长公共子序列计算全文检索的评分，并给出得分最高的结果
	 * @param course
	 * @param question
	 * @param path
	 * @param resultList
	 * @return
	 */
	public JSONArray getSimilarAnswer(String course,String question,String path,List<String> resultList){
		
		JSONArray resultArray=new JSONArray();
		
		if(resultList.size()>0){
			
			String answer="";
			path+="resources/lcs.py";	
			
			//读取python文件
	        PyFunction func = PythonFile.getPyFuntion(path, "question_parse");

	        List<String> questionSpilt=new ArrayList<String>();
	        List<Term> wordsList=WordParser.CutWord(question);
	        String nsStr="";
	        String regex=question.replaceAll("\\?", "");
	        double total=0;
	        for(Term word:wordsList){
	        	if(word.nature==Nature.ns||word.nature==Nature.nsf){
	        		nsStr=word.word;
	        		break;
	        	}
	        	else if (word.nature==Nature.nz&&word.word.endsWith("湖")) {
	        		nsStr=word.word;
	        		break;
				}
	        	if(word.nature==Nature.udeng&&question.split("等").length>1){
	        		String temp=question.split("等")[1];
	        		if(temp.length()>5&&course.equals("physics")) question=temp;
	        	}
	        	if(word.nature==Nature.ry||word.nature==Nature.rys||word.nature==Nature.ryt||word.nature==Nature.ryv){
	        		regex=regex.replaceAll(word.word, "(.*)?");
	        	}
	        }
	        
	        if(question.contains("。"))
	        	questionSpilt=Arrays.asList(question.split("。"));

	        if(questionSpilt.size()<2)
	        	questionSpilt=Arrays.asList(question.split("？"));
	        List<String> uniqList=new ArrayList<String>();
//	        String[] array=question.split("，|,|。|\\?|？");
	        for(String queString:questionSpilt){
	        	AnswerObject answerObject=new AnswerObject();
//	        	queString=queString.replaceAll("是", "").replaceAll("的", "");
	        	
	        	queString=queString.replaceAll("\\d+世纪", "").replaceAll("\\d+年代", "");
	        	Map<String, Nature> wordMap = WordParser.splitWordandNature(WordParser.CutWord(queString));
	        	if(questionSpilt.size()==1||wordMap.containsValue(Nature.ry)||wordMap.containsValue(Nature.rys)||wordMap.containsValue(Nature.ryt)||wordMap.containsValue(Nature.ryv)||queString.contains("哪")||queString.contains("谁")){
			        //根据最长公共子序列计算评分
	        		PyObject pyobj = func.__call__(new PyString(queString), new PyList(resultList)); 

					PyObject iterObject=pyobj.__iter__();
					PyObject nextbject=null;
					String maxString="";
					String maxCommonStr="";
					
					int count=0;
					double maxScore=0;
					//利用全文检索的结果与最长公共子序列选取最佳的全文检索答案
					while ((nextbject=iterObject.__iternext__())!=null&&count<10) {
						String commonStr,lcs="";						
						PyObject iter=nextbject.__iter__();
						String value=iter.__iternext__().toString();
						answer=value;
						
						answer=answer.substring(answer.indexOf(":")+1);
						if(uniqList.contains(answer)) continue;
						else uniqList.add(answer);
						
						double score=iter.__iternext__().asDouble();

						commonStr=MaxString.getMaxString(queString, answer);
						lcs=MaxString.longestCommonSubsequence(queString, answer);

						if(!answer.equals("")&&question.contains(answer)&&!answer.equals(question)&&!course.equals("geo")){
							score=(double)score/commonStr.length();
						}
						if(count==0){ 
							answer=value;
//							if(question.contains(value.split(":")[1]))
							maxScore=score;
							maxCommonStr=commonStr;
							System.out.println(value+"score:"+maxScore);
							maxString=value;
							count++;
							continue;
						}
//						String temp="";
//						if(answer.split(":").length>1) temp=answer.split(":")[1];
//						else temp=answer;
						
						if(course.equals("chemistry")&&value.contains("方程式")){
							score=score*0.5;
						}
						if((!nsStr.equals("")&&!maxString.startsWith(nsStr)&&value.contains(nsStr))||(!nsStr.equals("")&&!maxString.contains(nsStr)&&value.contains(nsStr))){
							
						}
						if(score>1&&commonStr.length()>maxCommonStr.length()&&(commonStr.length()>=4||value.contains("地位"))&&(maxString.contains("百度正文:")||(maxScore-score)<5||(commonStr.length()>10&&(maxScore-score)<100))){
							maxCommonStr=commonStr;
							maxString=value;
							maxScore=score;
						}	
						//最长公共子串相同时
						else if(commonStr.length()==maxCommonStr.length()){
							String maxLcs=MaxString.longestCommonSubsequence(question, maxString);
							if((lcs.length()-maxLcs.length())>=3&&(maxScore-score)<10){
								maxCommonStr=commonStr;
								maxString=value;
//								maxScore=score;
							}
							else if(resultList.get(0).toString().equals(value)&&(maxScore-score<20)){
								maxCommonStr=commonStr;
								maxString=value;
//								maxScore=score;
							}	
							else if(resultList.get(0).toString().equals(maxString)){
								
							}
							else if(maxString.length()>=value.length()&&commonStr.length()>5&&(maxScore-score)<6&&!value.contains("百度正文")){
								maxCommonStr=commonStr;
								maxString=value;
								maxScore=score;
							}
						}
						count++;
					}
		
					if(maxString!=null&&!maxString.equals("")&&maxScore>1){
						answer=maxString;
						String temp="";
						if(answer.contains(":")&&answer.split(":").length>1){
							temp=answer.split(":")[1];
						}
						else temp=answer;
						List<Term> cutWord=WordParser.CutWord(temp);
						if(cutWord.get(0).nature==Nature.rzv&&cutWord.get(0).word.length()==1){
							int index=resultList.indexOf(maxString);
							if(index>0)
								answer=resultList.get(index-1)+"。"+temp;
						}
						ansObj.setValue(answer);

						ansObj.setScore(maxScore);
					}
					if(answer.contains("-")&&answer.contains(":")&&maxScore>1){
						subject=answer.substring(0,answer.indexOf("-"));
						if(answer.contains("百度正文:")) maxScore=maxScore-5;
						else if(answer.contains("课本文本:")) maxScore*=0.6;
						answerObject.setSubject(subject);
						answerObject.setPredicate(answer.substring(answer.indexOf("-")+1,answer.indexOf(":")));
						answerObject.setValue(answer.substring(answer.indexOf(":")+1));
						
						//计算最终的得分，用于与模板评分的比较
						double rate=0.5;
//						String commonStr=MaxString.longestCommonSubsequence(queString, answerObject.getValue());
						if(!course.equals("geo")){
							rate=Math.sqrt((double)maxCommonStr.length()/answerObject.getValue().length());
							if(course.equals("chemistry")) rate*=0.5;
							else rate*=0.8;
//							if(rate>1) rate=(double)(Math.abs(answerObject.getValue().length()-queString.length())+1)/maxCommonStr.length();
						}
						else if(course.equals("geo")){
							rate=Math.sqrt((double)maxCommonStr.length()/answerObject.getValue().length());
							
							
							if(!nsStr.equals("")&&answer.contains(nsStr)) rate*=1.1;
							else if (!nsStr.equals("")&&!answer.contains(nsStr)) {
								rate*=0.8;
							}
						}
						maxScore*=rate;
						//多个结果将每个结果的分值都设为其中得分较高者的分值
						if(maxScore>total){
							total=maxScore;
							if(!resultArray.isEmpty()){
								JSONArray AnswerArray=new JSONArray();
								for(int t=0;t<resultArray.size();t++){
									JSONObject newObject=new JSONObject();
									newObject=(JSONObject) resultArray.get(t);
									newObject.put("score", total);
									AnswerArray.add(newObject);
								}
								resultArray.clear();
								resultArray.addAll(AnswerArray);
							}
						}
						answerObject.setScore(total);
						resultArray.add(answerObject);
					}
	        	}
	        }
		}
		return resultArray;
	}
	
//	public JSONArray getBestAnswer(String course,String question,List<String> resultList){
//		
//		JSONArray resultArray=new JSONArray();
//		
//		if(resultList.size()>0){
//			
//			String answer="";
//			
//	        List<String> questionSpilt=new ArrayList<String>();
//	        List<Term> wordsList=WordParser.CutWord(question);
//	        String nsStr="";
//	        String regex=question.replaceAll("\\?", "");
//	        double total=0;
//	        for(Term word:wordsList){
//	        	if(word.nature==Nature.ns||word.nature==Nature.nsf){
//	        		nsStr=word.word;
//	        		break;
//	        	}
//	        	else if (word.nature==Nature.nz&&word.word.endsWith("湖")) {
//	        		nsStr=word.word;
//	        		break;
//				}
//	        	if(word.nature==Nature.udeng&&question.split("等").length>1){
//	        		String temp=question.split("等")[1];
//	        		if(temp.length()>5&&course.equals("physics")) question=temp;
//	        	}
//	        	if(word.nature==Nature.ry||word.nature==Nature.rys||word.nature==Nature.ryt||word.nature==Nature.ryv){
//	        		regex=regex.replaceAll(word.word, "(.*)?");
//	        	}
//	        }
//	        
//	        if(question.contains("。"))
//	        	questionSpilt=Arrays.asList(question.split("。"));
//
//	        if(questionSpilt.size()<2)
//	        	questionSpilt=Arrays.asList(question.split("？"));
//	        List<String> uniqList=new ArrayList<String>();
////	        String[] array=question.split("，|,|。|\\?|？");
//	        for(String queString:questionSpilt){
//	        	AnswerObject answerObject=new AnswerObject();
////	        	queString=queString.replaceAll("是", "").replaceAll("的", "");
//	        	
//	        	queString=queString.replaceAll("\\d+世纪", "").replaceAll("\\d+年代", "");
//	        	Map<String, Nature> wordMap = WordParser.splitWordandNature(WordParser.CutWord(queString));
//	        	if(questionSpilt.size()==1||wordMap.containsValue(Nature.ry)||wordMap.containsValue(Nature.rys)||wordMap.containsValue(Nature.ryt)||wordMap.containsValue(Nature.ryv)||queString.contains("哪")||queString.contains("谁")){
//	        		Map<String, Double> resultMap = AnswerScore(resultList,queString);
//					String maxString="";
//					String maxCommonStr="";
//					
//					int count=0;
//					double maxScore=0;
//
//					for(Entry entry:resultMap.entrySet()){
//						String commonStr,lcs="";						
//						
//						String value=(String) entry.getKey();
//						answer=value;
//						
//						answer=answer.substring(answer.indexOf(":")+1);
//						if(uniqList.contains(answer)) continue;
//						else uniqList.add(answer);
//						
//						double score=(double) entry.getValue();
//
//						commonStr=MaxString.getMaxString(queString.replaceAll("世界上", "世界"), answer.replaceAll("世界上", "世界"));
//						lcs=MaxString.longestCommonSubsequence(queString, answer);
//
//						if(!answer.equals("")&&question.contains(answer)&&!answer.equals(question)&&!course.equals("geo")){
//							score=(double)score/commonStr.length();
//						}
//						if(count==0){ 
//							answer=value;
////							if(question.contains(value.split(":")[1]))
//							maxScore=score;
//							maxCommonStr=commonStr;
//							System.out.println(value+"score:"+maxScore);
//							maxString=value;
//							count++;
//							continue;
//						}
////						String temp="";
////						if(answer.split(":").length>1) temp=answer.split(":")[1];
////						else temp=answer;
//						
//						if(course.equals("chemistry")&&value.contains("方程式")){
//							score=score*0.5;
//						}
//						if((!nsStr.equals("")&&!maxString.startsWith(nsStr)&&value.contains(nsStr))||(!nsStr.equals("")&&!maxString.contains(nsStr)&&value.contains(nsStr))){
//							
//						}
//						if(score>1&&commonStr.length()>maxCommonStr.length()&&(commonStr.length()>=4||value.contains("地位"))&&(maxString.contains("百度正文:")||(maxScore-score)<5||(commonStr.length()>10&&(maxScore-score)<100))){
//							maxCommonStr=commonStr;
//							maxString=value;
//							maxScore=score;
//						}	
//						else if(commonStr.length()==maxCommonStr.length()){
//							String maxLcs=MaxString.longestCommonSubsequence(question, maxString);
//							if((lcs.length()-maxLcs.length())>=3&&(maxScore-score)<10){
//								maxCommonStr=commonStr;
//								maxString=value;
////								maxScore=score;
//							}
//							else if(resultList.get(0).toString().equals(value)&&(maxScore-score<20)){
//								maxCommonStr=commonStr;
//								maxString=value;
////								maxScore=score;
//							}	
//							else if(resultList.get(0).toString().equals(maxString)){
//								
//							}
//							else if(maxString.length()>=value.length()&&commonStr.length()>5&&(maxScore-score)<6&&!value.contains("百度正文")){
//								maxCommonStr=commonStr;
//								maxString=value;
//								maxScore=score;
//							}
//						}
//						count++;
//					}
//		
//					if(maxString!=null&&!maxString.equals("")&&maxScore>1){
//						answer=maxString;
//						String temp="";
//						if(answer.contains(":")&&answer.split(":").length>1){
//							temp=answer.split(":")[1];
//						}
//						else temp=answer;
//						List<Term> cutWord=WordParser.CutWord(temp);
//						if(cutWord.get(0).nature==Nature.rzv&&cutWord.get(0).word.length()==1){
//							int index=resultList.indexOf(maxString);
//							if(index>0)
//								answer=resultList.get(index-1)+"。"+temp;
//						}
//						ansObj.setValue(answer);
//
//						ansObj.setScore(maxScore);
//					}
//					if(answer.contains("-")&&answer.contains(":")&&maxScore>1){
//						subject=answer.substring(0,answer.indexOf("-"));
//						if(answer.contains("百度正文:")) maxScore=maxScore-5;
//						else if(answer.contains("课本文本:")) maxScore*=0.6;
//						answerObject.setSubject(subject);
//						answerObject.setPredicate(answer.substring(answer.indexOf("-")+1,answer.indexOf(":")));
//						answerObject.setValue(answer.substring(answer.indexOf(":")+1));
//						double rate=0.5;
////						String commonStr=MaxString.longestCommonSubsequence(queString, answerObject.getValue());
//						if(!course.equals("geo")){
//							rate=Math.sqrt((double)maxCommonStr.length()/answerObject.getValue().length());
//							if(course.equals("chemistry")) rate*=0.5;
//							else rate*=0.8;
////							if(rate>1) rate=(double)(Math.abs(answerObject.getValue().length()-queString.length())+1)/maxCommonStr.length();
//						}
//						else if(course.equals("geo")){
//							rate=Math.sqrt((double)maxCommonStr.length()/answerObject.getValue().length());
//							
//							
//							if(!nsStr.equals("")&&answer.contains(nsStr)) rate*=1.1;
//							else if (!nsStr.equals("")&&!answer.contains(nsStr)) {
//								rate*=0.8;
//							}
//						}
//						maxScore*=rate;
//						if(maxScore>total){
//							total=maxScore;
//							if(!resultArray.isEmpty()){
//								JSONArray AnswerArray=new JSONArray();
//								for(int t=0;t<resultArray.size();t++){
//									JSONObject newObject=new JSONObject();
//									newObject=(JSONObject) resultArray.get(t);
//									newObject.put("score", total);
//									AnswerArray.add(newObject);
//								}
//								resultArray.clear();
//								resultArray.addAll(AnswerArray);
//							}
//						}
//						answerObject.setScore(total);
//						resultArray.add(answerObject);
//					}
//
//	        	}
//	        }
//		}
//		return resultArray;
//	}
//	public static Map<String, Double> AnswerScore(List<String> resulstList,String question){
//		
//		Map<String, Double> resultMap=new HashMap<String, Double>();
//		String lcs="";
//		String commonStr="";
//		double score=0;
//		String temp="";
//		
//		List<Term> wordsList=WordParser.CutWord(question);
//        String nsStr="";
//        List<String> list=new ArrayList<String>();
//        
//        for(Term word:wordsList){
//        	if(word.nature==Nature.ns||word.nature==Nature.nsf){
//        		nsStr=word.word;
//        		break;
//        	}
//        	else if(word.nature==Nature.n&&word.length()>1){
//        		list.add(word.word);
//        	}
//        	else if (word.nature==Nature.nz&&word.word.endsWith("湖")) {
//        		nsStr=word.word;
//        		break;
//			}
//        	
//        }
//        
//		for(String item:resulstList){
//			score=0;
//			int count=0;
//			temp=item.replaceAll("\\-(.*)?:", "").replaceAll("(，|？|。|\n)", "");
//			
//			commonStr=MaxString.getMaxString(question.replaceAll("(，|？|。)", ""), temp);
//			lcs=MaxString.longestCommonSubsequence(question.replaceAll("(，|？|。)", ""), temp);
//			
//
//			if(lcs.length()>3){
//			
//				score = (double)lcs.length()/Math.sqrt(Math.abs(question.length()-lcs.length()));
//			    score = (double)score*lcs.length()/((MaxString.right_word_index_answer-MaxString.left_word_index_answer)+1);
//			    score = (double)score*lcs.length()*Math.sqrt(commonStr.length());
//			    
//			    for(String nString:list){
//			    	if(lcs.contains(nString)){
//			    		count++;
//			    		score=(score+nString.length())*(double)count;
//			    	}			    	
//			    }
//			    if(!nsStr.equals("")&&lcs.contains(nsStr)) 
//			    	score*=nsStr.length();
//			}
//			
//			resultMap.put(item, score);
//		}
//		
//		resultMap=Sort.sortMapByDoubleValue(resultMap);
//		
//		return resultMap;
//		
//	}
	
	/**
	* 得到最终的全文检索结果
	* @param course
	* @param question
	* @param path
	* @return
	*/
	public static JSONArray AnswerByCommonStr(String course,String question,String path){
		JSONArray resultArray=new JSONArray();
		List<String> resultList=new ArrayList<String>();
		FullSearch fullSearch=new FullSearch();
		
		//获取Elasticsearch的索引结果
		ElasticSearch eSearch=new ElasticSearch();
		question=question.replaceAll("\n", "").replaceAll("\\s", "").replaceAll("\\t", "");		
		resultList=eSearch.SearchIndex(course, question);		
		
		//获取得分最高的结果
		resultArray=fullSearch.getSimilarAnswer(course, question, path, resultList);
//		System.out.println(AnswerScore(resultList,question));
		//logger.info("fullSearch:"+resultArray.toString());
		return resultArray;
	}
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		String question="北宋时，谁发明活字印刷术，15世纪，欧洲才出现活字印刷，比我国晚了约多少年";
//		List<String> usageList=new ArrayList<String>();
		String course="history";
		AnswerByCommonStr(course, question, "F:\\eclispseworkspace\\KnowledgeQA\\");
	}

	public AnswerObject getAnswer() {
		return ansObj;
	}

	public void setAnswer(AnswerObject answer) {
		this.ansObj = answer;
	}

}
