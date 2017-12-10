package com.ld.pattern;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.internal.compiler.ast.ThisReference;

import com.ld.IO.File.GetFiles;
import com.ld.IO.remove.RemoveDuplicate;
import com.ld.model.Tamplate;

/**   
*    
* 项目名称：KnowledgeQA   
* 类名称：PatternMatching   
* 类描述： 根据正则模板确定谓语
* 创建人：ludan   
* 创建时间：2017年7月17日 下午3:53:26   
* @version        
*/
public class PatternMatching {
	
	private static String path="D:/Documents/workspace/KnowledgeQA/resources/pattern/";
	private List<Tamplate> TamplateList=new ArrayList<Tamplate>();
	private List<List<Tamplate>> tamplateLists=new ArrayList<List<Tamplate>>();
	

	/** 根据正则模板确定谓语
	 * @param question
	 * @param course
	 */
	public void Matching(String question,String course){
		
		List<Tamplate> TamplateList1=new ArrayList<Tamplate>();
	
		
		Pattern pattern;
		Matcher matcher;

		try {
			TamplateList1=ReadPattern.readPattern(course);
		} catch (ClassNotFoundException | SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int i=0;
		for(Tamplate tamplate:TamplateList1){
			
			pattern=Pattern.compile(tamplate.getContent());
			matcher=pattern.matcher(question);
			i++;
			if (matcher.find()){
				
				if(tamplate.isSubject()){
					if(matcher.group("title")!=null){
						tamplate.setSubjectName(matcher.group("title").toString());
					}
					else tamplate.setSubjectName(matcher.group(1).toString());
					
				}
				if(!tamplate.isSubject()&&tamplate.isValue()) 
					if(matcher.group("title")!=null){
						tamplate.setValueName(matcher.group("title").toString());
					}
					else tamplate.setValueName(matcher.group(1).toString());

				if(!tamplate.isSubject()&&!tamplate.isValue())
					if(matcher.group("title")!=null){
						tamplate.setFilter(matcher.group("title").toString());
					}
					else tamplate.setFilter(matcher.group(1).toString());
				
				this.TamplateList.add(tamplate);
			}	
		}

	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public List<Tamplate> getTamplateList() {
		return TamplateList;
	}

	public void setTamplateList(List<Tamplate> tamplateList) {
		TamplateList = tamplateList;
	}

	public List<List<Tamplate>> getTamplateLists() {
		return tamplateLists;
	}

	public void setTamplateLists(List<List<Tamplate>> tamplateLists) {
		this.tamplateLists = tamplateLists;
	}
}
