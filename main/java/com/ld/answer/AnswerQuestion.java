package com.ld.answer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.struts2.ServletActionContext;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import com.ld.FullSearch.FullSearch;
import com.ld.IO.StringProcess;
import com.ld.IO.Property.ReadProperty;
import com.ld.IO.Term.SelectTerms;
import com.ld.IO.remove.RemoveDuplicate;
import com.ld.MainPartExtractor.DecideSubject;
import com.ld.MainPartExtractor.DecideTriple;
import com.ld.Parser.WordParser;
import com.ld.QueryGeneration.GenerateQuery;
import com.ld.QueryGeneration.SetPrefix;
import com.ld.Util.HttpConnectUtil;
import com.ld.model.AnswerObject;
import com.ld.model.QueryElement;
import com.ld.model.QueryObject;
import com.ld.model.Tamplate;
import com.ld.pattern.PatternMatching;
import com.ld.search.VirtuosoSearch;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：AnswerQuestion   
* 类描述： 对整个问答流程的集成
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:16:40   
* @version        
*/
public class AnswerQuestion {
	
	private JSONArray resultArray=new JSONArray();
	private JSONArray result=new JSONArray();
	private List<Tamplate> tamplateList=new ArrayList<Tamplate>();
	public String subjectName="";
	HashMap<String,Integer> map=new HashMap<String,Integer>();
	String subjectTemp="";
	Map<String,String> courseTerms=new HashMap<String,String>();
	Map courseMap=new HashMap();
	List<String> subjectList=new ArrayList<String>();
	
	HttpServletRequest request = ServletActionContext.getRequest();
	private Map<String, String> altLabelMap=new HashMap<String, String>();
	private Logger logger = LoggerFactory.getLogger(AnswerQuestion.class);
	
	GenerateQuery gquery = new GenerateQuery();
	JSONArray fullSearchArray = new JSONArray();
	public AnswerQuestion(){
		
	}

