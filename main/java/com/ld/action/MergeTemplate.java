package com.ld.action;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionSupport;

	
/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：MergeTemplate   
* 类描述： 合并模板
* 创建人：ludan   
* 创建时间：2017年6月2日 上午11:13:07   
* @version        
*/
public class MergeTemplate extends ActionSupport {

	private String course;
	private String result;
	private String jsonObj;
	
	Connection cn;
	
	public String execute(){
		
		JSONObject jsonObject = JSONObject.fromObject(jsonObj);
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
			cn = DriverManager.getConnection("jdbc:mysql://localhost:3306/knowledgeqa?useUnicode=true&characterEncoding=utf8","root","111111");
			
			Statement stmt=cn.createStatement();
			
			Iterator it = jsonObject.keys();  
	        while (it.hasNext()) {  
	            String key = (String) it.next();  
	            HttpServletRequest request = ServletActionContext.getRequest();
	            JSONObject pattern = JSONObject.fromObject(jsonObject.get(key));

	            String content=pattern.getString("content");
	            boolean subject=pattern.getBoolean("subject");
	            boolean value=pattern.getBoolean("value");
	            String myclass=pattern.getString("myclass");
	            String type=pattern.getString("type");
	            String usage=pattern.getString("usage");
	            int priority=pattern.getInt("priority");
	            String course=pattern.getString("course");
	            
	            String sql="insert into "+course+"_template SET `content`='"+content+"',`subject`="+booleanToInt(subject)+",`value`="+booleanToInt(value)+", `type`='"+type+"', `class`='"+myclass+"', `usage`='"+usage+"',`priority`="+priority+";";	            stmt.addBatch(sql);
	            
	        }
	        stmt.executeBatch();
	        cn.close();
	        
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		return SUCCESS;
	}
	
	/**
	 * 使excel中的模板数据显示在前端页面上
	 * @return
	 */
	public String showExcel(){
		
		JSONObject jsonObject=new JSONObject();
		HttpServletRequest request = ServletActionContext.getRequest();
		String path=request.getRealPath("/");
		
		if(path.contains("\\.metadata"))
			path=path.split("\\.metadata")[0]+"KnowledgeQA/";
		else if(path.contains("WEB-INF")){
			path=path.split("WEB-INF")[0];
		}
		String fileName=path+"resources/pattern/"+getCourse()+".xlsx";
		File file=new File(fileName);
		if(!file.exists()){
		    request.setAttribute("alert", "本学科没有待整合的模板");
		    return "error";
		}
		else{
			XSSFWorkbook xwb;
			try {
				xwb = new XSSFWorkbook(fileName);
				XSSFSheet sheet = xwb.getSheetAt(0);
		        
		        XSSFRow row;
		        for (int i = 1; i<sheet.getPhysicalNumberOfRows(); i++) {
		        	
		        	JSONObject cellObject=new JSONObject();
		        	String myclass="";
		        	row = sheet.getRow(i);
		        	int id = Integer.parseInt(row.getCell(0).toString());
		        	String content=row.getCell(1).toString();
		        	String subject = row.getCell(2).toString();
		        	String value = row.getCell(3).toString();
		        	String type=row.getCell(4).toString();
		        	if(row.getCell(5)!=null&&row.getCell(5).equals("")){
		        		myclass=row.getCell(5).toString();
		        	}		
		        	String usage=row.getCell(6).toString();
		        	int priority =Integer.parseInt(row.getCell(7).toString());	        	
		        		        	
		        	cellObject.put("id",id);
					cellObject.put("content",content);
					cellObject.put("subject",subject);
					cellObject.put("value",value);
					cellObject.put("type",type);
					cellObject.put("myclass",myclass);
					cellObject.put("usage",usage);
					cellObject.put("priority",priority);
					
					jsonObject.put(i, cellObject);		        	
		        }				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	        
			request.setAttribute("result", jsonObject);
			course=change(course);
			request.setAttribute("course", course);
			
			return "success";
		}	
	}
	
	/**
	 * 学科英文到中文的转换
	 * @param item
	 * @return
	 */
	public static String change(String item) {
		String course="";
		
		switch(item){
		case "chinese": course="语文";break;
		case "geo": course="地理";break;
		case "math": course="数学";break;
		case "english": course="英语";break;
		case "chemistry": course="化学";break;
		case "history": course="历史";break;
		case "biology": course="生物";break;
		case "politics": course="政治";break;
		case "physics":course="物理";break;	
		}
		
		return course;
		
	}

	/**
	 * bool型数据转换为int型
	 * @param value
	 * @return
	 */
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

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getJsonObj() {
		return jsonObj;
	}

	public void setJsonObj(String jsonObj) {
		this.jsonObj = jsonObj;
	}
}
