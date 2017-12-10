package com.ld.answer;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import com.ld.IO.StringProcess;
import com.ld.IO.remove.RemoveDuplicate;
import com.ld.Parser.WordParser;
import com.ld.model.AnswerObject;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：FurtherProcess   
* 类描述： 对结果的后续抽取
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:26:40   
* @version        
*/
public class FurtherProcess {

	
	/**
	 * 计算存在时间
	 * @param resultArray
	 * @param usageList
	 * @return
	 * @throws ParseException
	 */
	public static JSONArray TotalTime(JSONArray resultArray,List<String> usageList) throws ParseException{
		
		JSONArray results=new JSONArray();
		AnswerObject answerObject=new AnswerObject();
		SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日");

		int begintime = 0;
		int endtime = 0;
		int today=0;
		int time=0;
		
		Date first =new Date();
		Date last=new Date();
		long month=0;
		String regex="[\\u4E00-\\u9FFF]+";
		Pattern pattern=Pattern.compile(regex);
		ObjectMapper mapper=new ObjectMapper();
		int count=0;

		for(int i=0;i<resultArray.size();i++) {
        	if(count>1)break;
			AnswerObject ansObj=new AnswerObject();
    		JSONObject jsonObject=(JSONObject) resultArray.get(i);
    		
			try {
				ansObj=mapper.readValue(jsonObject.toString(), AnswerObject.class);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
//        	answerObject=ansObj;
        	String item=ansObj.getValue().replaceAll("。", "");
        	String predicate=ansObj.getPredicate();
        	if(item.contains("末年")) break;
        	item=item.replaceAll("<br>", "");
        	
        	if(item.contains("<br>")){
//        		String start=item.substring(0,item.indexOf(":"));
        		String temp=item;
	        	String[] array=temp.split("<br>");
	        	if(array.length>1){
	        		if(array[0].contains(array[1])) 
	        			item=array[0];
	        		else if(array[1].contains(array[0])) 
	        			item=array[1];
//	        		item=start+":"+item;
	        	}	
	        	else item=item.replace("<br>", "");
        	}
        	
        	//处理开始时间
			if(predicate.equals("开始时间")){
				count++;
				answerObject=ansObj;
				String begin=item.replaceAll("。", "");
				if(item.contains("月")){
					begin=item.substring(item.indexOf(":")+1);
					if(begin.contains("日")){
						first = df.parse(begin);
					}
					else{
						begin=begin+"1日";
//						if(!pattern.matcher(begin).find())
						first = df.parse(begin);
					}		
				}
				//将开始时间转化为整型
				else{
					if(item.contains("年"))
						begin=begin.substring(0,begin.indexOf("年"));
	        		if(begin.contains("公元")) begin=begin.substring(begin.indexOf("公元")+2);
	        		
	        		if(begin.contains("万")) {
	        			begin=begin.replace("万", "0000");
	        			
	        		}
	        		
	        		if(begin.contains("前")) {
	        			begin=begin.substring(begin.indexOf("前")+1);
	        			begintime=-Integer.parseInt(begin);
	        		}
	        		else if(!pattern.matcher(begin).find())
	        			begintime=Integer.parseInt(begin);
					}
        		
			}
			
			//处理结束时间
			if(predicate.equals("结束时间")){
				
				count++;
				String end=item.substring(item.indexOf(":")+1);
				
				if(item.contains("月")){
					
					if(end.contains("日"))
						last = df.parse(end);
					else{
						end=end+"1日";
						last = df.parse(end);
					}
				}
				//将结束时间转化为整型
				else{
					if(item.contains("年"))
						end=end.substring(0,end.indexOf("年"));
					if(end.contains("公元")) end=end.substring(end.indexOf("公元")+2);
					
					if(end.contains("万")) {
						end=end.replace("万", "0000");
						
					}
	        		
	        		if(end.contains("前")){
	        			end=end.substring(end.indexOf("前")+1);
	        			endtime=-Integer.parseInt(end);
	        		}
	        		else if(!pattern.matcher(end).find())
	        			endtime=Integer.parseInt(end);
				}
			}
			
			//计算存在时间：结束时间-开始时间
			if(endtime>0&&begintime<0){
				time=endtime-begintime-1;
			}
			
			else if((endtime>0&&begintime>0)||(endtime<0&&begintime<0)&&endtime!=0&&begintime!=0) time=endtime-begintime;
			if(first!=null&&last!=null) {
				month=(last.getMonth()-first.getMonth());
			}
        }
        
        if(time!=0) {
        	answerObject.setPredicate("存在时间");
        	answerObject.setValue(time+"年（非直接查询出的结果，经后续处理得到的答案）");
        	results.add(answerObject);
        }
        else if(month!=0) {
        	answerObject.setPredicate("存在时间");
        	answerObject.setValue(month+"个月（非直接查询出的结果，经后续处理得到的答案）");
        	results.add(answerObject);
        }
		
        //计算距离今天多长时间
		if(usageList.contains("today")){
			Calendar data = Calendar.getInstance();
			today=data.get(Calendar.YEAR);
			time=today-begintime;
			if(begintime!=0){
				answerObject.setPredicate("距今年代");
	        	answerObject.setValue(time+"年（非直接查询出的结果，经后续处理得到的答案）");
	        	results.add(answerObject);
			}
		}
        
        if(results.isEmpty()) results=resultArray;
		
		return results;
	}
	
	/**
	 * 语文：抽取成语中的某个字解释或读音
	 * @param course
	 * @param question
	 * @param resultArray
	 * @return
	 */
	public static JSONArray ExtractWord(String course,String question,JSONArray resultArray){
		
		JSONArray results=new JSONArray();
		Pattern pattern;
		Matcher matcher;
		Pattern pattern1;
		Matcher matcher1;
		Pattern pattern2;
		Matcher matcher2;
		ObjectMapper mapper=new ObjectMapper();
		
		if(course.equals("chinese")){
			String regex="(.*)?(中)?的(?<title>(.*)?)(什么|何|怎么|的|是指|指的)(是)?(什么|指|意思|解释|释义|含义)(.*)?";
			String regex1="(.*)?(中)?的(?<title>(.*)?)(发什么音|怎么读|怎么念)(.*)?";
			String regex2="(.*)?中(?<title>(.*)?)的(拼音|读音)(.*)?";
			pattern=Pattern.compile(regex);
			matcher=pattern.matcher(question);
			
			//抽取成语中的某个词的解释
			if(matcher.find()){
				String title=matcher.group("title");
				if(title.contains("“")&&title.contains("”")) title=title.replace("“", "").replace("”", "");
				if(title.contains("是")) title=title.replace("是", "");
				if(title.contains("的")) title=title.replace("的", "");
				if(title.contains("指")) title=title.replace("指", "");
				
	            for(int i=0;i<resultArray.size();i++) {
	            	
	            	AnswerObject ansObj=new AnswerObject();
	        		JSONObject jsonObject=(JSONObject) resultArray.get(i);
	        		
	    			try {
	    				ansObj=mapper.readValue(jsonObject.toString(), AnswerObject.class);
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}
	            	
	            	String item=ansObj.getValue();
	            	
	            	if(item.contains(title+"：")) {
	            		item=replace(item,title);//词语解释的抽取
	            		ansObj.setValue(item+"（非直接查询出的结果，经后续处理得到的答案）");
	            		ansObj.setPredicate("");
	            		results.add(ansObj);
	            	}
	            }
			}
			
			pattern1=Pattern.compile(regex1);
			matcher1=pattern1.matcher(question);
			pattern2=Pattern.compile(regex2);
			matcher2=pattern2.matcher(question);
			
			//抽取词语中某个词的读音
			if(matcher1.find()){
				String title=matcher1.group("title");
				if(title.contains("是")) title=title.replace("是", "");
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
	            	String item=ansObj.getValue();
	            	
            		item=replace1(item,title,subject);//抽取字的拼音
            		
            		if(item!=null&&!"".equals(item)){
            			ansObj.setValue(item+"（非直接查询出的结果，经后续处理得到的答案）");
            			ansObj.setPredicate("");
                		results.add(ansObj);
            		}	            	
	            }
			}
			if(matcher2.find()){
				String title=matcher2.group("title");
				if(title.contains("是")) title=title.replace("是", "");
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
	            	String item=ansObj.getValue();
	            	
            		item=replace1(item,title,subject);
            		
            		if(item!=null&&!"".equals(item)){
            			ansObj.setValue(item+"（非直接查询出的结果，经后续处理得到的答案）");
            			ansObj.setPredicate("");
                		results.add(ansObj);
            		}	            	
	            }
			}
		}
		if(results.isEmpty())
		 results=resultArray;
		return results;
	}
	
	/**
	 * 在语文的解释中抽取语文常用的属性：作者、字、号
	 * @param typeList
	 * @param resultArray
	 * @param subject
	 * @param question
	 * @return
	 */
	public static JSONArray ExtractPersonProperty(List<String> typeList,JSONArray resultArray,String subject,String question){
		
		JSONArray results=new JSONArray();
//		Pattern pattern;
//		Matcher matcher;
//		Pattern pattern1;
//		Matcher matcher1;
//		Pattern pattern2;
//		Matcher matcher2;
		ObjectMapper mapper=new ObjectMapper();
		List<Term> words=WordParser.CutWord(question);
		int ryCount=0;
		for(Term word:words){
			if(word.nature.toString().startsWith("ry")){
				ryCount++;
			}
		}
		for(String usage : typeList){
			String temp="";
			
			//抽取作者
			if(usage.equals("author")&&ryCount<=1){
				
				for(int i=0;i<resultArray.size();i++) {
	            	
					AnswerObject ansObj=new AnswerObject();
	        		JSONObject jsonObject=(JSONObject) resultArray.get(i);
	        		
	    			try {
	    				ansObj=mapper.readValue(jsonObject.toString(), AnswerObject.class);
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}            	
	            	String item=ansObj.getValue();
	            	String predicate=ansObj.getPredicate();
	            	
	            	//根据关键词抽取作者
	            	if(predicate.equals("解释")||predicate.equals("简介")){
		            	temp=split(item,"写于");
		            	if(temp.isEmpty()||item.equals(temp)) temp=split(item,"创作于");
		            	if(temp.isEmpty()||item.equals(temp)) temp=split(item,"写作于");
		            	if(temp.isEmpty()||item.equals(temp)) temp=split(item,"作于");
		            	if(temp.isEmpty()||item.equals(temp)) temp=split(item,"作");
		            	if(temp.isEmpty()||item.equals(temp)) temp=split(item,"著");
		            	if(item.equals(temp)) temp="";
		            	if(!temp.isEmpty()){
		            		List<Term> wordsList=WordParser.CutWord(temp);
		            		for(Term word:wordsList){
		            			if(word.nature==Nature.nr||word.nature==Nature.nrf||word.nature==Nature.nrj||word.nature==Nature.n)
		            				temp=word.word;
		            		}
		            		ansObj.setPredicate("作者");
		            		ansObj.setValue(temp+"（非直接查询出的结果，经后续处理得到的答案）");
		            		results.add(ansObj);
		            	}
	            	}
	            }
			}
			//抽取人物的字
			else if(usage.equals("personzi")){
				for(int i=0;i<resultArray.size();i++) {
	            	
					AnswerObject ansObj=new AnswerObject();
	        		JSONObject jsonObject=(JSONObject) resultArray.get(i);
	        		
	    			try {
	    				ansObj=mapper.readValue(jsonObject.toString(), AnswerObject.class);
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}	            	
	            	String item=ansObj.getValue();
	            	String predicate=ansObj.getPredicate();
	            	
	            	if(predicate.equals("解释")||predicate.equals("简介")){
		            	temp=split2(item,"字",1);
		            	if(temp.contains("，"))temp=temp.substring(0, temp.indexOf("，"));
		            	if(!temp.isEmpty()){
		            		ansObj.setPredicate("字");
		            		ansObj.setValue(temp+"（非直接查询出的结果，经后续处理得到的答案）");
		            		results.add(ansObj);
	            		}
	            	}
	            }
			}
			//抽取人物的号
			else if(usage.equals("personhao")){
				for(int i=0;i<resultArray.size();i++) {
	            	
					AnswerObject ansObj=new AnswerObject();
	        		JSONObject jsonObject=(JSONObject) resultArray.get(i);
	        		
	    			try {
	    				ansObj=mapper.readValue(jsonObject.toString(), AnswerObject.class);
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}	            	
	            	String item=ansObj.getValue();
	            	String predicate=ansObj.getPredicate();
	            	
	            	if(predicate.equals("解释")||predicate.equals("简介")){
		            	temp=split2(item,"号",1);
		            	if(!temp.isEmpty()){
		            		ansObj.setPredicate("号");
		            		ansObj.setValue(temp+"（非直接查询出的结果，经后续处理得到的答案）");
		            		results.add(ansObj);
		            	}
	            	}
	            }
			}
			//抽取人物所处时代
			else if(usage.equals("time")){
				for(int i=0;i<resultArray.size();i++) {
	            	
					AnswerObject ansObj=new AnswerObject();
	        		JSONObject jsonObject=(JSONObject) resultArray.get(i);
	        		
	    			try {
	    				ansObj=mapper.readValue(jsonObject.toString(), AnswerObject.class);
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}         	
	            	String item=ansObj.getValue();
	            	String predicate=ansObj.getPredicate();
	            	
	            	if(predicate.equals("解释")||predicate.equals("简介")){
		            	temp=split3(item,"朝",1);
		            	if(temp.isEmpty()||temp.length()>8) temp=split3(item,"代",1);
		            	if(temp.contains("古代")) temp="";
		            	
	//	            	temp=split3(item,"国",1);
		            	
		            	if(item.equals(temp)) temp="";
		            	if(!temp.isEmpty()){
		            		ansObj.setPredicate("时代");
		            		ansObj.setValue(temp+"（非直接查询出的结果，经后续处理得到的答案）");
		            		results.add(ansObj);
		            	}
	            	}
	            }
			}
			//抽取人物的籍贯
			else if(usage.equals("address")){
				for(int i=0;i<resultArray.size();i++) {
	            	
					AnswerObject ansObj=new AnswerObject();
	        		JSONObject jsonObject=(JSONObject) resultArray.get(i);
	        		
	    			try {
	    				ansObj=mapper.readValue(jsonObject.toString(), AnswerObject.class);
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}            	
	            	String item=ansObj.getValue();
	            	String predicate=ansObj.getPredicate();
	            	
	            	if(predicate.equals("解释")||predicate.equals("简介")){
		            	temp=split3(item,"人",0);
		            	
		            	if(!temp.isEmpty()){
		            		String nsString="";
		            		List<Term> wordsList=WordParser.CutWord(temp);
		            		for(int t=0;t<wordsList.size();t++){
		            			if(wordsList.size()==2&&wordsList.get(0).nature==Nature.ns&&wordsList.get(1).nature==Nature.ns)
		            				nsString=temp;
		            			else if(wordsList.get(t).nature==Nature.ns)
		            				nsString=wordsList.get(t).word;
		            		}
		            		if(nsString!=null&&!nsString.equals("")){
			            		ansObj.setPredicate("籍贯");
			            		ansObj.setValue(nsString+"（非直接查询出的结果，经后续处理得到的答案）");
			            		results.add(ansObj);
			            	}
	            		}
	            	}
	            }
			}
			//抽取主要作品
			else if(usage.equals("literature")&&ryCount<=1){
				
				for(int i=0;i<resultArray.size();i++) {
	            	
					AnswerObject ansObj=new AnswerObject();
	        		JSONObject jsonObject=(JSONObject) resultArray.get(i);
	        		
	    			try {
	    				ansObj=mapper.readValue(jsonObject.toString(), AnswerObject.class);
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}            	
	            	String item=ansObj.getValue();
	            	String predicate=ansObj.getPredicate();
	            	
	            	if(predicate.equals("解释")||predicate.equals("简介")){
		            	String temp1=split2(item,"著有",2);
		            	String temp2=split2(item,"写有",2);
		            	String temp3=split2(item,"写成了",3);
		            	String temp4=split2(item,"创作了",3);
	//	            	String temp5=split2(item,"出版了",3);
		            	String temp6=split2(item,"创作有",3);
		            	String temp7=split2(item,"代表作品有",0);
		            	String temp8=split2(item,"代表作有",0);
		            	
		            	temp=temp1+temp2+temp3+temp4+temp6+temp7+temp8;
		            	if(temp!=null&&!temp.equals("")){
			            	ansObj.setPredicate("主要作品");
		            		ansObj.setValue(temp+"（非直接查询出的结果，经后续处理得到的答案）");
		            		results.add(ansObj);
		            	}
	            	}
	            }
			}
			//抽取作品出处
			else if(usage.equals("wordsoure")||usage.equals("source")){
				
				for(int i=0;i<resultArray.size();i++) {
	            	
					AnswerObject ansObj=new AnswerObject();
	        		JSONObject jsonObject=(JSONObject) resultArray.get(i);
	        		
	    			try {
	    				ansObj=mapper.readValue(jsonObject.toString(), AnswerObject.class);
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}        	
	            	String item=ansObj.getValue();
	            	String predicate=ansObj.getPredicate();
	            	
	            	if(predicate.equals("解释")||predicate.equals("简介")){
		            	String temp1=splitBymark(item,"载",1);
		            	String temp2=split2(item,"出自",2);
		            	
		            	temp=temp1+temp2;
		            	if(temp.contains("《")&&temp.contains("》"))
		            		temp=temp.substring(temp.indexOf("《"),temp.indexOf("》")+1);
		            	System.out.println(temp);
		            	if(!temp.isEmpty()){
		            		ansObj.setPredicate("出自");
		            		ansObj.setValue(temp+"（非直接查询出的结果，经后续处理得到的答案）");
		            		results.add(ansObj);
		            	}
	            	}
	            }
			}
			//抽取人物的职业
			else if(usage.equals("occupation")){
				temp="";
				for(int i=0;i<resultArray.size();i++) {
	            	
					AnswerObject ansObj=new AnswerObject();
	        		JSONObject jsonObject=(JSONObject) resultArray.get(i);
	        		
	    			try {
	    				ansObj=mapper.readValue(jsonObject.toString(), AnswerObject.class);
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}         	
	            	String item=ansObj.getValue();
	            	String title=ansObj.getSubject();
	            	String predicate=ansObj.getPredicate();
	            	
	            	if(predicate.equals("解释")||predicate.equals("简介")){
		            	List<Term> cutword=WordParser.CutWord(title);
		            	if(question.startsWith(title)||cutword.get(0).nature==Nature.nr||cutword.get(0).nature==Nature.nrf||cutword.get(0).nature==Nature.nrj){
		            		String[] array=item.split("。");
		            		for(int t=0;t<array.length;t++){
				            	List<Term> wordsList=WordParser.CutWord(array[t]);
				            	int count=0;
				            	int AndCount=StringProcess.CountNumber(array[t], "和");
				            	int MarkCount=StringProcess.CountNumber(array[t], "、");
				            	
			        			for(Term word:wordsList){
			        				if(word.nature==Nature.nnt||word.nature==Nature.nnd){
			        					temp+=word.word+"、";
			        					count++;
			        				}
			        			}
			        			
			        			if(count<AndCount+MarkCount){
			        				String[] SplitAnd=array[0].split("和");
			        				for(int k=0;k<SplitAnd.length;k++){
			        					if(SplitAnd[0].contains(temp.replace("、", ""))) continue;
			        					else temp+=SplitAnd[0];
			        					if(k!=0&&(SplitAnd[k].contains("家")||SplitAnd[k].contains("人")))
			        							temp+=SplitAnd[k]+"、";
			        				}
			        			}
			        			if(temp.endsWith("、"))temp=temp.substring(0,temp.lastIndexOf("、"));
			        			if(temp!=null&&!temp.equals("")) break;
		            		}
		        			if(!temp.equals("")){
		        				ansObj.setPredicate("职业");
			            		ansObj.setValue(temp+"（非直接查询出的结果，经后续处理得到的答案）");
			            		results.add(ansObj);
		        			}
		            	}
		            }
				}
			}
		}
		if(results.isEmpty()) results=resultArray;
			
		return results;
	}
	
	/**
	 * 抽取人物
	 * @param resultArray
	 * @param question
	 * @param typeList
	 * @return
	 */
	public static JSONArray ExtractPerson(JSONArray resultArray,String question,List<String> typeList){
		
		ObjectMapper mapper=new ObjectMapper();
		JSONArray results=new JSONArray();
		String temp="";
		List<String> tempList=new ArrayList<String>();
		List<Term> words=new ArrayList<Term>();
		List<Term> qwords=WordParser.CutWord(question);
		String tag="";
		int count=0;
		
//		if(resultObject.size()>1&&question.contains("谁")||question.contains("哪位")||question.contains("哪一位")){
		if(question.contains("谁")||question.contains("哪位")||question.contains("哪一位")){
			
//			SemanticSimilarity semanticSimilarity=SemanticSimilarity.getInstance();
			if(question.contains("《")&&question.contains("》"))
				tag=question.substring(question.indexOf("《"),question.indexOf("》")+1);
			if(tag.equals("")||tag==null)
				for(Term word:qwords){
					if(word.nature==Nature.g||word.nature==Nature.nz||word.nature==Nature.i||word.nature==Nature.nr)
						tag=word.word;
					if(word.nature.toString().startsWith("ry")||word.word.equals("怎样"))
						count++;
				}
			
			String name="";
			String predice="";
			String maxitem="";
			AnswerObject answerObject=new AnswerObject();
			if(count==1){
				for(int i=0;i<resultArray.size();i++) {
	            	
					AnswerObject ansObj=new AnswerObject();
	        		JSONObject jsonObject=(JSONObject) resultArray.get(i);
	        		
	    			try {
	    				ansObj=mapper.readValue(jsonObject.toString(), AnswerObject.class);
	    			} catch (IOException e) {
	    				// TODO Auto-generated catch block
	    				e.printStackTrace();
	    			}         	
	            	String item=ansObj.getValue();
	            	if(item.contains("非直接查询出的结果，经后续处理得到的答案")) continue;
		         	if(question.contains(temp)) temp="";
		         	words=HanLP.segment(temp);
		         	 
		         	//根据词性抽取
		         	if(words.size()==1&&(words.get(0).nature==Nature.nr||words.get(0).nature==Nature.nrf||words.get(0).nature==Nature.nrj)&&!question.contains(temp))
		         		tempList.add(temp);
			         
		         	//根据关键词抽取
					if(predice.contains("别称")||predice.contains("者")||predice.contains("人")||predice.contains("开国君主")||predice.contains("身份:")){
						results.add(ansObj);
						continue;
					}
					if(typeList.contains("author")&&item.contains("解释:")){
						
						item=item.split(":")[1];
		            	
		            	temp=split(item,"写于");
		            	if(temp.isEmpty()||item.equals(temp)) temp=split(item,"创作于");
		            	if(temp.isEmpty()||item.equals(temp)) temp=split(item,"写作于");
		            	if(temp.isEmpty()||item.equals(temp)) temp=split(item,"作于");
		            	if(temp.isEmpty()||item.equals(temp)) temp=split(item,"作");
		            	if(temp.isEmpty()||item.equals(temp)) temp=split(item,"著");
		            	
		            	if(!temp.equals("")&&temp!=null&&!temp.equals(item)&&!question.contains(temp)){
		    	         
		            		List<Term> CutWords=HanLP.segment(temp);
		            		if(CutWords.size()>1){
		            			for(Term word:CutWords){
		            				if(word.nature==Nature.nr||word.nature==Nature.nrf||word.nature==Nature.nrj){
		            					temp=word.word;
		            					name=temp;
	            					}
		            			}
		            		}
		            		tempList.add(temp);
		            	}
					}
					else if(words.size()==1&&(words.get(0).nature==Nature.nr||words.get(0).nature==Nature.nrf||words.get(0).nature==Nature.nrj)&&!question.contains(words.get(0).word))
						name=temp;
					else {
						temp=getMostFrequency(item,question);
						if(temp!=null&&!temp.equals("")) tempList.add(temp);
					}		
				 }
			}
			else return resultArray;
			 if(maxitem!=null&&!maxitem.equals("")){

				 name=getMostFrequency(maxitem,question);
				 
			 }
			 tempList=RemoveDuplicate.remove2(tempList);
		     if(tempList.size()==1){
	         	answerObject.setValue(tempList.get(0)+"（非直接查询出的结果，经后续处理得到的答案）");
		     }
	         else if(!name.equals("")&&name!=null){
	        	 answerObject.setValue(name+"（非直接查询出的结果，经后续处理得到的答案）");
	        	 results.add(answerObject);
	         }
		}
		
		if(results.isEmpty()) 
			results=resultArray;
		
		return results;
	}
	
	/**
	 * 语文：抽取诗句的上下句
	 * @param resultArray
	 * @param question
	 * @param usageList
	 * @return
	 */
	public static JSONArray nextProcess(JSONArray resultArray,String question,List<String> usageList){
		
		JSONArray results=new JSONArray();
		AnswerObject answerObject=new AnswerObject();
		ObjectMapper mapper=new ObjectMapper();
		boolean flag=true;
		if(!resultArray.isEmpty()&&(usageList.contains("nextContent")||usageList.contains("lastContent"))){
            for(int i=0;i<resultArray.size();i++) {
            	AnswerObject ansObj=new AnswerObject();
        		JSONObject jsonObject=(JSONObject) resultArray.get(i);
        		
    			try {
    				ansObj=mapper.readValue(jsonObject.toString(), AnswerObject.class);
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
            	answerObject=ansObj;
            	String value=ansObj.getValue();
            	String predicate=ansObj.getPredicate();
            	String subject=ansObj.getSubject();
	         	if(value.contains("非直接查询出的结果，经后续处理得到的答案")) break;
	         	if(subject.startsWith("【")||subject.contains("（集句）"))continue;
	         	if(predicate.equals("内容")||predicate.equals("")){
		         	String bracket="";
		         	if(value.contains("（")&&value.contains("）"))
		         		bracket = value.substring(value.indexOf("（"),value.indexOf("）")+1);
		         	else if(value.contains("(")&&value.contains(")")){
		         		bracket = value.substring(value.indexOf("("),value.indexOf(")")+1);
	
		         	}
		         	value=value.replaceAll("<br>", "").replaceAll("&nbsp;", "").replace(bracket,"");
		         	
		         	//将全部内容以符号分割
		         	String[] array=value.split("。|！|？|“|”");
					for(int k=0;k<array.length;k++){
						if(!flag) break;
						if(array[k].equals("")||array[k].equals(" ")) continue;
						array[k]=array[k].replace("?", "，");
						String[] items=array[k].split("，|！|？");
//						if(items.length==1) items=array[i].split("！");
//						if(items.length==1) items=array[i].split("？");
						if(items.length==1){
							if(question.contains(array[k].replaceAll(" ", ""))&&usageList.contains("nextContent")&&k+1<array.length){
								answerObject.setValue(array[k+1].replace("？", "")+"（非直接查询出的结果，经后续处理得到的答案）");
							}
							else if(question.contains(array[k].replaceAll(" ", ""))&&usageList.contains("lastContent")&&k-1>=0){
								answerObject.setValue(array[k-1].replace("？", "")+"（非直接查询出的结果，经后续处理得到的答案）");
							}
						}
						else for(int j=0;j<items.length;j++){
							String temp=items[j].replaceAll(" ", "");
							
							if(items[j].contains("？")&&!items[j].endsWith("？")){
								
								if(question.contains(temp))
									items[j]=items[j].split("？")[1];
								else if(question.contains(items[j].split("？")[0].replaceAll(" ", "")))
									items[j]=items[j].split("？")[0];
							}
							
							//确定诗句的下一句
							if(j+1<items.length&&!temp.equals("")&&question.contains(temp)&&usageList.contains("nextContent")){
								if(j+1<items.length&&items[j+1].contains("？")&&!items[j+1].endsWith("？"))
									items[j+1]=items[j+1].substring(0,items[j+1].indexOf("？"));
//								if(items[j].contains("？"))
								answerObject.setValue(items[j+1].replace("？", "")+"（非直接查询出的结果，经后续处理得到的答案）");
								flag=false;
								continue;
							}
							//确定诗句的上一句
							else if(!temp.equals("")&&question.contains(temp)&&usageList.contains("lastContent")){
								if(j-1>=0&&items[j-1].contains("？")&&!items[j-1].endsWith("？"))
									items[j-1]=items[j-1].substring(items[j-1].indexOf("？")+1);
								answerObject.setValue(items[j-1].replace("？", "")+"（非直接查询出的结果，经后续处理得到的答案）");
								
								flag=false;
								break;
							}	
						}		
					}
					if(answerObject!=null&&answerObject.getValue().contains("（非直接查询出的结果，经后续处理得到的答案）")){
		            	answerObject.setPredicate("");
		            	results.add(answerObject);
		            }
				 }	
			 }
            
		}
		
		if(results.isEmpty()) results=resultArray;
		
		return results;
	}
	
	/**
	 * 化学：抽取物质的元素组成
	 * @param resultArray
	 * @param usageList
	 * @return
	 */
	public static JSONArray findElements(JSONArray resultArray,List<String> usageList){
		
		JSONArray results=new JSONArray();
		AnswerObject answerObject=new AnswerObject();
		ObjectMapper mapper=new ObjectMapper();
		boolean flag=true;
		if(!resultArray.isEmpty()&&usageList.contains("Elements")){
            for(int i=0;i<resultArray.size();i++) {
            	AnswerObject ansObj=new AnswerObject();
        		JSONObject jsonObject=(JSONObject) resultArray.get(i);
        		
    			try {
    				ansObj=mapper.readValue(jsonObject.toString(), AnswerObject.class);
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
            	answerObject=ansObj;
            	String value=ansObj.getValue();
            	String predicate=ansObj.getPredicate();
//            	String subject=ansObj.getSubject();
	         	if(value.contains("非直接查询出的结果，经后续处理得到的答案")) break;
	         	if(predicate.equals("组成元素")){
	         		results.add(answerObject);
	         		break;
	         	}
	         	
	         	//将化学式分割得到物质的组成元素
	         	if(predicate.equals("化学式")||predicate.equals("组成的物质")){
		         	String bracket="";
		         	if(value.contains("（")&&value.contains("）"))
		         		bracket = value.substring(value.indexOf("（"),value.indexOf("）")+1);
		         	else if(value.contains("(")&&value.contains(")")){
		         		bracket = value.substring(value.indexOf("("),value.indexOf(")")+1);
	
		         	}
		         	value=value.replaceAll("<br>", "").replaceAll("&nbsp;", "").replace(bracket,"");
		         	Pattern pattern=Pattern.compile("[A-Z]");
		         	char[] array=value.toCharArray();
		         	String elements="";
					for(int k=0;k<array.length;k++){
						
						String character=String.valueOf(array[k]);
						
						if(pattern.matcher(character).find()){
							if(!elements.contains(character))elements+=character+"";
							if(k+1<array.length&&Pattern.compile("[a-z]").matcher(String.valueOf(array[k+1])).find()){
								elements+=array[k+1]+"、";
							}
							else elements+="、";
						}
					}
					
					if(elements!=null&&!elements.equals("")){
						if(elements.endsWith("、"))elements=elements.substring(0,elements.length()-1);
		            	answerObject.setPredicate("组成元素");
		            	answerObject.setValue(elements+"（非直接查询出的结果，经后续处理得到的答案）");
		            	results.add(answerObject);
		            	
		            }
				 }	
			 }
            
		}
		
		if(results.isEmpty()) results=resultArray;
		
		return results;
	}
	
	/**
	 * 语文：抽取成语的相关人物
	 * @param resultArray
	 * @param question
	 * @return
	 */
	public static JSONArray findPerson(JSONArray resultArray,String question){
		
		JSONArray results=new JSONArray();
		
		Pattern pattern;
		Matcher matcher;
		Pattern pattern1;
		Matcher matcher1;
		Pattern pattern2;
		Matcher matcher2;
		
		String regex="(.*)?(主人公|主角|相关(的)?人物)(.*)?";
		String regex1="(.*)?(与|和|同)(谁|哪)(.*)?(有关|相关)(.*)?";
		String regex2="(.*)?指的是(谁|哪)(.*)?";
		pattern=Pattern.compile(regex);
		matcher=pattern.matcher(question);
		pattern1=Pattern.compile(regex1);
		matcher1=pattern1.matcher(question);
		pattern2=Pattern.compile(regex2);
		matcher2=pattern2.matcher(question);
		
		ObjectMapper mapper=new ObjectMapper();
		
		String name="";
		String name1="";
		if(matcher.find()||matcher1.find()||matcher2.find()){
			
			String temp="";
			String source=null;
			AnswerObject answerObject=new AnswerObject();
			List<String> sourceList=new ArrayList<String>();
			List <String> personList=new ArrayList<String>();
			for(int i=0;i<resultArray.size();i++) {
            	
				AnswerObject ansObj=new AnswerObject();
        		JSONObject jsonObject=(JSONObject) resultArray.get(i);
        		
    			try {
    				ansObj=mapper.readValue(jsonObject.toString(), AnswerObject.class);
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
            	answerObject=ansObj;
            	String item=ansObj.getValue();
            	String predicate=ansObj.getPredicate();
	         	
	         	List<String> keyList=HanLP.extractKeyword(item.replaceAll("<br>", "").replaceAll("&nbsp;", ""), 3);
	         	for(String keyword:keyList){
	         		List<Term> cuts=HanLP.segment(keyword);
	         		if(cuts.get(0).nature==Nature.nr||cuts.get(0).nature==Nature.nr1||cuts.get(0).nature==Nature.nrf||cuts.get(0).nature==Nature.nrj||cuts.get(0).nature==Nature.n)
	         			personList.add(keyword);
	         	}
	         	
	         	//根据词性和词频抽成语故事中的人物
	         	if(predicate.equals("成语故事")){
	         		
	         		temp=item;
	         		List<Term> words1=HanLP.segment(item);
		         	for(Term word:words1){
		         		if(word.nature==Nature.nr)
		         			personList.add(word.word);
		         		else if(word.nature==Nature.nz&&word.word.endsWith("公"))
		         			personList.add(word.word);
		         	}
		         	List list=RemoveDuplicate.remove2(personList);
		         	int max=0;
		         	for(int i1=0;i1<list.size();i1++){
		         	
		         		int count=Collections.frequency(personList, list.get(i1));
		         		if(count>=max){
		         			max=count;
		         			name1=(String) list.get(i1);
		         		}
		         	}
	         	}
	         	
	         	//根据词性和关键词抽成语出处中的人物
	         	if(predicate.equals("成语出处")){
	         		if(item.contains("·")){
		         		
	         			int index=item.lastIndexOf("·");
		         		item=item.substring(index+1);
		         		if(item.contains("列传》")) source=item.substring(0,item.indexOf("列传"));
		         		else if(item.contains("传》")) source=item.substring(0,item.indexOf("传"));
		         		else if(item.contains("世家》")) source=item.substring(0,item.indexOf("世家"));
		         		else if(item.contains("本纪》")) source=item.substring(0,item.indexOf("本纪"));
		         		else if(item.contains("《")) source=item.substring(0,item.indexOf("《"));
	         		}
	         		if(source==null) continue;
	         		if(source.length()<=2){
	         			name=source;
	         		}
	         		else{
			         	List<Term> words=HanLP.segment(source);
			         	for(Term word:words){
			         		if(temp.contains(word.word)) sourceList.add(word.word);
			         		if(word.nature==Nature.nr||word.nature==Nature.n)
			         			personList.add(word.word);
			         	}
			         	
			         	if(!sourceList.isEmpty()){
			         		for(String person:sourceList) name+=" "+person;
			         	}
	         		}
	         	}
			}
			 
			if(name.equals("")||name==null)name=name1;
			if(!name.equals("")&&name!=null){
				answerObject.setPredicate("相关人物");
				answerObject.setValue(name+"（非直接查询出的结果，经后续处理得到的答案）");
				results.add(answerObject);
			}
		}
		
		if(results.isEmpty()) results=resultArray;
		
		return results;
	}
	
	
	/**
	* 根据冒号和句号逗号抽取某个词的解释
	* @param item
	* @param title
	* @return
	*/
	public static String replace(String item,String title){
		
		String original=null;
		int start=item.indexOf(title);
		int end=0;
		int end1=item.indexOf("；");
		int end2=item.indexOf("。");
		if(end1>start) end=end1;
		else end=end2;
		original=item.substring(start, end);
		
		return original;
	}
	
	/**
	* 根据空格抽取字的拼音
	* @param item
	* @param title
	* @param subject
	* @return
	*/
	public static String replace1(String item,String title,String subject){
		
		String spell=null;
		
		int index=subject.indexOf(title);
		
		if(item.contains(" "))
			spell=item.trim().split(" ")[index];
		
		return spell;
	}
	
	
	/**
	* 抽取关键词之前的词
	* @param item
	* @param mark
	* @return
	*/
	public static String split(String item,String  mark){
		
//		int start=0;
    	int end=0;
    	boolean flag=false;
    	List<Term> words=WordParser.CutWord(item);
    	Map wordMap=WordParser.splitWordandNature(words);
    	if(wordMap.containsKey(mark))
    		flag=true;
    	if(flag||item.contains("作于")){
	    	String[] array=item.split("。");
	    	for(int i=0;i<array.length;i++){
		    	if(array[i].contains(mark)){
		    		if(mark.equals("作")){
		    			if((item.contains("作，")||item.contains("作。"))&&!item.contains("代表作")&&!item.contains("著作")){
				    		end=array[i].indexOf(mark);
				    		item=array[i].substring(0, end);
				    		break;
		    			}
		    		}
		    		else{
		    			end=array[i].indexOf(mark);
			    		item=array[i].substring(0, end);
			    		break;
		    		}
		    	}
	    	}
    	}
    	return item;
 
	}
	
	/**
	* 
	* @param item
	* @param mark
	* @param index
	* @return
	*/
	public static String split2(String item,String  mark,int index){
		
		int start=0;
    	int end=0;
    	String temp="";

    	String[] array=item.split("。");
    	for(int i=0;i<array.length;i++){
	    	if(array[i].contains(mark)){
	    		String[] array2=array[i].split(",");
	    		
	    		for(int j=0;j<array2.length;j++){
	    			if(array2[j].contains(mark)){
			    		start=array2[j].indexOf(mark)+index;
			    		temp=temp+array2[j].substring(start);
	    			}
	    		}
	    	}
    	}
    	
    	return temp;
 
	}
	
	public static String splitBymark(String item,String  mark,int index){
		
		int start=0;

    	String temp="";
    	Map<String, Nature> wordMap=WordParser.splitWordandNature(WordParser.CutWord(item));
    	if(wordMap.containsKey(mark)){
	    	String[] array=item.split("。");
	    	for(int i=0;i<array.length;i++){
		    	if(array[i].contains(mark)){
		    		String[] array2=array[i].split(",");
		    		
		    		for(int j=0;j<array2.length;j++){
		    			if(array2[j].contains(mark)){
				    		start=array2[j].indexOf(mark)+index;
				    		temp=temp+array2[j].substring(start);
		    			}
		    		}
		    	}
	    	}
    	}
    	
    	return temp;
 
	}
	
	public static String split3(String item,String mark,int index){
		
		int start=0;
    	int end=0;
    	
    	String[] array=item.split("。");
    	boolean flag = false;  
    	
    	for(int i=0;i<array.length&& !flag;i++){
	    	if(array[i].contains(mark)){
	    		String[] array2=null;
	    		array2=array[i].split("，");
	    		if(array[i].contains(",")) array2=array[i].split(",");
	    		
	    		for(int j=0;j<array2.length&& !flag;j++){
	    			if(array2[j].contains(mark)){
			    		end=array2[j].indexOf(mark)+index;
			    		item=array2[j].substring(0,end);
			    		
			    		List<Term> words=HanLP.segment(item);
			    		
			    		for(Term word:words){
			    			Nature tag=word.nature;
			    			if(tag==Nature.ns){
			    				flag=true;
			    				break;
			    			}
			    		}
	    			}	
	    		}
	    	}
    	}
    	return item;
	}
	
	/**
	* 获取词频最高的人名
	* @param item
	* @param question
	* @return
	*/
	public static String getMostFrequency(String item,String question){
		
		String name="";
		item=item.replaceAll("<br>", "").replaceAll("&nbsp;", "");
		
		List<Term> cutWord=HanLP.segment(item);
     	List<String> personList=new ArrayList<String>();
		for(Term word:cutWord){
     		if((word.nature==Nature.nr||word.nature==Nature.nrf||word.nature==Nature.nrj)&&!question.contains(word.word)&&!word.word.endsWith("是"))
     			personList.add(word.word);
     	}
     	List<String> list=RemoveDuplicate.remove(personList);
     	int max=0;
     	for(int i=0;i<list.size();i++){
     	
     		int count=Collections.frequency(personList, list.get(i));
     		if(count>max){
     			max=count;
     			name=list.get(i);
     		}
     	}
     	return name;
	}
	public static void main(String[] args){
		
		String temp="";
		List<String> personList=new ArrayList<String>();
		
		String item1="战国时候，有七个大国，它们是秦、齐、楚、燕、韩、赵、魏，历史上称为“战国七雄”。这七国当中，又数秦国最强大。秦国常常欺侮赵国。有一次，赵王派一个大臣的手下人蔺相如到秦国去交涉。蔺相如见了秦王，凭着机智和勇敢，给赵国争得了不少面子。秦王见赵国有这样的人才，就不敢再小看赵国了。赵王看蔺相如这么能干。就封他为“上卿”（相当于后来的宰相）。赵王这么看重蔺相如，可气坏了赵国的大将军廉颇。他想：我为赵国拚命打仗，功劳难道不如蔺相如吗？蔺相如光凭一张嘴，有什么了不起的本领，地位倒比我还高！他越想越不服气，怒气冲冲地说：“我要是碰着蔺相如，要当面给愣芽埃此馨盐以趺囱　?/FONT>廉颇的这些话传到了蔺相如耳朵里。蔺相如立刻吩咐他手下的人，叫他们以后碰着廉颇手下的人，千万要让着点儿，不要和他们争吵。他自己坐车出门，只要听说廉颇打前面来了，就叫马车夫把车子赶到小巷子里，等廉颇过去了再走。廉颇手下的人，看见上卿这么让着自己的主人，更加得意忘形了，见了蔺相如手下的人，就嘲笑他们。蔺相如手下的人受不了这个气，就跟蔺相如说：“您的地位比廉将军高，他骂您，您反而躲着他，让着他，他越发不把您放在眼里啦！这么下去，我们可受不了。”蔺相如心平气和地问他们：“廉将军跟秦王相比，哪一个厉害呢？”大伙儿说：“那当然是秦王厉害。”蔺相如说：“对呀！我见了秦王都不怕，难道还怕廉将军吗？要知道，秦国现在不敢来打赵国，就是因为国内文官武将一条心。我们两人好比是两只老虎，两只老虎要是打起架来，不免有一只要受伤，甚至死掉，这就给秦国造成了进攻赵国的好机会。你们想想，国家的事儿要紧，还是私人的面子要紧？”蔺相如手下的人听了这一番话，非常感动，以后看见廉颇手下的人，都小心谨慎，总是让着他们。蔺相如的这番话，后来传到了廉颇的耳朵里。廉颇惭愧极了。他脱掉一只袖子，露着肩膀，背了一根荆条，直奔蔺相如家。蔺相如连忙出来迎接廉颇。廉颇对着蔺相如跪了下来，双手捧着荆条，请蔺相如鞭打自己。蔺相如把荆条扔在地上，急忙用双手扶起廉颇，给他穿好衣服，拉着他的手请他坐下。蔺相如和廉颇从此成了很要好的朋友。这两个人一文一武，同心协力为国家办事，秦国因此更不敢欺侮赵国了。“负荆请罪”也就成了一句成语，表示向别人道歉、承认错误的意思。";
		List<Term> words=HanLP.segment(item1);
     	
		for(Term word:words){
     		if(word.nature==Nature.nr)
     			personList.add(word.word);
     	}
     	
     	List list=RemoveDuplicate.remove2(personList);
     	int max=0;
     	String source="";
     	for(int i=0;i<list.size();i++){
     	
     		int count=Collections.frequency(personList, list.get(i));
     		if(count>max){
     			max=count;
     			temp=(String) list.get(i);
     		}
     	}
     	String item="成语出处：《史记·廉颇蔺相如列传》：“城入赵而璧留秦；城不入，臣请完璧归赵。”";
     	
     	if(item.contains("成语出处")){
     		int index=item.lastIndexOf("·");
     		source=item.substring(index);
     		if(source.contains("列传》")) source=source.substring(1,source.indexOf("列传》"));
     		if(source.contains("传》")) source=source.substring(1,source.indexOf("传》"));
     		if(source.contains("世家》")) source=source.substring(1,source.indexOf("世家》"));
     		if(source.contains("本纪》")) source=source.substring(1,source.indexOf("本纪》"));
     	}
     	List<Term> words2=HanLP.segment(source);
     	for(Term word:words2){
     		
     	}
     	System.out.println(temp);
     	System.out.println(source);
	}

}
