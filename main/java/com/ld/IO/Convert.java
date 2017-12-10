package com.ld.IO;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import com.ld.IO.remove.RemoveDuplicate;
import com.ld.Parser.WordParser;
import com.ld.search.VirtuosoSearch;

public class Convert {

	public static String clabel="";
	public Convert() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	* 将化学式转化为化学名称
	* @param question
	* @param course
	* @return
	*/
	public static String convertLabel(String question,String course){
		
		String replaceQuestion="";
		
		if(course.equals("chemistry")){
			List<String> cList=new ArrayList<String>();
			String ChemicalFormula="";
			
			String regex="[\\u4e00-\\u9fa5]+";
			Pattern pattern=Pattern.compile(regex);
			Matcher matcher;
			
			//根据词性获取问句中所有的化学式
			List<Term> words=WordParser.CutWord(question);
			for(int t=0;t<words.size();t++){
				matcher=pattern.matcher(words.get(t).word);
				if(matcher.find()){
					if(!ChemicalFormula.equals("")){
						cList.add(ChemicalFormula);
						ChemicalFormula="";
					}
				}
				if(words.get(t).nature==Nature.nx||words.get(t).nature==Nature.m||words.get(t).word.equals("（")||words.get(t).word.equals("）")||words.get(t).word.equals("(")||words.get(t).word.equals(")")){
					ChemicalFormula+=words.get(t).word;
//					if(t+1<words.size()&&words.get(t+1).nature==Nature.m)
//						ChemicalFormula+=words.get(t+1).word;
//					if(t+3<words.size()&&words.get(t+2).word.equals("·")){
//						
//					}
//					else break;
				}
			}
			cList=RemoveDuplicate.remove(cList);
			
			//根据化学式在知识库中查询物质的名称
			if(!cList.isEmpty()){
				replaceQuestion=question;
				for(int i=0;i<cList.size();i++){
					ChemicalFormula=cList.get(i);
					clabel=VirtuosoSearch.searchByChemicalFormula(ChemicalFormula);
					if(!clabel.equals(""))
						replaceQuestion=replaceQuestion.replace(ChemicalFormula, clabel);
				}
			}
		}
		return replaceQuestion;
	}

}
