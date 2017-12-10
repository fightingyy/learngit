package com.ld.IO.File;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：SaveFile   
* 类描述： 保存文件
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:38:49   
* @version        
*/
public class SaveFile {

	/**
	 * 保存未查询到模板的问题
	 * @param question
	 * @param path
	 */
	public static void SaveNoMatch(String question,String path) {

		String file=path+"/resources/NoPatternQuestion.txt";
		File write = new File(file);
		try {
	    	BufferedWriter out = new  BufferedWriter(new OutputStreamWriter(new FileOutputStream(write,true), "utf-8"));  
            System.out.println(question);
	    	
			out.write(question+"\n");
			
//			out.flush();
		    out.close();
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
	}
	
	/**
	 * 保存答错的题
	 * @param course
	 * @param path
	 * @param question
	 */
	public static void SaveWrongQuestion(String course,String path,String question) {

		String file=path+"/resources/TestResults/"+course+"/"+course+"WrongQustion.txt";
		File write = new File(file);
		try {
	    	BufferedWriter out = new  BufferedWriter(new OutputStreamWriter(new FileOutputStream(write,true ), "utf-8"));  
	    	
			out.write(question+"\n");
			
//			out.flush();
		    out.close();
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
	}
	
	/**
	 * 保存答错题的答案
	 * @param course
	 * @param path
	 * @param answer
	 */
	public static void SaveWrongAnswer(String course,String path,String answer) {

		String file=path+"/resources/TestResults/"+course+"/"+course+"WrongAnswer.txt";
		File write = new File(file);
		try {
	    	BufferedWriter out = new  BufferedWriter(new OutputStreamWriter(new FileOutputStream(write,true), "utf-8"));  
	    	
			out.write(answer+"\n");
			
//			out.flush();
		    out.close();
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		}
	}
	

}
