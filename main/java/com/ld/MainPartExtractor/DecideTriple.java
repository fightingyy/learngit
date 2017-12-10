package com.ld.MainPartExtractor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.python.antlr.PythonParser.else_clause_return;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import com.ld.FullSearch.FullSearch;
import com.ld.IO.Convert;
import com.ld.IO.MaxString;
import com.ld.IO.ExtractFilter;
import com.ld.IO.Property.ReadProperty;
import com.ld.IO.Term.SelectTerms;
import com.ld.IO.remove.RemoveDuplicate;
import com.ld.Parser.WordParser;
import com.ld.QueryGeneration.SetVariables;
import com.ld.QuestionClassification.QuestionClassify;
import com.ld.model.QueryElement;
import com.ld.model.Tamplate;
import com.ld.model.sparql.*;
import com.ld.search.VirtuosoSearch;
import com.sun.corba.se.spi.copyobject.CopierManager;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：DecideTriple   
* 类描述： 确定三元组
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:01:07   
* @version        
*/
public class DecideTriple {
	
	public List<Tamplate> tamplateList;
	public Value subject;
	public Value value;
	public Value predicate;
	double score=0;
	public List<QueryElement> queryElementList=new ArrayList<QueryElement>();
	public List<QueryElement> queryElementList2=new ArrayList<QueryElement>();
	public List<QueryElement> queryElementList3=new ArrayList<QueryElement>();
//	private List<SetVariables> variableList=new ArrayList<SetVariables>();

	public String subjectName;
	public List<String> propertyList=new ArrayList<String>();
	public List<String> subjectList=new ArrayList<String>();
	
	private Logger logger = LoggerFactory.getLogger(DecideTriple.class);

	public DecideTriple(){
		
	}
	public void DecideTriple(String course){
		propertyList.add("sameAs");
		propertyList.add("consistedOf");
		propertyList.add("topicOf");
		propertyList.add("includes");
		propertyList.add("relatedTo");
		propertyList.add("label");
		propertyList.add("altLabel");
		propertyList.add("definition");
		propertyList.add("content");
		propertyList.add("usage");
		propertyList.add("example");
		propertyList.add("image");
		propertyList.add("belongsTo");
		propertyList.add("partOf");
		
	}
	