	/**
	 * 问答流程的集成
	 * @param inputQuestion
	 * @param course
	 * @throws Exception
	 */
	public AnswerQuestion(String inputQuestion,String course) throws Exception {
		
		SetPrefix prefixes = new SetPrefix(course);
		Map<String, String> prefixMap = prefixes.getPrefixes();
	
		String path=request.getSession().getServletContext().getRealPath("");//获取tomcat下webapps下的项目根路径
		request.setCharacterEncoding("utf-8");

		if(path.contains("\\.metadata"))
			path=path.split("\\.metadata")[0]+"KnowledgeQA/";
		else if(path.contains("WEB-INF")){
			path=path.split("WEB-INF")[0];
		}
		else {
			path+="/";
		}
		path = "F:\\eclispseworkspace\\KnowledgeQA\\";
		JSONArray subjectFilterArray=new JSONArray();
		JSONArray subjectToValueArray=new JSONArray();
		JSONArray subjectGraphArray=new JSONArray();
		
		inputQuestion=inputQuestion.trim();
		
//		//繁体转换为简体
//		inputQuestion=HanLP.convertToSimplifiedChinese(inputQuestion);
		//logger.info(inputQuestion);

		boolean flag=false;
		if(course.equals("math")){
			AnswerMath(inputQuestion);//查询数学问题的答案，得到resultArray的值
			flag=true;
		}
		else{
			if(!course.equals("chinese")){
				courseTerms=SelectTerms.getMapBySql(course);//从数据库中获取course这个学科表中的所有属性值term,regex
			}
			
			//政治学科常有大段没有意义的描述，句号前的无用描述剔除掉
			if(course.equals("politics")){
				int count=StringProcess.CountNumber(inputQuestion, "。");
				if(count>0&&!inputQuestion.contains("“")&&!inputQuestion.contains("\"")){
					String[] array=inputQuestion.split("。");
					
					for(int i=0;i<array.length;i++){
						Map<String, Nature> wordMap=WordParser.splitWordandNature(WordParser.CutWord(array[i]));
						if(array[i].length()>4&&!wordMap.containsValue(Nature.ry)&&!wordMap.containsValue(Nature.rys)&&!wordMap.containsValue(Nature.ryv)&&!wordMap.containsValue(Nature.ryt)&&!array[i].contains("哪")){
							inputQuestion=inputQuestion.replace(array[i], "");
						}
					}
				}
			}
			
			/*分词处理*/
			List<Term> words = WordParser.CutWord(inputQuestion);
			System.out.println(words.toString());
			
			/*对问句进行模板匹配*/
			PatternMatching patternMatching=new PatternMatching();
			patternMatching.Matching(inputQuestion, course);
			tamplateList=patternMatching.getTamplateList();
			
			patternMatching=new PatternMatching();
			patternMatching.Matching(inputQuestion,"Common");
			List<Tamplate> CommontamplateList=patternMatching.getTamplateList();
				
			tamplateList.addAll(CommontamplateList);
	
			List<String> typeList=new ArrayList<String>();
			List<String> usageList=new ArrayList<String>();
			for(Tamplate tamplate:tamplateList){
	//			logger.info(tamplate.getContent());
				String type=tamplate.getType().toLowerCase();
				typeList.add(type);
				usageList.add(tamplate.getUsage());
			}
			typeList=RemoveDuplicate.remove(typeList);
			
			
			VirtuosoSearch virtuosoSearchs=new VirtuosoSearch(course);
			
			//判断问句是否由匹配到可用的模板，有则进行之后的分析
			if(tamplateList.size()>0&&inputQuestion.length()>=4){
				 
				resultArray=firstResult(course, prefixMap, inputQuestion, usageList,tamplateList,false,typeList,path);
				
				//英语根据同义词查询单词的意思
				if(resultArray.isEmpty()&&course.equals("english")){
					System.out.println("====================英语根据同义词查询单词的意思====================");
					EnglishAnswer(inputQuestion,course,prefixMap,usageList,typeList);
					
				}
				
				//语文查询解释，解释中含有大量有用的信息
				if(resultArray.isEmpty()&&!tamplateList.isEmpty()&&course.equals("chinese")){
					System.out.println("====================根据语文解释查询====================");
					ChineseAnswer(inputQuestion,course,prefixMap,usageList,typeList);
				}
				
				
				/*根据政治的强相关于进行查询*/
//				if(resultArray.isEmpty()&&!tamplateList.isEmpty()&&course.equals("politics")){
//					System.out.println("====================根据政治的强相关于进行查询====================");
//					relateToAnswer(inputQuestion,course,prefixMap,usageList);
//				}
				
				//除英语学科外全部进行全文检索，语文只有在模板未查询到结果时才进行全文检索
				if(!course.equals("chinese")&&!course.equals("english")||(resultArray.isEmpty()&&course.equals("chinese"))){
//					JSONObject fullSearchObject=virtuosoSearchs.FullSearch(course, inputQuestion,courseTerms,typeList);

					fullSearchArray=FullSearch.AnswerByCommonStr(course, inputQuestion, path);
					
					if(!fullSearchArray.isEmpty()){
						
						resultArray.addAll(fullSearchArray);
					}
				}
	
				//谓语不变，以filter主语的形式查询
				if(resultArray.isEmpty()&&!course.equals("english")){
					System.out.println("====================以filter主语的形式查询====================");

					SubjectFilterSearch(course, prefixMap, inputQuestion, usageList);
					subjectFilterArray=result;
				}
				
				//模板和全文检索都未查询到结果时，查询主语的子图
				if(resultArray.isEmpty()&&!course.equals("chinese")&&!course.equals("english")){
					System.out.println("====================查询主语的子图====================");
					if(!subjectList.isEmpty()){
						for(int j=0;j<subjectList.size();j++){
							String subjectString=subjectList.get(j);
							if(subjectString.equals("细胞")) continue;
							if(subjectString!=null&&!subjectString.toString().equals("")&&subjectString.length()>2)
								subjectGraphArray=virtuosoSearchs.searchGraph(null,subjectString,true,course);
						}
	//					if(!subjectGraphObject.isEmpty()){
	//						subjectGraphObject.put("attention", "可能的答案如下");
	//					}
					}
				}
				
				//模板和全文检索都未查询到结果时，将主谓对换进行查询
				if(resultArray.isEmpty()&&!course.equals("english")){
					map=new HashMap<String, Integer>();
						
					System.out.println("====================主谓对换进行查询====================");
					DecideTriple decideTriple1=new DecideTriple();
	
					GenerateQuery gquery1 =new GenerateQuery();
	
					//确定三元组
					decideTriple1.getSubjectLabel(tamplateList, course, prefixMap,words,courseTerms);
					
					List<QueryElement> sv1=new ArrayList<QueryElement>();					
					sv1=decideTriple1.queryElementList;
					
					//生成查询语句
					gquery1.generateQuery(sv1,course);
	
					List<QueryObject> queryList1 = gquery1.queryList;
					queryList1=RemoveDuplicate.remove(queryList1);
				
					//对查询语句逐个进行查询
					for (QueryObject queryObject: queryList1) {
						String queryStr=queryObject.getQuery();
						if(queryStr.contains("null")) continue;
						System.out.println(queryStr);
						result=virtuosoSearchs.searchQuery(queryObject, course,usageList,inputQuestion);
						
						subjectToValueArray=FurtherProcess.ExtractWord(course, inputQuestion, result);
					}
				}
			}
			
			//从filter主语、查询子图、主谓对换查询三种查询方式的结果中选取出最佳的一个结果
			if(resultArray.isEmpty()){
				DecideAnswer dAnswer=new DecideAnswer();
				resultArray=dAnswer.decide(course,subjectFilterArray, subjectGraphArray, subjectToValueArray, inputQuestion,typeList);
			}
			
			//进一步抽取语文学科中的常用属性：作者、字、号
			if(usageList.contains("source")){
				resultArray=FurtherProcess.ExtractPersonProperty(typeList, resultArray,subjectName,inputQuestion);
			}
			
			/*对结果进行进一步的处理,抽出成语的相关人物*/
			resultArray=FurtherProcess.findPerson( resultArray, inputQuestion);
			if(course.equals("chinese")){
				resultArray=FurtherProcess.ExtractPersonProperty(typeList, resultArray,subjectName,inputQuestion);
			}
			
			//计算存在总时间
			if(usageList.contains("subtraction")||usageList.contains("today")) 
				resultArray=FurtherProcess.TotalTime(resultArray,usageList);
			
			/*物质的组成元素*/
			if(usageList.contains("Elements")) 
				resultArray=FurtherProcess.findElements(resultArray,usageList);
			
			/*抽出人物*/
			if(!course.equals("history"))
				resultArray=FurtherProcess.ExtractPerson(resultArray, inputQuestion,typeList);
			/*抽取上下句*/
			resultArray=FurtherProcess.nextProcess(resultArray, inputQuestion, usageList);
			
			/*对结果进行筛选*/
			if(!resultArray.isEmpty()){
				resultArray=PreciseAnswer.Precise(resultArray, inputQuestion,course,usageList,path);
//				resultObject=DecideAnswer.decide(resultObject, inputQuestion,typeList);
			}
			
			//在以上的查询都未查询到结果时，整个问句作为关键词查询子图
			if(resultArray.isEmpty()&&inputQuestion.length()<5&&typeList.size()>0){
				if(inputQuestion.contains("\"")) 
					inputQuestion=inputQuestion.replaceAll("\"", "\\\\\"");
				inputQuestion=inputQuestion.trim();
				resultArray=virtuosoSearchs.searchGraph(null,inputQuestion,true,course);
				
				if(!resultArray.isEmpty()) flag=true;
			}
			if(tamplateList.size()==0&&resultArray.isEmpty()){
				
				if(inputQuestion.contains("\"")) 
					inputQuestion=inputQuestion.replaceAll("\"", "\\\\\"");
				inputQuestion=inputQuestion.trim();
				resultArray=virtuosoSearchs.searchGraph(null,inputQuestion,true,course);
				if(!resultArray.isEmpty()) flag=true;
				
				if(resultArray.isEmpty()&&inputQuestion.length()>4){
					fullSearchArray=FullSearch.AnswerByCommonStr(course, inputQuestion, path);
					
					if(!fullSearchArray.isEmpty()){
						
						resultArray.addAll(fullSearchArray);
					}
				}
			}
			
			//历史学科在前面的查询未查询到结果时，重新查询语文知识库
			if((resultArray.isEmpty()||usageList.contains("writer"))&&course.equals("history")){
				System.out.println("================================历史跨学科查询===============================");
				HistorySearchChinese(inputQuestion,course,prefixMap,usageList,typeList,path);
				/*对结果进行进一步的处理,抽出成语的相关人物*/
				resultArray=FurtherProcess.findPerson( resultArray, inputQuestion);
			}
		}//获取resultArray的值
		
		
		/*对最终的结果去重、排序*/
		if(resultArray.size()>1){
			resultArray=PreciseAnswer.removeObject(resultArray,flag);
		}
		
		if(resultArray.isEmpty()&&tamplateList.isEmpty()){
			AnswerObject ansObj=new AnswerObject();
			ansObj.setMessage("您提的问题暂时没有可用的模板，您可以自己添加模板");
			resultArray.add(ansObj);
		}
		else if(resultArray.isEmpty()&&!tamplateList.isEmpty()){
			AnswerObject ansObj=new AnswerObject();
			ansObj.setMessage("此问题没有直接的答案，可参考相关词条找寻可能的答案！");
			resultArray.add(ansObj);
		}
		//for(JsonObject result: resultArray)
		for (int i = 0; i < resultArray.size(); i++) {
			JSONObject jObject = resultArray.getJSONObject(i);
			if(jObject.get("tamplateContent") == "") {
				resultArray.remove(jObject);
			}
		}
		
		if (!fullSearchArray.isEmpty()) {
			JSONObject jsObject = (JSONObject) fullSearchArray.get(0);
			String value = (String) jsObject.get("value");
			double score = (double) jsObject.get("score");
			String fsanswer = value + "；" + score;
			if (fsanswer.indexOf("\n") != -1) {
				fsanswer = fsanswer.replace("\n", "");
			}
			jsObject.put("fsanswer", fsanswer);
			jsObject.put("fs", 1);
			resultArray.add(jsObject);
		}
	}

	
	/**
	 * 根据模板生成的查询语句进行查询
	 * @param course
	 * @param prefixMap
	 * @param inputQuestion
	 * @param usageList
	 * @param tamplateList
	 * @param isPrecise
	 * @param typeList
	 * @param path
	 * @return
	 */
	public JSONArray firstResult(String course,Map<String, String> prefixMap,String inputQuestion,List<String> usageList,List<Tamplate> tamplateList,boolean isPrecise,List<String> typeList,String path){
		
		JSONArray searchResult=new JSONArray();
		JSONArray resultArray=new JSONArray();
		
		ServletContext sct= ServletActionContext.getServletContext();
		@SuppressWarnings("unchecked")
		Map<String,String> chineseTerms=(Map<String,String>) sct.getAttribute("chineseTerms");
		Map<String,String> courseTerms=new HashMap<String,String>();
		if(!course.equals("chinese")){
		
			courseTerms=SelectTerms.getMapBySql(course);
		}
		Map chineseMap=(Map) sct.getAttribute("chineseMap");
		
		VirtuosoSearch vs=new VirtuosoSearch(course);

		try {
			if(course.equals("chinese")){
				courseTerms=chineseTerms;
//				altLabelMap=(Map) sct.getAttribute("altLabelMap");
				altLabelMap=SelectTerms.getAltLabel(course);
				courseMap=ReadProperty.selectProperty(course);
			}
			else{
				courseTerms=SelectTerms.getMapBySql(course);
				altLabelMap=SelectTerms.getAltLabel(course);
				courseMap=ReadProperty.selectProperty(course);
			}
			
			/*根据模板匹配的结果抽出主谓宾，确定三元组*/				
			DecideTriple decideTriple=new DecideTriple(tamplateList,course,prefixMap,courseTerms,courseMap,inputQuestion,altLabelMap,path);
			
			subjectList=decideTriple.subjectList;
			subjectList=RemoveDuplicate.remove2(subjectList);
			List<QueryElement> sv=new ArrayList<QueryElement>();
			List<QueryElement> sv2=new ArrayList<QueryElement>();
			List<QueryElement> sv3=new ArrayList<QueryElement>();
			
			sv=decideTriple.queryElementList;
			sv2=decideTriple.queryElementList2;
			sv3=decideTriple.queryElementList3;
			
			System.out.println("====================优先级为1的查询====================");
			searchResult=ResultBySv(sv,inputQuestion,course,usageList,vs);
			if(!searchResult.isEmpty()){
				resultArray.addAll(searchResult);
			}
			
			
			if(!sv2.isEmpty()&&!isPrecise){
				System.out.println("====================优先级为2的查询====================");
				searchResult=ResultBySv(sv2,inputQuestion,course,usageList,vs);
				if(!searchResult.isEmpty()) resultArray.addAll(searchResult);
							
			}

			/*优先级高的模板生成的查询未找到答案时，使用优先级低的模板生成查询语句*/
			if(resultArray.isEmpty()&&!sv3.isEmpty()&&!isPrecise){
				System.out.println("====================优先级为3的查询====================");
				searchResult=ResultBySv(sv3,inputQuestion,course,usageList,vs);
				if(!searchResult.isEmpty()) resultArray.addAll(searchResult);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
			return resultArray;
		
	}

	/**
	 * 三元组生成查询语句，并查询出结果
	*/
	public JSONArray ResultBySv(List<QueryElement> sv,String inputQuestion,String course,List<String> usageList,VirtuosoSearch vs) throws RepositoryException, MalformedQueryException, QueryEvaluationException, ClassNotFoundException, SQLException, IOException{
		
		gquery.generateQuery(sv,course);
		JSONArray SearchResult=new JSONArray();
		JSONArray SearchArray=new JSONArray();
		
		//生成查询语句
		List<QueryObject> queryList = gquery.queryList;
		queryList=RemoveDuplicate.remove(queryList);
		
		//对查询语句逐个进行查询
		for (QueryObject queryObject : queryList) {
			String queryStr=queryObject.getQuery();
			if(queryStr.contains("null")) continue;
			//System.out.println(queryStr);
			SearchResult=vs.searchQuery(queryObject, course,usageList,inputQuestion);

			if(!SearchResult.isEmpty()){
				/*对结果进行进一步的处理,抽出词语中的某个字或词的解释、拼音*/
				SearchResult=FurtherProcess.ExtractWord(course, inputQuestion, SearchResult);
				SearchArray.addAll(SearchResult);		
			}
		}
		//System.out.println(SearchArray.toString());
		return SearchArray;
	}
	
	/**
	 * 利用相关于这个属性查询结果
	 * @param inputQuestion
	 * @param course
	 * @param prefixMap
	 * @param usageList
	 */
	public void relateToAnswer(String inputQuestion,String course,Map<String, String> prefixMap, List<String> usageList){
		
		DecideTriple Triples=new DecideTriple();
		
		List<String> termlist=new ArrayList<String>();
		termlist.addAll(courseTerms.keySet());		
		List<String> subjectList=DecideSubject.DecideByMap(termlist, inputQuestion, course);
		subjectList=RemoveDuplicate.removeContians(subjectList, inputQuestion, prefixMap, course, "");
		
		GenerateQuery gquery = new GenerateQuery();
		VirtuosoSearch virtuosoSearchs=new VirtuosoSearch(course);
		
		for(int j=0;j<subjectList.size();j++){
			for(int t=0;t<tamplateList.size();t++){

				Tamplate tamplate=tamplateList.get(t);
				if(tamplate.getPriority()==3) continue;
				
				Triples.qianXiangguan(prefixMap, tamplate, subjectList.get(j), inputQuestion,course);						
				
				List<QueryElement> sv = Triples.queryElementList;
				
				gquery.generateQuery(sv,course);

				List<QueryObject> queryList = gquery.queryList;
				queryList=RemoveDuplicate.removeQueryObject(queryList);
				
				for (QueryObject queryObject: queryList) {
					String queryStr=queryObject.getQuery();
					if(queryStr.contains("null")) break;
					System.out.println(queryStr);
					
					result=virtuosoSearchs.searchQuery(queryObject, course,usageList,inputQuestion);
					if(!result.isEmpty()) resultArray.addAll(result);
				}
			}
		}
	}
	
	/**
	 * 语文根据解释、简介这两个属性查询结果
	 * @param inputQuestion
	 * @param course
	 * @param prefixMap
	 * @param usageList
	 * @param typeList
	 */
	public void ChineseAnswer(String inputQuestion,String course,Map<String, String> prefixMap, List<String> usageList,List<String> typeList){
		
		DecideTriple Triples=new DecideTriple();
		List<String> chineseList=new ArrayList<String>();
		chineseList.add("explanation");
		chineseList.add("jianjie");
		GenerateQuery gquery=new GenerateQuery();
		VirtuosoSearch virtuosoSearchs=new  VirtuosoSearch(course);
		String temp="";
		
		for(int j=0;j<subjectList.size();j++){
			for(int t=0;t<chineseList.size();t++){
				try {
					Triples.explanationTriple(subjectList.get(j),prefixMap,chineseList.get(t));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}						
				
				List<QueryElement> sv = Triples.queryElementList;

				gquery.generateQuery(sv,course);

				List<QueryObject> queryList = gquery.queryList;
				queryList=RemoveDuplicate.removeQueryObject(queryList);
				
				for (QueryObject queryObject: queryList) {
					String queryStr=queryObject.getQuery();
					if(queryStr.contains("null")) break;
					System.out.println(queryStr);

					result=virtuosoSearchs.searchQuery(queryObject, course,usageList,inputQuestion);
					if(!result.isEmpty()) break;
				}
			}
			
			//对结果进行筛选
			ObjectMapper mapper=new ObjectMapper();
			if(!result.isEmpty()){
				String title="";
				for(int t=0;t<result.size();t++) {
	            	
					AnswerObject ansObj=new AnswerObject();
	        		JSONObject jsonObject=(JSONObject) result.get(t);      		
	        		
	    			try {
						ansObj=mapper.readValue(jsonObject.toString(), AnswerObject.class);
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}
	            	title=ansObj.getSubject();
	            	String item=ansObj.getValue();
	            	
	            	List<Term> wordList=WordParser.CutWord(title);
	            	if(inputQuestion.startsWith(title)||inputQuestion.contains("《"+title+"》")||inputQuestion.startsWith("“"+title+"”")||wordList.get(0).nature==Nature.g||wordList.get(0).nature==Nature.i||wordList.get(0).nature==Nature.nr||wordList.get(0).nature==Nature.nrf||wordList.get(0).nature==Nature.nrj){
	            		if(item.equals(temp))continue;
		            	temp=item;
		            	resultArray.add(ansObj);

	            	}
	            }
			}
		}
		/*抽取语文解释属性中的作者、作品等*/
		resultArray=FurtherProcess.ExtractPersonProperty(typeList, resultArray,subjectName,inputQuestion);
	}
	
	/**
	 * 英语：根据同义词确定英译汉这一题型的答案
	 * @param inputQuestion
	 * @param course
	 * @param prefixMap
	 * @param usageList
	 * @param typeList
	 */
	public void EnglishAnswer(String inputQuestion,String course,Map<String, String> prefixMap, List<String> usageList,List<String> typeList){
		
		DecideTriple decideTriple=new DecideTriple();
		VirtuosoSearch virtuosoSearchs=new VirtuosoSearch(course);
		
		GenerateQuery gquery =new GenerateQuery();
		List<QueryElement> sv=new ArrayList<QueryElement>();
		if(typeList.contains("chinese")){
			decideTriple.SynonymVariableList(prefixMap, inputQuestion);		
		}
		else if(typeList.contains("fanyici")){
			decideTriple.AntonymVariableList(prefixMap, inputQuestion, courseTerms);
		}
		sv=decideTriple.queryElementList;
		if(sv!=null&&!sv.isEmpty()){
			gquery.generateQuery(sv,course);

			List<QueryObject> queryList = gquery.queryList;
		
			for (QueryObject queryObject : queryList) {
				String queryStr=queryObject.getQuery();
				if(queryStr.contains("null")) continue;
				System.out.println(queryStr);
				
				resultArray=virtuosoSearchs.searchQuery(queryObject, course,usageList,inputQuestion);
				
				resultArray=FurtherProcess.ExtractWord(course, inputQuestion, resultArray);
			}
		}
	}
	
	/**
	 * 历史学科查询语文知识库
	 * @param inputQuestion
	 * @param course
	 * @param prefixMap
	 * @param usageList
	 * @param typeList
	 * @param path
	 */
	public void HistorySearchChinese(String inputQuestion,String course,Map<String, String> prefixMap, List<String> usageList,List<String> typeList,String path){

		
		List<Tamplate> tamplateList2=new ArrayList<Tamplate>();
		PatternMatching patternMatching = new PatternMatching();
		patternMatching.Matching(inputQuestion, "chinese");
		tamplateList2=patternMatching.getTamplateList();
		
		patternMatching=new PatternMatching();
		patternMatching.Matching(inputQuestion,"Common");
		List<Tamplate> CommontamplateList = patternMatching.getTamplateList();
			
		tamplateList2.addAll(CommontamplateList);
		if(tamplateList.isEmpty()&&!tamplateList2.isEmpty())
			tamplateList=tamplateList2;

		result=firstResult("chinese", prefixMap, inputQuestion, usageList,tamplateList2,false,typeList,path);
		result=FurtherProcess.ExtractPerson(result, inputQuestion,typeList);
		if(!result.isEmpty()){
			resultArray.addAll(result);
		}
	}
	
	
	/**
	* 主语模糊查询
	* @param course
	* @param prefixMap
	* @param inputQuestion
	* @param usageList
	*/
	public void SubjectFilterSearch(String course, Map<String, String> prefixMap, String inputQuestion, List<String> usageList){
		
		VirtuosoSearch virtuosoSearch=new VirtuosoSearch(course);
		
		map=new HashMap<String, Integer>();
		DecideTriple decideTriple1=new DecideTriple();

		GenerateQuery gquery1 =new GenerateQuery();

		decideTriple1.SubjectFilter(tamplateList, course, prefixMap,inputQuestion,courseTerms,courseMap);
		
		List<QueryElement> sv1=new ArrayList<QueryElement>();
		
		sv1=decideTriple1.queryElementList;
	
		gquery1.generateQuery(sv1,course);

		List<QueryObject> queryList1 = gquery1.queryList;
		queryList1=RemoveDuplicate.remove(queryList1);
	
		for (QueryObject queryObject: queryList1) {
			String queryStr=queryObject.getQuery();
			if(queryStr.contains("null")) continue;
			System.out.println(queryStr);
			result=virtuosoSearch.searchQuery(queryObject, course,usageList,inputQuestion);
			
			result=FurtherProcess.ExtractWord(course, inputQuestion, result);

		}
	}
	
	
	/**
	* 精确查询，只查询模板生成的查询语句
	* @param course
	* @param inputQuestion
	* @return
	* @throws Exception
	*/
	public JSONArray preciseAnswer(String course,String inputQuestion) throws Exception{
		
		SetPrefix prefixes = new SetPrefix(course);
		Map<String, String> prefixMap = prefixes.getPrefixes();
		
		String path=request.getSession().getServletContext().getRealPath("");
		if(path.contains("\\.metadata"))
			
			path=path.split("\\.metadata")[0]+"KnowledgeQA/";
		else path=System.getProperty("user.dir").split("bin")[0]+"webapps\\KnowledgeQA\\";
		path =  "F:\\eclispseworkspace\\KnowledgeQA\\";
		
		inputQuestion=inputQuestion.trim();
		
		/*分词处理*/
		List<Term> words = WordParser.CutWord(inputQuestion);
		System.out.println(words.toString());
		
		/*对问句进行模板匹配*/
		
		PatternMatching patternMatching=new PatternMatching();
		patternMatching.Matching(inputQuestion, course);
		tamplateList=patternMatching.getTamplateList();
		
		patternMatching=new PatternMatching();
		patternMatching.Matching(inputQuestion,"Common");
		List<Tamplate> CommontamplateList=patternMatching.getTamplateList();
			
		tamplateList.addAll(CommontamplateList);

		List<String> typeList=new ArrayList<String>();
		List<String> usageList=new ArrayList<String>();
		for(Tamplate tamplate:tamplateList){
//			System.out.println(tamplate.getContent());
			String type=tamplate.getType().toLowerCase();
			typeList.add(type);
			usageList.add(tamplate.getUsage());
		}
		typeList=RemoveDuplicate.remove(typeList);
		
		/*判断问句是否由匹配到可用的模板，有则进行之后的分析*/
		JSONArray resultSet=new JSONArray();
		if(tamplateList.size()>0){
			 
			resultSet=firstResult(course, prefixMap, inputQuestion, usageList,tamplateList,true,typeList,path);
		}
		return resultSet;
	}
	
	
	/**
	* 数学问答查询
	* @param question
	*/
	public void AnswerMath(String question){

		try {
			question=URLEncoder.encode(question,"UTF-8");
			System.out.println(question);
			String queryUrl = "http://kb.cs.tsinghua.edu.cn:28090/server/getAns?question="+question;

	        String response=HttpConnectUtil.getHttp(queryUrl);
            //System.out.println(response);

	        if(response!=null&&!response.equals("")){
		        JSONArray json=JSONArray.fromObject(response);
		        for(int i=0;i<json.size();i++){
		        	String item=json.getString(i).replaceAll("<", "&lt;").replaceAll(">", "&gt;");
		        	item+="<br>";
		        	AnswerObject answerObject=new AnswerObject();
		        	answerObject.setValue(item);
		        	resultArray.add(answerObject);
		        }
	        }
	        
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public List<Tamplate> getTamplateList() {
		return tamplateList;
	}

	public void setTamplateList(List<Tamplate> tamplateList) {
		this.tamplateList = tamplateList;
	}

	public JSONArray getResultArray() {
		return resultArray;
	}

	public void setResultArray(JSONArray resultArray) {
		this.resultArray = resultArray;
	}

}
