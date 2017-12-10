package com.ld.Util;

import java.io.*;
import java.net.*;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;

/** 
 * 项目名称:  KnowledgeQA  
 * 类名称:   HttpConnectUtil
 * 类描述:   工具类，发起https请求并获取结果
 * 创建人:    ludan
 * 创建时间:  2017年8月21日 下午3:20:18
 *      
 */  
public class HttpConnectUtil {
	
	/**
	 * get方式，发起https请求并获取结果
	 */
	public static String getHttp(String url) {
		String responseMsg = "";
	 	try {
	 		
            URL Url = new URL(url);    // 把字符串转换为URL请求地址
            HttpURLConnection connection = (HttpURLConnection) Url.openConnection();// 打开连接
            connection.setConnectTimeout(60000);

            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept-Charset", "utf-8");
//            connection.setRequestProperty("contentType", "utf-8");
            connection.setRequestMethod("GET");
            connection.connect();// 连接会话

            if (connection.getResponseCode() == connection.HTTP_OK) {
            	InputStream in=connection.getInputStream();

    			BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
    			String line;  
    			while ((line = br.readLine())!= null){  
    				responseMsg += line;  
    			}
                br.close();// 关闭流
                connection.disconnect();// 断开连接
//                System.out.println(responseMsg.toString());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("失败!");
        }
		return responseMsg;
	}

	/**
	 * post方式，发起https请求并获取结果
	 */
	public static String postHttp(String url) {
		String responseMsg = "";
		URL Url=null;
		try {
			Url = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) Url.openConnection(); 
			
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setUseCaches(false);
            connection.setInstanceFollowRedirects(true);
            connection.setRequestProperty("Content-Type", "application/json");
            
            connection.connect(); 

            InputStream in=connection.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
			String line;  
			while ((line = br.readLine())!= null){  
				responseMsg += line;  
			}  
		   br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  

		return responseMsg;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
