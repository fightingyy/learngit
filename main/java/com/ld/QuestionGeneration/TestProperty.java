package com.ld.QuestionGeneration;
import java.io.File;
import java.io.FileOutputStream;
import java.util.*;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.*;

import com.ld.IO.*;
import com.ld.answer.AnswerQuestion;
import com.ld.model.AnswerObject;
import com.ld.model.Tamplate;

public class TestProperty{
    
	public static void Test(String path,String course) throws Exception{
		
		JSONObject resultObject=new JSONObject();
		
		long start = System.currentTimeMillis(); 		
		
        String question;
        JSONArray result=new JSONArray();
        List<Tamplate> tamplateList=new ArrayList<Tamplate>();
        List<String> answerList=new ArrayList<String>();
        int total=0;
        int right=0;
        int NoMatch=0;
        int NoKnowledge=0;
        int process=0;
        
      //写入excel
        String ExcelPath="D:\\TestSet\\"+course+"\\"+course+"GenerateResult.xlsx";
		File file=new File(ExcelPath);

		XSSFWorkbook wb = new XSSFWorkbook();
           
        Sheet sheet0 = (Sheet) wb.createSheet("sheet1"); 
        XSSFCellStyle my_style = wb.createCellStyle();
        my_style.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND); // 填充单元格
        my_style.setFillForegroundColor(HSSFColor.RED.index);
        
        if (sheet0==null) {
            sheet0 = (Sheet) wb.createSheet("sheet1");  
            
        }
        sheet0.setColumnWidth(1, 10000); 
        //添加表头  
        Row row0 = sheet0.createRow(0);
        row0.createCell(0).setCellValue("编号");
        row0.createCell(1).setCellValue("问题");
        row0.createCell(2).setCellValue("答案");
        
        //读取excel
		XSSFWorkbook xwb = new XSSFWorkbook(path);
        
        XSSFSheet sheet = xwb.getSheetAt(0);
        
        XSSFRow row;
        
        for (int i = sheet.getFirstRowNum()+1; i<sheet.getPhysicalNumberOfRows(); i++) {
//        	for (int i = sheet.getFirstRowNum()+1; i<100; i++) {
            
        	answerList=new ArrayList<String>();
    	    row = sheet.getRow(i);
    	    question = row.getCell(1).toString();
    	    answerList.add(question);
    	    System.out.println(question);
    	    //精确查询
    	    AnswerQuestion answerQuestion=new AnswerQuestion();
    	    result=answerQuestion.preciseAnswer(course,question);
    	    
    	    row0 = (Row) sheet0.createRow(i);  
            row0.createCell(0).setCellValue(i);
            row0.createCell(1).setCellValue(question);
    	    
    	    String answer="";
    	    boolean flag=false;
    	    if(!result.isEmpty()){
    	    	
    	    	String items="";
    	    	for(int j=0;j<result.size();j++){
    	    		AnswerObject ansObj=(AnswerObject) result.get(j);
    	    		items=ansObj.getValue();
	            	answer+=items.replace("（非直接查询出的结果，经后续处理得到的答案）", "").replaceAll("&nbsp;", "").replaceAll("<br>", "\n").replace("\t", "").replaceAll(" ", "");
	            	if(!items.equals("")&&items!=null) flag=true;
    	    	}
    	    }
    	    tamplateList=answerQuestion.getTamplateList();
    	    System.out.println("result:"+result.toString());
    	    
    	    if(!result.isEmpty()) {

    	    	if(flag){    
    	    		
    	    		row0.createCell(2).setCellValue(answer); 
    	    	}
    	    	
    	    }
    	    else{
    	    	//未查询到答案时，将此问题所在行颜色设为红色
	    		row0.setRowStyle(my_style); 
	            row0.getCell(0).setCellStyle(my_style);
	            row0.getCell(1).setCellStyle(my_style);
    	    }
    	    if(result.isEmpty()&&!tamplateList.isEmpty()){
    	    	
    	    }
    	    else if(tamplateList.isEmpty()) {
    	    	
    	    	NoMatch++;
//    	    	SaveNoMatching save=new SaveNoMatching(question,path);
    	    	
    	    }
    	    total=i;
    	    resultObject.put(i, answerList);
            
    }
        
        FileOutputStream stream;
        stream = new FileOutputStream(ExcelPath);
		   
        wb.write(stream);  
        //关闭文件流   
        stream.flush();
        stream.close(); 
        
        right=right-NoKnowledge;
        System.out.println("total:"+total);
        System.out.println("right:"+right);
        System.out.println("NoMatch:"+NoMatch);
        System.out.println();
        
        resultObject.put("total", total);
        resultObject.put("right",right);
        resultObject.put("NoMatch",NoMatch);
        resultObject.put("process",process);
        
        long end = System.currentTimeMillis();
        long day=((end - start)/(24*60*60*1000));
        long hour=((end - start)/(60*60*1000)-day*24);
        long min=(((end - start)/(60*1000))-day*24*60-hour*60);
        long s=((end - start)/1000-day*24*60*60-hour*60*60-min*60);
        System.out.println(resultObject.toString());
        System.out.println("所用时间为："+min+"分"+s+"秒");

	}
	
	public static void main(String[] args) {

		String course="chinese";
		String path="D:\\TestSet\\";
		
		try {
			Test(path,course);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}