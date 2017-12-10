package com.ld.Listener;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.ld.IO.Term.SelectTerms;
import com.ld.TimerTask.RunByTime;
/** 
 * 项目名称:  KnowledgeQA  
 * 类名称:   InitListener
 * 类描述:   监听器
 * 创建人:   ludan
 * 创建时间:  2017年8月23日 下午2:49:10
 *      
 */  
public class InitListener implements ServletContextListener { 
	
	public void contextDestroyed(ServletContextEvent sce) { 
		System.out.println("web exit ... ");
	} 
	
	/**
	 * web应用初始化
	 */
	public void contextInitialized(ServletContextEvent sce) { 
		
		System.out.println("web starting ... "); 
		ServletContext context = sce.getServletContext();
		String path=context.getRealPath("/");
		
		if(path.contains("\\.metadata"))
			path=path.split("\\.metadata")[0]+"KnowledgeQA";
		else if(path.contains("WEB-INF")){
			path=path.split("WEB-INF")[0];
		}
		System.out.println(path);
		String chinese=path+"/resources/Terms/chineseTerms.txt";
		
		Map<String,String> chineseTerms=SelectTerms.getMap(chinese);

		//加载语文学科实例表
		context.setAttribute("chineseTerms",chineseTerms);

		//定时任务
		RunByTime.RunTask(path);
		
		System.out.println("web start ... "); 
	}
}
