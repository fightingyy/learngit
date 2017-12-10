package com.ld.IO.File;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.ServletActionContext;

public class GetFiles {

	
	public static void main(String[] args) {

	}
	
	/**
	 * 找出文件夹下时间最近的html文件
	 * @param path
	 * @return
	 */
	public static String getLastHtml(String path){
		
		String fileName="";
		String name="";
		String temp="";
		
		File file = new File(path);
		File files[] = file.listFiles();
		
	    if(!file.exists()){
	    	
	    	System.out.println(path + " not exists");
	    }			
			
		     for (int i = 0; i < files.length; i++) {
		    	 File fs = files[i];
		         if (!fs.isDirectory()&&fs.getName().endsWith(".html")) {		        	 
		        	 name=fs.getName();

		        	 if(name.compareTo(temp)>0)
		        		 fileName=name;
		        	 temp=name;
		         } 
		     }

		return fileName;
	}
	
	/**
	 * 找出文件夹下的所有文件的名字
	 * @param path
	 * @param groupList
	 * @return
	 */
	public static List<String> getFileList(String path){	
		
		List<String> fileList=new ArrayList<String>();		
		
		File file = new File(path);
		
		File files[] = file.listFiles();
		
		if(!file.exists()){
	    	
	    	System.out.println(path + " not exists");
	    }			
			
		else{
			for (int i = 0; i < files.length; i++) {
				File fs = files[i];
				if (!fs.isDirectory()&&fs.getName().endsWith(".html")) {
					fileList.add(fs.getName());
				}
			}
		}
		
		return fileList;
	}
}
