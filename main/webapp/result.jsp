<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="net.sf.json.*"
	import="java.util.*" import="com.ld.search.*" import="com.ld.model.*"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
<head>
<title>KnowledgeQA</title>
<link rel="shortcut icon" href="image/magnifier3.ico">
<link rel="stylesheet" href="css/main.css" />
	<script src="js/jquery.js"></script>
<%-- 	<script src="//malsup.github.com/jquery.form.js"></script>  --%>
<%-- 	<script src="js/index.js"></script> --%>
</head>
<body>
	<%@ include file="menu.jsp"%>


	<div id="spinner">
		<img src="image/ajax-loader.gif" alt="Please wait" />
	</div>

	<div id="show">
		<%JSONArray resultArray=(JSONArray)request.getAttribute("result"); 
			  String subject=(String)request.getAttribute("subject");
			  String answer=null;
			  if(resultArray!=null){%>

			<fieldset>
	   			<legend><b><%=subject %>的相关知识</b></legend>
	   			<div id="graph">

					<% 
					for(int i=0;i<resultArray.size();i++) {
		        		JSONObject jsonObject=(JSONObject) resultArray.get(i);
		            	String value=jsonObject.get("value").toString();
		            	String predicate=jsonObject.get("predicate").toString();
		            	String subjectName=jsonObject.get("subject").toString();

				  	 	if(!SesameSearch.isUrl(value)||predicate.equals("出处")){
				  			answer="<b>"+subjectName+"--"+predicate+"：</b>"+value;
						%>
					<p><%=answer %></p>
			
					<%}
				  	 else  {
				  		 answer="<b>"+subjectName+"--"+predicate+"：</b>";
				  	 	 String temp=value.replaceAll("<br>", "");
					%>
					<%=answer %><img alt="" src="<%=temp%>"><br>
					<%  }}} %>
			  	</div>
			</fieldset>

	</div>
	<%@ include file="footer.jsp"%>

</body>
</html>