package com.ld.IO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;

import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import com.ld.IO.remove.RemoveDuplicate;
import com.ld.MainPartExtractor.DecideSubject;
import com.ld.Parser.WordParser;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：SubjectProcess   
* 类描述： 抽取模糊查询的关键词
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:33:01   
* @version        
*/
public class ExtractFilter {

	/**
	* 挑选模糊查询的关键词
	* @param subjectName
	* @param usage
	* @param course
	* @return
	*/
	public static String filterProcess(String subjectName,String usage,String course){
		
		String subject=subjectName;
		String temp="";
		
		List<String> removeList=new ArrayList<String>();
		removeList.add("新中国");
		removeList.add("中国");
		removeList.add("核心内容");
		removeList.add("不平等条约");
		removeList.add("哪位");
		removeList.add("习惯上");
//		subjectName=subjectName.replace("?", "").replace("？", "");
		List<Term> cutWord=WordParser.CutWord(subjectName);
		String noun="";
		int i=0;
		for(int t=0;t<cutWord.size();t++){
			Term word =cutWord.get(t);
			//对于问地点的题抽取词性为地名的词
			if((usage.equals("place"))&&word.nature.toString().startsWith("ns")){
				if(course.equals("history"))
					temp=word.word+"|";
				else{
					temp+=word.word+"|";
				}
				subjectName=temp;
			}
			//对于问人物的题抽取词性为人名的词
			else if((usage.equals("person"))&&word.nature.toString().startsWith("nr")){
				subjectName=word.word;
				break;
			}
			//对于问地问的题
			else if(usage.equals("status")){
				if(!temp.contains(word.word)&&!word.word.equals("上")&&(word.nature==Nature.f||word.nature==Nature.a||word.nature==Nature.vn||word.nature==Nature.mq||word.nature==Nature.gm||word.nature==Nature.gb)&&!word.word.equals("为基础")){
					temp+=word.word+"|";
				}
				else if (word.nature==Nature.nz&&word.length()>3) {
					temp+=word.word+"|";
				}
				if(!temp.contains(word.word)&&word.word.contains("最"))
					temp+=word.word+"|";
				if(!temp.contains(word.word)&&word.nature==Nature.n&&!word.word.equals("面积")&&!word.word.equals("历史")&&!word.word.equals("城市")){
//					noun=word.word;
					temp+=word.word+"|";
				}
				if(!temp.contains(word.word)&&course.equals("geo")&&word.nature.toString().startsWith("ns")) temp+=word.word+"|";
				subjectName=temp;
			}
//			else if(usage.equals("text")){
//				if(!temp.contains(word.word)&&(word.nature==Nature.ns||word.nature==Nature.f||word.word.equals("我国"))){
//					temp+=word.word+"";
//				}
//				else if(!temp.contains(word.word)&&(word.word.contains("最")||word.nature==Nature.t||word.nature==Nature.a||word.nature==Nature.nz||word.nature==Nature.vn||word.nature==Nature.mq||word.nature==Nature.gm||word.nature==Nature.gb)&&!word.word.equals("为基础")){
//					temp+="|"+word.word+"|";
//				}
//				else if(!temp.contains(word.word)&&word.nature==Nature.n&&!word.word.equals("地区")){
//					temp+="|"+word.word+"";
//					if(t+1<cutWord.size()&&(cutWord.get(t+1).nature==Nature.n||cutWord.get(t+1).nature==Nature.a)){
//						temp+=cutWord.get(t+1).word+"";
//						t=t+1;
//					}
//				}
//				subjectName=temp;
//			}
			//对于问化学式的题抽取字母和数字
			else if(usage.equals("ChemicalFormula")){
				if(word.nature==Nature.nx||word.nature==Nature.m){
					temp+=word.word;
				}
				subjectName=temp;
			}
			else if(usage.equals("translation")){
				subjectName=subjectName.replace("词组", "").replace("词语", "").replace("某人", "|").replace("某事", "|");
				if(word.nature==Nature.w){
					subjectName=subjectName.replace(word.word, "|");
				}
			}
			//对于英语学科抽取字母
			else if(course.equals("english")){
				if(word.nature==Nature.nx){
					temp+=word.word;
				}
				
				if(!temp.equals(""))
					subjectName=temp;
			}
//			else if(subjectName.contains(",")){
//				subjectName=subjectName.substring(0,subjectName.indexOf(","));
//				break;
//			}
			
			else if((word.nature==Nature.nz||word.nature==Nature.g)&&!removeList.contains(word.word)&&!word.word.endsWith("下")&&!word.word.endsWith("上")){
				
				if(!word.word.equals("英语")||!course.equals("english")) 
					subjectName=word.word;
			}
			else if((course.equals("geo"))&&word.nature==Nature.ns&&!word.word.equals("地球")){
				subjectName=word.word;
			}
			i++;
		}
		
		if(!noun.equals("")) subjectName+=noun;

		if(subjectName.contains("'")){
			subjectName=subjectName.substring(subjectName.indexOf("'")+1, subjectName.lastIndexOf("'"));
		}
		if(subjectName.endsWith("的")&&!course.equals("chinese"))
			subjectName=subjectName.replace("的", "");
		if(subjectName.contains("等"))
			subjectName=subjectName.substring(0,subjectName.indexOf("等"));
		
		//对于问上下句的题的特殊处理
		if(usage.equals("nextContent")||usage.equals("lastContent")||usage.equals("author")&&(subjectName.length()<=3||subjectName.equals(subject))){
			if(subject.endsWith("的"))
				subjectName=subject.replace("的", "");
			else if(subjectName.length()<=3)
				subjectName=subject;
			if(subjectName.startsWith("填写")||subjectName.startsWith("默写")||subjectName.startsWith("补充")||subjectName.startsWith("写"))
				subjectName=subjectName.substring(2);
		}
		
		//去除关键词中的引号
		if(subjectName.contains("“")&&subjectName.contains("”"))
			subjectName=subjectName.substring(subjectName.indexOf("“")+1, subjectName.indexOf("”"));
		else if(subjectName.contains("\"")&&subjectName.contains("\"")){
			subjectName=subjectName.substring(subjectName.indexOf("\"")+1, subjectName.lastIndexOf("\""));
		}
		
		subject=subjectName;

		if(subject.contains("，")){
			if(subject.split("，")[0].contains("默写")||subject.split("，")[0].contains("古诗"))
				subject=subject.split("，")[1];
		}
		subject=subject.replace(",", "|").replace("，", "|").replace("?", "|").replace("？", "|").replace("。", "|").replace("习惯上", "");
		
		//去除关键词中的疑问词
		if(subject!=null&&!subject.equals("")){
			List<Term> words=WordParser.CutWord(subject);
			if(!course.equals("english")&&words.size()==1&&words.get(0).nature.toString().startsWith("ry"))
				subject="";
			if(words.get(0).nature==Nature.vshi&&words.size()>1&&words.get(1).nature==Nature.ry)
				subject="";
		}
		
		if(subject.startsWith("|"))subject=subject.substring(1);
		if(subject.endsWith("是")||subject.endsWith("是")) subject=subject.substring(0,subject.length()-1);
		if(usage.equals("status")&&(subject.contains("我国")||subject.contains("第|"))&&subject.split("\\|").length<3) subject="";
		return subject;
	}
	
	public static String filterBySubject(String subjectName,String usage,String course,List<String> termlist){
		
		String subject="";
		
		DecideSubject dSubject=new DecideSubject();
		Map<String,String> courseMap=new HashMap<String,String>();
		if(subjectName!=null&&!subjectName.equals("")){
			List<String> subList=dSubject.DecideByMap(termlist, subjectName, course, "");
			subList=RemoveDuplicate.removeContians(subList, subjectName, courseMap, course, "");
			
			for(int i=0;i<subList.size();i++){
				subject=subList.get(i);
			}
		}
		return subject;
	}
	
}
