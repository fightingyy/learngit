package com.ld.IO;

import java.io.File;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：TestCompare   
* 类描述： 此次测试结果与上次测试结果的对比
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:34:26   
* @version        
*/
public class TestCompare {
	

	/**
	* 比较两次测试结果，确定div的class的值
	* @param items
	* @param i
	* @param Right
	* @return
	* @throws IOException
	*/
	public static String compare(Elements items,int i,boolean Right) throws IOException{
		
		String Class="";
		
		if(i<=items.size()){
			Element node=items.get(i-1);
			String className=node.className();
			
			if(Right&&(className.equals("noright")||className.equals("nomatch")||className.equals("lastRight")))
				Class="thisRight";
			if(!Right&&(className.equals("right")||className.equals("thisRight")))
				Class="lastRight";
		}
		return Class;
		
	}
	
	/**
	* 读取解析HTML文件
	* @param input
	* @return
	* @throws IOException
	*/
	public static Elements parserHtml(File input) throws IOException{
		
		Document doc = Jsoup.parse(input, "UTF-8", "http://www.dangdang.com");
		Elements items = doc.select("div#item");
		
		return items;
		
	}
}