	/**
	 * 根据主语谓语拼凑三元组
	 * @param tamplateList
	 * @param course
	 * @param prefix_map
	 * @param courseTerms
	 * @param courseMap
	 * @param inputQuestion
	 * @param altMap
	 * @param path
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public DecideTriple(List<Tamplate> tamplateList,String course,Map<String,String> prefix_map,Map<String,String> courseTerms,Map courseMap,String inputQuestion,Map<String,String> altMap,String path) throws Exception {
		
		DecideTriple(course);
//		boolean optional=true;
		boolean isObject=false;
		
		//地理学科确定三元组
		if(course.equals("geo")){
			Geography geography=new Geography(tamplateList,"geo",prefix_map,courseTerms,courseMap,inputQuestion,altMap,path);
			queryElementList=geography.queryElementList;
			queryElementList2=geography.queryElementList2;
			queryElementList3=geography.queryElementList3;
		}
		else{
			String prefixName=null;	
			String coursePrefix="";
			String regex="(.*)?(上|中)的(?<title>(.*)?(?=的))(的|有|是|为)";
			Pattern pattern=Pattern.compile(regex);
			Matcher matcher=pattern.matcher(inputQuestion);
			
			//确定学科的前缀
			switch(course){
			case "chinese": coursePrefix="chp";break;
			case "geo": coursePrefix="geop";break;
			case "math": coursePrefix="mp";break;
			case "english": coursePrefix="ep";break;
			case "chemistry": coursePrefix="cyp";break;
			case "history": coursePrefix="hp";break;
			case "biology": coursePrefix="bp";break;
			case "politics": coursePrefix="pp";break;
			case "physics":coursePrefix="php";break;
			default:coursePrefix="cop";break;
			}
			
			DecideSubject decideSubject=new DecideSubject();
			boolean isContinue=true;
			List<String> termList=SelectTerms.getTypeBySql(course);
			List<String> termTamplateList=new ArrayList<String>();
			List<String> ConutStrList=new ArrayList<String>();
			List<String> tamplateContentList=new ArrayList<String>();
			List<String> list2=new ArrayList<String>();
			List<String> list=new ArrayList<String>();
			List<String> tamplateContentList2=new ArrayList<String>();
			try {
				//确定学科的所有谓语
				tamplateContentList2.addAll(ReadProperty.selectProperty(course).values());
			} catch (ClassNotFoundException | IOException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			boolean flag=true;
			for(Tamplate tamplate:tamplateList){
				double pscore=0;
				pscore=(double)1/tamplate.getPriority();
				QueryElement qe=new QueryElement();
				//add 11.26
				qe.setTamplate(tamplate);
				
				score=0;
				SetVariables v=null;
				String subjectTemp=tamplate.getSubjectName();
				
				int priority=tamplate.getPriority();
				
				String type=tamplate.getType();
				String typeLabel=ReadProperty.selectLabelByUri(course, type);

				if(propertyList.contains(tamplate.getType())){
					prefixName="cop";
					
					//确定属性是否为公共对象属性
					switch(tamplate.getType()){
					case "sameAs": 
					case "consistedOf":	
					case "topicOf":
					case "includes":
					case "relatedTo":
					case "belongsTo":
					case "partOf":
						isObject=true;break;
					default:isObject=false;break;
					}
				}
				else if(type.equals("type"))
					prefixName="rdf";
				else prefixName=coursePrefix;
				
				if(!propertyList.contains(tamplate.getType())) isObject=ReadProperty.isObject(course, type.toLowerCase());
				
				List<SetVariables> variableList=new ArrayList<SetVariables>();
				System.out.println(tamplate.getType()+"     "+tamplate.getContent());
					
				String filterStr=tamplate.getFilter();
				
				//问句主语不确定的情况下，拼凑三元组
				if(filterStr!=null&&!filterStr.equals("")){
					List<String> subjectlist=new ArrayList<String>();
					if(isObject){
						subjectName=decideSubject.decide(courseTerms,inputQuestion, course,tamplate,courseMap,subjectTemp,altMap);
						subjectlist=decideSubject.list;
						if(subjectName!=null&&!subjectName.equals(""))subjectlist.add(subjectName);
					}
					//拼凑模糊查询的三元组
					FilterSetVariables(filterStr,course,prefix_map,prefixName,isObject,tamplate,subjectlist);
				}
				//问句主语确定的情况下，拼凑三元组
				else {
					
					if(tamplate.isSubject()&&isContinue){
						//确定问句可能的主语
						if(isContinue){
							subjectName=decideSubject.decide(courseTerms,inputQuestion, course,tamplate,courseMap,subjectTemp,altMap);
							isContinue=false;
						}
						list=decideSubject.list;
						list2=decideSubject.list2;
						termTamplateList=decideSubject.termTamplateList;
						ConutStrList = decideSubject.ConutStrList;
						tamplateContentList=decideSubject.tamplateContentList;
						//化学根据相关于或反应物这两个属性确定主语
						if(course.equals("chemistry")&&list.size()>1){
							termTamplateList.addAll(DecideSubject.relateToSubject(course, prefix_map, list,"relatedTo"));	
//							if(inputQuestion.contains("分解"))
							termTamplateList.addAll(DecideSubject.relateToSubject(course, prefix_map, list,"Reactant"));
						}

						//从ConutStrList多个可能主语中确定最可能的主语
						ConutStrList=FullSearch.getSimilarSubject(course, inputQuestion, path, ConutStrList);
						
						subjectList.addAll(list);
						subjectList.addAll(list2);
						subjectList.addAll(termTamplateList);
						subjectList.addAll(ConutStrList);
						subjectList.addAll(tamplateContentList);
						if(subjectName!=null&&!subjectName.equals(""))subjectList.add(subjectName);
						subjectList=RemoveDuplicate.remove(subjectList);
						System.out.println("主语："+subjectList);
					}//if(tamplate.isSubject()&&isContinue)
					if(subjectList.isEmpty()) continue;
				
					else{
						if(!subjectList.isEmpty()){
							//逐个组合可能的主语谓语形成三元组，并计算分值
							for(int i=0;i<subjectList.size();i++){
								qe=new QueryElement();
								//add 11.26
								qe.setTamplate(tamplate);
								String sTemp=subjectList.get(i);
								String temp=sTemp.replaceAll("(《|》|\\s+)", "");
								String commonStr=MaxString.longestCommonSubsequence(inputQuestion, temp);
//								String commonStr=MaxString.getMaxString(inputQuestion, sTemp);
								String replaceCommon=MaxString.getMaxString(decideSubject.replaceQuestion, temp);
								if(replaceCommon.length()>commonStr.length()) commonStr=replaceCommon;
								double subjectScore=0;
								double rate=Math.sqrt((double)commonStr.length()/temp.length());
								if(commonStr.length()>temp.length()) commonStr=temp;
								if(commonStr.length()==temp.length()&&temp.length()<4)rate=1;
								if(course.equals("chemistry")&&temp.length()==1)rate=1;
								rate=rate*(double)Math.pow(temp.length(),1.0/2);
								
//								if(rate>1) rate=(double)inputQuestion.length()/sTemp.length();
								//根据确定确定主语的不同方式，给出每种方式的置信度
								if(list.contains(sTemp)){
									subjectScore=20*rate;
									if(matcher.find()&&matcher.group("title")!=null){
										String matchString=matcher.group("title").toString();
										if(matchString.equals(sTemp))
											subjectScore*=1.3;
									}
								}
								else if(list2.contains(sTemp)){
									subjectScore=20*0.9*rate;
									if(sTemp.endsWith("战")) subjectScore*=0.8;
								}
								else if(termTamplateList.contains(sTemp)||tamplateContentList.contains(sTemp))
									subjectScore=20*0.7*rate; 
								else if(subjectName.contains(sTemp)||ConutStrList.contains(sTemp))
									subjectScore=20*0.5*rate; 
								
								if(termList.contains(sTemp)||tamplateContentList.contains(sTemp)){
									if(!course.equals("physics")&&!sTemp.contains("的"))
										subjectScore*=0.6;
									else if(!course.equals("physics")&&sTemp.contains("的"))
										subjectScore*=0.9;
								}
								
								if(course.equals("physics")&&(tamplate.getContent().contains(sTemp)||sTemp.equals("物体"))) subjectScore*=0.5;
								
								if(inputQuestion.contains(sTemp+"是什么")&&(type.equals("definition")||type.equals("concept"))) subjectScore*=1.2;
								score=subjectScore*pscore;
								qe.setScore(score);
								
								//组合拼凑三元组
								variableList=new ArrayList<SetVariables>();
//								sTemp=sTemp.replace("　", "");
								if(sTemp.contains("'")) 
									sTemp=sTemp.replace("\'", "\\'");
								qe.setSubjectName(sTemp);
								v=getLabels("'"+sTemp+"'", prefix_map);
								variableList.add(v);
								subject =new Value("","subject",true,false);
																
								predicate=new Value("","predicate",true,false);
								
								value=new Value("",type.replaceAll("-\\d+", ""),true,true);
								if(courseMap.containsKey(type)){
									SetVariables propertyV=setPredicate(course,prefix_map, type);
									if(propertyV==null) continue;
									variableList.add(propertyV);
								}
								else if(propertyList.contains(type)){
									predicate=new Value("cop",type,false,false);	
								}
								else continue;
								
								SetVariables variables=new SetVariables(prefix_map,subject,predicate,value);
								
								variableList.add(variables);
								
								//对含“的”的主语处理
								if(sTemp.contains("的")&&!sTemp.equals(subjectName)&&!list.contains(subjectName)&&flag){
									List<SetVariables> firstTriple=addFirstTriple(prefix_map, sTemp, tamplateList,course);
									if(!firstTriple.isEmpty()){
										QueryElement newQElement=new QueryElement();

										newQElement.setSubjectName(sTemp);
										//add 11.26
										newQElement.setTamplate(tamplate);
										if(!course.equals("chemistry")){
											if(rate>1&&priority==1&&!course.equals("physics"))newQElement.setScore(sTemp.length()*2.5);
											
											else newQElement.setScore(sTemp.length()*2);	
										}
										
										else newQElement.setScore(sTemp.length()*0.8);
										newQElement.setVariablesList(firstTriple);
										queryElementList.add(newQElement);
										flag=false;
									}
								}

								if(priority==3&&!course.equals("chemistry")&&!course.equals("physics")&&(termList.contains(sTemp)||tamplateContentList.contains(sTemp)||tamplateContentList2.contains(sTemp))&&(typeLabel.equals("内容")||typeLabel.equals("定义")||sTemp.equals(typeLabel))){
									qe.setVariablesList(variableList);							
									queryElementList3.add(qe);
									continue;
								}
								else if(course.equals("history")&&sTemp.length()==1){
									qe.setVariablesList(variableList);							
									queryElementList3.add(qe);
									continue;
								}
								if(isObject&&type.equals("consistedOf")&&priority==1){
									
								}
								qe.setVariablesList(variableList);
								if(priority==1&&qe!=null)
									queryElementList.add(qe);
								else if(priority==2&qe!=null)
									queryElementList2.add(qe);
								else if(priority==3&qe!=null)
									queryElementList3.add(qe);
							}
						}
					}
				}//else				
			}//for templist
		}
	}
	
	/**
	* filter模糊查询
	* @param filterStr
	* @param course
	* @param prefix_map
	* @param prefixName
	* @param isObject
	* @param tamplate
	* @param list
	*/
	public void FilterSetVariables(String filterStr,String course,Map<String,String> prefix_map,String prefixName,boolean isObject,Tamplate tamplate,List<String> list){
		
		QueryElement qElement=new QueryElement();
		//add 11.26
		qElement.setTamplate(tamplate);
		String usage=tamplate.getUsage();
		String type=tamplate.getType();
		int priority=tamplate.getPriority();
		String myClass=tamplate.getMyclass();
		
		List<SetVariables> variableList=new ArrayList<SetVariables>();
		
		//抽取模糊查询的关键词
		filterStr=ExtractFilter.filterProcess(filterStr,usage,course);
		HashSet<Pair> pairs=new HashSet<Pair>();
		if(filterStr==null||filterStr.replaceAll(" ", "").equals("")||filterStr.equals("|")) return;
		String[] array=filterStr.split("\\|");
		array=RemoveDuplicate.CheckNull(array);
		qElement.setFilterStr(filterStr);
		
		//根据关键词的长度计算分值
		double subjectScore=(double)Math.pow(filterStr.length(), 3.0/2);
		double pscore=(double)5/tamplate.getPriority();
		if(tamplate.getPriority()==3) pscore=0.1;		
		score=subjectScore*pscore;

		//根据关键词和谓语确定模糊查询的语句
		Pair pair=null;
		for(int i=0;i<array.length;i++){
			if(array[i].replaceAll(" ", "").equals("")) continue;
			pair=new Pair(type.replaceAll("-\\d+", ""),array[i]);
			pairs.add(pair);
		}
		
		//对间接问作者这类问题的处理
		if (usage.equals("author")){
			if(filterStr.replace("《", "").replace("》", "").length()<5) return;
			Value subject=new Value("","subject",true,true);
			Value predicate = new Value("rdfs", "label" ,false,false);
			Value value = new Value("", "subjectlabel", true,true);
			
			pairs.clear();
			
			for(int i=0;i<array.length;i++){
				pair=new Pair("content",array[i]);
				pairs.add(pair);
			}
			Filter filter=new Filter(pairs);
			
			SetVariables v1 = new SetVariables(prefix_map, subject,predicate, value,false,filter);
			variableList.add(v1);
			List<SetVariables> ContentFilters=setContentFilter(course,prefix_map,"cop","content",false);
			List<SetVariables> authorSvList=setContentFilter(course,prefix_map,prefixName,type,true);
			if(ContentFilters!=null&&!ContentFilters.isEmpty()) variableList.addAll(ContentFilters);
			if(authorSvList!=null&&!authorSvList.isEmpty())variableList.addAll(authorSvList);
			if(filterStr.length()>4)score=filterStr.length()*3;
			else score=filterStr.length();
		}
		//对最早最晚这类问题的处理
		else if(usage.equals("first")||usage.equals("last")){

			variableList=getSort(prefix_map,myClass,"first");
			
			qElement.setVariablesList(variableList);
			queryElementList.add(qElement);
			
		}
		else{
			Value subject=new Value("","subject",true,true);
			Value predicate = new Value("rdfs", "label", false,false);
			Value value = new Value("", "subjectlabel", true,true);
			
			//对象属性的处理
			if(isObject){
				if(course.equals("history")){
					list.clear();
					list.add(filterStr);
				}
				for(int i=0;i<list.size();i++){
					if(course.equals("history")||(course.equals("geo")||course.equals("chinese")||course.equals("english"))&&!list.get(i).equals("中国")&&!list.get(i).equals("我国")){
						variableList=new ArrayList<SetVariables>();
						pairs=new HashSet<Pair>();

						SetVariables v1 = new SetVariables(prefix_map, subject,predicate, value);
						variableList.add(v1);
						
						List<SetVariables> ObjectVariables=new ArrayList<SetVariables>();
						ObjectVariables.addAll(variableList);
						ObjectVariables.addAll(setObjectFilter(prefix_map,prefixName,type,list.get(i),course));
						qElement.setVariablesList(ObjectVariables);
						
						score=Math.sqrt(list.get(i).length())+4/tamplate.getPriority();
						qElement.setFilterStr(list.get(i));
						qElement.setScore(score);
						if(tamplate.getPriority()<3)
							queryElementList2.add(qElement);
						else queryElementList3.add(qElement);
						qElement=new QueryElement();
					}
				}
			}
			//数据属性的处理
			else{
				pairs.add(pair);
				Filter filter=new Filter(pairs);
				SetVariables v1 = new SetVariables(prefix_map, subject,predicate, value,false,filter);
				variableList.add(v1);
				
				List<SetVariables> svList=setContentFilter(course,prefix_map,prefixName,type,true);
				if(svList!=null&&!svList.isEmpty()) 
					variableList.addAll(svList);
			}
		}
		qElement.setScore(score);
		qElement.setVariablesList(variableList);
		if(priority==1&&variableList.size()>1)
			queryElementList.add(qElement);
		else if(priority==2&&variableList.size()>1)
			queryElementList2.add(qElement);
		else if(priority==3&&variableList.size()>1) queryElementList3.add(qElement);
		
	}
	
