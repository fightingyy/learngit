package com.ld.IO.remove;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import org.python.antlr.PythonParser.else_clause_return;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hankcs.hanlp.corpus.tag.NR;
import com.hankcs.hanlp.corpus.tag.NS;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import com.ld.FullSearch.FullSearch;
import com.ld.Parser.WordParser;
import com.ld.Util.Sort;
import com.ld.model.AnswerObject;
import com.ld.model.QueryObject;
import com.ld.pattern.word2vecPattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：RemoveDuplicate   
* 类描述：  去重
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:43:00   
* @version        
*/
public class RemoveDuplicate {

	static List<String> propertyList=new ArrayList<String>();
	static{
		
		propertyList.add("类型");
		propertyList.add("等同");
		propertyList.add("组成");
		propertyList.add("强相关于");
		propertyList.add("包含");
		propertyList.add("相关于");
		propertyList.add("名称");
		propertyList.add("别称");
		propertyList.add("定义");
		propertyList.add("内容");
		propertyList.add("用途");
		propertyList.add("示例");
		propertyList.add("图片");
		propertyList.add("下属于");
		propertyList.add("是什么");
	}
	//通过HashSet去重
	
	/**
	 * 去重字符串中的重复子串
	 * @param str
	 * @return
	 */
	public static String removeSameStr(String str){
		
		String[] array=str.split("\\|");
		List<String> list=Arrays.asList(array);
		list=remove2(list);
		str="";
		for(int i=0;i<list.size();i++)
			str+=list.get(i)+"|";
		
		return str;
	}

