package com.ld.QuestionGeneration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryException;

import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import com.ld.Parser.WordParser;
import com.ld.search.VirtuosoSearch;


/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：GenerateQuestion   
* 类描述： 根据三元组生成问题
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:57:26   
* @version        
*/
public class GenerateQuestion {
	
	private static String SERVER = "http://166.111.7.170:18080/openrdf-sesame/";
	static Connection cn;
	
	public static void generate(String course,String path) throws ClassNotFoundException, SQLException{
		
		String repo=course;
		
		XSSFWorkbook wb = null;
		Sheet sheet =null;
		String ExcelPath=path+course+"\\"+course+"Generate.xlsx";
//		File file=new File(ExcelPath);

		wb = new XSSFWorkbook();
           
        sheet = (Sheet) wb.createSheet("sheet1"); 
        //设置列宽
        sheet.setColumnWidth(1, 10000);
        
        //添加表头  
        Row row = sheet.createRow(0);
       
        row.createCell(0).setCellValue("编号");
        row.createCell(1).setCellValue("问题");
        row.createCell(2).setCellValue("答案");
        
        try {
        	//从学科知识库获取实例及属性
			JSONArray jsonArray=GetInstanceProperty.getInstanceandPropertyofClass(SERVER, repo);
			
			JSONObject resultObject=new JSONObject();
			int t=1;
			Class.forName("com.mysql.jdbc.Driver");
			cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			for(int s=0;s<jsonArray.size();s++){
				
				resultObject=jsonArray.getJSONObject(s);      
		        
		        JSONArray instances =resultObject.getJSONArray("instance"); 
		        JSONArray propertys=resultObject.getJSONArray("property");
		        String className=resultObject.getString("className");       
	            
	            for(int i=0;i<instances.size();i++){
	            	for(int j=0;j<propertys.size();j++){
	            		
	            		String question=union(instances.getString(i), propertys.getString(j),course,className);
	            		
	            		row = (Row) sheet.createRow(t);  
	    	            row.createCell(0).setCellValue(t);
	    	            row.createCell(1).setCellValue(question);    
	    	            row.createCell(3).setCellValue(instances.getString(i)); 
	    	            row.createCell(4).setCellValue(propertys.getString(j)); 
	    	            t++;
	            	}
	            }

			}
	        
			
	        FileOutputStream stream;
	       
	        stream = new FileOutputStream(ExcelPath);
			   
	        wb.write(stream);  
	        //关闭文件流   
	        stream.flush();
	        stream.close(); 
	        cn.close();
		} catch (RepositoryException | MalformedQueryException
				| QueryEvaluationException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}        
	}
	