	/**
	* 主语作为宾语模糊查询的关键词组合三元组
	* @param tamplateList
	* @param course
	* @param prefix_map
	* @param cutWord
	* @param courseTerms
	*/
	public void getSubjectLabel(List<Tamplate> tamplateList,String course,Map<String,String> prefix_map,List<Term> cutWord,Map<String,String> courseTerms){
		
		DecideTriple(course);
		String prefixName=null;
		List<String> termList=new ArrayList<String>();
		termList.addAll(courseTerms.keySet());

		switch(course){
		case "chinese": prefixName="chp";break;
		case "geo": prefixName="geop";break;
		case "math": prefixName="mp";break;
		case "english": prefixName="ep";break;
		case "chemistry": prefixName="cyp";break;
		case "history": prefixName="hp";break;
		case "biology": prefixName="bp";break;
		case "politics": prefixName="pp";break;
		case "physics":prefixName="php";break;
		default:prefixName="cop";break;		
		}
		
		for(Tamplate tamplate:tamplateList){
			
			boolean isObject=false;
			if(propertyList.contains(tamplate.getType())){
				prefixName="cop";
				
				switch(tamplate.getType()){
				case "sameAs": 
				case "consistedOf":	
				case "topicOf":
				case "includes":
				case "relatedTo":
				case "belongsTo":
				case "partOf":
					isObject=true;break;
				default:isObject=false;break;
				}
			}
			
			List<SetVariables> variableList=new ArrayList<SetVariables>();
			DecideSubject s=new DecideSubject();
			
			String usage=tamplate.getUsage();
			String subjectname=s.DecideByTag(cutWord,usage,tamplate.getSubjectName());

			String filterStr=subjectname;
			if(subjectname==null||(course.equals("chinese")&&subjectname.length()<=2)) continue;
			//确定问句主语
			if(isObject)
				filterStr=ExtractFilter.filterBySubject(filterStr, usage, course,termList);
			
			if(filterStr!=null&&!filterStr.equals(""))
				filterStr=ExtractFilter.filterProcess(filterStr,usage,course);
			else continue;
			
			//将抽取出的主语作为主语模糊查询的关键词
			if(filterStr==null||filterStr.equals("")) continue;
			Value subject=new Value("","subject",true,true);
			Value predicate = new Value("rdfs", "label", false,false);
			Value value = new Value("", "subjectlabel", true,true);
			
			SetVariables v1 = new SetVariables(prefix_map, subject,predicate, value);
			variableList.add(v1);
			HashSet<Pair> pairs=new HashSet<Pair>();
			Value predicate1 = new Value(prefixName, tamplate.getType(), false,false);
			
			String type=tamplate.getType().toLowerCase();

//			if(type.contains("-1")) type=type.replace("-1", "");
			Value value1=new Value("",type.replaceAll("-\\d+", ""),true,true);
			Pair pair=new Pair(type.replaceAll("-\\d+", ""),filterStr);

			pairs.add(pair);
			Filter filter =new Filter(pairs);
			
			//对象属性的处理
			if(isObject){
				Value subject2=new Value("",type.replaceAll("-\\d+", "")+"O",true,false);
				
				SetVariables ObjectLabel = new SetVariables(prefix_map, subject2,predicate, value1);
				variableList.add(ObjectLabel);
				SetVariables v=new SetVariables(prefix_map, subject,predicate1, subject2,false,filter);
				
				variableList.add(v);
			}
			else{
				
				SetVariables v=new SetVariables(prefix_map, subject,predicate1, value1,false,filter);
				
				variableList.add(v);
			}
			QueryElement qElement=new QueryElement();
			//add 11.26
			qElement.setTamplate(tamplate);
			qElement.setVariablesList(variableList);
			queryElementList.add(qElement);
			
		}
		
	}
	
