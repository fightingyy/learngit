package com.ld.MainPartExtractor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import com.ld.QueryGeneration.GenerateQuery;
import com.ld.QueryGeneration.SetVariables;
import com.ld.QuestionClassification.ExtractKeyWord;
import com.ld.model.QueryElement;
import com.ld.model.QueryObject;
import com.ld.model.Tamplate;
import com.ld.search.VirtuosoSearch;
import com.ld.IO.Convert;
import com.ld.IO.MaxString;
import com.ld.IO.StringProcess;
import com.ld.IO.Property.ReadProperty;
import com.ld.IO.remove.RemoveDuplicate;
import com.ld.Parser.*;


/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：DecideSubject   
* 类描述： 确定问句的主语
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:00:40   
* @version        
*/
public class DecideSubject {
	
	private List<SetVariables> sVariables=new ArrayList<SetVariables>();
	public List<String> list=new ArrayList<String>();
	public List<String> list2=new ArrayList<String>();
	public List<String> subjecRegextList=new ArrayList<String>();
	List<String> RemoveList=new ArrayList<String>();
	static List<String> removeValueList=new ArrayList<String>();
	static List<String> typeList=new ArrayList<String>();
	String replaceQuestion="";
	List<String> termTamplateList=new ArrayList<String>();
	List<String> ConutStrList=new ArrayList<String>();
	List<String> tamplateContentList=new ArrayList<String>();
	
	static{
		removeValueList.add("是");
		removeValueList.add("怎么");
		typeList.add("content");
		typeList.add("author");
		typeList.add("time");
	}
	
	/**
	* 确定主语
	* @param courseTerms
	* @param question
	* @param course
	* @param tamplate
	* @param coursePropertyMap
	* @param subjectTemp
	* @param altMap
	* @return
	*/
	public String decide(Map<String,String> courseTerms,String question,String course,Tamplate tamplate,Map coursePropertyMap,String subjectTemp,Map<String, String> altMap){
		
		List<String> termList=new ArrayList<String>();
		termList.addAll(courseTerms.keySet());
		String subject = "";

		replaceQuestion=Convert.convertLabel(question,course);
		
		List<String> removeList=new ArrayList<String>();
		//根据术语表确定主语
		list=DecideByMap(termList,question,course,replaceQuestion);
		//根据实例别名确定主语
		list2=decideByAltLabel(course, altMap, question);

		//根据实例的正则模板确定主语
		if(!course.equals("chinese")){
			
			termTamplateList=decideRegex(course,courseTerms,question,replaceQuestion);
		}
		
		termTamplateList.removeAll(list);

		//去重
		if(list.size()>1)
			list=RemoveDuplicate.removeContians(list,question,coursePropertyMap,course,tamplate.getContent());
		
		//去除可作为谓语的主语
		if(!list.isEmpty()&&list.size()>1){
			
//			list=DecideByry(list,question);
			
			removeList.clear();
			if(!course.equals("physics")){
				for(int i=0;i<list.size();i++){
					if(tamplate.getContent().contains(list.get(i))&&list.get(i).length()>1){
						removeList.add(list.get(i));
						tamplateContentList.add(list.get(i));
					}
					else if (list.get(i).endsWith("?")||list.get(i).endsWith("？")||list.get(i).equals("和世㻋")||list.get(i).equals("后")) {
						removeList.add(list.get(i));
						
					}
				}
				list.removeAll(removeList);
				removeList.clear();
			}
		}
		
		//语文去重
		if(course.equals("chinese")&&list.size()>1) 
			list=RemoveDuplicate.removeChineseSubject(question, list);

		list=RemoveDuplicate.remove(list);
		//政治根据强相关于确定主语
		if(course.equals("politics")){
			for(String item:list){
				if(item.length()>2)
					ConutStrList.addAll(VirtuosoSearch.SearchRelate(item));
			}
		}
		
		return subject;
	}
	
	/**
	* 根据术语表确定主语
	* @param termlist
	* @param question
	* @param course
	* @param replaceQuestion
	* @return
	*/
	public static List<String> DecideByMap(List<String> termlist,String question,String course,String replaceQuestion) {
		
		List<String> subjectList=new ArrayList<String>();
		
		int i=1;

		//遍历术语表中的术语
		for (String value : termlist) {
			
			String temp="";
			if(i==termlist.size()) break;
			if(value.startsWith("《")&&value.endsWith("》"))
				temp=value.replace("《", "").replace("》", "");
			temp=temp.replace("\\d）", "").replaceAll("（\\d）", "");

			if((question.contains(value.trim())||replaceQuestion.contains(value)||(question.contains(temp)&&!temp.equals("")))&&!removeValueList.contains(value)){ 
	    	if(course.equals("geo")&&value.length()>1)
	    		subjectList=StringProcess.array_unique(subjectList,value);
	    	else if(!course.equals("geo"))
	    		subjectList=StringProcess.array_unique(subjectList,value);
		    }
		    i++;
		} 
		subjectList.remove("地区");
		
		if(subjectList.size()>2&&course.equals("geo")){
			if(subjectList.contains("世界"))
				subjectList.remove("世界");
			subjectList.remove("大洲");
		}
		if(subjectList.contains("是什么"))
			subjectList.remove("是什么");
		if(course.equals("history")&&subjectList.contains("历史"))
			subjectList.remove("历史");
		else if(course.equals("english")&&subjectList.contains("英语")) 
			subjectList.remove("英语");
		else if(course.equals("chinese")&&subjectList.contains("语文")) 
			subjectList.remove("语文");
		
		return subjectList;
	}

