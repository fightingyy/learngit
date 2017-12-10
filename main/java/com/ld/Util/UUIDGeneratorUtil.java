package com.ld.Util;

import java.util.*;

/** 
 * 项目名称:  KnowledgeQA  
 * 类名称:   UUIDGeneratorUtil
 * 类描述:   获取指定位数的UUID
 * 创建人:   ludan
 * 创建时间:  2017年8月21日 下午3:23:40
 *      
 */  
public class UUIDGeneratorUtil {
	
	public static String getUUID(int number) {
		UUID uuid = UUID.randomUUID();
		String str = uuid.toString();
		// 去掉"-"符号
		String temp = str.substring(0, 8) + str.substring(9, 13)
				+ str.substring(14, 18) + str.substring(19, 23)
				+ str.substring(24);
		temp = temp.substring(0, number).toUpperCase();
		return temp;
	}

	public static String getRandomString(int length) { // length表示生成字符串的长度
		//String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		String base = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	public static void main(String[] args) {
		System.out.println(getUUID(24));
		System.out.println(getRandomString(6));
	}
}