	/**
	 * 去重，不改变原list顺序
	 * @param list
	 * @return
	 */
	public   static   List  remove(List list)   { 
	    
		for (int i = 0; i < list.size() - 1; i++) {
		    for (int  j  =  list.size()  -   1 ; j  >  i; j --) {
		    	String str1=list.get(j).toString();
		    	String str2=list.get(i).toString();
		     if (str1.equals(str2)||str1==str2) {
		      list.remove(j);
		     }
		     if(str1.equals(str2+"。")) list.remove(j);
		    }
		   }
	   
	    return list;
	} 
	/**
	 * 去除被包含在另一个主语中的主语
	 * @param list
	 * @param question
	 * @param courseMap
	 * @param course
	 * @param templateContent
	 * @return
	 */
	public   static   List  removeContians(List<String> list,String question,Map<String,String> courseMap,String course,String templateContent)   { 
		
		List<String> temp=new ArrayList<String>();
		String tempStr="";
		List<Term> cutWord=WordParser.CutWord(question);
		Map<String, Nature> WNmap = WordParser.splitWordandNature(cutWord);
		Pattern pattern=Pattern.compile(templateContent);
		
		String gString="";
		
		if(WNmap.containsKey(Nature.g)){
			for(Term word:cutWord){
				if(word.nature==Nature.g&&question.contains("《"+word.word+"》")){
					gString=word.word;
					break;
				}
			}
		}

		list.remove("什么意思");
		if(course.equals("english")&&list.contains("英语")) list.remove("英语");
		for (int i = 0; i < list.size(); i++) {
			tempStr=list.get(i).replaceAll("\\d）", "").replaceAll("（\\d）", "").replaceAll("\\d\\.", "");
			String tempi=list.get(i);
			String repStri=tempi.replaceAll("《", "").replace("》", "");
			
			if(tempStr.equals(tempi)) tempStr="";
			if(!gString.equals("")&&gString.contains(tempi)){
				temp.add(tempi);
				continue;
			}
		    for (int  j  =  i+1 ; j < list.size(); j ++) {
		    	String tempj=list.get(j);
		    	String repStrj=tempj.replaceAll("《", "").replace("》", "");
		    	if(course.equals("politics")||course.equals("biology")){
		    		String replace="";
		    		if(tempi.contains(tempj)){
		    			replace=tempi.replace(tempj,"");
		    		}
		    		else if(tempj.contains(tempi)){
		    			replace=tempj.replace(tempi,"");
		    		}
		    		if(replace!=null&&!replace.equals("")){
		    			List<Term> wordList=WordParser.CutWord(replace);
		    			if(wordList.size()==1&&(wordList.get(0).nature==Nature.v||wordList.get(0).nature==Nature.vn||wordList.get(0).nature==Nature.vi)) continue;
		    		}
		    	}
		    	if(question.contains("，")&&tempi.contains("和")&&tempi.contains(tempj))
		    			continue;
		    	if(course.equals("english")&&tempStr.contains(tempj))
		    		continue;
		    	
		    	if(tempj.contains("的")&&tempj.contains(tempi)&&tempi.equals(tempj.substring(0,tempj.indexOf("的")))&&courseMap.containsValue(tempj.substring(tempj.indexOf("的")+1)))
		    		continue;
		    	else if((!tempi.equals(repStri)||!tempj.equals(repStrj))&&repStri.equals(repStrj))
		    			continue;
		    	else if(tempi.contains("的")&&tempi.contains(tempj)&&tempj.equals(tempi.substring(0,tempi.indexOf("的")))&&courseMap.containsValue(tempi.substring(tempi.indexOf("的")+1)))
		    		continue;
		    	else if(tempj.equals("怎样"+tempi)||tempj.equals("如何"+tempi)||tempj.equals("为什么"+tempi)||tempj.equals("什么是"+tempi)||tempj.contains("中的"+tempi))
		    		continue;
		    	else if(tempi.equals("怎样"+tempj)||tempi.equals("如何"+tempj)||tempi.equals("为什么"+tempj)||tempi.equals("什么是"+tempj)||tempi.contains("中的"+tempj))
		    		continue;
		    	else if(!tempj.equals(tempi)&&(tempi.equals(tempj.trim())||tempj.equals(tempi.trim())||tempi.equals("“"+tempj+"”")||tempj.equals("“"+tempi+"”")))
		    			continue;
		    	else if(((tempj.contains(tempi)&&pattern.matcher(tempj).find())||(tempi.contains(tempj)&&pattern.matcher(tempi).find()))&&!course.equals("geo"))
		    		continue;
		    	else if((tempj.contains(tempi)&&(tempj.replaceAll(tempi, "").equals("市")||tempj.replaceAll(tempi, "").equals("省")))||(tempi.contains(tempj)&&(tempi.replaceAll(tempj, "").equals("市")||tempi.replaceAll(tempj, "").equals("省")))){
		    		continue;
		    	}
		    	
		    	if(!tempi.equals(tempj)&&tempi.contains(repStrj)&&i!=j){
		    		temp.add(tempj);
		    	}
		    	else if(!tempi.equals(tempj)&&tempj.contains(repStri)&&i!=j)
		    		temp.add(tempi);
		    	if(tempj.contains(tempi)&&i!=j&&tempi.contains("")){
		    		temp.add(tempi);
		    	}
		    	else if(tempi.contains(tempj)&&i!=j){
		    		temp.add(tempj);
		    	}
		    }
		    if(WNmap.containsKey(tempi)){
		    	if(WNmap.get(tempi).toString().startsWith("ry")||WNmap.get(tempi)==Nature.ude1){
		    		temp.add(tempi);
		    	}
		    }
	    }
		temp=remove(temp);
		list.removeAll(temp);
		temp.clear();

		
		if(list.size()>1){
			for(int t=0;t<list.size();t++){
				
				String tempi=list.get(t);
				
				if(tempi.length()<=2&&!temp.contains(tempi)){
			    	for(Term word:cutWord){
			    		
			    		if(word.nature!=Nature.nr&&word.word.contains(tempi)&&!word.word.equals(tempi)&&!course.equals("chemistry")&&!course.equals("geo")){
			    			temp.add(tempi);
			    			break;
			    		}
			    		if(course.equals("biology")&&word.word.equals("种")&&tempi.equals("种")){
			    			temp.add(tempi);
			    			break;
			    		}
			    	}
			    }	
			    List<String> removeList=new ArrayList<String>();
			    
			    if(!temp.contains(tempi)&&(course.equals("chinese")&&(courseMap.containsValue(tempi)||templateContent.contains(tempi))))
		    		temp.add(tempi);
			    else if(!temp.contains(tempi)&&propertyList.contains(tempi)) 
			    	temp.add(tempi);
			    if(!temp.contains(tempi)&&course.equals("chinese")&&removeList.size()>1&&tempi.length()<=2){
			    	if(WNmap.containsKey(tempi)){
			    		Nature nature=WNmap.get(tempi);
			    		if(nature!=Nature.a&&nature!=Nature.nr&&nature!=Nature.nz&&nature!=Nature.n&&nature!=Nature.g&&!question.startsWith(tempi))
			    			temp.add(tempi);
			    	}
			    	else {
			    		List<Term> wordList=WordParser.CutWord(tempi);
			    		if(wordList.get(0).nature!=Nature.nr){
			    			temp.add(tempi);
			    		}
					}	
			    }
			    	
			    List<Term> words=WordParser.CutWord(tempi);
			    if(!temp.contains(tempi)&&words.size()==1){
			    	if(words.get(0).nature==Nature.p||words.get(0).nature==Nature.ude1||words.get(0).nature==Nature.rys||words.get(0).nature==Nature.ry||words.get(0).nature==Nature.ryv||words.get(0).nature==Nature.ryt)
			    		temp.add(tempi);
			    }
			    
			    if(course.equals("geo")&&(tempi.equals("世界")||tempi.equals("国家")||tempi.equals("地区"))) temp.add(tempi);
			    if(course.equals("chinese")&&tempi.equals("作品")) temp.add(tempi);
			    
			    removeList.addAll(list);
			    removeList.removeAll(temp);
			}
		}

		list.removeAll(temp);
		
		if(list.contains("哪位")){
			list.remove("哪位");
		}
		
		
		if(list.size()>3&&list.contains("世界")) list.remove("世界");
		
		if(course.equals("english")&&list.contains("英语")) list.remove("英语");
		
	    return list;
	} 
	