	/**
	* 根据词性确定主语
	* @param cutWord
	* @param usage
	* @param subjectName
	* @return
	*/
	public String DecideByTag(List<Term> cutWord,String usage,String subjectName){
		
		String subject="";
			
		if(usage.equals("person")){
			subject=DecideBynr(cutWord);

		}
		if(usage.equals("word"))
			subject=DecideByi(cutWord);
		if(subject==null||subject.equals("")){
			subject=DecideByg(cutWord);
		}
					
		if(subject==null||subject.equals("")){
			subject=subjectName;
			if(subject!=null&&!subject.equals("")) 
				subject=process(subject);
		}
	
		return subject;
	}
	
	/**
	* 根据人名确定主语
	* @param cutWord
	* @return
	*/
	public String DecideBynr(List<Term> cutWord){
		
		String subject="";
		
		for(int i=0;i<cutWord.size();i++){
			
			
			if(cutWord.get(i).nature==Nature.nr){
				
				subject=cutWord.get(i).word;
			}

		}
		return subject;
	}
	
	/**
	* 根据成语确定主语
	* @param cutWord
	* @return
	*/
	public String DecideByi(List<Term> cutWord){
		
		String subject="";
		
		for(int i=0;i<cutWord.size();i++){
			
			
			if(cutWord.get(i).nature==Nature.i||cutWord.get(i).nature==Nature.vl){
				
				subject=cutWord.get(i).word;
			}

		}
		return subject;
	}
	
	public String DecideByg(List<Term> cutWord){
		String subject="";
		String temp="";
		
		for(int i=0;i<cutWord.size();i++){
	
			if(cutWord.get(i).nature==Nature.g){
				
				temp=cutWord.get(i).word;
				if(temp.length()>subject.length())
					subject=temp;
			}

		}
		return subject;
	}
	
	public String process(String subjectName){
		
		String subject=subjectName;
		
		if(subjectName!=null){
			
			
			List<Term> word=WordParser.CutWord(subject);
			
			for(int i=0;i<word.size();i++){
				
				if(word.get(i).nature==Nature.w&&!word.get(i).word.equals("·")){
					subject=subject.replace(word.get(i).word, "");
				}
				
				if(word.get(i).nature==Nature.rz||word.get(i).nature==Nature.rzv||word.get(i).toString().startsWith("ry")){
	
					int index=subject.indexOf(word.get(i).word);
			    	   
			    	subject=subject.substring(0,index);
			    	break;
				}
			}
		
			if(!subjectName.equals("钢铁是怎样炼成的")){	
				String subjectStr= String.valueOf(subjectName.charAt(subjectName.length()-1));
				
				if(subjectStr.equals("的")){
					subject=subjectName.substring(0,subjectName.length()-1);
				}
			}
			
			if(subject.contains("请问")){	
				subject=subject.replace("请问", "");
			}
			
		}
		return subject;
	}

	public String processFilter(String subjectName){
		
		String subject=subjectName;
		
		if(subjectName!=null&&!subjectName.equals("")){
		
			List<Term> word=WordParser.CutWord(subject);
			
			for(int i=0;i<word.size();i++){
				
				if(word.get(i).nature==Nature.ns&&!word.get(i).word.equals("世界")){
					subject=word.get(i).word;
				}
				else if(word.get(i).nature==Nature.n){
					subject=word.get(i).word;
				}
				
				if(word.get(i).nature==Nature.w&&!word.get(i).word.equals("·")){
					subject=subject.replace(word.get(i).word, "");
				}
				
				if(word.get(i).nature==Nature.rz||word.get(i).nature==Nature.rzv){
	
					int index=subject.indexOf(word.get(i).word);
			    	   
			    	subject=subject.substring(0,index);
			    	break;
				}
			}
		
			if(!subjectName.equals("钢铁是怎样炼成的")){	
				String subjectStr= String.valueOf(subjectName.charAt(subjectName.length()-1));
				
				if(subjectStr.equals("的")){
					subject=subjectName.substring(0,subjectName.length()-1);
				}
			}
			
			if(subject.contains("请问")){	
				subject=subject.replace("请问", "");
			}
			if(subject.endsWith("开")&&subject.contains("召开")){	
				subject=subject.replace("开", "");
			}
			
		}
		return subject;
	}
	
