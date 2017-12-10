package com.ld.IO.File;

import java.io.*;

import com.ld.Util.UUIDGeneratorUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：TextJson   
* 类描述： 生成json文件
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:34:59   
* @version        
*/
public class TextJson {

	public static void createJson(String course,String path,JSONArray cell){
//		String path="D:\\Documents\\Workspace\\KnowledgeQA\\";
		StringBuffer buffer = new StringBuffer(); 	
		buffer = new StringBuffer();
		try {
			Writer write = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path+"WebContent/js/TextJson/"+course+"Text.json"), "UTF-8"));
			JSONObject idObject=new JSONObject();
			JSONObject indexJsonObject=new JSONObject();
			for(int i=0;i<cell.size();i++){
				JSONObject json=cell.getJSONObject(i);
	        	String indexID = UUIDGeneratorUtil.getRandomString(12);
	        	idObject.put("_id", indexID);
	        	indexJsonObject.put("index", idObject);
				
	        	write.write(indexJsonObject.toString()+"\r\n");
	        	write.write(json.toString()+"\r\n");
			}	    	 	
			write.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
