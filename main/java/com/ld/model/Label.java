package com.ld.model;

import java.sql.*;

public class Label {

	private String property;
	private String label;
	private String course;
	
	public Connection Connection() throws ClassNotFoundException, SQLException{
		
		  Connection cn;  
		  Class.forName("com.mysql.jdbc.Driver");
		  cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
		  
		  return cn;
	}
	
	public String selectByUri(String uri,String course) throws ClassNotFoundException, SQLException{
		
		String labelString = null;
		
		Connection con=Connection();
		Statement stmt=con.createStatement();
		String sql="select label from Property where property='"+uri+"' and course='"+course+"'";
//		System.out.println(sql);
		ResultSet results=stmt.executeQuery(sql);
		
		if(results.next()){
			labelString=results.getString("label");
		}
		
		return labelString;
	}
	
	public void insert(String uri,String label,String course) throws ClassNotFoundException, SQLException{
		
		Connection con=Connection();
		Statement stmt=con.createStatement();
		String sql="insert into Property (property,label,course) values('"+uri+"','"+label+"','"+course+"')";
//		System.out.println(sql);
		boolean s=stmt.execute(sql);
		if(s) System.out.println("insert success");
		
	}
	
	public Label() {
		// TODO Auto-generated constructor stub
	}
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public String getCourse() {
		return course;
	}
	public void setCourse(String course) {
		this.course = course;
	}

}