	/**
	* 根据正则模板确定主语
	* @param course
	* @param courseTerms
	* @param question
	* @param replaceQuestion
	* @return
	*/
	public List<String> decideRegex(String course,Map<String, String> courseTerms,String question,String replaceQuestion){
		
		List<String> termTamplateList=new ArrayList<String>();
		String temp="";
		int max=0;
		String maxMatch="";
		for (Map.Entry<String, String> entry : courseTerms.entrySet()) {  
			  
		    String key =  entry.getKey() ;
		    String matchStr="";
		    
		    if(key.equals("♀＞♂")||key.equals("♀＜♂")||key.contains("是什么"))continue;
//		    if(course.equals("chinese")&&!key.contains(" / ")) continue;
		    
		    String value = entry.getValue(); 
		    if(value!=null&&!value.equals("(.*)?")){
		    	Pattern pattern;
				Matcher matcher;
				Matcher matcher1;				

				pattern=Pattern.compile(value);
				matcher = pattern.matcher(question);
				matcher1 = pattern.matcher(replaceQuestion);
				String[] array=null;
				
				int count=0;				
				
				//正则模板的直接匹配
				if (matcher.find()||matcher1.find()){

					termTamplateList.add(key);
				}
				else if(!Pattern.compile("[\\u4e00-\\u9fa5]").matcher(key).find()) continue;
				//将正则模板分割，计算在问句中出现的次数
				else if(!value.equals("null")&&key.length()>3){
					boolean flag=false;
//					if(value.contains("|")) flag=false;
//					else 
						if(value.contains("(.*)?")||value.contains(".{0,4}")){
						
						array=value.split("[\\(\\.\\*\\)\\?,\\.\\{0,4\\}]");				
						if(course.equals("english"))
							array=RemoveDuplicate.CheckNull(array);
						else array=RemoveDuplicate.removeArray(array, key);
						
						if(array.length==0) 
							return termTamplateList;
						
						for(int j=0;j<array.length;j++){
							if(!array[j].equals("")&&!array[j].equals("(.*)?")){
								String regexString=array[j];
								if(!regexString.contains(".{0,4}"))
									regexString=array[j].replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");
								regexString=regexString.replace("稀盐酸", "盐酸");
//								if(regexString.length()<=1&&!course.equals("chemistry")) continue;
								if(regexString.length()<3&&!Pattern.compile("[\\u4e00-\\u9fa5]").matcher(regexString).find()||(course.equals("chemistry")&&regexString.equals("法"))) continue;
								matcher = Pattern.compile(regexString).matcher(question);
								matcher1 = Pattern.compile(regexString).matcher(replaceQuestion);	
								if(!matcher.find()&&!matcher1.find()){	
									flag=false;
								}
								else {
									matchStr+=regexString;
									count++;
								}
							}
						}
					}
//					else flag=false;
					//计算最长公共子串
					String commonStr=MaxString.getMaxString(key, question);
					//计算最长公共子序列
					String commonSeq=MaxString.longestCommonSubsequence(key, question);
					if(!replaceQuestion.equals(""))commonStr=MaxString.getMaxString(key, replaceQuestion);
					String repkey=key.replaceAll("　", "").replaceAll("的", "").replaceAll(",", "");
					
					if(course.equals("history")&&key.length()>6&&key.indexOf("的")==key.length()-3){
						List<Term> cutWord=WordParser.CutWord(key);
						if(cutWord.get(cutWord.size()-1).nature==Nature.vn){
							repkey=repkey.replaceAll(cutWord.get(cutWord.size()-1).word, "");
						}
					}
					if(array!=null&&count!=0&&count==array.length&&matchStr.length()>2){
						temp=key;
						max=count;
						if(matchStr.length()>5||course.equals("chemistry")){
							termTamplateList.add(key);
						}
						else ConutStrList.add(temp);
					}
					else if(array!=null&&count>=array.length/2&&matchStr.length()>repkey.length()/2){

						if(matchStr.length()==2&&repkey.length()==5&&course.equals("geo")){
							continue;
						}
						if(course.equals("chemistry")&&count==1&&(matchStr.equals("化学")||matchStr.equals("常用"))){
							continue;
						}
						else if(count>=max||(count==max&&temp.length()>key.length())&&count>0){												
							temp=key;
							max=count;
							maxMatch=matchStr;
							ConutStrList.add(temp);
						}
						else if(count==max&&matchStr.length()>maxMatch.length()){												
							temp=key;
							max=count;
							maxMatch=matchStr;
							ConutStrList.add(temp);
						}
						
					}

					if (array!=null&&count>array.length/2&&commonStr.length()>=key.length()/2&&(commonStr.length()>4||commonSeq.length()>5)&&!ConutStrList.contains(key)) {

						ConutStrList.add(key);
					}
					else if(key.length()>3&&(key.length()-commonStr.length())==1)
						ConutStrList.add(key);
					if(flag)
						ConutStrList.add(key);
				}
		    }   	
		}  
		
		return termTamplateList;
	}

