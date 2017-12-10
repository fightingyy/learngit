package com.ld.Parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：WordParser   
* 类描述：  分词
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:50:35   
* @version        
*/
public class WordParser extends Segment{
	
	int tag=-1;
	
	public static List<Term> CutWord(String text){
		List<Term> words=HanLP.segment(text);
		List<Term> wordList=new ArrayList<Term>();
		
		wordList=Process(text,words,"《","》",1);
		wordList=Process(text,wordList,"“","”",1);
		wordList=Process(text,wordList,"'","'",1);
		wordList=Process(text,wordList,"\"");
		
		return wordList;
		
	}

	/**对已分词的结果进行进一步的处理
	 * @param text
	 * @param words
	 * @param symbol1
	 * @param symbol2
	 * @param index
	 * @return
	 */
	public static List<Term> Process(String text,List<Term> words,String symbol1,String symbol2,int index){
	   
		String word="";
		Term term=new Term(null,null);
		List<Term> wordList=new ArrayList<Term>();
		int count=0;
		int tag=0;
		String temp="";
		for(int i=0;i<words.size();i++){
			
			if(words.get(i).word.equals("·")){
				tag++;
				if(tag==1&&i-1>=0){
					temp+=words.get(i-1).word+words.get(i).word+words.get(i+1).word;
					term=new Term(temp,Nature.g);
					wordList.remove(words.get(i-1));
					wordList.add(term);
					i++;
					continue;
				}
				if(tag>1){
					temp=wordList.get(wordList.size()-1).word+words.get(i).word+words.get(i+1).word;
					term=new Term(temp,Nature.g);
					wordList.remove(wordList.size()-1);
					wordList.add(term);
					i++;
					continue;
				}
				
			}
			if(text.contains(symbol1)&&text.contains(symbol2)){	
				
				if(symbol1.equals(words.get(i).word)){
					
					if(count>0) word="";
					
					int s=i;
					int f=i;
					while(!symbol2.equals(words.get(f).word)){
						f++;
						if(f>=words.size()-1)break; 
					}
					for(s=i+index;s<=f-index;s++){
						word=word+words.get(s).word;
					}
					term=new Term(word,Nature.g);
					wordList.add(term);
					i=f;	
					count++;
				}
				else if(words.get(i).nature==Nature.mq){
					if(((i+1)<words.size())){
						if(words.get(i+1).nature==Nature.qv){
							word=words.get(i).word+words.get(i+1).word;
							term=new Term(word,Nature.qv);
							wordList.add(term);
							i++;
						}
					}
				}
				else wordList.add(words.get(i));
			}
			else break;
		}
		
		if(wordList.isEmpty()) wordList=words;
	   
		return wordList;
	}
	
	public static List<Term> Process(String text,List<Term> words,String symbol1){
		   
		String word="";
		Term term=new Term(null,null);
		List<Term> wordList=new ArrayList<Term>();
		int count=0;
		
		if(text.indexOf(symbol1)!=text.lastIndexOf(symbol1)){
			for(int i=0;i<words.size();i++){
				
				if(symbol1.equals(words.get(i).word)&&count==0){
					
					if(count>0) word="";
					
					int s=i;
					int f=i+1;
					while(f<wordList.size()&&!symbol1.equals(words.get(f).word)){
						f++;
					}
					for(s=i+1;s<=f;s++){
						word=word+words.get(s).word;
					}
					term=new Term(word,Nature.g);
					wordList.add(term);
					i=f;	
					count++;
				}
				else if(words.get(i).nature==Nature.mq){
					if(((i+1)<words.size())){
						if(words.get(i+1).nature==Nature.qv){
							word=words.get(i).word+words.get(i+1).word;
							term=new Term(word,Nature.qv);
							wordList.add(term);
							i++;
						}
						else wordList.add(words.get(i));
					}
					else wordList.add(words.get(i));
				}
				else wordList.add(words.get(i));
			}
		}
		if(wordList.isEmpty()) wordList.addAll(words);
		return wordList;
	}
	
	public List<Term> process(List<Term> words){
		
		List<Term> wordList=new ArrayList<Term>();
		
		
		
		return wordList;
	}
	
	/**
	 * 将分词结果放入map
	 * @param words
	 * @return
	 */
	public static Map<String, Nature> splitWordandNature(List<Term> words){
		Map<String, Nature> wordMap=new HashMap<String, Nature>();
		
		for(Term word:words){
			wordMap.put(word.word, word.nature);
		}
		return wordMap;
	}
	
	@Override
	protected List<Term> segSentence(char[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String[] args){

		System.out.println(CutWord("春秋末年"));
	}
}
