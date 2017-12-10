package com.ld.QuestionClassification;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import com.ld.IO.remove.RemoveDuplicate;
import com.ld.Parser.WordParser;
import com.ld.model.*;

public class QuestionClassify {
	
	static Connection cn;
	
	//选取置信度最高的问题类型
	public static List<QuestionType> classify(String question,String keyWord){
		
		QuestionType type=null;
		List<QuestionType> QuestionTypeList=new ArrayList<QuestionType>();
		List<EvaluateQuestionType> typeList=CandidateClassify(question,keyWord);
		double max=0;
		for(int i=0;i<typeList.size();i++){
			EvaluateQuestionType EquestionType=typeList.get(i);
			if(EquestionType.getProbability()>=max){
				max=EquestionType.getProbability();
				type=EquestionType.getQuestionType();
				QuestionTypeList.add(type);
			}
		}
		QuestionTypeList=RemoveDuplicate.remove(QuestionTypeList);
		
		return QuestionTypeList;
	}
	public static List<EvaluateQuestionType> CandidateClassify(String question,String keyWord){
		
		List<EvaluateQuestionType> typeList=new ArrayList<EvaluateQuestionType>();
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			Statement stmt=cn.createStatement();

			QuestionType type = null;
			EvaluateQuestionType equstionType=null;
			boolean flag=false;
			List<Term> words=WordParser.CutWord(question);
			String questionWord="";
			//判断问句中是否含有疑问词
			for(Term word : words){
				if(word.nature==Nature.ry||word.nature==Nature.ryv||word.nature==Nature.r){
					flag=true;
					questionWord=word.word;
				}
				else if(word.nature ==Nature.rys){
					type=QuestionType.LOCATION;
					equstionType=new EvaluateQuestionType(type,0.5);
					typeList.add(equstionType);
					
					flag=true;
					questionWord=word.word;
				}
				else if(word.nature ==Nature.ryt){
					type=QuestionType.TIME;
					equstionType=new EvaluateQuestionType(type,0.8);
					typeList.add(equstionType);
					
					flag=true;
					questionWord=word.word;
				}		
			}
			if(flag){
				String focus=question+keyWord;
				String sql="select * from questionword;";
		  		ResultSet resultSet=stmt.executeQuery(sql);
		  		
		  		while(resultSet.next()){
		  			
		  			String qtype=resultSet.getString("type");
		  			String quesitonword=resultSet.getString("questionword");
		  			String keyword=resultSet.getString("keyword");
//		  			String example=resultSet.getString("example");
		  			
		  			switch(qtype){
		  			case"HUMAN":type=QuestionType.HUMAN;break;
		  			case"LOCATION":type=QuestionType.LOCATION;break;
		  			case"TIME":type=QuestionType.TIME;break;
		  			case"NUMBER":type=QuestionType.NUMBER;break;
		  			case"OBJECT":type=QuestionType.OBJECT;break;
		  			case"DESCRIPTION":type=QuestionType.DESCRIPTION;break;
		  			}

		  			List<String> qList=Arrays.asList(quesitonword.split("、"));
		  			List<String> kList=Arrays.asList(keyword.split("、"));
		  			
		  			if(qList.contains(questionWord)){
		  				equstionType=new EvaluateQuestionType(type,0.4);
		  				flag=true;
		  			}
		  			if(kList.contains(keyWord)){
		  				if(flag) 
		  					equstionType=new EvaluateQuestionType(type,1);
		  				else 
		  					equstionType=new EvaluateQuestionType(type,0.6);
		  			}
		  			if(equstionType!=null)
		  				typeList.add(equstionType);
		  		}
		  		cn.close();
				
			}
			else{
				
			}
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		
		
		return typeList;
	}
	
	//提取问句中的焦点
	public static String extractKeyByLocation(String question){
		
		String keyword=null;
		List<Term> words=WordParser.CutWord(question);
		
		//判断问句中是否含有疑问词
		for(int i=0;i<words.size();i++){
			Term word=words.get(i);
			int length=question.replace("?", "").replace("？", "").length();
			if(word.nature==Nature.ry||word.nature==Nature.ryv||word.nature==Nature.rys||word.nature==Nature.ryt){
				int index=question.indexOf(word.word);
				
				//如果疑问词在句末，则往前查找第一个出现的名词
				if(index==length-1||index==length-word.word.length()){
					for(int t=i;t>0;t--){
						if(words.get(t).nature==Nature.n){
							keyword=words.get(t).word;
							break;
						}
					}
				}
				//如果疑问词出现在其他位置，则往后查找第一个出现的名词
				else {
					for(int t=i+1;t<words.size();t++)
						if(words.get(t).nature==Nature.n){
							keyword=words.get(t).word;
							break;
						}
				}
			}
		}
				
		return keyword;
	}
	
	public static String extractKeyByDependency(String question){
		String key="";
		
		return key="";
	}
	
	public static void main(String[] args){
		
		String question="世界上最大的群岛是什么群岛？";
		String key=extractKeyByLocation(question);
		System.out.println(key);
	}
}
