package com.ld.IO;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：CourseChange   
* 类描述： 学科名称的中英文转换
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:30:36   
* @version        
*/
public class CourseChange {

	public CourseChange() {
		
	}
	
	public static String change(String orign,boolean isEnglish){
		String course="";
		
		if(isEnglish){
			switch (orign) {
			case "chinese":course="语文";break;
			case "english":course="英语";break;
			case "geo":course="地理";break;
			case "history":course="历史";break;
			case "chemistry":course="化学";break;
			case "biology":course="生物";break;
			case "physics":course="物理";break;
			case "politics":course="政治";break;
			case "math":course="属性";break;

			}
		}
		return course;
	}

}
