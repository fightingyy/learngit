package com.ld.QuestionClassification;

import java.util.List;

import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLSentence;
import com.hankcs.hanlp.corpus.dependency.CoNll.CoNLLWord;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.dependency.CRFDependencyParser;
import com.hankcs.hanlp.seg.common.Term;
import com.ld.IO.StringProcess;
import com.ld.Parser.WordParser;

public class ExtractKeyWord {

	//根据位置提取问句中的焦点
	public static String extractKeyByLocation(String question){
		
		String keyword="";
		boolean flag=false;
		List<Term> words=WordParser.CutWord(question);
		if(words.size()<4) return keyword;
		//判断问句中是否含有疑问词
		for(int i=0;i<words.size();i++){
			int ryCount=StringProcess .CountNumber(question, "什么");
			if(ryCount>1) break;
			Term word=words.get(i);
			int length=question.replace("?", "").replace("？", "").length();
			if(word.nature==Nature.ry||word.nature==Nature.ryv||word.nature==Nature.rys||word.nature==Nature.ryt){
				flag=true;
				int index=question.indexOf(word.word);
				
				//如果疑问词在句末，则往前查找第一个出现的名词
				if(index==length-1||index==length-word.word.length()){
					for(int t=i;t>0;t--){
						if(words.get(t).nature==Nature.n&&t>2){
							keyword=words.get(t).word;
							break;
						}
					}
					break;
				}
				//如果疑问词出现在其他位置，则往后查找第一个出现的名词
				else {
					for(int t=i+1;t<words.size();t++)
						if(words.get(t).nature==Nature.n){
							keyword=words.get(t).word;
							break;
						}
					break;
				}
			}
		}
//		if(!flag){
//			if(question.contains("的")){
//				int index=question.indexOf("的");
//				keyword=question.substring(index+1).replace("？", "").replace("？", "");
//			}
//		}
		if(question.contains("叫做"+keyword)||question.contains("叫"+keyword)||question.contains("称为"+keyword))keyword="";	
		return keyword;
	}
	
	public static String extractKeyByDependency(String question){
		String key="";
		CoNLLSentence CSentence=CRFDependencyParser.compute("印度北部是哪座山脉？");
		
		return key="";
	}
	public ExtractKeyWord() {
		
	}

	public static void main(String[] args) {
		String question="东南亚的主要粮食作物是什么";
				
		System.out.println(extractKeyByLocation(question));
	}

}
