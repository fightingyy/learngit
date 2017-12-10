package com.ld.action;

import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.struts2.ServletActionContext;

import com.ld.IO.remove.RemoveDuplicate;
import com.ld.answer.PreciseAnswer;
import com.ld.search.VirtuosoSearch;
import com.opensymphony.xwork2.ActionSupport;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：AddAction   
* 类描述： 查询子图
* 创建人：ludan   
* 创建时间：2017年6月2日 上午10:59:18   
* @version        
*/
public class RelatedAction extends ActionSupport{

	private static final long serialVersionUID = -2392305257708569857L;
	private String subjectName;
	private String course;
	private String result;

	/* (non-Javadoc)
	 * @see com.opensymphony.xwork2.ActionSupport#execute()
	 */
	public String execute() throws Exception{
		
		
		HttpServletRequest request = ServletActionContext.getRequest();
        request.setCharacterEncoding("UTF-8");//服务器上编码;
		
//		String subjectName=new String(getSubjectName().getBytes("ISO8859-1"),"UTF-8");
		String subjectName=getSubjectName();

		
		String course=getCourse();

		JSONArray resultArray=new JSONArray();
		
	    VirtuosoSearch s=new VirtuosoSearch(course);
	   		
	    resultArray=s.searchGraph(subjectName,null,false,course);
	    resultArray=PreciseAnswer.removeObject(resultArray,true);
	    
	    request.setAttribute("result", resultArray);
		request.setAttribute("subject", subjectName);
//	    System.out.println(result);
	    
		return SUCCESS;
	}
	

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}


	public String getResult() {
		return result;
	}


	public void setResult(String result) {
		this.result = result;
	}


	public String getCourse() {
		return course;
	}


	public void setCourse(String course) {
		this.course = course;
	}

}
