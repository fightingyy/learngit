package com.ld.MainPartExtractor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import com.ld.FullSearch.FullSearch;
import com.ld.IO.MaxString;
import com.ld.IO.ExtractFilter;
import com.ld.IO.Property.ReadProperty;
import com.ld.IO.Term.SelectTerms;
import com.ld.IO.remove.RemoveDuplicate;
import com.ld.Parser.WordParser;
import com.ld.QueryGeneration.SetVariables;
import com.ld.model.QueryElement;
import com.ld.model.Tamplate;
import com.ld.model.sparql.Filter;
import com.ld.model.sparql.Pair;
import com.ld.model.sparql.Value;
import com.ld.search.VirtuosoSearch;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：Geography   
* 类描述： 地理确定三元组
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:50:12   
* @version        
*/
public class Geography {
	
	List<QueryElement> queryElementList=new ArrayList<QueryElement>();
	List<QueryElement> queryElementList2=new ArrayList<QueryElement>();
	List<QueryElement> queryElementList3=new ArrayList<QueryElement>();
	private List<String> propertyList=new ArrayList<String>();
	private String subjectName;
//	private String valueName;
	public List<String> subjectList=new ArrayList<String>();
	private Value subject;
	private Value value;
	private Value predicate;
	double score;
//	private Logger logger = LoggerFactory.getLogger(Geography.class);
	
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
	