	/**
	* 主语模糊查询
	* @param tamplateList
	* @param course
	* @param prefix_map
	* @param question
	* @param courseTerms
	* @param courseMap
	*/
	public void SubjectFilter(List<Tamplate> tamplateList,String course,Map<String,String> prefix_map,String question,Map<String,String> courseTerms,Map courseMap){
		
		QueryElement qElement=new QueryElement();
		
		DecideTriple(course);
		List<String> termList=new ArrayList<String>();
		termList.addAll(courseTerms.keySet());
		String prefixName=null;

		switch(course){
		case "chinese": prefixName="chp";break;
		case "geo": prefixName="geop";break;
		case "math": prefixName="mp";break;
		case "english": prefixName="ep";break;
		case "chemistry": prefixName="cyp";break;
		case "history": prefixName="hp";break;
		case "biology": prefixName="bp";break;
		case "politics": prefixName="pp";break;
		case "physics":prefixName="php";break;
		default:prefixName="cop";break;
		
		}
		
		for(Tamplate tamplate:tamplateList){
			if(tamplate.getPriority()>2||tamplate.getFilter()!=null) continue;
			//add 11.26
			qElement.setTamplate(tamplate);
			if(propertyList.contains(tamplate.getType())) prefixName="cop";

			DecideSubject s=new DecideSubject();

			String usage=tamplate.getUsage();
//			String myClass=tamplate.getMyclass();
			String replaceQuestion=Convert.convertLabel(question,course);
			
			//确定问句的主语
			List<String> list=s.DecideByMap(termList, question, course,replaceQuestion);
			if(course.equals("chinese")) list.clear();
			if(!list.isEmpty()&&list.size()>1){
				List<String> removeList=new ArrayList<String>();
				list=RemoveDuplicate.removeContians2(list,question,courseMap,course,tamplate.getContent());
				
				removeList.clear();
				for(int i=0;i<list.size();i++){
					if(tamplate.getContent().contains(list.get(i)))
						removeList.add(list.get(i));		
				}
				list.removeAll(removeList);
				removeList.clear();
			}
			
			if(list.isEmpty()){
				String temp=ExtractFilter.filterProcess(tamplate.getSubjectName(), usage, course);
				if(!temp.equals(""))
					list.add(temp);
			}
			
			//将主语作为主语模糊查询的关键词
			for(int i=0;i<list.size();i++){
				
				String filterStr=list.get(i);
				if(filterStr!=null&&!filterStr.equals("")&&filterStr.length()>=2)
					filterStr=ExtractFilter.filterProcess(filterStr,usage,course);
				else continue;
				
				List<SetVariables> variableList=new ArrayList<SetVariables>();
				if(filterStr.equals("")||filterStr.length()<3) continue;
				
				Value subject=new Value("","subject",true,true);
				Value predicate = new Value("rdfs", "label", false,false);
				Value value = new Value("", "subjectlabel", true,true);
				
				SetVariables v1 = new SetVariables(prefix_map, subject,predicate, value);
				
//				Value predicate1 = new Value(prefixName, tamplate.getType(), false,false);				
				String type=tamplate.getType().replaceAll(" ", "");

				Value predicate1=null;

				Value value1=new Value("",type.replaceAll("-\\d+", ""),true,true);
				if(propertyList.contains(type)){
					predicate1=new Value("cop",type,false,false);	
				}
				else{
					predicate1=new Value("","predicate",true,false);
					SetVariables propertyV=setPredicate(course,prefix_map, type);
					if(propertyV==null) continue;
					variableList.add(propertyV);
				}
				
				Pair pair=new Pair("subjectlabel",filterStr);
				HashSet<Pair> pairs=new HashSet<Pair>();
				
				pairs.add(pair);
				Filter filter =new Filter(pairs);
				
				variableList.add(v1);
				SetVariables v=new SetVariables(prefix_map, subject,predicate1, value1,false,filter);
				
				variableList.add(v);
				
				qElement.setVariablesList(variableList);
				queryElementList.add(qElement);
			}
		}
		
	}
	