	/**
	 * 语文主语去重
	 * @param question
	 * @param subjectList
	 * @return
	 */
	public static List<String> removeChineseSubject(String question,List<String> subjectList){
		
		List<String> resultList=new ArrayList<String>();
		Map<String, Nature> wordMap=WordParser.splitWordandNature(WordParser.CutWord(question));
//		List<String> words=new ArrayList<String>(wordMap.keySet());
		List<String> removeList=new ArrayList<String>();

		Map<String, Nature> subjectMap=new HashMap<String, Nature>();
		for(String subject: subjectList){
			if(question.contains("默写")) return subjectList;
			if(subject.startsWith("什么")||subject.endsWith("什么")){
				removeList.add(subject);
				continue;
			}
			if(subjectList.contains("《"+subject+"》"))
				resultList.add(subject);
			if(wordMap.containsKey(subject)){
				
				Nature nature=wordMap.get(subject);
				subjectMap.put(subject, nature);
				if(nature==Nature.g||nature==Nature.l||nature.toString().startsWith("nr")||nature==Nature.i||nature==Nature.vl||(nature==Nature.nz&&subject.length()>3)||(question.startsWith(subject)&&subject.length()>2))
					resultList.add(subject);
				if(nature.toString().startsWith("ry")||nature==Nature.nnt||nature==Nature.q)
					removeList.add(subject);
				else if(nature==Nature.n&&subject.length()==2&&!question.startsWith(subject)){
					removeList.add(subject);
				}
			}
			else if(subject.length()>3){
				resultList.add(subject);
			}
			else if(question.startsWith(subject)&&subject.length()>2)
				resultList.add(subject);
//			else if(subject.length()==2&&subjectList.size()>2)
//				removeList.add(subject);
		}
		
		if(resultList.isEmpty()){
			subjectList.removeAll(removeList);
			resultList=subjectList;
		}
		
		return resultList;
	}
	public static List removeInclude(List<String> list)   { 
		
		List<String> temp=new ArrayList<String>();
		
		for (int i = 0; i < list.size(); i++) {			
			String tempi=list.get(i).replaceAll(" ", "");
		    for (int  j  =  i+1 ; j < list.size(); j ++) {
		    	String tempj=list.get(j);
		    	if(tempi.equals(".{0,4}")||tempj.equals(".{0,4}")||tempi.equals("(.*)?")||tempj.equals("(.*)?")||tempj.equals("")||tempi.equals("")||tempj.equals(" ")||tempi.equals(" ")) continue;
		    	if(!tempj.equals(tempi)&&(tempi.equals(tempj.trim())||tempj.equals(tempi.trim())))
		    			continue;
		    	
		    	if(tempj.contains(tempi)&&i!=j&&tempi.contains("")){
		    		temp.add(tempi);
		    	}
		    	else if(tempi.contains(tempj)&&i!=j){
		    		temp.add(tempj);
		    	}
		    }
	    }
//		temp=remove(temp);
		list.removeAll(temp);
		
	    return list;
	} 
	
