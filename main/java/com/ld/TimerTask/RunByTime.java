package com.ld.TimerTask;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.*;

import com.ld.FullSearch.ElasticSearch;
import com.ld.IO.Property.PropertyJson;
import com.ld.IO.Term.AddTerm;
import com.ld.pattern.AddPattern;
import com.ld.search.VirtuosoSearch;

public class RunByTime {

	public static void RunTask(final String path) {
		TimerTask task = new TimerTask() {		
			@Override
			public void run() {
				List<String> courseList=new ArrayList<String>();
				
				ElasticSearch elasticSearch=new ElasticSearch();
				
				courseList.add("chinese");
				courseList.add("history");
				courseList.add("english");
				courseList.add("geo");
				courseList.add("chemitry");
				courseList.add("biology");
				courseList.add("politics");
				courseList.add("physics");
				
				String course="";
				for(int i=0;i<courseList.size();i++){
					
					course=courseList.get(i);
					elasticSearch.updateIndex(course);
					updateTable(course, path);
				}
				AddTerm.addCourseAlt();
			}
		};
				
		Calendar calendar = Calendar.getInstance();
		long PERIOD_DAY = 24 * 60 * 60 * 1000; 
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);//每天
        //定制每天的00:00:00执行，
        calendar.set(year, month, day, 02, 00, 00);
        Date date = calendar.getTime();
        if (date.before(new Date())) {  
            date = addDay(date, 1);  
        }  
        Timer timer = new Timer();
        System.out.println(date);

        //每天的date时刻执行task, 仅执行一次
        timer.schedule(task, date,PERIOD_DAY);
	}
	
	public static void updateTable(String course, String path){
		
		Connection cn;
		try {
			if(course.equals("chinese")){
				AddTerm.WriteTermtoTxt(path,course);
				AddTerm.AddPoetryName();
			}
			else{
				//先删除已有的术语表
				Class.forName("com.mysql.jdbc.Driver");
				cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
				Statement stmt=cn.createStatement();
				String sql="truncate table "+course+"_terms;";
				stmt.execute(sql);
				
				//插入新的术语表
				AddTerm.searchInstance(course);
				VirtuosoSearch vSearch=new VirtuosoSearch(course);
				//添加实例的别称
				Map<String, String> map=vSearch.selectOtherName(course);
				AddTerm.addAltLabel(course, map);
				cn.close();
			}
			
			AddPattern.insertTemplate(course,path);
			//生成新的属性json文件，用于前端展示
			PropertyJson.createJsonFile(course, path);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	 public static Date addDay(Date date, int num) {  
	        Calendar startDT = Calendar.getInstance();  
	        startDT.setTime(date);  
	        startDT.add(Calendar.DAY_OF_MONTH, num);  
	        return startDT.getTime();  
    }  
	
	public static void main(String[] args) {
		
//		RunTask(path);
    }

}