	/**
	* 根据实例别名确定主语
	* @param course
	* @param map
	* @param question
	* @return
	*/
	public List<String> decideByAltLabel(String course,Map<String, String> map,String question){
		
		List<String> list=new ArrayList<String>();
		Map<String, Nature> wordMap=WordParser.splitWordandNature(WordParser.CutWord(question));
		for(Map.Entry<String,String> entry:map.entrySet()){
			String key=entry.getKey();
			String value=entry.getValue();
			String[] array=value.split("、");
			for(int i=0;i<array.length;i++){
				if(question.contains(array[i])&&array[i].length()>1){
					if(array[i].length()==2&&!wordMap.containsKey(array[i])&&!course.contains("chemistry")) continue;
					list.add(key);
					
					if(!replaceQuestion.equals("")&&key.length()<10)
						replaceQuestion=replaceQuestion.replace(array[i], key);
					else if(array[i].length()>1&&key.length()<10)
						replaceQuestion=question.replace(array[i], key);
				}
			}
		}
		return list;
	}
	
	/**
	* 根据术语表确定主语
	* @param termlist
	* @param question
	* @param course
	* @return
	*/
	public static List<String> DecideByMap(List<String> termlist,String question,String course) {
		
		List<String> subjectList=new ArrayList<String>();
		
		int i=1;

		for (String value : termlist) {
			
			String temp="";
			if(i==termlist.size()) break;
			if(value.startsWith("《")&&value.endsWith("》"))
				temp=value.replace("《", "").replace("》", "");
			temp=temp.replace("\\d）", "").replaceAll("（\\d）", "");

			if((question.contains(value.trim())||(question.contains(temp)&&!temp.equals("")))&&!removeValueList.contains(value)){ 
	    	if(course.equals("geo")&&value.length()>1)
	    		subjectList=StringProcess.array_unique(subjectList,value);
	    	else if(!course.equals("geo"))
	    		subjectList=StringProcess.array_unique(subjectList,value);
		    }
		    i++;
		} 
		subjectList.remove("地区");
		
		if(subjectList.size()>2&&course.equals("geo")){
			if(subjectList.contains("世界"))
				subjectList.remove("世界");
			subjectList.remove("大洲");
		}
		if(subjectList.contains("是什么"))
			subjectList.remove("是什么");
		if(course.equals("history")&&subjectList.contains("历史"))
			subjectList.remove("历史");
		else if(course.equals("english")&&subjectList.contains("英语")) 
			subjectList.remove("英语");
		else if(course.equals("chinese")&&subjectList.contains("语文")) 
			subjectList.remove("语文");
		
		return subjectList;
	}
	public List<SetVariables> getsVariables() {
		return sVariables;
	}
	
	/**
	* 根据相关于确定问化学方程式的题目的主语
	* @param course
	* @param prefixMap
	* @param list
	* @param type
	* @return
	*/
	public static List<String> relateToSubject(String course,Map<String, String> prefixMap, List<String> list,String type){
		
		List<String> resultList=new ArrayList<String>();
		DecideTriple Triples=new DecideTriple();
		List<String> subjectList=new ArrayList<String>();
		subjectList.addAll(list);
		subjectList=RemoveDuplicate.removeContians3(subjectList);

		//拼凑查询主语的三元组
		Triples.RelatedTo(prefixMap, subjectList, course,type);
		
		GenerateQuery gquery = new GenerateQuery();
		VirtuosoSearch virtuosoSearchs=new VirtuosoSearch(course);
		
		List<QueryElement> sv = Triples.queryElementList;
		if(!sv.isEmpty()){
			//生成sparql查询语句
			gquery.generateQuery(sv,course);
			List<QueryObject> queryList = gquery.queryList;
			queryList=RemoveDuplicate.removeQueryObject(queryList);
			
			//对查询语句逐一进行查询
			for (QueryObject queryObject: queryList) {
				String queryStr=queryObject.getQuery();
				if(queryStr.contains("null")) break;
	//			logger.info(queryStr);
				
				resultList=virtuosoSearchs.search(queryStr);
				
			}
		}
		return resultList;
	}

	public void setsVariables(List<SetVariables> sVariables) {
		this.sVariables = sVariables;
	}

}
