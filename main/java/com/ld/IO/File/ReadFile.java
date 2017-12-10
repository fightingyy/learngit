package com.ld.IO.File;

import java.io.*;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：ReadFile   
* 类描述： 操作文件
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:38:27   
* @version        
*/
public class ReadFile {

	public ReadFile() {
		
	}
	
	/**
	 * 计算文件的行数
	 * @param file
	 * @return
	 */
	public static int readLineCount(String file){
		
		int count=0;
		
        try {
        	InputStream is = new BufferedInputStream(new FileInputStream(file));
            byte[] c = new byte[1024];
            int readChars = 0;
			while ((readChars = is.read(c)) != -1) {
			    for (int i = 0; i < readChars; ++i) {
			        if (c[i] == '\n')
			            ++count;
			    }
			}
			is.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return count;
	}
	
	/**
	 * 删除文件夹下的空文件
	 * @param course
	 * @param path
	 */
	public static void deleteNullFile(String course,String path){

		String filePath=path+"/resources/TestResults/"+course;
		File file = new File(filePath);
		
		if (file.isDirectory()) {
			String[] tempList = file.list();
	        File temp = null;
	        if(tempList.length>1){
			    for (int i = 0; i < tempList.length; i++) {
			    	if (path.endsWith(File.separator)) {
			    		temp = new File(filePath +"/"+ tempList[i]);
			    	} 
			    	else {
			    		temp = new File(filePath + File.separator + tempList[i]);
			    	}
			    	if (temp.isFile()) {
			    		long fileLength=temp.length();
			    		if(fileLength==0){
			    			temp.delete();
			    			System.out.println(temp.getName()+"删除成功");
			    		}
			    	}
			    }
	        }
	    }
	}
	
	public static void main(String[] args){
		deleteNullFile("biology", "D:\\Documents\\Workspace\\KnowledgeQA");
	}

}
