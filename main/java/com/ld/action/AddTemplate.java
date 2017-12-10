package com.ld.action;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.json.JSONObject;

import org.apache.struts2.ServletActionContext;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.corpus.tag.Nature;
import com.hankcs.hanlp.seg.common.Term;
import com.ld.Parser.WordParser;
import com.opensymphony.xwork2.ActionSupport;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：AddTemplate   
* 类描述： 添加模板
* 创建人：ludan   
* 创建时间：2017年6月2日 上午11:07:33   
* @version        
*/
public class AddTemplate extends ActionSupport {
	
	private String template;
	private String course;
	private String inputSubject;
	private String type;
	private String response;
	JSONObject result=new JSONObject();
	
	HttpServletRequest request = ServletActionContext.getRequest();
	
	/**
	 * 添加正则模板
	 * @return
	 * @throws Exception
	 */
	public String AddRegexTemplate() throws Exception{
		
		String template=getTemplate();
		String course=getCourse();
		String type=getType();
		
		HttpSession session=request.getSession();
		String userCourse=(String)session.getAttribute("course");
		if(userCourse.equals("all")||course.equals(userCourse)){
		
			boolean flag=insert(course, template, type);
			if(flag)
				result.put("imformation","模板添加成功！");
			 response=result.toString();
		}
		else {
				request.setAttribute("message", "您没有修改该学科模板的权限");
			}
		return SUCCESS;
	}
	
	/**
	 * 添加问题模板
	 * @return
	 * @throws Exception
	 */
	public String AddQuestionTemplate() throws Exception{
		
		String template=getTemplate();
		System.out.println(template);
		String course=getCourse();
		String type=getType();
		
		HttpSession session=request.getSession();
		String userCourse=(String)session.getAttribute("course");
		if(userCourse.equals("all")||course.equals(userCourse)){
			String inputSubject=getInputSubject();
			
			template=Extract(template, inputSubject);
			boolean flag=insert(course, template, type);
			if(flag)
				result.put("imformation","模板添加成功！");
			 response=result.toString();
		}
		else {
				request.setAttribute("message", "您没有修改该学科模板的权限");
			}
		return SUCCESS;
	}

	/**
	 * 将问题转换成正则模板
	 * @param template
	 * @param subject
	 * @return
	 */
	public static String Extract(String template,String subject){
		
		if(subject!=null||!subject.equals("")){
			template=template.replace(subject, "");
			
			String[] splitArray=template.split("，|。|？|,|\\?");
			
			for(String item:splitArray){
				List<Term> cutWord=HanLP.segment(item);
				Map<String, Nature> wordMap = WordParser.splitWordandNature(cutWord);
				if(!wordMap.containsValue(Nature.ry)&&!wordMap.containsValue(Nature.rys)&&!wordMap.containsValue(Nature.ryt)&&!wordMap.containsValue(Nature.ryv)){
					template=template.replaceAll(item, "");
				}
				else{
					//保留下问句中的名词、动词及疑问词
					for(Term wordTerm:cutWord){
						if(template.startsWith(wordTerm.word)||wordTerm.word.equals("什么")||(!wordTerm.nature.equals(Nature.n)&&!wordTerm.nature.equals(Nature.v)&&!wordTerm.nature.equals(Nature.vn)&&!wordTerm.nature.equals(Nature.ry))){
							template=template.replaceAll(wordTerm.word, "@");
						}
					}
				}
			}
			
			template=template.replaceAll("@+", "@");
			if(template.length()>2){
				
				template=template.replace(",", "").replace("，", "").replace("。", "").replace("？", "").replace("？", "");
				
				template=template.replace("谁", "(谁|哪(一)?个|哪(一)?位)");
				template=template.replace("怎样", "(如何|怎(么)?样)");
				template=template.replace("时间", "(何时|什么时候|哪(一)?年|几年|哪(一)?天|哪(个|一)?朝(代)?|什么时期|哪个时期)");
				
				if(!template.endsWith("@"))
					template=template+"(.*)?";
				if(template.startsWith("@"))
					template="(?<title>(.*)?)"+template.substring(1,template.length());
				else template="(?<title>(.*)?)"+template;
				template=template.replaceAll("@", "(.*)?");
			}
		}
		return template;
	}

	
	/**在模板表中插入新的模板
	 * @param course
	 * @param content
	 * @param type
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public static boolean insert(String course,String content,String type) throws ClassNotFoundException, SQLException{
		
		Connection cn;
		
		Class.forName("com.mysql.jdbc.Driver");		
		cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
		Statement stmt=cn.createStatement();
		
		String sql="insert ignore into "+course+"_template (`content`, `type`) values ('"+content+"','"+type+"');";
  		boolean result=stmt.execute(sql);
  		cn.close();
  		
  		return result;
	}
	
	public static void main(String[] args){
		
		Extract("高级神经活动的基本方式是什么", "高级神经活动");
	}
	
	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getCourse() {
		return course;
	}

	public void setCourse(String course) {
		this.course = course;
	}

	public String getInputSubject() {
		return inputSubject;
	}

	public void setInputSubject(String inputSubject) {
		this.inputSubject = inputSubject;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

}
