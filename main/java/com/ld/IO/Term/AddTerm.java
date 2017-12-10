package com.ld.IO.Term;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.openrdf.repository.RepositoryException;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;

import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.ld.Parser.WordParser;

public class AddTerm {
	
	public static String url="jdbc:virtuoso://166.111.68.66:1111/charset=GB2312/log_enable=2";
	
	static BufferedWriter bufferedWriter =null;
	static List<Nature> natures=new ArrayList<Nature>();
	
	static{
//		natures.add(Nature.r);
		natures.add(Nature.ry);
		natures.add(Nature.rys);
		natures.add(Nature.ryt);
		natures.add(Nature.ryv);
//		natures.add(Nature.rr);
		natures.add(Nature.rz);
		natures.add(Nature.rzt);
		natures.add(Nature.rzs);
		natures.add(Nature.cc);
		natures.add(Nature.vshi);
		natures.add(Nature.vyou);
		natures.add(Nature.w);
		natures.add(Nature.p);
//		natures.add(Nature.f);
		
	}

  //初始化连接
//	public static void initConnection() throws RepositoryException {
//      repository = new HTTPRepository(SERVER, REPOSITORY);
//      repository.initialize();
//      repository.setUsernameAndPassword("sesame863", "KEG#$*205");
//      connection = repository.getConnection();
//      valueFactory = connection.getValueFactory();
//  }

//	public static void closeConnection() throws RepositoryException {
//      connection.close();
//      repository.shutDown();
//  }
	
