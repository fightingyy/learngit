package com.ld.action;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;

/**   
*    
* 椤圭洰鍚嶇О锛欿nowledgeQA   
* 绫诲悕绉帮細loginAction   
* 绫绘弿杩帮細 鐧诲綍
* 鍒涘缓浜猴細ludan   
* 鍒涘缓鏃堕棿锛�2017骞�6鏈�2鏃� 涓婂崍11:12:28   
* @version        
*/
public class LoginAction extends ActionSupport{

	private String userName;
	private String password;
	Connection cn;
	HttpServletRequest request = ServletActionContext.getRequest();
	public String execute() throws Exception{
		
		String userName=getUserName();
		String password=getPassword();
		int count=0;
		Class.forName("com.mysql.jdbc.Driver");		
		cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
		
		Statement stmt=cn.createStatement();
		String sql="select * from users where userName='"+userName+"' and password='"+password+"';";
		
		ResultSet results=stmt.executeQuery(sql);
		
	    while(results.next()){
			
			HttpSession session=request.getSession();
			count=1;
			int test=results.getInt("test");
			int add=results.getInt("add");
			int modify=results.getInt("modify");
			String course=results.getString("course");
			
			session.setAttribute("user", userName);
			session.setAttribute("test", test);
			session.setAttribute("add", add);
			session.setAttribute("modify", modify);
			session.setAttribute("course", course);
		}
		cn.close();
		if(count==1)
			return "success";
		else {
			request.setAttribute("message", "用户名或密码错误");
			return "error";
		}
	}
	
	public String logout(){
		HttpSession session=ServletActionContext.getRequest().getSession();
		
		session.invalidate();
		return SUCCESS;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
}
