package com.ld.IO;

/** 
 * 项目名称:  KnowledgeQA  
 * 类名称:   Time
 * 类描述:   时间格式转换
 * 创建人:   ludan
 * 创建时间:  2017年8月30日 下午2:37:15
 *      
 */  
public class Time {

	public Time() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	* 时间格式转换
	* @param start
	* @param end
	* @return
	*/
	public static String computeTime(long start ,long end){
		String time="";
		long day=((end - start)/(24*60*60*1000));
        long hour=((end - start)/(60*60*1000)-day*24);
        long min=(((end - start)/(60*1000))-day*24*60-hour*60);
        long s=((end - start)/1000-day*24*60*60-hour*60*60-min*60);
        
        time=hour+"时"+min+"分"+s+"秒";
        
        return time;
	}

}