	/**
	* 根据同义词查询单词的解释
	* @param prefix_map
	* @param question
	*/
	public void SynonymVariableList(Map<String,String> prefix_map,String question){
		
		QueryElement qElement=new QueryElement();
		List<SetVariables> variableList=new ArrayList<SetVariables>();
		List<Term> CutWord=WordParser.CutWord(question);
		String synonymLabel="";
		
		//根据词性确定英文单词
		for(int i=0;i<CutWord.size();i++){
			Term word=CutWord.get(i);
			if(word.nature==Nature.nx||word.word.equals(" ")){
				synonymLabel+=word.word;
			}
		}
		
		//拼凑三元组
		if(!synonymLabel.equals("")){
		
			subject =new Value("","subject",true,false);
			
			predicate=new Value("rdfs","label",false);
				
			value=new Value("","subjectlabel",true,true);
			
			SetVariables SubjectVariables=new SetVariables(prefix_map,subject,predicate,value);
			
			Value predicate2=new Value("ep","synonym",false);
			Value value2=new Value("","synonym",true,false);
			SetVariables SynonymVariables=new SetVariables(prefix_map,subject,predicate2,value2);
			
			Value value3=new Value("","'"+synonymLabel+"'",false,false);
			SetVariables SynonymLabelVariables=new SetVariables(prefix_map,value2,predicate,value3);
			
			Value predicate4=new Value("ep","chinese",false);
			Value value4=new Value("","chinese",true,true);
			SetVariables ChineseVariables=new SetVariables(prefix_map,subject,predicate4,value4);

			variableList.add(SubjectVariables);	
			variableList.add(SynonymVariables);
			variableList.add(SynonymLabelVariables);
			variableList.add(ChineseVariables);
	
			qElement.setVariablesList(variableList);
			queryElementList.add(qElement);
		}
		
	}
	