	public   static   List  removeContians2(List<String> list,String question,Map<String,String> courseMap,String course,String templateContent)   { 
		
		List<String> temp=new ArrayList<String>();
		String tempStr="";
		
		List<String> propertyList=new ArrayList<String>();
		propertyList.add("类型");
		propertyList.add("等同");
		propertyList.add("组成");
		propertyList.add("强相关于");
		propertyList.add("包含");
		propertyList.add("相关于");
		propertyList.add("名称");
		propertyList.add("别称");
		propertyList.add("定义");
		propertyList.add("内容");
		propertyList.add("用途");
		propertyList.add("示例");
		propertyList.add("图片");
		propertyList.add("下属于");
		
		for (int i = 0; i < list.size(); i++) {
			String tempi=list.get(i).replaceAll("\\d）", "").replaceAll("（\\d）", "").replaceAll("\\d\\.", "");

		    for (int  j  =  i+1 ; j < list.size(); j ++) {
		    	String tempj=list.get(j);
		    	if(question.contains("，")&&tempi.contains("和")&&tempi.contains(tempj))
		    			continue;
		    	if(tempi.contains("的")&&tempi.contains(tempj)){
		    		if(tempi.indexOf(tempj)>tempi.indexOf("的")){
			    		temp.add(tempi);
			    		temp.add(tempj);
			    		continue;
		    		}
		    		else if(tempi.indexOf(tempj)<tempi.indexOf("的")){
		    			temp.add(tempi);
			    		continue;
		    		}
		    	}
		    	else if(tempj.contains("的")&&tempj.contains(tempi)){
		    		if(tempj.indexOf(tempi)>tempj.indexOf("的")){
		    			temp.add(tempi);
			    		temp.add(tempj);
			    		continue;
		    		}
		    		else if(tempj.indexOf(tempi)<tempj.indexOf("的")){
		    			temp.add(tempj);
			    		continue;
		    		}
		    	}
//		    	if(tempi.contains("的")&&tempi.contains(tempj)&&tempi.indexOf(tempi.indexOf(tempj))<tempi.indexOf("的")){
//		    		temp.add(tempi);
//		    		continue;
//		    	}
//		    	else if(tempj.contains("的")&&tempj.contains(tempi)&&tempj.indexOf(tempj.indexOf(tempi))<tempj.indexOf("的")){
//		    		temp.add(tempj);
//		    		continue;
//		    	}	
		    	if(tempj.contains(tempi)&&i!=j){
		    		temp.add(tempi);
		    	}
		    	else if(tempi.contains(tempj)&&i!=j){
		    		temp.add(tempj);
		    	}
		    }
		    if((courseMap.containsValue(tempi)||templateContent.contains(tempi))){
	    		temp.add(tempi);
	    		continue;
		    }
		    else if(propertyList.contains(tempi)){ 
		    	temp.add(list.get(i));
		    	continue;
		    }
		    if(course.equals("chinese")&&list.size()>2&&tempi.length()==1){
		    	temp.add(tempi);
		    	continue;
		    }
		    List<Term> words=WordParser.CutWord(tempi);
		    if(words.size()==1){
		    	if(words.get(0).nature==Nature.p||words.get(0).nature==Nature.d||words.get(0).nature.toString().startsWith("ry")){
		    		temp.add(tempi);
		    		continue;
		    	}
		    }
		    if(course.equals("geo")&&tempi.equals("世界")) temp.add(tempi);
		   }
		temp=remove(temp);
		list.removeAll(temp);

		if(list.size()>2&&list.contains("什")&&list.contains("么")){
			list.remove("什");
			list.remove("么");
		}
		if(list.contains("诗句")||list.contains("句")){
			list.remove("诗句");
			list.remove("句");
		}
		if(list.contains("哪位")){
			list.remove("哪位");
		}
		if(list.size()>3&&list.contains("世界")) list.remove("世界");
		
		if(course.equals("english")&&list.contains("英语")) list.remove("英语");
		
	    return list;
	} 
	
