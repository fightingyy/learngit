package com.ld.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts2.ServletActionContext;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ruc.irm.similarity.sentence.morphology.SemanticSimilarity;

import com.ld.IO.TestCompare;
import com.ld.IO.Time;
import com.ld.IO.File.GetFiles;
import com.ld.IO.File.ReadFile;
import com.ld.IO.File.SaveFile;
import com.ld.QuestionGeneration.TestProperty;
import com.ld.Test.TestQuestion;
import com.ld.answer.AnswerQuestion;
import com.ld.model.AnswerObject;
import com.ld.model.Tamplate;
import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.ActionSupport;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：TestAction   
* 类描述： 测试
* 创建人：ludan   
* 创建时间：2017年6月2日 上午11:17:29   
* @version        
*/
public class TestAction extends ActionSupport{
	
	private String file;

    private String response;
    
    private File excel; //上传的文件
    private String excelFileName; //文件名称
    private String excelContentType; //文件类型
    private String realpath;
    private String filepath;

    HttpServletRequest request = ServletActionContext.getRequest();
    HttpServletResponse Response=ServletActionContext.getResponse();
    
    private static Logger logger = LoggerFactory.getLogger(TestAction.class);
    
	public String execute() throws Exception{
		
		JSONObject resultObject=new JSONObject();
		String path=request.getSession().getServletContext().getRealPath("");
		//path is F:\eclispseworkspace\KnowledgeQA\
		if(path.contains("\\.metadata"))
			path=path.split("\\.metadata")[0]+"KnowledgeQA\\";
		else if(path.contains("WEB-INF")){
			path=path.split("WEB-INF")[0];
			
		}
		else {
			path+="/";
		}
		
		
		long start = System.currentTimeMillis(); 		
		SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd HHmmss");
		
		String course="";
		
		//先将文件上传至服务器，再进行读取
		uploadFile();
		
		course=request.getParameter("course");
        System.out.println("course："+course);
        System.out.println("上传的测试文件全路径："+getFilepath());
        ReadFile.deleteNullFile(course, path);
        if(getFilepath().contains("exam")){
        	exam(course,path);
        	response=null;
        }
        else if(!getFilepath().contains("Generate")){
	        HttpSession session=request.getSession();
			String userCourse=(String)session.getAttribute("course");
			if(userCourse.equals("all")||course.equals(userCourse)){
				String path2=path+"resources/TestResults/"+course;
				
				String file=GetFiles.getLastHtml(path2);
				File input = new File(path2+"/"+file);
				Elements elements=TestCompare.parserHtml(input);
				
				StringBuilder sb = new StringBuilder();
				String htmlname=path+"resources/TestResults/"+course+"/"+df.format(start)+".html";
				String html="./resources/TestResults/"+course+"/"+df.format(start)+".html";
				PrintStream printStream = new PrintStream(new FileOutputStream(htmlname),true,"UTF-8");
				//the content in html file
				sb.append("<html>\r\n"); 
				sb.append("<head>\r\n");
				
				sb.append("<title>"+df.format(start)+course+" 测试结果</title>\r\n"); 
				sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" pageEncoding=\"UTF-8\" />\r\n"); 
				sb.append("<link rel='stylesheet' href='../result.css' />\r\n");
				sb.append("<script src='../jquery.js'></script>\r\n");
				sb.append("<script src='../SelectResult.js'></script></head>\r\n");
				sb.append("<body>\r\n");
				sb.append("<nav id='bg'>\r\n");
				sb.append("<div id='show'><b style='color: #FFFFFF;font-size: 24px;font-family: '微软雅黑';font-weight: 500;'>筛选测试结果：</b><button id='showRight'>回答正确</button><button id='showNoRight'>回答错误</button><button id='showNoMatch'>未匹配模板</button><button id='showThisRight'>此次回答正确</button><button id='showLastRight'>上次回答正确</button><button id='showProcess'>经后续处理回答正确</button><button id='All'>显示所有</button></div>");
				sb.append("<div id='box' style='font-family: '微软雅黑';'>\r\n");
		        
		        String question;
		        JSONArray result=new JSONArray();
		        List<Tamplate> tamplateList=new ArrayList<Tamplate>();
		        List answerList=new ArrayList();
		        String ExcelAnswer="";
		        int total=0;
		        int right=0;
		        int NoMatch=0;
		        int NoKnowledge=0;
		        int process=0;
		        
		//        SemanticSimilarity semanticSimilarity=SemanticSimilarity.getInstance();
				//operate excel file
				XSSFWorkbook xwb = new XSSFWorkbook(getFilepath());
		        //get sheet data
		        XSSFSheet sheet = xwb.getSheetAt(0);
		        
		        XSSFRow row;
		        SemanticSimilarity semanticSimilarity=SemanticSimilarity.getInstance();
		        for (int i = sheet.getFirstRowNum()+1; i<sheet.getPhysicalNumberOfRows(); i++) {
		//        	for (int i = sheet.getFirstRowNum()+1; i<100; i++) {
		            
		        	answerList=new ArrayList<String>();
		    	    row = sheet.getRow(i);
		    	    question = row.getCell(1).toString();
		    	    if(question==null||question.equals("")) continue;
		    	    	
		    	    if(row.getCell(2)!=null){
		    	    	ExcelAnswer=row.getCell(2).toString();
		    	    }
		    	    answerList.add(question);
//		    	    System.out.println(question);
		    	    AnswerQuestion answerQuestion=null;
	    	    	
		    	    answerQuestion=new AnswerQuestion(question,course);
		    	    
		    	    result=answerQuestion.getResultArray();  
		    	    
		    	    String uuid = UUID.randomUUID().toString();
		    		SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
		    		String time = df1.format(new Date());//得到当前时间
		    		JSONObject jObject = new JSONObject();
		    		boolean fs = true;
		    		if (result.size() >= 1) {
		    			jObject = result.getJSONObject(result.size()-1);
		    			if ((int)jObject.get("fs") == 0) {
		    				fs = false;
		    			}
		    		}
		    		if (fs && result.size() > 1) {
		    			for (int j = 0; j < result.size()-1; j++) {
		    				JSONObject jsonObject = result.getJSONObject(j);
		    				String subject = (String) jsonObject.get("subject");
		    				String predicate = (String) jsonObject.get("predicate");
		    				double score = (double) jsonObject.get("score");
		    				String value = (String) jsonObject.get("value");
		    				if (value.indexOf("\n") != -1) {
		    					value = value.replaceAll("\n", "");
		    				}
		    				String tamplate = (String) jsonObject.get("tamplateContent");	
		    				String fsanswer = (String) jObject.get("fsanswer");
		    					
		    				logger.debug("id:"+uuid+" time:"+time+" question:"+question+" value:"+value+" subject:"+subject+" predicate:"+predicate+" score:"+score+" template:"+tamplate+" fsanswer:"+fsanswer);
		    			}
		    			
		    		}
		    		else if (fs && result.size() == 1) {
		    			JSONObject jsonObject = result.getJSONObject(0);
		    			String subject = "";
		    			String predicate = "";
		    			double score = 0.0;
		    			String value = (String) jsonObject.get("value");
		    			if (value.indexOf("\n") != -1) {
		    				value = value.replaceAll("\n", "");
		    			}
		    			String tamplate = (String) jsonObject.get("tamplateContent");	
		    			String fsanswer = (String) jObject.get("fsanswer");
		    				
		    			logger.debug("id:"+uuid+" time:"+time+" question:"+question+" value:"+value+" subject:"+subject+" predicate:"+predicate+" score:"+score+" template:"+tamplate+" fsanswer:"+fsanswer);
		    		}
		    		else if (!fs) {
		    			for (int j = 0; j < result.size(); j++) {
		    				JSONObject jsonObject = result.getJSONObject(j);
		    				String subject = (String) jsonObject.get("subject");
		    				String predicate = (String) jsonObject.get("predicate");
		    				double score = (double) jsonObject.get("score");
		    				String value = (String) jsonObject.get("value");
		    				if (value.indexOf("\n") != -1) {
		    					value = value.replaceAll("\n", "");
		    				}
		    				String tamplate = (String) jsonObject.get("tamplateContent");
		    				String fsanswer = "";
		    					
		    				logger.debug("id:"+uuid+" time:"+time+" question:"+question+" value:"+value+" subject:"+subject+" predicate:"+predicate+" score:"+score+" template:"+tamplate+" fsanswer:"+fsanswer);
		    			}
		    			
		    		}
		    	    
		    	    question=question.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
		    	    String answer="";
		    	    boolean flag=false;
		    	    if(!result.isEmpty()){
		
		    	    	TestQuestion tQuestion=new TestQuestion();
		    	    	flag=tQuestion.isRight(course,result, answerQuestion, ExcelAnswer, semanticSimilarity);
		    	    	answer=tQuestion.getAnswer();
		    	    	if(!flag){
		    	    		SaveFile.SaveWrongQuestion(course, path, question);
		    	    		SaveFile.SaveWrongAnswer(course, path, answer);
		    	    	}
		    	    	answerList.add(answer);
		    	    }
		    	    tamplateList=answerQuestion.getTamplateList();
		    	    System.out.println("result:"+result.toString());		    	    
		    	    
		    	    if(!result.isEmpty()&&!ExcelAnswer.equals("")) {
		    	    	
		    	    	if(flag){
		    	    		right++;
		    	    		String className=TestCompare.compare(elements, i, true);
		    	    		if(className.equals("thisRight")){
		    	    			sb.append("<div id='item' class='thisRight'>\r\n");
			    	    		sb.append("<b>"+i+"、"+question+"</b><br>\r\n");
			    	    		sb.append("<div class='item'>"+answer+"</div>\r\n");
		    	    		}
		    	    		else{
			    	    		sb.append("<div id='item' class='right'>\r\n");
			    	    		sb.append("<b>"+i+"、"+question+"</b><br>\r\n");
			    	    		sb.append("<div class='item'>"+answer+"</div>\r\n");
		    	    		}
		    	    	}
		    	    	else {
		    	    		String className=TestCompare.compare(elements, i, false);
		    	    		
		    	    		if(className.equals("lastRight")){
		    	    			sb.append("<div id='item' class='lastRight'>\r\n");
				   	    		sb.append("<b>"+i+"、"+question+"</b><br>\r\n");
				   	    		sb.append("<div class='item'>"+answer+"</div>\r\n");
		    	    		}
		    	    		else{
			    	    		sb.append("<div id='item' class='noright'>\r\n");
				   	    		sb.append("<b>"+i+"、"+question+"</b><br>\r\n");
				   	    		sb.append("<div class='item'>"+answer+"</div>\r\n");
		    	    		}
		    	    	}
//		    	    	sb.append("<div id='check'><label><input name='check"+i+"' type='radio' value='right' />对 </label> <label><input name='check"+i+"' type='radio' value='wrong' />错 </label></div>\r\n");
		    	    	sb.append("</div>");
		    	    }
		    	    else if(result.isEmpty()&&!tamplateList.isEmpty()){
		    	    	sb.append("<div id='item' class='noright'>\r\n");
		   	    		sb.append("<b>"+i+"、"+question+"</b><br>\r\n");
		   	    		sb.append("<div class='item'>未搜索到答案<br></div>\r\n");
//		   	    		sb.append("<div id='check'><label><input name='check"+i+"' type='radio' value='right' />对 </label> <label><input name='check"+i+"' type='radio' value='wrong' />错 </label></div>\r\n");
		   	    		sb.append("</div>");
		    	    }
		    	    else if(tamplateList.isEmpty()) {
		    	    	
		    	    	NoMatch++;
		    	    	SaveFile.SaveNoMatch(question,path);
		    	    	sb.append("<div id='item' class='nomatch'>\r\n");
			    		sb.append("<b>"+i+"、"+question+"</b><br>\r\n");
			    		sb.append("<div class='item'>未匹配到模板</div>\r\n");
//			    		sb.append("<div id='check'><label><input name='check"+i+"' type='radio' value='right' />对 </label> <label><input name='check"+i+"' type='radio' value='wrong' />错 </label></div>\r\n");
			    		sb.append("</div>");
		    	    }
		    	    total=i;
		    	    resultObject.put(i, answerList);
		    }
		        long end = System.currentTimeMillis();
		        sb.append("<div id='Total'>");
		        sb.append("<br><p>总数："+total+"</p>\r\n");	
		        sb.append("<p>正确数："+right+"</p>\r\n");	
		        sb.append("<p>正确率："+((float)right)/total+"</p>\r\n");	
		        sb.append("<p>经后续处理正确数："+process+"</p>\r\n");
		        sb.append("<p>匹配到模板数："+(total-NoMatch)+"</p>\r\n");	
		        sb.append("</div>");
		        
		        sb.append("</div></body>\r\n");
		        sb.append("</html>\r\n");
		        printStream.println(sb.toString()); 
		        
		        right=right-NoKnowledge;
		        System.out.println("total:"+total);
		        System.out.println("right:"+right);
		        System.out.println("NoMatch:"+NoMatch);
		        System.out.println();
		        
		        resultObject.put("total", total);
		        resultObject.put("right",right);
		        resultObject.put("NoMatch",NoMatch);
		        resultObject.put("process",process);
		        resultObject.put("file", html);
		        resultObject.put("time", Time.computeTime(start, end));
		        
//		        System.out.println(resultObject.toString());
		        System.out.println("所用时间为："+Time.computeTime(start, end));
		        response=resultObject.toString();
		        
		        xwb.close();
		        deleteFile(getFilepath());
		        printStream.close();
			}
			
			else {
				request.setAttribute("message", "您没有测试该学科的权限");
			}
        }
        else{ 
//        	TestProperty.Test(filepath, course);
        	List<String> courseList=new ArrayList<String>();
    		courseList.add("chinese");
    		courseList.add("history");
    		courseList.add("english");		
    		courseList.add("physics");
    		courseList.add("chemistry");
    		courseList.add("biology");
    		courseList.add("politics");
    		for(int i=0;i<courseList.size();i++){
    			course=courseList.get(i);
    			filepath="D:\\TestSet\\"+course+"\\"+course+"Generate.xlsx";
    			TestProperty.Test(filepath, course);
    		}
        	response=null;
        }
		