	/**
	* 反义词
	* @param prefix_map
	* @param question
	* @param courseTerms
	*/
	public void AntonymVariableList(Map<String,String> prefix_map,String question,Map<String,String> courseTerms){
		
		QueryElement qElement=new QueryElement();
		List<SetVariables> variableList=new ArrayList<SetVariables>();
//		List<Term> CutWord=WordParser.CutWord(question);
		String aynonymLabel="";
		List<String> termList=new ArrayList<String>();
		termList.addAll(courseTerms.keySet());
		List<String> list=DecideSubject.DecideByMap(termList, question, "english");
		if(list.size()==1)
			aynonymLabel=list.get(0);
		if(!aynonymLabel.equals("")){
		
			subject =new Value("","subject",true,true);
			
			predicate=new Value("rdfs","label",false);
				
			value=new Value("","subjectlabel",true,true);
			
			SetVariables SubjectVariables=new SetVariables(prefix_map,subject,predicate,value);
			
			Value predicate2=new Value("ep","yanyici",false);
			Value value2=new Value("","yanyici",true,false);
			SetVariables AynonymVariables=new SetVariables(prefix_map,subject,predicate2,value2);
			
			Value value3=new Value("","'"+aynonymLabel+"'",false,false);
			SetVariables AynonymLabelVariables=new SetVariables(prefix_map,value2,predicate,value3);

			variableList.add(SubjectVariables);	
			variableList.add(AynonymVariables);
			variableList.add(AynonymLabelVariables);
	
			qElement.setVariablesList(variableList);
			queryElementList.add(qElement);
		}
		
	}
	
	/**
	* 解释
	* @param subjectName
	* @param prefix_map
	* @param type
	* @throws Exception
	*/
	public void explanationTriple(String subjectName,Map<String,String> prefix_map,String type) throws Exception {

		
		this.subjectName="'"+subjectName+"'";
		
		SetVariables v=null;
	
		List<SetVariables> variableList=new ArrayList<SetVariables>();
	
		v=getLabels(this.subjectName, prefix_map);
		variableList.add(v);
		subject =new Value("","subject",true,false);
			
		predicate=new Value("chp",type,false);
			
		value=new Value("",type,true,true);
		
		SetVariables variables=new SetVariables(prefix_map,subject,predicate,value);
			
		variableList.add(variables);					

		QueryElement qElement=new QueryElement();
		qElement.setSubjectName(subjectName);
		qElement.setVariablesList(variableList);
		queryElementList.add(qElement);

	}
	
	/**
	* 最早最晚这类问题的处理
	* @param prefix_map
	* @param myClass
	* @param sort
	* @return
	*/
	public static List<SetVariables> getSort(Map<String,String> prefix_map,String myClass,String sort){
		
		List<SetVariables> variableList=new ArrayList<SetVariables>();
		
		Value subject1=new Value("","subject",true,true);
		Value predicate1 = new Value("rdfs", "label", false,false);
		Value value1 = new Value("", "subjectlabel", true,true);
		SetVariables v1 = new SetVariables(prefix_map, subject1,predicate1, value1);
		
		Value subject2=new Value("","subject",true,false);
		Value predicate2 = new Value("hp", "begintime", false,false);
		Value value2 = new Value("", "begintime", true,true);
		SetVariables v2 = new SetVariables(prefix_map, subject2,predicate2, value2);
		
//		Predicate predicate = new Predicate("hp", "shijian", false);
//		Value value = new Value("", "shijian", true,true);
//		SetVariables v = new SetVariables(prefix_map, subject2,predicate, value,true);
		
		Value subject3=new Value("","subject",true,false);
		Value predicate3 = new Value("rdf", "type", false,false);
		Value value3 = new Value("hc", myClass, false,false);
		SetVariables v3 = new SetVariables(prefix_map, subject3,predicate3, value3);
		
		variableList.add(v1);
		variableList.add(v2);
//		variableList.add(v);
		variableList.add(v3);
		
		return variableList;
	}
	
