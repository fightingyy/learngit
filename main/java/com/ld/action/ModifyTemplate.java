package com.ld.action;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.opensymphony.xwork2.ActionSupport;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：ModifyTemplate   
* 类描述： 编辑模板
* 创建人：ludan   
* 创建时间：2017年6月2日 上午11:15:43   
* @version        
*/
public class ModifyTemplate extends ActionSupport {

	private String course;
	private String property;
	private String jsonObj;
	private String data;
	private String id;
	
	Connection cn;
	
	public String execute() throws Exception{
		
		JSONObject jsonObject = JSONObject.fromObject(jsonObj);
		
		Class.forName("com.mysql.jdbc.Driver");		
		cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
		
		Statement stmt=cn.createStatement();
		
		Iterator it = jsonObject.keys();  
        while (it.hasNext()) {  
            String key = (String) it.next();  
            HttpServletRequest request = ServletActionContext.getRequest();
            JSONObject pattern = JSONObject.fromObject(jsonObject.get(key));

            int id=pattern.getInt("id");
            String content=pattern.getString("content");
            boolean subject=pattern.getBoolean("subject");
            boolean value=pattern.getBoolean("value");
            String myclass=pattern.getString("myclass");
            String type=pattern.getString("type");
            String usage=pattern.getString("usage");
            int priority=pattern.getInt("priority");
            String course=pattern.getString("course");
            
            HttpSession session=request.getSession();
    		String userCourse=(String)session.getAttribute("course");
    		if(userCourse.equals("all")||course.equals(userCourse)){
    			if(id!=-1){
		            String sql="UPDATE "+course+"_template SET `content`='"+content+"',`subject`="+booleanToInt(subject)+",`value`="+booleanToInt(value)+", `type`='"+type+"', `class`='"+myclass+"', `usage`='"+usage+"',`priority`="+priority+" WHERE `pattern_id`="+id+";";
		            
		            stmt.execute(sql);
    			}
    			else {
    				String sql="insert ignore into "+course+"_template SET `content`='"+content+"',`subject`="+booleanToInt(subject)+",`value`="+booleanToInt(value)+", `type`='"+type+"', `class`='"+myclass+"', `usage`='"+usage+"',`priority`="+priority+";";
		            
		            stmt.execute(sql);
    			}
    		}
		
			else {
				request.setAttribute("message", "您没有修改该学科模板的权限");
				break;
			}
        }
        
        cn.close();
		return SUCCESS;
	}
	
	/**
	 * 删除模板
	 * @return
	 * @throws Exception
	 */
	public String deleteTemplate() throws Exception{
		
		HttpServletRequest request = ServletActionContext.getRequest();
		HttpSession session=request.getSession();
 		String userCourse=(String)session.getAttribute("course");
           
		if(userCourse.equals("all")||course.equals(userCourse)){
			int id=0;
			id=Integer.parseInt(getId().replace("X", ""));
			
			Class.forName("com.mysql.jdbc.Driver");		
			cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			Statement stmt=cn.createStatement();
			
			if(id>0){
	            String sql="delete from "+course+"_template where pattern_id="+id+";";
		            
		            stmt.execute(sql);
    			}
 
    		}
		
			else {
				request.setAttribute("message", "您没有修改该学科模板的权限");
		}

        cn.close();
		return SUCCESS;
	}
	
	/**
	 * 将模板在前端页面中显示出来
	 * @return
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public String Import() throws ClassNotFoundException, SQLException{
		
		String course=getCourse();
		
		JSONObject resultObject=new JSONObject();
		
		Class.forName("com.mysql.jdbc.Driver");		
		cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
		
		Statement stmt=cn.createStatement();
		String sql="select * from "+course+"_template order by pattern_id;";
		
		ResultSet results=stmt.executeQuery(sql);
		boolean subject = true;
		boolean value=false;
		int i=0;
		while(results.next()){ 
			JSONObject cellObject=new JSONObject();
			
			int id = results.getInt("pattern_id"); 
			String content=results.getString("content");
			int subjectInt=results.getInt("subject");
			if(subjectInt==0)subject=false;
			else subject=true;
			int valueInt=results.getInt("value");
			if(valueInt==0)value=false;
			else value=true;
			String type=results.getString("type");
			String myclass=results.getString("class");
			String usage=results.getString("usage");
			int priority=results.getInt("priority");
			
			cellObject.put("id",id);
			cellObject.put("content",content);
			cellObject.put("subject",subject);
			cellObject.put("value",value);
			cellObject.put("type",type);
			cellObject.put("myclass",myclass);
			cellObject.put("usage",usage);
			cellObject.put("priority",priority);
			
			resultObject.put(i, cellObject);
			i++;
		}
		
		data=resultObject.toString(); 
		results.close();
		cn.close();
		return SUCCESS;
	}
	
	public int booleanToInt(boolean value){
		int result=0;
		if(value)
			result=1;
		else result=0;
		
		return result;
	}
	public String getCourse() {
		return course;
	}
	public void setCourse(String course) {
		this.course = course;
	}
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public String getJsonObj() {
		return jsonObj;
	}
	public void setJsonObj(String jsonObj) {
		this.jsonObj = jsonObj;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
