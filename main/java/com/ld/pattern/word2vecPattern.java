package com.ld.pattern;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.*;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.ld.IO.Property.ReadProperty;
import com.ld.Parser.WordParser;
import com.ld.Vec.Word2Vec;
import com.ld.Vec.domain.WordEntry;

public class word2vecPattern {
	
	public static String url="jdbc:virtuoso://166.111.68.66:1111/charset=GB2312/log_enable=2";
	static List<Nature> natures=new ArrayList<Nature>();
	
	static{
		natures.add(Nature.ns);
		natures.add(Nature.nsf);
		natures.add(Nature.nr);
		natures.add(Nature.nrf);
		natures.add(Nature.nrj);
		natures.add(Nature.t);
		natures.add(Nature.tg);
		natures.add(Nature.nz);
		natures.add(Nature.m);
		natures.add(Nature.mq);
//		natures.add(Nature.vshi);
//		natures.add(Nature.vyou);
//		natures.add(Nature.w);
//		natures.add(Nature.p);
//		natures.add(Nature.f);
	}
	
	public static void insertVecPattern(String path,String course){
		String fromString="from <http://edukb.org/"+course+">";
		
		
		String queryString = "SELECT distinct ?subject ?label ?description "+fromString+" WHERE {"
				+ "?subject <http://www.w3.org/2000/01/rdf-schema#label> ?label."
				+ "?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#DatatypeProperty>."
				+ "?subject <http://purl.org/dc/elements/1.1/description> ?description"
				+ "}";
		String queryString2 = "SELECT distinct ?subject ?label ?description "+fromString+" WHERE {"
				+ "?subject <http://www.w3.org/2000/01/rdf-schema#label> ?label."
				+ "?subject <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty>."
				+ "?subject <http://purl.org/dc/elements/1.1/description> ?description"
				+ "}";
		
		VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");

		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (queryString, set);
		ResultSet result = vqe.execSelect();
       
		vqe = VirtuosoQueryExecutionFactory.create (queryString2, set);
		ResultSet result2 = vqe.execSelect();

        try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			Statement stmt=cn.createStatement();
			
			while (result.hasNext()) {
	            QuerySolution rs=result.nextSolution();
	            
	            String subject=rs.get("subject").toString();
	            
	            String type=null;
	            if(subject.endsWith("biology#")){
	            	type="biology";           			
	            }
	            else if (subject.split("#")[1].startsWith("-")) {
	            	type=subject.substring(subject.indexOf("biology")).replaceAll("\\-", "").replaceAll("#", "");
				}
	            else if((subject.contains("biology")||subject.contains("common"))&&course.equals("biology")&&subject.contains("-"))
	            	type=subject.substring(subject.indexOf("#")).replaceAll("\\-", "").replaceAll("#", "");
	            else if(subject.contains("-")&&subject.contains(course)&&subject.contains("#")){
	            	type=subject.split("#")[1].replaceAll("\\-\\d+", "").replaceAll("\\-", "");
	            }
	            else if(subject.split("#").length>1)
	            	type=subject.split("#")[1];
	            

	            String description=rs.get("description").toString();
//	            if(subject.contains(label)&&description!=null&&!description.equals("")) label=description.replace("geonames-", "");
	            
	            String sql="";
	            if(subject.contains(course)){
	            	
	            	String regex=similarPattern(path,type,course);
		    	  	if(!regex.equals("")){
		            	sql="insert ignore into "+course+"_template (content,type,priority) values ('"+regex+"','"+type+"','3');";
		            	stmt.addBatch(sql);
		    	  	}
	            }
	            if(!result.hasNext()) 
	            	result=result2;
	        }
			stmt.executeBatch();
			cn.close();
			
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
        set.close();
		vqe.close();
	}
	
	public static String similarPattern(String path,String type,String course){
		
		String regex="";
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			Statement stmt=cn.createStatement();
			
			String keyWord=ReadProperty.selectLabelByUri(course, type);
			Set<WordEntry> wordVecSet=getVecWordList(path, keyWord);
			List<Term> keyWords=WordParser.CutWord(keyWord);
			
			//查询此属性对应的所有模板
	        String selSql="select content from "+course+"_template where type='"+type+"';";
	        java.sql.ResultSet resultSet=stmt.executeQuery(selSql);

			for(WordEntry wordEntry:wordVecSet){
				boolean flag=false;
				
				if(wordEntry.score>0.8&&wordEntry.name.length()>1){
					while(resultSet.next()){
		    			String content=resultSet.getString("content");
		    			Matcher matcher=Pattern.compile(content).matcher(wordEntry.name);
		    			if(matcher.find()){
		    				flag=true;
		    				break;
		    			}
		    		}
					if(flag) continue;
					List<Term> words=WordParser.CutWord(wordEntry.name);
					if(!keyWord.equals(wordEntry.name)&&!natures.contains(words.get(0).nature)&&!wordEntry.name.contains(keyWord)){
						if((keyWords.get(0).nature==Nature.n&&words.get(0).nature==Nature.n)||words.get(0).nature!=Nature.n)
							regex+=wordEntry.name+"|";
						
					}
				}
			}
			if(!regex.equals("")){
				regex=regex.substring(0,regex.length()-1);
				regex="(?<title>(.*)?)("+regex+")";
			}
			else return regex;
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return regex;
	}

	public static Set<WordEntry> getVecWordList(String path,String keyWord) {
		
		Set<WordEntry> wordVecSet=new HashSet<WordEntry>();
		
		Word2Vec w1 = new Word2Vec() ;
        w1.loadGoogleModel(path+"/resources/地理全部文本除杂_100.bin") ;
        
        wordVecSet=w1.distance(keyWord);
        System.out.println(wordVecSet);
		
		return wordVecSet;
		
	}
	public static Set<WordEntry> getVecWordList(String path,List<String> words) {
		
		Set<WordEntry> wordVecSet=new HashSet<WordEntry>();
		
		Word2Vec w1 = new Word2Vec() ;
        w1.loadGoogleModel(path+"/resources/地理全部文本除杂_100.bin") ;
        
        wordVecSet=w1.distance(words);
        System.out.println(wordVecSet);
		
		return wordVecSet;
		
	}
	
	public static void main(String[] args) {
//		String sentence="北京 上海 天津 重庆";
//		java.util.Map<String, Nature> wordMap=WordParser.splitWordandNature(WordParser.CutWord(sentence));
//		List<String> words=new ArrayList<String>(wordMap.keySet());
//		List<String> words=Arrays.asList(sentence.split(" "));
//		getVecWordList("D:/Documents/Workspace/KnowledgeQA", words);
		insertVecPattern("D:/Documents/Workspace/KnowledgeQA","geo");

	}

}
