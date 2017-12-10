package com.ld.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ld.model.sparql.Value;

public class Tamplate {

	private int patternID;
	private String content;
	private String group;
	private String type;
	private String example;
	private String myclass;
	private String course;
	private boolean subject;
	private boolean value;
	private String usage;
	private String subjectName;
	private String valueName;
	private String filter;
	private String title;
	private int priority;
	public List<String> filters=new ArrayList<String>();
	
	private Document doc = null;

	public int getPatternID() {
		return patternID;
	}

	public void setPatternID(int patternID) {
		this.patternID = patternID;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getExample() {
		return example;
	}

	public void setExample(String example) {
		this.example = example;
	}
	
	public void init(String xmlFile) throws Exception {   
		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();   
		DocumentBuilder db = dbf.newDocumentBuilder();   
		doc = db.parse(new File(xmlFile));   
	} 
	
	public List<Tamplate> read(String xmlFile,int level) throws Exception{
		
		
		List<Tamplate> list=new ArrayList<Tamplate>();
		init(xmlFile);
//		Element element = doc.getDocumentElement();   
		 
		
		NodeList patternList = doc.getElementsByTagName("pattern"); 
		if(level==1){
		
			for (int i = 0; i < patternList.getLength(); i++) {
			   
			   Element pattern = (Element) patternList.item(i);
			   
			   Tamplate tamplate = new Tamplate();
			   
			   NodeList patternID = pattern.getElementsByTagName("pattern_id");
			   Element e = (Element) patternID.item(0);
			   Node t = e.getFirstChild();
			   tamplate.setPatternID(Integer.parseInt(t.getNodeValue()));
			   
			   NodeList content = pattern.getElementsByTagName("content");
			   e = (Element) content.item(0);
			   t = e.getFirstChild();
			   tamplate.setContent(t.getNodeValue());
			   
			   NodeList course = pattern.getElementsByTagName("course");
			   e = (Element) course.item(0);
			   t = e.getFirstChild();
			   tamplate.setCourse(t.getNodeValue());
			   
			   NodeList group = pattern.getElementsByTagName("group");
			   e = (Element) group.item(0);
			   t = e.getFirstChild();
			   tamplate.setGroup(t.getNodeValue());
			   
			   list.add(tamplate);
			  }
		}
		else if(level==2){
			for (int i = 0; i < patternList.getLength(); i++) {
				   
				   Element pattern = (Element) patternList.item(i);
				   
				   Tamplate tamplate = new Tamplate();
				   
				   NodeList patternID = pattern.getElementsByTagName("pattern_id");
				   Element e = (Element) patternID.item(0);
				   Node t = e.getFirstChild();
				   tamplate.setPatternID(Integer.parseInt(t.getNodeValue()));
				   
				   NodeList content = pattern.getElementsByTagName("content");
				   e = (Element) content.item(0);
				   t = e.getFirstChild();
				   tamplate.setContent(t.getNodeValue());
				   
				   NodeList subject = pattern.getElementsByTagName("subject");
				   e = (Element) subject.item(0);
				   t = e.getFirstChild();
				   tamplate.setSubject(Boolean.parseBoolean(t.getNodeValue()));
				   
				   NodeList value = pattern.getElementsByTagName("value");
				   e = (Element) value.item(0);
				   t = e.getFirstChild();
				   tamplate.setValue(Boolean.parseBoolean(t.getNodeValue()));
				   
				   NodeList type = pattern.getElementsByTagName("type");
				   e = (Element) type.item(0);
				   t = e.getFirstChild();
				   tamplate.setType(t.getNodeValue());
				   
				   NodeList usage = pattern.getElementsByTagName("usage");
				   e = (Element) usage.item(0);
				   t = e.getFirstChild();
				   tamplate.setUsage(t.getNodeValue());
				   
				   NodeList priority = pattern.getElementsByTagName("priority");
				   e = (Element) priority.item(0);
				   t = e.getFirstChild();
				   tamplate.setPriority(Integer.parseInt(t.getNodeValue()));
				   
				   list.add(tamplate);
			}
		}
		
		else if(level==3){
			for (int i = 0; i < patternList.getLength(); i++) {
				   
				   Element pattern = (Element) patternList.item(i);
				   
				   Tamplate tamplate = new Tamplate();
				   
				   NodeList patternID = pattern.getElementsByTagName("pattern_id");
				   Element e = (Element) patternID.item(0);
				   Node t = e.getFirstChild();
				   tamplate.setPatternID(Integer.parseInt(t.getNodeValue()));
				   
				   NodeList content = pattern.getElementsByTagName("content");
				   e = (Element) content.item(0);
				   t = e.getFirstChild();
				   tamplate.setContent(t.getNodeValue());
				   
				   NodeList subject = pattern.getElementsByTagName("subject");
				   e = (Element) subject.item(0);
				   t = e.getFirstChild();
				   tamplate.setSubject(Boolean.parseBoolean(t.getNodeValue()));
				   
				   NodeList value = pattern.getElementsByTagName("value");
				   e = (Element) value.item(0);
				   t = e.getFirstChild();
				   tamplate.setValue(Boolean.parseBoolean(t.getNodeValue()));
				   
				   NodeList myclass = pattern.getElementsByTagName("class");
				   e = (Element) myclass.item(0);
				   t = e.getFirstChild();
				   tamplate.setMyclass(t.getNodeValue());
				   
				   NodeList type = pattern.getElementsByTagName("type");
				   e = (Element) type.item(0);
				   t = e.getFirstChild();
				   tamplate.setType(t.getNodeValue());
				   
				   NodeList usage = pattern.getElementsByTagName("usage");
				   e = (Element) usage.item(0);
				   t = e.getFirstChild();
				   tamplate.setUsage(t.getNodeValue());
				   
				   NodeList priority = pattern.getElementsByTagName("priority");
				   e = (Element) priority.item(0);
				   t = e.getFirstChild(); 
				   tamplate.setPriority(Integer.parseInt(t.getNodeValue()));
				   
				   list.add(tamplate);
			}
		}
		else if (level==4) {
			for (int i = 0; i < patternList.getLength(); i++) {
				   
				   Element pattern = (Element) patternList.item(i);
				   
				   Tamplate tamplate = new Tamplate();
				   
				   NodeList patternID = pattern.getElementsByTagName("pattern_id");
				   Element e = (Element) patternID.item(0);
				   Node t = e.getFirstChild();
				   tamplate.setPatternID(Integer.parseInt(t.getNodeValue()));
				   
				   NodeList content = pattern.getElementsByTagName("content");
				   e = (Element) content.item(0);
				   t = e.getFirstChild();
				   tamplate.setContent(t.getNodeValue());
				   
				   NodeList subject = pattern.getElementsByTagName("subject");
				   e = (Element) subject.item(0);
				   t = e.getFirstChild();
				   tamplate.setSubject(Boolean.parseBoolean(t.getNodeValue()));
				   
				   NodeList value = pattern.getElementsByTagName("value");
				   e = (Element) value.item(0);
				   t = e.getFirstChild();
				   tamplate.setValue(Boolean.parseBoolean(t.getNodeValue()));
				   
				   NodeList type = pattern.getElementsByTagName("type");
				   e = (Element) type.item(0);
				   t = e.getFirstChild();
				   tamplate.setType(t.getNodeValue());
				   
				   NodeList Class = pattern.getElementsByTagName("class");
				   e = (Element) Class.item(0);
				   t = e.getFirstChild();
				   tamplate.setMyclass(t.getNodeValue());
				   
				   NodeList usage = pattern.getElementsByTagName("usage");
				   e = (Element) usage.item(0);
				   t = e.getFirstChild();
				   tamplate.setUsage(t.getNodeValue());
				   
				   list.add(tamplate);
			}
		}
		return list;
		
	}
	
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tamplate other = (Tamplate) obj;
		if (subject != other.subject)
			return false;
		if (example != other.example) {
			return false;
		} 
		if (value != other.value){
			return false;
		}
		return true;
	}

	public String getMyclass() {
		return myclass;
	}

	public void setMyclass(String myclass) {
		this.myclass = myclass;
	}

	public String getCourse() {
		return course;
	}

	public void setCourse(String course) {
		this.course = course;
	}

	public boolean isSubject() {
		return subject;
	}

	public void setSubject(boolean subject) {
		this.subject = subject;
	}

	public boolean isValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}

	public String getSubjectName() {
		return subjectName;
	}

	public void setSubjectName(String subjectName) {
		this.subjectName = subjectName;
	}

	public String getValueName() {
		return valueName;
	}

	public void setValueName(String valueName) {
		this.valueName = valueName;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

}