	public static void generateByExcel(String course,String ExcelPath) {
		
		XSSFWorkbook wb = null;
		XSSFWorkbook wb2=null;
		try {
			wb = new XSSFWorkbook(ExcelPath);
			wb2=new XSSFWorkbook();
			
			XSSFSheet sheet = wb.getSheetAt(0);
	        
	        Sheet sheet2 = (Sheet) wb2.createSheet("sheet1");
	        //设置列宽
	        sheet2.setColumnWidth(1, 10000);
	        
	        //添加表头  
	        Row row2 = sheet2.createRow(0);
	       
	        row2.createCell(0).setCellValue("编号");
	        row2.createCell(1).setCellValue("问题");
	        row2.createCell(2).setCellValue("答案");
	        String content="";
	        String ExcelAnswer="";
	    
	    	Row row=null;
	    	String[] contentArray=null;
	    	String[] answerArray=null;
	    	int t=1;
	    	
			for(int i = sheet.getFirstRowNum(); i<sheet.getPhysicalNumberOfRows(); i++) {

	    	    row = sheet.getRow(i);
	    	    content= row.getCell(1).toString();
	    	    ExcelAnswer=row.getCell(2).toString();
	    	    if(course.equals("english")){
		    	    if(content.contains("句子")) continue;
		    	    
		    	    contentArray=content.split("\\d+\\.");
		    	    answerArray=ExcelAnswer.split("\\d+\\.");
		    	    for(int j=1;j<contentArray.length;j++){
		    	    	String question=contentArray[j].replaceAll("\\d+", "").replaceAll(" ", "").replaceAll("_", "");
		    	    	String answer=answerArray[j].replaceAll("\\d+", "").replaceAll(" ", "").replaceAll("_", "");
		    	    	question=RandomQuestion(question,true);
		    	    	
		    	    	row2 = (Row) sheet2.createRow(t);  
			            row2.createCell(0).setCellValue(t);  
			            row2.createCell(1).setCellValue(question); 
			            row2.createCell(2).setCellValue(answer); 
			            t++;
		    	    }
	    	    }
	    	    else if(course.equals("chinese")){
	    	    	contentArray=content.split("（\\d+）");
		    	    answerArray=ExcelAnswer.split("（\\d+）");
		    	    for(int j=1;j<contentArray.length;j++){
		    	    	String question=contentArray[j];
		    	    	String answer=answerArray[j];
		    	    	
		    	    	row2 = (Row) sheet2.createRow(t);  
			            row2.createCell(0).setCellValue(t);  
			            row2.createCell(1).setCellValue(question); 
			            row2.createCell(2).setCellValue(answer); 
			            t++;
		    	    }
	    	    }
			}
			
	        FileOutputStream stream;
	       ExcelPath=ExcelPath.replaceAll("\\.xlsx", "(2).xlsx");
	        stream = new FileOutputStream(ExcelPath);
			   
	        wb2.write(stream);  
	        //关闭文件流   
	        stream.flush();
	        stream.close();
	        wb.close();
	        wb2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void generateByChoice(String course, String ExcelPath){
		XSSFWorkbook wb = null;
		XSSFWorkbook wb2=null;
		try {
			wb = new XSSFWorkbook(ExcelPath);
			wb2=new XSSFWorkbook();
			
			XSSFSheet sheet = wb.getSheetAt(0);
	        
	        Sheet sheet2 = (Sheet) wb2.createSheet("sheet1");
	        //设置列宽
	        sheet2.setColumnWidth(1, 10000);
	        
	        //添加表头  
	        Row row2 = sheet2.createRow(0);
	       
	        row2.createCell(0).setCellValue("编号");
	        row2.createCell(1).setCellValue("问题");
	        row2.createCell(2).setCellValue("答案");
	        String content="";
	        String ExcelAnswer="";
	    
	    	Row row=null;
	    	String[] contentArray=null;
	    	String[] answerArray=null;
	    	int t=1;
	    	
			for(int i = sheet.getFirstRowNum(); i<sheet.getPhysicalNumberOfRows(); i++) {

	    	    row = sheet.getRow(i);
	    	    content= row.getCell(0).toString();
	    	    ExcelAnswer=row.getCell(1).toString();
	    	    if(ExcelAnswer.contains("故选："))
	    	    	ExcelAnswer=ExcelAnswer.split("故选：")[1].replaceAll("．", "").replaceAll("\\.", "");

	    	    String question="";
	    	    String answer="";
	    	    if(content.length()>60&&!content.contains("______")&&!content.contains("A"))
	    	    	continue;
	    	    if(content.contains("不正确")||content.contains("不包括")||content.contains("错误")||content.contains("下列"))
	    	    	continue;
	    	    else if(content.contains("A")&&content.contains("B")){
	    	    	
		    	    contentArray=content.split("A\\.");
		    	    if(contentArray.length>1){
		    	    	question=contentArray[0].replaceAll(" ", "").replace("（　　）。", "")+"什么";
		    	    	answerArray=contentArray[1].split("[A-D]\\.");
		    	    	switch (ExcelAnswer) {
						case "A": answer=answerArray[0];break;
						case "B": answer=answerArray[1];break;
						case "C": answer=answerArray[2];break;
						case "D": answer=answerArray[3];break;
						
						}
		    	    }
	    	    }
	    	    else if(content.contains("______")){
	    	    	question=content.replace("______", "什么");
	    	    	answer=ExcelAnswer;
	    	    }
	    	    if(question.equals("")&&answer.equals("")){
	    	    	question=content;
	    	    	answer=ExcelAnswer;
	    	    }
	    	    row2 = (Row) sheet2.createRow(t);  
	            row2.createCell(0).setCellValue(t);  
	            row2.createCell(1).setCellValue(question); 
	            row2.createCell(2).setCellValue(answer); 
	            t++;
			}
			
	        FileOutputStream stream;
	        ExcelPath=ExcelPath.replaceAll("\\.xlsx", "(2).xlsx");
	        stream = new FileOutputStream(ExcelPath);
			   
	        wb2.write(stream);  
	        //关闭文件流   
	        stream.flush();
	        stream.close();
	        wb.close();
	        wb2.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//将没有属性值的三元组存入excel中
	public static void generateNoProperty(String course,String path) throws ClassNotFoundException, SQLException{
		
		XSSFWorkbook wb = null;
		Sheet sheet =null;
		String ExcelPath=path+course+"\\"+course+"NoPreperty.xlsx";

		VirtuosoSearch search=new VirtuosoSearch(course);

		wb = new XSSFWorkbook();
           
        sheet = (Sheet) wb.createSheet("sheet1"); 
        //设置列宽
        sheet.setColumnWidth(1, 10000);
        
        //添加表头  
        Row row = sheet.createRow(0);
       
        row.createCell(0).setCellValue("编号");
        row.createCell(1).setCellValue("实例");
        row.createCell(2).setCellValue("属性");
        
        try {
        	//从学科知识库获取实例及属性
			JSONArray jsonArray=new JSONArray();

			BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream("D:\\TestSet\\"+course+"\\"+course+".json"),"UTF-8")); 
			StringBuffer stringBuffer = new StringBuffer();
			String line = null ;	
			while( (line = br.readLine())!= null ){
				stringBuffer.append(line);
			} 
			
			jsonArray=JSONArray.fromObject(stringBuffer.toString());
			
			JSONObject resultObject=new JSONObject();
			int t=1;
			Class.forName("com.mysql.jdbc.Driver");
			cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			for(int s=0;s<jsonArray.size();s++){
				
				resultObject=jsonArray.getJSONObject(s);      
		        
		        JSONArray instances =resultObject.getJSONArray("instance"); 
		        JSONArray propertys=resultObject.getJSONArray("property");
		        String className=resultObject.getString("className");
		        GenerateQuestion gq=new GenerateQuestion();
		       	             
		        if(!className.equals("地理概念")&&!className.equals("区域")&&!className.equals("地理事实")&&!className.equals("地理方法")&&!className.equals("常识概念")){
		            for(int i=0;i<instances.size();i++){
		            	for(int j=0;j<propertys.size();j++){
		            		if(!propertys.getString(j).equals("内容")&&!propertys.getString(j).equals("特征")&&!propertys.getString(j).equals("原理")){
			            		boolean flag=search.SubjectAndProperty(course,instances.getString(i), propertys.getString(j));
			            		if(!flag){
				            		
				            		row = (Row) sheet.createRow(t);  
				    	            row.createCell(0).setCellValue(t);  
				    	            row.createCell(1).setCellValue(instances.getString(i)); 
				    	            row.createCell(2).setCellValue(propertys.getString(j)); 
				    	            t++;
			            		}
		            		}
		            	}
		            }
		        }
			}
	        
			
	        FileOutputStream stream;
	       
	        stream = new FileOutputStream(ExcelPath);
			   
	        wb.write(stream);  
	        //关闭文件流   
	        stream.flush();
	        stream.close(); 
	        cn.close();
		} catch (RepositoryException | MalformedQueryException
				| QueryEvaluationException | IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}        
	}

	public static String union(String instance,String property,String course,String className){
		
		String question="";
		List<Term> words=WordParser.CutWord(property);
		if(className.equals("关系")||className.equals("地理关系")){
			question=replaceEnd(question,instance,property,"相同点");
			question=replaceEnd(question,instance,property,"不同点");
			question=replaceEnd(question,instance,property,"异同");
			question=replaceEnd(question,instance,property,"异同点");
			question=replaceEnd(question,instance,property,"比较");
			question=replaceEnd(question,instance,property,"对比");
			question=replaceEnd(question,instance,property,"区别");
			question=replaceEnd(question,instance,property,"差异");
			question=replaceEnd(question,instance,property,"联系");
			question=replaceEnd(question,instance,property,"关系");
			question=replaceEnd(question,instance,property,"影响");
			question=replaceStart(question,instance,property,"比较");
			question=replaceStart(question,instance,property,"对比");
			if(question.equals("")){
				question=instance+property+"的异同有哪些？";
			}
			
		}
		else if(className.equals("人物")&&property.equals("解释")){
			question="对"+instance+"进行简要介绍";
		}
		else if((className.equals("词语")||className.equals("成语"))&&property.equals("解释")){
			question=instance+"是什么意思";
		}
		else if(property.endsWith("者")||property.endsWith("人物")||property.endsWith("人")||property.endsWith("员")){
			question=instance+"的"+property+"是谁？";
		}
		else if((property.endsWith("时间")&&!property.equals("时间"))||(property.endsWith("日期")&&!property.equals("日期"))){
			question=instance+"是什么时候"+property.substring(0,2)+"的";
		}
		else if(property.endsWith("地点")&&!property.equals("地点")){
			question=instance+"在哪里"+property.substring(0,2)+"的";
		}
		else if(property.endsWith("地位")&&!property.equals("地位")){
			question=instance+"在"+property.substring(0,2)+"上有着怎样的地位";
		}
		else if(property.endsWith("方法")&&!property.equals("方法")){
			question=instance+"是如何"+property.substring(0,2)+"的";
		}
		else if(property.endsWith("基础")){
			question=instance+"有着怎样的"+property;
		}
		else if(property.endsWith("图")){
			question=instance+"的"+property+"怎么画";
		}
		else if(property.contains("原因")&&instance.contains("原因")){
			question=instance+"包含哪些"+property.substring(0,2)+"的原因";
		}
		if(question.equals("")){
			
			switch(property){
			case "定义":question="什么是"+instance;break;
			case "国籍":question=instance+"是哪国人";break;
			case "时代":question=instance+"是哪个朝代的人";break;
			case "字":question=instance+"字什么";break;
			case "号":question=instance+"号什么";break;
			case "职业":question=instance+"是什么家";break;
			case "籍贯":question=instance+"故居在哪里";break;
			case "性别":question=instance+"是什么性别";break;
			case "气候类型":question=instance+"属于什么气候类型";break;
			case "相关人物":question="与"+instance+"相关的人物是谁";break;
			case "时间":question=instance+"发生在何时";break;
			case "地点":question=instance+"发生在什么地方";break;
			case "原因":question=instance+"的成因是什么";break;
			case "过程":question=instance+"经历了哪几个过程？";break;
			case "内容":question="简要说明"+instance+"的具体内容";break;
			case "包含":question=instance+"包含哪些部分";break;
			case "历史条件":question=instance+"有什么前提条件？";break;
			case "含义":question=instance+"有什么含义？";break;
			case "示例":question="请举几个"+instance+"的例子";break;
			case "解释":question="解释"+instance+"是什么";break;
			case "总部":question=instance+"的总部在哪儿？";break;
			case "转折点":
			case "名":
			case "谥号":
			case "笔名":
			case "别名":
			case "标志":
			case "公式":
			case "单位":
			case "依据":
			case "电路符号":
			case "方程":
			case "符号":
				question=instance+"的"+property+"是什么";break;
			case "常数":
				question=instance+"的"+property+"是多少";break;
			case "开国君主":
				question=instance+"的"+property+"是谁";break;
			case "亡国君主":question=instance+"的最后一位皇帝是谁";break;
			case "作用":
			case "用途":
			case "特点":
			case "特征":
			case "影响":
			case "意义":
			case "别称":
			case "缺点":
			case "危害":
				question=instance+"有什么"+property;break;
			case "历史地位":question=instance+"在历史上有怎样的地位";break;
			case "解决问题":question=instance+"解决了什么问题";break;
			case "启示":question=instance+"给了人们什么样的"+property;break;
			case "成就":
			case "方法":
			case "因素":
			case "优点":
				question=instance+"有哪些"+property;break;
			case "辨析":question="试辨析“"+instance+"”这一观点是否正确";break;
			case "表现":question=instance+"表现在哪些方面";break;
			case "措施":question=instance+"应采取怎样的措施";break;
			case "风险性":
			case "流动性":
			case "收益":
				question=instance+"的"+property+"如何";break;
			case "联系":
			case "区别":
			case "目的":
				question=instance+"有着怎样的"+property;break;
			case "规律":
				question=instance+"有什么样的"+property;break;
			case "评价":
				question="如何"+property+instance;break;
			case "起源":question=instance+property+"于哪";break;
			case "态度":question="对待"+instance+"应采取怎样的态度";break;
			case "途径":question=instance+"应通过怎样的途径来实现";break;
			case "重要性":question=instance+"重要性体现在哪";break;
			case "类别":question=instance+"可分为哪些类别";break;
			case "范围":question=instance+"适用的范围是什么";break;
			case "功能关系":question=instance+"的"+property+"是怎样的";break;
			default: break;
			}
		}
		if(question.equals("")&&property.length()>2){
//			try {
//				Statement stmt=cn.createStatement();
//				String sql="select * from "+course+"_template where priority=1 and type in (select uri from "+course+"_property where label='"+property+"');";
//				
//				ResultSet results=stmt.executeQuery(sql);
//				while (results.next()) { 
//					String template=results.getString("content");
//					String temp=template.replace("(?<title>(.*)?)", instance);
//					if(template.contains("|")){
//						temp=selectOne(temp);
//					}
//					if(temp.indexOf("(.*)?")<temp.length())
//						temp=temp.substring(0,temp.lastIndexOf("(.*)?"));
//					if(temp.indexOf("(.*)?")<temp.length())
//						temp=temp.replace("(.*)?", "什么");
//					temp=temp.replace(".{0,4}", "的");
//					
//					question=temp.replace("(?<title1>(.*)?)", "").replace("(", "").replace(")", "").replace("?", "");
//					System.out.println(question);
//					if(!temp.contains("(.*)?")) break;
//					
//				}
//				
//			} catch ( SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			if(words.size()==2)
				question=instance+words.get(0).word+"的"+words.get(1).word+"是什么";
			else question=instance+"有哪些"+property+"？";
		}
		else if(question.equals("")&&property.length()<=2){
			
			if(words.size()==1&&(words.get(0).nature==Nature.v||words.get(0).nature==Nature.vi||words.get(0).nature==Nature.vn)){
				question=instance+"是如何"+property+"的";
			}
			else if(words.size()==1&&(words.get(0).nature==Nature.n||words.get(0).nature==Nature.nz))
				question=instance+"的"+property+"是怎样的？";
		}
		
		
		return question;
		
	}
	
	public static String replaceEnd(String question,String instance,String property,String mark){
		
		if(question.equals("")){
			if(instance.endsWith("的"+mark)){
				if(property.equals("内容"))
					question=instance.replace("的"+mark, "有哪些"+mark);
				else
					question=instance.replace("的"+mark, "在"+property+"上有什么"+mark);
			}	
			else if(instance.endsWith(mark)){
				if(property.equals("内容"))
					question=instance.replace(mark, "有哪些"+mark);
				else
					question=instance.replace(mark, "在"+property+"上有什么"+mark);
			}	
		}
		return question;
	}
	
	public static String replaceStart(String question,String instance,String property,String mark){
		
		if(question.equals("")){
			if(instance.startsWith(mark)){
				if(property.equals("内容"))
					question="试"+instance+"异同";
				else
					question="试"+instance+property+"的异同";	
			}	
		}
		return question;
	}
	
	public static String selectOne(String temp){
		
		String selectStr="";
		String select1="";
		String result=temp;
		if(temp.contains("|")){
			int index1=temp.indexOf("|");
			select1=temp.substring(temp.lastIndexOf("(", index1),temp.indexOf(")",index1)+1);		
			selectStr=select1.substring(1,select1.indexOf("|"));
			
			result=temp.replace(select1, selectStr);
		}
		
		if(!result.equals(temp))
			return result=selectOne(result);
		else 
			return result;
	}
	
	public static String RandomQuestion(String key,boolean isTratoChinese){
		String question="";
		
		Random rand = new Random();
    	int randNum = rand.nextInt(101);
    	int mod=randNum%5;
    	if(isTratoChinese){
	    	switch (mod) {
	    	case 0:question=key+"的英文是什么";break;
			case 1:question=key+"用英语怎么说";break;
			case 2:question=key+"汉译英";break;
			case 3:question="怎么用英语表示"+key;break;
			case 4:question=key+"翻译成英语";break;
			default:question=key+"的英语是什么";break;
			}
    	}
    	else{
    		switch (mod) {
	    	case 0:question=key+"的中文意思是什么";break;
			case 1:question=key+"是什么意思";break;
			case 2:question=key+"英译汉";break;
			case 3:question="如何解释"+key;break;
			case 4:question=key+"翻译成中文";break;
			default:question=key+"的中文解释是什么";break;
			}
    	}
    	
    	return question;
	}
	
	public static void generateByTxt(String course,String path) throws ClassNotFoundException, SQLException{

		XSSFWorkbook wb = null;
		Sheet sheet =null;
		String ExcelPath=path+course+"\\"+course+"generate.xlsx";

		wb = new XSSFWorkbook();
           
        sheet = (Sheet) wb.createSheet("sheet1"); 
        //设置列宽
        sheet.setColumnWidth(1, 10000);
        
        //添加表头  
        Row row = sheet.createRow(0);
       
        row.createCell(0).setCellValue("编号");
        row.createCell(1).setCellValue("问题");
        row.createCell(2).setCellValue("答案");
        
        try {
        	File ansswerFile=new File("D:\\TestSet\\math\\answers.txt");
        	File questionFile=new File("D:\\TestSet\\math\\questions_gen.txt");
            if(ansswerFile.isFile() && ansswerFile.exists()&&questionFile.isFile()){ //判断文件是否存在
                InputStreamReader readAnswer = new InputStreamReader(new FileInputStream(ansswerFile),"utf-8");
                InputStreamReader readQuestion = new InputStreamReader(new FileInputStream(questionFile),"utf-8");
                BufferedReader brAnswer = new BufferedReader(readAnswer);
                BufferedReader brQuestion = new BufferedReader(readQuestion);

                String lineQustion= null;
				String lineAnswer= null;
				int t=1;
				while((lineQustion = brQuestion.readLine()) != null&&(lineAnswer = brAnswer.readLine()) != null){
					row = (Row) sheet.createRow(t);  
    	            row.createCell(0).setCellValue(t);
    	            row.createCell(1).setCellValue(lineQustion);    
    	            row.createCell(2).setCellValue(lineAnswer); 
    	            
    	            t++;
                }
                readAnswer.close();
                readQuestion.close();
            }
	        		
	        FileOutputStream stream;
	       
	        stream = new FileOutputStream(ExcelPath);
			   
	        wb.write(stream);  
	        //关闭文件流   
	        stream.flush();
	        stream.close(); 
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}        
	}

	public static void main(String[] args) throws ClassNotFoundException, SQLException {
		
//		List<String> courseList=new ArrayList<String>();
//		courseList.add("chinese");
//		courseList.add("history");
//		courseList.add("english");		
//		courseList.add("physics");
//		courseList.add("chemistry");
//		courseList.add("biology");
//		courseList.add("politics");
//		courseList.add("geo");
//		
//		String course="";
//		for(int i=0;i<courseList.size();i++){
//			course=courseList.get(i);
//			String path="D:\\TestSet\\";
//			generate(course,path);
//			System.out.println(course+"生成问题完毕\n");
//		}
		
//		generateNoProperty("geo","D:\\TestSet\\");
		
//		String path="D:\\TestSet\\";
//		generateByTxt("math",path);
		
		generateByChoice("politics","D:\\TestSet\\politics\\政治100道选题9.xlsx");
		
	}
}