	public   static   List<String>  removeContians3(List<String> list)   { 
		
		List<String> temp=new ArrayList<String>();
		String tempStr="";

		for (int i = 0; i < list.size(); i++) {
			tempStr=list.get(i).replaceAll("\\d）", "").replaceAll("（\\d）", "").replaceAll("\\d\\.", "");
			String tempi=list.get(i);
			String repStri=tempi.replaceAll("《", "").replace("》", "");
			
			if(tempStr.equals(tempi)) tempStr="";
		    for (int  j  =  i+1 ; j < list.size(); j ++) {
		    	String tempj=list.get(j);
		    	String repStrj=tempj.replaceAll("《", "").replace("》", "");
		    	
		    	if(!tempi.equals(tempj)&&tempi.contains(repStrj)&&i!=j){
		    		temp.add(tempj);
		    	}
		    	else if(!tempi.equals(tempj)&&tempj.contains(repStri)&&i!=j)
		    		temp.add(tempi);
		    	if(tempj.contains(tempi)&&i!=j&&tempi.contains("")){
		    		temp.add(tempi);
		    	}
		    	else if(tempi.contains(tempj)&&i!=j){
		    		temp.add(tempj);
		    	}
		    }
		}
		    
		temp=remove(temp);
		list.removeAll(temp);
		
	    return list;
	} 

	
	/**
	 * 去重，改变原list顺序
	 * @param list
	 * @return
	 */
	public static List<String> remove2(List<String> list){
		
		Set set = new HashSet();    
		List newList = new ArrayList();    
		for (Iterator iter = list.iterator(); iter.hasNext(); ){    
		Object element = iter.next();    
			if (set.add(element)) 
				newList.add(element);    
		}    
		list=new ArrayList<String>();
		list.addAll(newList);    
		
		return list;
	}
	
	 public static String RemoveSpace(String sourceString) {
        if (sourceString != null) {
            int len = sourceString.length();
            if (len > 0) {
                char[] dest = new char[len];
                int destPos = 0;
                for (int i = 0; i < len; ++i) {
                    char c = sourceString.charAt(i);
                    if (!Character.isWhitespace(c)) {
                        dest[destPos++] = c;
                    }
                }
                return new String(dest, 0, destPos);
            }
        }
        return sourceString;
    }
	 
	public static String[] removeArray(String[] array,String key){

		String and="";
		
		String[] arrays=array;
		array=CheckNull(array);
		List<String> list=Arrays.asList(array);
		List<String> newList=new ArrayList<String>(list);
		List<String> tempList=new ArrayList<String>();
		if(key.contains("的")){
			return array;
		}
		key=key.replaceAll("\\d+\\.", "").replaceAll("（\\d+）", "");
		newList=removeInclude(newList);
		if(newList.size()<array.length){
			List<Term> words=WordParser.CutWord(key);
			for(Term word:words){
				if(word.nature==Nature.cc){
					and=word.word;
					break;
				}
			}
			if(and!=null&&!and.equals("")){
				String[] splitArray=key.split(and);
				if(splitArray.length==2&&Math.abs(splitArray[0].length()-splitArray[1].length())<3){
					
					tempList.add(splitArray[1]);
					tempList.add(splitArray[0]);
					array=(String[]) tempList.toArray(new String[tempList.size()]);
					
				}
			}
		}
		arrays=CheckNull(array);
		if(array.length==2&&!and.equals("")){
			if(array[0].contains(array[1])||array[1].contains(array[0])){
				array=new String[1];
				array[0]=arrays[1]+"(.*)?"+arrays[0];
				arrays=array;
			}
		}
		
		return arrays;
	}
	
	public static String[] CheckNull(String[] array){
		
		List<String> resultList=new ArrayList<String>();
		List<String> list=Arrays.asList(array);
		for(int i=0;i<list.size();i++){
			String item=list.get(i);
			if(!item.equals("")&&!item.equals(" ")&&!item.equals(".{0,4}")&&!item.equals("(.*)?"))
				resultList.add(item);
		}
		array=null;
		array=(String[])resultList.toArray(new String[resultList.size()]);
		return array;
	}
	
	public static List<String>  removeCharacter(List<String> list)   { 
		
		List<String> resultList=new ArrayList<String>();
		
		for (String item:list) {
			item=item.replaceAll("【(.*)?】", "").replaceAll("（(.*)?）", "").replaceAll("〈(.*)?〉", "").replaceAll("网络:", "");
		    resultList.add(item);
		}
	   
	    return resultList;
	}

	/**
	 * 查询语句去重
	 * @param queryList
	 * @return
	 */
	public static List<QueryObject> removeQueryObject(List<QueryObject> queryList) {
		
		List<QueryObject> removeList=new ArrayList<QueryObject>();
		List<String> uniList=new ArrayList<String>();
		for (QueryObject queryObject: queryList) {
			String queryStr=queryObject.getQuery();
			if(!uniList.contains(queryStr)){
				removeList.add(queryObject);
			}
		}
		return removeList;
	}

}