	public Geography(List<Tamplate> tamplateList,String course,Map<String,String> prefix_map,Map<String,String> courseTerms,Map<String,String> courseMap,String inputQuestion,Map<String,String> altMap,String path) throws Exception {

		DecideTriple(course);

		boolean isObject=false;
		
		String prefixName=null;	
		String coursePrefix="";
		List<Term> cutWords=WordParser.CutWord(inputQuestion);
		List<String> termTamplateList=new ArrayList<String>();
		List<String> ConutStrList=new ArrayList<String>();
		List<String> tamplateContentList=new ArrayList<String>();
		List<String> tamplateContentList2=new ArrayList<String>();
		try {
			tamplateContentList2.addAll(ReadProperty.selectProperty(course).values());
		} catch (ClassNotFoundException | IOException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		List<String> list2=new ArrayList<String>();
		List<String> list=new ArrayList<String>();

		DecideSubject decideSubject=new DecideSubject();
		boolean isContinue=true;
		List<String> termList=SelectTerms.getTypeBySql(course);
		
		String regex="在(?<title>(.*)?(地区|区域))(.*)?";
		Pattern pattern=Pattern.compile(regex);
		Matcher matcher=pattern.matcher(inputQuestion);
		
		for(Tamplate tamplate:tamplateList){
			
			double pscore=0;
			pscore=(double)1/tamplate.getPriority();
			if(tamplate.getPriority()==3)pscore=0.1;
			QueryElement qe=new QueryElement();
			//add 11.28
			qe.setTamplate(tamplate);
			SetVariables v=null;
			String subjectTemp=tamplate.getSubjectName();

//			decideSubject=new DecideSubject();
			int priority=tamplate.getPriority();
			
			String type=tamplate.getType();
			type=type.replaceAll("-\\d+","");
			String typeLabel=ReadProperty.selectLabelByUri(course, type);
//			Set<WordEntry> wordVecSet=new HashSet<WordEntry>();
//			if(priority<3){
//				wordVecSet=word2vecPattern.getVecWordList(path, typeLabel);
//				int count=0;
//				for(WordEntry wordEntry:wordVecSet){
//					if(inputQuestion.contains(wordEntry.name)&&wordEntry.name.length()>1){
//						count++;
//					}
//				}
//				if(count>1) pscore+=(double)(count-1)*0.1;
//			}

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
			else if(tamplate.getType().equals("type"))
				prefixName="rdf";
			else prefixName=coursePrefix;
			if(!propertyList.contains(tamplate.getType())) isObject=ReadProperty.isObject(course, type.toLowerCase());
			
			List<SetVariables> variableList=new ArrayList<SetVariables>();
			System.out.println(tamplate.getType()+"     "+tamplate.getContent());


			String filterStr=tamplate.getFilter();
			
			if(filterStr!=null&&!filterStr.equals("")){
				
				FilterSetVariables(filterStr,course,prefix_map,prefixName,isObject,tamplate,list);
			}
			else {
				
				if(tamplate.isSubject()&&isContinue){
					if(isContinue){
						subjectName=decideSubject.decide(courseTerms,inputQuestion, course,tamplate,courseMap,subjectTemp,altMap);
						isContinue=false;
					}
					list=decideSubject.list;
					list2=decideSubject.list2;
					termTamplateList=decideSubject.termTamplateList;
					ConutStrList = decideSubject.ConutStrList;
					tamplateContentList=decideSubject.tamplateContentList;
					if(ConutStrList.size()>1) 
						ConutStrList=FullSearch.getSimilarSubject(course, inputQuestion, path, ConutStrList);
					
					subjectList.addAll(list);
					subjectList.addAll(list2);
					subjectList.addAll(termTamplateList);
					subjectList.addAll(ConutStrList);
					subjectList.addAll(tamplateContentList);
					
					subjectList=RemoveDuplicate.remove(subjectList);
				}
				if(subjectList.isEmpty()) continue;
			
				else{
					System.out.println("主语："+subjectList);
					if(!subjectList.isEmpty()){
						String temp="";
						for(int i=0;i<subjectList.size();i++){
							
							qe=new QueryElement();
							//add 11.28
							qe.setTamplate(tamplate);
							variableList=new ArrayList<SetVariables>();
							String sTemp=subjectList.get(i);
							
							String commonStr=MaxString.getMaxString(inputQuestion, sTemp);
							String replaceCommon=MaxString.getMaxString(decideSubject.replaceQuestion, sTemp);
							if(replaceCommon.length()>commonStr.length()) commonStr=replaceCommon;
							double subjectScore=0;
							double rate=Math.sqrt((double)commonStr.length()/sTemp.length());
							if(commonStr.length()==sTemp.length()&&sTemp.length()<4)rate=1;
							
							rate=rate*(double)(sTemp.length()/2);
							if(rate==1) rate+=0.5;

							if(inputQuestion.contains(sTemp)&&inputQuestion.contains(temp)){
								if(inputQuestion.contains(temp+"的"+sTemp))
									subjectScore+=2;
								else if(inputQuestion.startsWith(sTemp+"是"))
									subjectScore+=2;
							}
							temp=sTemp;
//							if(rate>1) rate=(double)inputQuestion.length()/sTemp.length();
							if(list.contains(sTemp)||tamplateContentList.contains(sTemp)){
								subjectScore+=20*rate;
								if(inputQuestion.contains(sTemp+"位于"))
									subjectScore*=1.1;
								if(matcher.find()&&matcher.group("title")!=null){
									String matchString=matcher.group("title").toString();
									if(matchString.equals(sTemp))
										subjectScore*=0.9;
								}
							}
							else if(list2.contains(sTemp))
								subjectScore+=20*0.9*rate;
							else if(termTamplateList.contains(sTemp))
								subjectScore+=20*0.7*rate; 
							else if(subjectName.contains(sTemp)||ConutStrList.contains(sTemp))
								subjectScore+=20*0.6*rate; 
							Map<String, Nature> wordMap=WordParser.splitWordandNature(WordParser.CutWord(sTemp));
							if(sTemp.length()<=4&&(termList.contains(sTemp)||tamplateContentList.contains(sTemp)||(tamplateContentList2.contains(sTemp)&&!wordMap.containsValue(Nature.ns)&&!wordMap.containsValue(Nature.nsf))||sTemp.equals("我国"))) subjectScore*=0.5;
						
							if(typeLabel.equals(sTemp)||(sTemp.endsWith("地理")&&sTemp.length()>3))subjectScore*=0.4;
							score=subjectScore*pscore;
							qe.setScore(score);
							
							sTemp=sTemp.replace("　", "");
							if(sTemp.equals("我国")&&type.equals("content"))continue;
							
							qe.setSubjectName(sTemp);
							v=getLabels("'"+sTemp+"'", prefix_map);
							variableList.add(v);
							subject =new Value("","subject",true,false);

							predicate=new Value("","property",true,false);
							
							value=new Value("",type.replaceAll("-\\d+", ""),true,true);
							if(propertyList.contains(type)){
								predicate=new Value("cop",type,false,false);	
							}
							else{
								SetVariables propertyV=Geography.setGeoPredicate(prefix_map, type);
								if(propertyV==null) continue;
								variableList.add(propertyV);
							}
							SetVariables variables=new SetVariables(prefix_map,subject,predicate,value);
							
							variableList.add(variables);
							
							
							if(sTemp.contains("的")&&Pattern.matches(tamplate.getContent(), sTemp)&&!tamplate.getContent().contains(sTemp)&&typeLabel.length()>1){
								List<SetVariables> firstTriple=DecideTriple.addFirstTriple(prefix_map, sTemp, tamplateList,course);
								if(!firstTriple.isEmpty()){
									QueryElement newQElement=new QueryElement();
//									newQElement=qe;
									//add 11.28
									newQElement.setTamplate(tamplate);
									newQElement.setLimitName(qe.getLimitName());
									score=commonStr.length();
									newQElement.setSubjectName(sTemp);
									newQElement.setScore(score);								
									newQElement.setVariablesList(firstTriple);
									queryElementList2.add(newQElement);
								}
							}

							String entityName="";

							for(int j=0;j<cutWords.size();j++){
								if(cutWords.get(j).nature==Nature.ns){
									if(!sTemp.contains(cutWords.get(j).word)&&j+2<cutWords.size()&&(cutWords.get(j+1).word.equals(sTemp)||cutWords.get(j+2).word.equals(sTemp))){
										entityName=cutWords.get(j).word;
										break;
									}
									else if(sTemp.contains(cutWords.get(j).word)&&j+2<cutWords.size()&&(cutWords.get(j+1).word.equals(sTemp)||cutWords.get(j+2).word.equals(sTemp))){
										entityName=cutWords.get(j).word;
										sTemp=sTemp.replace(entityName, "");
										break;
									}
								}
							}
							if(entityName==null||entityName.equals("")){
								if(inputQuestion.contains("冬季气温")){
									entityName="冬季";
								}
								else if(inputQuestion.contains("夏季气温")){
									entityName="夏季";
								}
							}
							if(isObject&&type.equals("consistedOf")&&priority==1){
								
							}
							boolean hasEntityLimit=false;
							if(entityName!=null&&!entityName.equals(""))
								hasEntityLimit=VirtuosoSearch.isExist(hasEntityLimit(sTemp, entityName));
							if((termList.contains(sTemp)||tamplateContentList.contains(sTemp)||tamplateContentList2.contains(sTemp))&&!hasEntityLimit&&(typeLabel.equals("内容")||typeLabel.equals("定义")||sTemp.equals(typeLabel))){
								qe.setVariablesList(variableList);							
								queryElementList3.add(qe);
								continue;
							}
							if(priority==1&&course.equals("geo")){
								if(entityName!=null&&!entityName.equals("")&&hasEntityLimit){
									
									List<SetVariables> entityLimitSetVariables=DecideTriple.addEntityLimit(prefix_map, entityName);
									variableList.addAll(entityLimitSetVariables);
									qe.setScore(score+entityName.length());
									qe.setLimitName(entityName);
								}
								qe.setVariablesList(variableList);							
								queryElementList.add(qe);
							}
							if(priority==2&&course.equals("geo")){
								if(entityName!=null&&!entityName.equals("")&&hasEntityLimit){
									List<SetVariables> entityLimitSetVariables=DecideTriple.addEntityLimit(prefix_map, entityName);
									variableList.addAll(entityLimitSetVariables);
									qe.setScore(score+entityName.length());
									qe.setLimitName(entityName);
								}
								qe.setVariablesList(variableList);								
								queryElementList2.add(qe);
							}
							if(priority==3&&course.equals("geo")){
								if(entityName!=null&&!entityName.equals("")&&hasEntityLimit){
									List<SetVariables> entityLimitSetVariables=DecideTriple.addEntityLimit(prefix_map, entityName);
									variableList.addAll(entityLimitSetVariables);
									qe.setLimitName(entityName);
								}
								qe.setVariablesList(variableList);
								queryElementList3.add(qe);
							}
						}
					}
				}
			}				
		}			
	}

	public void FilterSetVariables(String filterStr,String course,Map<String,String> prefix_map,String prefixName,boolean isObject,Tamplate tamplate,List<String> slist){
		
		QueryElement qe=new QueryElement();
		//add 11.28
		qe.setTamplate(tamplate);
		String usage=tamplate.getUsage();
		String type=tamplate.getType();
		int priority=tamplate.getPriority();
		String myClass=tamplate.getMyclass();
		
		List<SetVariables> variableList=new ArrayList<SetVariables>();
		double pscore=0;
	
		filterStr=ExtractFilter.filterProcess(filterStr,usage,course);
		HashSet<Pair> pairs=new HashSet<Pair>();
		if(filterStr==null||filterStr.equals("")||filterStr.equals("|")) return;
		String[] array=filterStr.split("\\|");
		array=RemoveDuplicate.CheckNull(array);
		Pair pair=null;
	
		for(int i=0;i<array.length;i++){
			pair=new Pair(type,array[i]);
			pairs.add(pair);
		}
		if(usage.equals("first")||usage.equals("last")){

			variableList=DecideTriple.getSort(prefix_map,myClass,"first");
			qe.setFilterStr(filterStr);
			qe.setVariablesList(variableList);
			queryElementList.add(qe);					
		}
		else{
			Value subject=new Value("","subject",true,true);
			Value predicate = new Value("rdfs", "label", false,false);
			Value value = new Value("", "subjectlabel", true,true);
			
			//对象属性的处理
			if(isObject){
				
				if(subjectName!=null&&!subjectName.equals(""))subjectList.add(subjectName);
				
				for(int i=0;i<slist.size();i++){
					if(course.equals("geo")&&!slist.get(i).equals("中国")&&!slist.get(i).equals("我国")){
						variableList=new ArrayList<SetVariables>();
						pairs=new HashSet<Pair>();
						
//							pair=new Pair(type,list.get(i));
//							pairs.add(pair);
//							Filter filter=new Filter(pairs);
						SetVariables v1 = new SetVariables(prefix_map, subject,predicate, value);
						variableList.add(v1);
						
						List<SetVariables> ObjectVariables=new ArrayList<SetVariables>();
						ObjectVariables.addAll(variableList);
						ObjectVariables.addAll(setObjectFilter(prefix_map,prefixName,type,slist.get(i)));
//						qe.setScore(slist.get(i).length());
						qe.setVariablesList(ObjectVariables);
						queryElementList3.add(qe);
					}
				}
			}
			else{
				pairs.add(pair);
				Filter filter=new Filter(pairs);
				
				SetVariables v1 = new SetVariables(prefix_map, subject,predicate, value,false,filter);
				variableList.add(v1);
				
				List<SetVariables> svList=setContentFilter(prefix_map,prefixName,type,true);
				if(svList!=null&&!svList.isEmpty()) variableList.addAll(svList);
			}
		}
		double subjectScore=(double)Math.pow(filterStr.length(), 3.0/2);
		pscore=(double)5/tamplate.getPriority();
		if(tamplate.getPriority()==3) pscore=0.1;
		if(type.equals("jiancheng")||type.equals("fenjiexian")) subjectScore=30;
		score=subjectScore*pscore;
		
		qe.setScore(score);
		if(!isObject){
			qe.setVariablesList(variableList);
		}
		if(priority==1&&variableList.size()>1)
			queryElementList.add(qe);
		else if(priority==2&&variableList.size()>1)
			queryElementList2.add(qe);
		else if(priority==3&&variableList.size()>1) queryElementList3.add(qe);
		
	}
	public List<SetVariables> setObjectFilter(Map<String,String> prefix_map,String prefixName,String type,String val){
		List<SetVariables> sList=new ArrayList<SetVariables>();
		
		SetVariables v1,v2,propertyV=null;
		
		Value subject=new Value("","subject",true,true);
		Value predicate =new Value("","property",true,false);
		Value value = new Value("", type+"O", true,false);
		
		v1 = new SetVariables(prefix_map, subject,predicate, value);
		propertyV=Geography.setGeoPredicate(prefix_map, type);
		sList.add(v1);
		sList.add(propertyV);
		
		Value subject1=new Value("",type+"O",true,false);
		Value predicate1 = new Value("rdfs", "label", false,false);
		Value value1 = new Value("", type, true,true);
		
		Pair pair=new Pair(type,val);
		HashSet<Pair> pairs=new HashSet<Pair>();
		pairs.add(pair);
		Filter filter=new Filter(pairs);
		
		v2 = new SetVariables(prefix_map, subject1,predicate1, value1,false,filter);
		sList.add(v2);
		
		return sList;
	}
	
	public List<SetVariables> setContentFilter(Map<String,String> prefix_map,String prefixName,String type,boolean isSelect){
		
		SetVariables variables=null;
		List<SetVariables> sList=new ArrayList<SetVariables>();
		
		Value subject=new Value("","subject",true,true);
		Value predicate = new Value("","property",true,false);
		
		Value value = new Value("", type, true,isSelect);
		
		variables = new SetVariables(prefix_map, subject,predicate, value);
		SetVariables propertyV=Geography.setGeoPredicate(prefix_map, type);
		if(propertyV==null) return sList;
		
		sList.add(variables);
		sList.add(propertyV);
		
		return sList;
	}
	
	
	public String process(String filter){
		
		String subject=filter;
		
		if(filter!=null){
			
			
			List<Term> word=WordParser.CutWord(subject);
			
			for(int i=0;i<word.size();i++){
				
				if(word.get(i).nature==Nature.w&&!word.get(i).word.equals("·")){
					subject=subject.replace(word.get(i).word, "");
				}
				
				
				
				if(word.get(i).nature==Nature.rz||word.get(i).nature==Nature.rzv){
	
					int index=subject.indexOf(word.get(i).word);
			    	   
			    	subject=subject.substring(0,index);
			    	break;
				}
			}
		
					
				String subjectStr= String.valueOf(filter.charAt(filter.length()-1));
				
				if(subjectStr.equals("的")){
					subject=filter.substring(0,filter.length()-1);
				}
				
				if(subjectStr.contains("是")){
					subject=filter.replace("是", "");
				}
				if(subjectStr.contains("可")){
					subject=filter.replace("可", "");
				}
		}
		return subject;
	}
	
	public static String hasEntityLimit(String subjectName,String EntityLimit){
		String query="PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
				+ "PREFIX cop: <http://edukb.org/knowledge/0.1/property/common#>"
				+ "PREFIX geop: <http://edukb.org/knowledge/0.1/property/geo#>"
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
				+ "ASK from <http://edukb.org/geo> from <http://edukb.org/geo_baidu> from <http://edukb.org/geo_wiki> from <http://edukb.org/geo_china_pedia> from <http://edukb.org/geo_geonames> from <http://edukb.org/geo_china_administrative_divisions>  "
				+ "WHERE {"
				+ "?subject rdfs:label '"+subjectName+"'."
				+ "?limit geop:shitixianzhi ?subject."
				+ "?limit rdfs:label '"+EntityLimit+"'."
				+ "}";
		
		return query;
	}
	
	public SetVariables setFilter(Map<String,String> prefix_map,String filter){
		
		SetVariables variables=null;
		
		Value subject=new Value("","subject",true,true);
		Value predicate = new Value("rdfs", "label", false,false);
		Value value = new Value("", "subjectlabel", true,true);	
		
		variables = new SetVariables(prefix_map, subject,predicate, value,false,filter);
		
		
		return variables;
	}
	
//	public SetVariables setFilters(Map<String,String> prefix_map,List<String> filters){
//		
//		SetVariables variables=null;
//		
//		Value subject=new Value("","subject",true,false);
//		Predicate predicate = new Predicate("rdfs", "label", false);
//		Value value = new Value("", "subjectlabel", true,true);	
//		
//		variables = new SetVariables(prefix_map, subject,predicate, value,false,filters);
//		
//		
//		return variables;
//	}
	
	
	public static SetVariables setGeoPredicate(Map<String,String> prefix_map,String type){
		
		SetVariables variables=null;
		
		String typeLabel=ReadProperty.selectLabelByUri("geo", type).replaceAll(" ", "");
		if(typeLabel==null||typeLabel.equals("")) return null;
		
		Value subject=new Value("","property",true,false);
		Value predicate = new Value("rdfs", "label", false,false);
		Value value = new Value("", "'"+typeLabel+"'", false,false);
		
		variables = new SetVariables(prefix_map, subject,predicate, value);
		
		return variables;
	}
	
	public SetVariables setPredicate(Map<String,String> prefix_map,String property){
		
		SetVariables variables=null;
		
		Value subject=new Value("","subject",true,true);
		Value predicate = new Value("cop", property, false,false);
		Value value = new Value("", property, true,true);
		
		variables = new SetVariables(prefix_map, subject,predicate, value);
		
		return variables;
	}

	
	public SetVariables setClass(Map<String,String> prefix_map,String type){
		
		SetVariables variables=null;
		
		Value subject=new Value("","subject",true,true);
		Value predicate = new Value("cop", "type", false,false);
		Value value = new Value("geoc", type, false,false);
		
		variables = new SetVariables(prefix_map, subject,predicate, value);
		
		return variables;
	}
	
	//构造label为谓语的三元组
	public SetVariables getLabels(String subjectName,Map<String,String> prefix_map){
		
		SetVariables variables=null;
		
		Value subject=new Value("","subject",true,true);
		Value predicate = new Value("rdfs", "label", false,false);
		Value value = new Value("", subjectName, false,false);
		
		variables = new SetVariables(prefix_map, subject,predicate, value);
		
		return variables;
	}

}