	/**
	 * 将实例的label插入至实例表
	 * @param course
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static void searchInstance(String course) throws ClassNotFoundException, SQLException{
		
		VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");
		Map<String, String> instanceMap=new HashMap<String, String>();
		Map<String, String> typeMap=new HashMap<String, String>();
		
		String fromString="";
		switch (course) {
		case "chinese": fromString="from <http://edukb.org/chinese> from <http://edukb.org/chinese_chengyu> from <http://edukb.org/chinese_zidian> from <http://edukb.org/chinese_cidian> from <http://edukb.org/chinese_gushiwen> from <http://edukb.org/chinese_zuopin>";break;
		case "geo": fromString="from <http://edukb.org/geo> from <http://edukb.org/geo_textbook> from <http://edukb.org/geo_ad_baidu> from <http://edukb.org/geo_ad_wiki> from <http://edukb.org/geo_baidu> from <http://edukb.org/geo_wiki> from <http://edukb.org/geo_china_pedia> from <http://edukb.org/geo_geonames> from <http://edukb.org/geo_china_administrative_divisions> from <http://edukb.org/geo_resort> from <http://edukb.org/geo_resort_baidu> ";break;
		case "history":fromString="from <http://edukb.org/history> from <http://edukb.org/history_pedia> from <http://edukb.org/history_baidu> from <http://edukb.org/history_baidu_infobox>";break;		case "english":fromString="from <http://edukb.org/english> from <http://edukb.org/english_cidian>";break;
		default: fromString="from <http://edukb.org/"+course+">";
			break;
		}

		String queryString = "SELECT distinct ?x ?s ?t "+fromString+" WHERE { ?x <http://www.w3.org/2000/01/rdf-schema#label> ?s."
				+ "?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?t."
				+ "} order by ?x";
		
		VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (queryString, set);
		ResultSet result = vqe.execSelect();

		int i=0;
		while (result.hasNext()) { 
			
		    QuerySolution rs = (QuerySolution) result.next();
		    String label=rs.get("s").toString();
		    
		    if(label.equals("")) continue;
		    label=label.replace("'", "\\'");
//		    String uri=rs.get("x").toString();
		    String type=rs.get("t").toString();
	    	if(!course.equals("chinese")&&!course.equals("geo")&&!course.equals("english")&&!course.equals("history")){

	        	String query="CONSTRUCT { ?s ?p ?o } "+fromString+" WHERE { ?s ?p ?o .?s <http://www.w3.org/2000/01/rdf-schema#label> '"+label+"'.}";
	        	vqe = VirtuosoQueryExecutionFactory.create (query, set);

	    		Model model = vqe.execConstruct();
	            Graph g = model.getGraph();
	            int count = 0;
	            for (Iterator it = g.find(Node.ANY, Node.ANY, Node.ANY); it.hasNext();){
	            	Triple t = (Triple)it.next();
	        		String predicate=t.getPredicate().toString();
	        		if(!predicate.contains("Topic_analysis")&&!predicate.contains("Topic_content")&&!predicate.contains("type")&&!predicate.contains("category")&&!predicate.contains("annotator")&&!predicate.contains("annotation")&&!predicate.contains("description")&&!predicate.contains("categoryId")&&!predicate.contains("source")&&!predicate.contains("label")){
	        			count++;
	        		}
//	            		if(count>1) break;
	        	}
		        if(count>0)	{
		            if(type.contains("owl#Class")||type.contains("geo#Geonames")){
		            	typeMap.put(label, i+"");
			            System.out.println(i+"："+label);
				    	i++;
		            }
			    	else if(type.contains("owl#NamedIndividual")){
			    		instanceMap.put(label, i+"");
			            System.out.println(i+"："+label);
				    	i++;
			    	}
		        }
			}
	    	else{
	    		 if(type.contains("owl#Class")||type.contains("geo#Geonames")){
		            	typeMap.put(label, i+"");
			            System.out.println(i+"："+label);
				    	i++;
		            }
			    	else if(type.contains("owl#NamedIndividual")){
			    		instanceMap.put(label, i+"");
			            System.out.println(i+"："+label);
				    	i++;
			    	}
	    	}
		}
		set.close();
		vqe.close();
		//将实例表插入数据库
		insertTerms(instanceMap,course,0);
		insertTerms(typeMap,course,1);
        
    }
	
	/**
	 * 
	 * @param path
	 * @param course
	 * @throws RepositoryException
	 */
	public static void WriteTermtoTxt(String  path,String course) throws RepositoryException{
		
		try {
			VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");
			HashMap map=new HashMap();
			HashMap mapClass=new HashMap();
			String Filewriter = path+"resources/Terms/"+course+"Terms.txt";
		    bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Filewriter), "UTF-8")); 
		    String fromString="";
		    switch (course) {
			case "chinese": fromString="from <http://edukb.org/chinese> from <http://edukb.org/chinese_chengyu> from <http://edukb.org/chinese_zidian> from <http://edukb.org/chinese_cidian> from <http://edukb.org/chinese_gushiwen> from <http://edukb.org/chinese_zuopin>";break;
			case "geo": fromString="from <http://edukb.org/geo> from <http://edukb.org/geo_textbook> from <http://edukb.org/geo_ad_baidu> from <http://edukb.org/geo_ad_wiki> from <http://edukb.org/geo_baidu> from <http://edukb.org/geo_wiki> from <http://edukb.org/geo_china_pedia> from <http://edukb.org/geo_geonames> from <http://edukb.org/geo_china_administrative_divisions> from <http://edukb.org/geo_resort> from <http://edukb.org/geo_resort_baidu> ";break;
			case "history":fromString="from <http://edukb.org/history> from <http://edukb.org/history_pedia> from <http://edukb.org/history_baidu> from <http://edukb.org/history_baidu_infobox>";break;			case "Common": fromString="from <http://edukb.org/chinese>";break;
			default: fromString="from <http://edukb.org/"+course+">";
				break;
			}

	        String queryString = "SELECT ?x ?s "+fromString+" WHERE { ?x <http://www.w3.org/2000/01/rdf-schema#label> ?s."