	/**
	* 主语拼凑
	* @param subjectName
	* @param prefix_map
	* @return
	*/
	public SetVariables getLabels(String subjectName,Map<String,String> prefix_map){
		
		SetVariables variables=null;
		
		Value subject=new Value("","subject",true,true);
		Value predicate = new Value("rdfs", "label", false,false);
		Value value = new Value("", subjectName, false,false);
		
		variables = new SetVariables(prefix_map, subject,predicate, value);
		
		return variables;
	}
	
	/**
	* 谓语是内容的宾语模糊查询
	* @param course
	* @param prefix_map
	* @param prefixName
	* @param type
	* @param isSelect
	* @return
	*/
	public List<SetVariables> setContentFilter(String course,Map<String,String> prefix_map,String prefixName,String type,boolean isSelect){
		
		SetVariables variables=null;
		List<SetVariables> sList=new ArrayList<SetVariables>();
		Value predicate =null;
		
		Value subject=new Value("","subject",true,false);
		if(prefixName.equals("cop")){
			predicate = new Value("cop",type,false,false);
		}
		else{
			predicate = new Value("","predicate",true,false);
			SetVariables propertyV=setPredicate(course,prefix_map, type);
			if(propertyV==null) return sList;
			sList.add(propertyV);
		}
				
		Value value = new Value("", type, true,isSelect);
		
		variables = new SetVariables(prefix_map, subject,predicate, value);
		
		sList.add(variables);
		
		
		return sList;
	}
	
	/**
	* 对象属性的模糊查询
	* @param prefix_map
	* @param prefixName
	* @param type
	* @param val
	* @param course
	* @return
	*/
	public List<SetVariables> setObjectFilter(Map<String,String> prefix_map,String prefixName,String type,String val,String course){
		List<SetVariables> sList=new ArrayList<SetVariables>();
		
		SetVariables v1,v2=null;
		
		Value subject=new Value("","subject",true,false);
		Value predicate =new Value("","predicate",true,false);
		Value value = new Value("", type.replaceAll("-\\d+", "")+"O", true,false);
		
		v1 = new SetVariables(prefix_map, subject,predicate, value);
		SetVariables propertyV=setPredicate(course,prefix_map, type);
		if(propertyV==null) return sList;
		sList.add(v1);
		sList.add(propertyV);
		
		Value subject1=new Value("",type.replaceAll("-\\d+", "")+"O",true,false);
		Value predicate1 = new Value("rdfs", "label", false,false);
		Value value1 = new Value("", type.replaceAll("-\\d+", ""), true,true);
		
		Pair pair=new Pair(type.replaceAll("-\\d+", ""),val);
		HashSet<Pair> pairs=new HashSet<Pair>();
		pairs.add(pair);
		Filter filter=new Filter(pairs);
		
		v2 = new SetVariables(prefix_map, subject1,predicate1, value1,false,filter);
		sList.add(v2);
		
		return sList;
	}
	
	/**
	* 根据相关于或反应物拼凑三元组
	* @param prefix_map
	* @param subjectList
	* @param course
	* @param type
	*/
	public  void RelatedTo(Map<String,String> prefix_map,List<String> subjectList,String course,String type){
		
		List<SetVariables> sList=new ArrayList<SetVariables>();
		
		SetVariables v1,v3,v4=null;
		
		Value subject=new Value("","subject",true,true);
		Value labelPredicate = new Value("rdfs", "label", false,false);
		Value labelValue=new Value("","subjectlabel",true,true);
		v1 = new SetVariables(prefix_map, subject,labelPredicate, labelValue);

		sList.add(v1);
		Value RelatePredicate = null;
		if(type.equals("relatedTo")){
			RelatePredicate=new Value("cop", "relatedTo", false,false);
		}
		else if(type.equals("Reactant")){
			RelatePredicate=new Value("cyp", "Reactant", false,false);
		}
		Value Relatesubject=null;
		int count=0;
		for(int i=0;i<subjectList.size();i++){
			
			List<Term> wordList=WordParser.CutWord(subjectList.get(i));
			if(type.equals("Reactant")&&wordList.get(0).nature!=Nature.nmc&&wordList.get(0).nature!=Nature.nf) continue;
			count++;
			subjectName+=subjectList.get(i);
			Relatesubject=new Value("","relateTo"+i,true,false);
			v3 = new SetVariables(prefix_map, subject,RelatePredicate,Relatesubject);
			sList.add(v3);
			
			Value RelateLabel = new Value("", "'"+subjectList.get(i)+"'", false,false);
			v4 = new SetVariables(prefix_map, Relatesubject,labelPredicate,RelateLabel);
			sList.add(v4);
		}
		
		if(count>0){
			QueryElement queryElement=new QueryElement();
			queryElement.setVariablesList(sList);
			queryElement.setScore(subjectName.length());
			queryElementList.add(queryElement);
		}
	}