        return SUCCESS;
	}
	
	public void exam(String course,String path){
		
		long start = System.currentTimeMillis(); 		
		SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd HHmmss");
		
		HttpSession session=request.getSession();
		String userCourse=(String)session.getAttribute("course");
			String path2=path+"/resources/TestResults/"+course;
			
			String file=GetFiles.getLastHtml(path2);
			File input = new File(path2+"/"+file);
			
			StringBuilder sb = new StringBuilder();
			String htmlname=path+"/resources/TestResults/"+course+"/"+df.format(start)+".html";
			String html="./resources/TestResults/"+course+"/"+df.format(start)+".html";
			PrintStream printStream;
			try {
				printStream = new PrintStream(new FileOutputStream(htmlname),true,"UTF-8");
				
				sb.append("<html>\r\n"); 
				sb.append("<head>\r\n");
				
				sb.append("<title>"+df.format(start)+course+" 测试结果</title>\r\n"); 
				sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" pageEncoding=\"UTF-8\" />\r\n"); 
				sb.append("<link rel='stylesheet' href='../result.css' />\r\n");
				sb.append("<script src='../jquery.js'></script>\r\n");
				sb.append("<script src='../SelectResult.js'></script></head>\r\n");
				sb.append("<body>\r\n");
				sb.append("<nav id='bg'>\r\n");
				sb.append("<div id='show'><b style='color: #FFFFFF;font-size: 24px;font-family: '微软雅黑';font-weight: 500;'>筛选测试结果：</b><button id='showRight'>回答正确</button><button id='showNoRight'>回答错误</button><button id='showNoMatch'>未匹配模板</button><button id='showThisRight'>此次回答正确</button><button id='showLastRight'>上次回答正确</button><button id='showProcess'>经后续处理回答正确</button><button id='All'>显示所有</button></div>\r\n");
				sb.append("<div id='box' style='font-family: '微软雅黑';'>\r\n");
		        
		        String question;
		        JSONArray result=new JSONArray();
		        
				XSSFWorkbook xwb = new XSSFWorkbook(getFilepath());
		        
		        XSSFSheet sheet = xwb.getSheetAt(0);
		        
		        XSSFRow row;
		        for (int i = sheet.getFirstRowNum()+1; i<sheet.getPhysicalNumberOfRows(); i++) {
		            
		    	    row = sheet.getRow(i);
		    	    question = row.getCell(1).toString();
		    	    
		    	    System.out.println(question);
		    	    AnswerQuestion answerQuestion=null;
	    	    	
		    	    answerQuestion=new AnswerQuestion(question,course);
		    	    
		    	    result=answerQuestion.getResultArray();    
		    	    
		    	    String answer="";
		    	    sb.append("<div id='item' \r\n");
		    	    sb.append("<b>"+i+"、"+question+"</b><br>\r\n");
		    	    sb.append("<div class='item'>");
		    	    if(!result.isEmpty()){
		    	    	
		    	    	for(int j=0;j<result.size();j++) {
		    	        	
		    	        	AnswerObject ansObj =(AnswerObject) result.get(j);      	
		    	        	answer=ansObj.getValue();
		    	        	if(answer.contains(":http:")&&(answer.contains("getjpg")||answer.contains("getpng"))){
		    	        		String[] array=answer.split(":http:");
		    	        		sb.append(array[0]+":<image src='http:"+array[1]+"'/><br>\r\n");
		    	        	}
		    	        	else sb.append(answer);
		    	        	
		    	        }
		    	    }    	    
		    	    if(result.isEmpty()) sb.append("未搜索到答案");
		    		sb.append("</div>\r\n");
//		    		sb.append("<div id='check'><label><input name='check"+i+"' type='radio' value='right' />对 </label> <label><input name='check"+i+"' type='radio' value='wrong' />错 </label></div>\r\n");
		    		sb.append("</div>\r\n");
		    		System.out.println("result:"+result.toString());
		        }
		        sb.append("<button id='count'>统计测试结果</button>\r\n");
		        sb.append("<div id='Total'></div>");
		        sb.append("</div></body>\r\n");
		        sb.append("</html>\r\n");
		        printStream.println(sb.toString()); 

		        xwb.close();
		        printStream.close();
		        deleteFile(getFilepath());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	/**
	 * 将用户输入的测试文件上传至服务器
	 * @return
	 * @throws IOException
	 */
	public String uploadFile() throws IOException{
		
		realpath = ServletActionContext.getServletContext().getRealPath("/upload");
        
        if (getExcel() != null) { //excel is not null
            File savefile = new File(new File(realpath), getExcelFileName());
            if (!savefile.getParentFile().exists())
                savefile.getParentFile().mkdirs();
            FileUtils.copyFile(getExcel(), savefile);
            ActionContext.getContext().put("message", "文件上传成功");
        }
        
        JSONObject resultObject=new JSONObject();
        resultObject.put(0, realpath+"/"+getExcelFileName());
        response=realpath+"/"+getExcelFileName();
        return SUCCESS;
		
	}
	
	/**
	 * 删除
	 * @param fileName
	 * @return
	 */
	public static boolean deleteFile(String fileName){     
        File file = new File(fileName);     
        if(file.isFile() && file.exists()){     
            file.delete();     
            System.out.println("删除上传的测试文件"+fileName+"成功！");     
            return true;     
        }else{     
            System.out.println("删除上传的测试文件"+fileName+"失败！");     
            return false;     
        }     
    }  

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public File getExcel() {
		return excel;
	}

	public void setExcel(File excel) {
		this.excel = excel;
	}

	public String getExcelContentType() {
		return excelContentType;
	}

	public void setExcelContentType(String excelContentType) {
		this.excelContentType = excelContentType;
	}

	public String getExcelFileName() {
		return excelFileName;
	}

	public void setExcelFileName(String excelFileName) {
		this.excelFileName = excelFileName;
	}

	public String getRealpath() {
		return realpath;
	}

	public void setRealpath(String realpath) {
		this.realpath = realpath;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

}