//	        		+ "?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#NamedIndividual>."
	        		+ "} order by ?x";
	        
	        VirtuosoQueryExecution vqe = VirtuosoQueryExecutionFactory.create (queryString, set);
			ResultSet results = vqe.execSelect();
	        
	        int i=0;
	        while (results.hasNext()) {
	        	QuerySolution rs = results.nextSolution();

	            String label=rs.get("s").toString();
	            
	            if(label.equals("")) continue;
	            label=label.replace("'", "\\'");
	            String label2=rs.get("x").toString();
	            if(label2.contains("instance")&&!label.contains("\n")){
	            	map.put(label, i);
		            System.out.println(i+"："+label);
			    	i++;
	            }
	        } 
	        
	        for(Object value : map.keySet()){
	        	String regex=writeCutTerm(course,value.toString());
	        	if(value.toString().replaceAll(" ", "").equals("")) continue;
	        	bufferedWriter.write(value.toString()+"##"+regex);
		    	bufferedWriter.newLine();
	        }
	        
	        bufferedWriter.close();
	        vqe.close();
	        set.close();
	        
		} catch ( IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
		
	}
	
	
	/**
	 * 将实例label写出正则模板
	 * @param course
	 * @param item
	 * @return
	 */
	public static String writeCutTerm(String course,String item){
		
		String regex="";
		boolean flag=false;
		List<Term> cutWord=null;
		if(item.startsWith("“")&&item.endsWith("”")){
			regex=item.replaceAll("“", ".{0,1}").replace("”", ".{0,1}");
		}
		else if(course.equals("chinese")&&item.contains("/")){
			regex="("+item.replace("/", "|")+")";
			regex=regex.replaceAll(" ", "");
		}
		else if(item.length()>2){
			item=item.replaceAll("\\d）", "").replaceAll("（\\d）", "").replaceAll("\\d\\.", "");
			String orgin=item;
			if(item.length()>7&&item.endsWith("的方式")) item=item.replace("的方式", "");
			if(course.equals("chemistry")||course.equals("physics")){
				if(item.length()==3&&item.endsWith("单质")){
					item=item.replace("单质", "");
					regex=item;
				}
				
				item=item.replace("实验-", "");
				if(item.endsWith("的反应"))
					item=item.replace("的反应", "");
				else if(item.endsWith("的实验"))
					item=item.replace("的实验", "");
				cutWord=WordParser.CutWord(item);
				if(cutWord.size()==1) regex=item;
			}
			else if(course.equals("politics")&&item.contains("正确理解")&&item.length()>8){
				item=item.substring(item.indexOf("正确理解")+4);
				if(item.startsWith("“")&&item.endsWith("”"))
					item=item.replace("“", "").replace("”", "");
			}
			else if(course.equals("chinese")&&item.endsWith("·并序")){
				item=item.replace("·并序", "");
				regex=item;
			}
			else if(course.equals("chinese")&&item.contains("·其")){
				item=item.substring(0,item.indexOf("·其"));
				regex=item;
			}
			else if(course.equals("geo")&&(item.endsWith("自治州")||item.endsWith("自治区"))&&item.length()>4){
				item=item.substring(0,item.indexOf("自治"));
				cutWord=WordParser.CutWord(item);
				regex=cutWord.get(0).word;
			}
			else if (course.equals("geo")&&item.endsWith("极地区")&&item.length()>3) {
				item=item.replaceAll("地区", "");
				regex=item;
			}
			else if(course.equals("history")&&item.endsWith("原始居民")&&item.length()>4){
				item=item.replace("原始居民", "");
				cutWord=WordParser.CutWord(item);
				if(cutWord.size()==1) regex=item;
			}
			else if(course.equals("history")&&item.endsWith("政权")&&item.length()>2){
				item=item.replace("政权", "");
				cutWord=WordParser.CutWord(item);
				if(cutWord.size()==1) regex=item;
			}
			else if ((!course.equals("history"))&&item.contains("（")&&item.endsWith("）")) {
				item=item.replaceAll("（(.*)?）", "");
				
				cutWord=WordParser.CutWord(item);
				if(cutWord.size()==1) regex=item;
			}
			else if (item.replaceAll("\\?", "").length()==2) {
				regex=item.replaceAll("\\?", "");
			}
			else if (item.startsWith("和")&&item.length()<4) {
				regex=item;
			}
			
			item=item.replace("(", "").replace(")", "");
			cutWord=WordParser.CutWord(item);
			
			if(cutWord.size()>1&&regex.equals("")){

				String temp="";
				for(int j=0;j<cutWord.size();j++){
					Term word=cutWord.get(j);
					temp=word.word.replaceAll("\\{", "\\.").replaceAll("\\{", "\\.").replaceAll("\\}", "\\.").replaceAll("\\}", "\\.").replaceAll("\\*", "\\.");
					
					if(temp.equals("我国")||temp.equals("中国")) 
						temp="(我国|中国)";
					if(!course.equals("geo")&&!course.equals("history")&&word.nature==Nature.f&&item.length()>4) 
						temp="@";
					
					if(natures.contains(word.nature)||word.word.equals("-")){
						temp="@";
					}

//					String Synonym="";
//					if((word.nature==Nature.n||word.nature==Nature.vn||word.nature==Nature.v)&&word.word.length()>1)
//						Synonym=synonym.getSynonym(temp, wordsEncode,encodeWords);
//					if(Synonym!=null&&!Synonym.equals("")){
//						temp="("+Synonym+")";
//						regex+=temp;
//						continue;
//					}

					if(j<cutWord.size()){
						
						if(temp.equals("@")&&(regex.endsWith("@")||regex.endsWith(".{0,4}")))
							continue;
						else if(!temp.equals("@")&&j!=0&&!regex.equals("@")){
							regex+=".{0,4}"+temp;
						}
						else if(temp.equals("@")||j==0||(!temp.equals("@")&&regex.endsWith("@"))) 
							regex+=temp;
					}
					if(word.nature==Nature.v||word.nature==Nature.vi)
						flag=true;
				}
			}
			regex=regex.replace("青少年","(青少年|中学生)");
			regex=regex.replaceAll("\\.\\{0,4\\}的\\.\\{0,4\\}", "@").replace(".{0,4}与非.{0,4}", "(.*)?非.{0,4}");
			regex=regex.replaceAll("@\\.\\{0,4\\}", "@").replace(".{0,4}@", "@");
			regex=regex.replaceAll("\\.\\{0,4\\}、\\.\\{0,4\\}", "@");
			regex=regex.replaceAll("\\.\\{0,4\\}与\\.\\{0,4\\}", "@").replaceAll("\\(\\.\\*\\)?与\\(\\.\\*\\)?", "@").replaceAll("与\\.\\{0,4\\}", "@");
			regex=regex.replaceAll("\\.\\{0,4\\}和\\.\\{0,4\\}", "@").replaceAll("\\(\\.\\*\\)?和\\(\\.\\*\\)?", "@").replaceAll("和\\.\\{0,4\\}", "@");
//			regex=regex.replaceAll("与", "\\.");
//			regex=regex.replaceAll("和", "\\.");
			regex=regex.replaceAll("\\.\\{0,4\\}\\(\\.\\{0,4\\}", ".").replaceAll("\\.\\{0,4\\}\\)\\.\\{0,4\\}", ".").replaceAll("\\.\\{0,4\\}\\)", ".");
			regex=regex.replaceAll("\\.\\{0,4\\}\\（\\.\\{0,4\\}", ".").replaceAll("\\.\\{0,4\\}\\）\\.\\{0,4\\}", ".").replaceAll("\\.\\{0,4\\}\\）", ".");
			regex=regex.replaceAll("中的", "中");
			regex=regex.replace(".{0,4}/.{0,4}", "@").replace(".{0,4}.{0,4}", "@").replace("'", "");
			regex=regex.replaceAll("\\(\\d\\)", "@");
			regex=regex.replaceAll("@@@@", "@");
			regex=regex.replaceAll("@@@", "@");
			regex=regex.replaceAll("@@", "@");
			regex=regex.replaceAll("@", "(.*)?");

			
			if(flag)
				regex=regex.replaceAll("\\.\\{0,4\\}", "(.*)?");
			regex=regex.replaceAll("\\（", ".").replaceAll("\\）", ".");
			
//			regex=regex.replaceAll("和", "(同|与|跟|和)");
			if(regex.equals("")&&orgin.contains("实验-"))
				regex=item;
		}
		
		return regex;
		
	}

	
	/**
	 * 根据查询出的实例生成术语表，插入至数据库
	 * @param map
	 * @param course
	 * @param isType
	 */
	public static void insertTerms(Map map,String course,int isType){
		Connection cn;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			Statement stmt=cn.createStatement();

			for(Object value : map.keySet()){
				String sql="";
				String item=value.toString();
				String regex=writeCutTerm(course,item);

				if(course.equals("geo")&&regex.equals("")){
					if(item.length()>2&&item.endsWith("省")) regex=item.replace("省", ".");
					else if(item.length()>2&&item.endsWith("市")) regex=item.replace("市", ".");
				}
//				item=item.replace("'", "\\'");
				if(!regex.equals(""))
					sql="insert into "+course+"_terms (term, regex,isType) values ('"+item+"','"+regex+"',"+isType+");";
				else 
					sql="insert into "+course+"_terms (term,regex,isType) values ('"+item+"','null',"+isType+");";
				System.out.println(sql);
				stmt.addBatch(sql);
//				boolean results=stmt.execute(sql);
				
	        }
			stmt.executeBatch();
			cn.close();
			System.out.println("更新完毕");
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	
	/**
	 * 将实例的别名插入至实例表
	 * @param course
	 * @param labelMap
	 */
	public static void addAltLabel(String course,Map<String, String> labelMap){
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			Connection cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			
			Statement stmt=cn.createStatement();
			for(Entry<String,String> entry :labelMap.entrySet()){
				String key=entry.getKey();
				String value=entry.getValue();
				
				String sql="";
				String altLabel="";
				String selectSql="select altlabel from "+course+"_terms where term='"+value+"';";
				java.sql.ResultSet resultSet=stmt.executeQuery(selectSql);
				if(resultSet.next()){
					altLabel=resultSet.getString("altlabel");
					if(altLabel!=null&&!altLabel.equals("")&&!altLabel.contains(key)){
						altLabel+="、"+key;
					}
					else if(altLabel==null)
						altLabel=key;
					else if(!altLabel.contains(key))
						altLabel=key;
					else if(altLabel.contains(key))
						altLabel="";
				}
				else altLabel=key;
				if(altLabel!=null&&!altLabel.equals("")){
					sql="update "+course+"_terms set altlabel='"+altLabel+"' where term='"+value+"';";
					stmt.addBatch(sql);
				}
			}
	  		stmt.executeBatch();
	  		
	  		cn.close();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
	}
	
	public static void AddPoetryName(){
		
		String label=null;
		VirtGraph set = new VirtGraph (url, "dba", "#keg#$*205#");
		
//		bufferedWriter=null;
//		String Filewriter = path+"resources/Terms/chineseAltLabel.txt";
	    try {
//			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(Filewriter), "UTF-8"));
	    	Connection cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			
			Statement stmt=cn.createStatement();
			List<String> FilterList=new ArrayList<String>();
			FilterList.add("·并序");
			FilterList.add("·其");
			FilterList.add("/");
			VirtuosoQueryExecution vqe=null;

			String queryString = "SELECT ?subject ?label from <http://edukb.org/chinese> from <http://edukb.org/chinese_chengyu> from <http://edukb.org/chinese_zidian> from <http://edukb.org/chinese_cidian> from <http://edukb.org/chinese_gushiwen> from <http://edukb.org/chinese_zuopin> WHERE { "
					+ "?subject <http://www.w3.org/2000/01/rdf-schema#label> ?label."
//						+ "?subject bif:contains '\"instance\"'."
					+ "} ";
			
			vqe = VirtuosoQueryExecutionFactory.create (queryString, set);
			ResultSet result = vqe.execSelect();
			
		    while (result.hasNext()) { 
		
		    	QuerySolution rs = result.nextSolution();
		    	String altLabel="";
		        label=rs.get("label").toString();
		        String subject=rs.get("subject").toString();
		        if(!subject.contains("instance")) continue;
		        if(label.contains("/"))
		        	altLabel=label.replaceAll("/", "、").replaceAll(" ", "");
		        else if(label.contains("·其"))
		        	altLabel=label.substring(0,label.indexOf("·其"));
		        else if(label.contains("·并序"))
		        	altLabel=label.substring(0,label.indexOf("·并序"));
		        if(altLabel!=null&&!altLabel.equals("")){
		        	String sql="insert into chinese_terms (term, altlabel) values ('"+label+"','"+altLabel+"');";
		        	stmt.addBatch(sql);
		        }
		    }
		   
		    stmt.executeBatch();
		    
			vqe.close();
		    set.close();
		    cn.close();
//		    bufferedWriter.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void addCourseAlt(){
		
		Map<String, String> map=new HashMap<String, String>();
		map.put("第一个五年计划","一五计划");
		map.put("灭亡西晋","西晋的兴亡");
		map.put("红军远征","红军长征");
		map.put("二战","第二次世界大战");
		map.put("一战","第一次世界大战");
		map.put("河姆渡","河姆渡原始居民");
		map.put("分封制","西周的分封制");
		map.put("“百花齐放”","“百花齐放”“百家争鸣”");
		map.put("唐朝诗歌","唐诗");
		map.put("文成公主入吐蕃","文成公主入藏");
		map.put("法国资产阶级革命","法国大革命");
		map.put("同盟会","中国同盟会");
		map.put("甲午战争","甲午中日战争");
		map.put("科技革命","工业革命");
		map.put("虎门销烟","禁烟运动");
		map.put("七七事变","卢沟桥事变");
		addAltLabel("history", map);
		map.clear();
		
		map.put("比热","比热容");
		addAltLabel("physics", map);
		
		map.put("西双版纳","西双版纳傣族自治州");
		map.put("WTO","世界贸易组织");
		map.put("主次矛盾","主要矛盾和次要矛盾");
		addAltLabel("politics", map);
		map.clear();
		
		map.put("二氧化碳","二氧化碳的制取");
		addAltLabel("chemistry", map);
		map.put("硫磺","硫");
		map.put("过滤两次以上仍然浑浊","过滤失败的原因");
		map.put("干冰","二氧化碳");
		map.put("三氧化二铁","氧化铁");
		map.put("工业上制取氧气的方法","氧气的工业制法");
		map.put("湿法炼铜","铁与硫酸铜溶液的反应");
		map.put("大理石","碳酸钙");
		map.put("降低水的硬度","硬水软化的方法");
		map.put("炼铁","铁的冶炼");
		addAltLabel("chemistry", map);
		map.clear();
		
		map.put("孟子","孟轲");
		map.put("列子","列御寇");
		map.put("晏子","晏婴");
		addAltLabel("chinese", map);
		
		map.put("玻片","玻片标本");
		map.put("植物的芽","植株的芽");
		addAltLabel("biology", map);
	}
	
	public static void main(String[] args) throws Exception{
		
//		JsonData("Common");
//		Class.forName("com.mysql.jdbc.Driver");		
//		cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
//		
//		Statement stmt=cn.createStatement();
//		String sql="insert ignore into Common_property (uri,label) values ('usage','用途');";
//  		stmt.execute(sql);
//		cn.close();
		addCourseAlt();
		
		
//		String course="chemistry";
//		VirtuosoSearch vSearch=new VirtuosoSearch(course);
//		map=vSearch.selectOtherName(course);
//		addAltLabel(course, map);
//		AddPoetryName("D:\\Documents\\Workspace\\KnowledgeQA\\");
//		AddPoetryName();
	}
}