	/**
	* 强项关于属性的处理
	* @param prefix_map
	* @param tamplate
	* @param subjectName
	* @param inputQuestion
	* @param course
	*/
	public void qianXiangguan(Map<String,String> prefix_map,Tamplate tamplate,String subjectName,String inputQuestion,String course){
		List<SetVariables> sList=new ArrayList<SetVariables>();
		String type=tamplate.getType();
		String commonStr=MaxString.longestCommonSubsequence(inputQuestion, subjectName);

		double subjectScore=0;
		double rate=Math.sqrt((double)commonStr.length()/subjectName.length())*(double)Math.pow(subjectName.length(),1.0/2);
		
		SetVariables v1,v2,v3,v4=null;
		double pscore=(double)1/tamplate.getPriority();
		Value subject=new Value("","subject",true,true);
		Value labelPredicate = new Value("rdfs", "label", false,false);
		Value labelValue=new Value("","subjectlabel",true,true);
		v1 = new SetVariables(prefix_map, subject,labelPredicate, labelValue);
		
		Value predicate =new Value("","predicate",true,false);
		Value value = new Value("", type.replaceAll("-\\d+", ""), true,true);
		v2 = new SetVariables(prefix_map, subject,predicate, value);
		
		SetVariables propertyV=setPredicate(course,prefix_map, type);
		if(propertyV==null) return ;
		sList.add(v1);
		sList.add(v2);
		sList.add(propertyV);
		
		Value Relatesubject=new Value("","relateTo",true,false);
		Value RelatePredicate = new Value("pp", "qiangxiangguanyu", false,false);
		
		v3 = new SetVariables(prefix_map, subject,RelatePredicate,Relatesubject);
		sList.add(v3);
		
		Value RelateLabel = new Value("", "'"+subjectName+"'", false,false);
		v4 = new SetVariables(prefix_map, Relatesubject,labelPredicate,RelateLabel);
		sList.add(v4);
		
		QueryElement queryElement=new QueryElement();
		//add 11.26
		queryElement.setTamplate(tamplate);
		queryElement.setVariablesList(sList);
		queryElement.setScore(20*rate*pscore);
		queryElementList.add(queryElement);
	}
	
	/**
	* 实体限制属性的处理
	* @param prefix_map
	* @param entityName
	* @return
	*/
	public static List<SetVariables> addEntityLimit(Map<String,String> prefix_map,String entityName){
		
		List<SetVariables> variableList=new ArrayList<SetVariables>();
		
		Value subject1=new Value("","limit",true,false);
		Value predicate1 = new Value("geop", "shitixianzhi", false,false);
		Value value1 = new Value("", "subject", true,false);
		SetVariables v1 = new SetVariables(prefix_map, subject1,predicate1, value1);
		
		Value subject2=new Value("","limit",true,false);
		Value predicate2 = new Value("rdfs", "label", false,false);
		Value value2 = new Value("", "'"+entityName+"'", false,false);
		SetVariables v2 = new SetVariables(prefix_map, subject2,predicate2, value2);
		
		variableList.add(v1);
		variableList.add(v2);
		
		return variableList;
	}
	
	/**
	* 
	* @param prefix_map
	* @param subjectName
	* @param tamplateList
	* @param course
	* @return
	*/
	public static List<SetVariables> addFirstTriple(Map<String,String> prefix_map,String subjectName,List<Tamplate> tamplateList,String course){
		
		List<SetVariables> variableList=new ArrayList<SetVariables>();
		
		Pattern pattern;
		Matcher matcher;
		
		for(Tamplate tamplate:tamplateList){
			pattern=Pattern.compile(tamplate.getContent());
			matcher=pattern.matcher(subjectName);
			
			if (matcher.find()){
				Value subject=new Value("","subject",true,true);
				Value predicate1 = new Value("rdfs", "label", false,false);
				Value value1 = new Value("", "'"+subjectName+"'", false,false);
				SetVariables v1 = new SetVariables(prefix_map, subject,predicate1, value1);
				
				
				Value subject1=new Value("","subject",true,false);
				Value predicate2=null;
				Value value2=null;
				
				if(!course.equals("chemistry")){
					predicate2 = new Value("cop", "content", false,false);
					value2 = new Value("", "content", true,true);
					
				}
				else{
					predicate2 = new Value("cyp", "Content", false,false);
					value2 = new Value("", "Content", true,true);
				}
				SetVariables v2 = new SetVariables(prefix_map, subject1,predicate2, value2);
				variableList.add(v1);
				variableList.add(v2);
				
				break;
			}
		}

		return variableList;
	}
	
	public static SetVariables setPredicate(String course,Map<String,String> prefix_map,String type){
		
		SetVariables variables=null;
		
		String typeLabel=ReadProperty.selectLabelByUri(course, type).replaceAll(" ", "");
		if(typeLabel==null||typeLabel.equals("")) return null;
		
		Value subject=new Value("","predicate",true,false);
		Value predicate = new Value("rdfs", "label", false,false);
		Value value = new Value("", "'"+typeLabel+"'", false,false);
		
		variables = new SetVariables(prefix_map, subject,predicate, value);
		
		return variables;
	}

}
