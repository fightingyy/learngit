<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="net.sf.json.*"
	import="java.util.*" import="com.ld.search.*" import="com.ld.model.*"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
<head>
<title>showKnowledgeQA</title>
<link rel="shortcut icon" href="image/magnifier3.ico">
<link rel="stylesheet" href="css/main.css" />
<script src="js/jquery.js"></script>
	
</head>
<body>

	<div id="">
		<%JSONArray resultArray=(JSONArray)request.getAttribute("result"); 
			  if(resultArray!=null){%>
			  	<table border="1" style="width: 100%">
				<thead>
				<tr>
					<th>问题序列号</th>
					<th>提问时间</th>
					<th>问题内容</th>
					<th>给出的最终答案</th>
					<th>问题匹配的模板</th>
					<th>模板识别的主语</th>
					<th>模板识别的谓语</th>
					<th>模板匹配到的答案和得分</th>
					<th>全文检索给出的答案和得分</th>
				</tr>
				</thead>
					<%for(int i = 0; i < resultArray.size(); i++) {
						JSONObject jsonObject=(JSONObject) resultArray.get(i);
						String id=jsonObject.get("id").toString();
						String time=jsonObject.get("time").toString();
						String question=jsonObject.get("question").toString();
		            	String value=jsonObject.get("value").toString();
		            	
		            	String subject=jsonObject.get("subject").toString();
		            	String predicate=jsonObject.get("predicate").toString();
		            	String score=jsonObject.get("score").toString();
		            	String template=jsonObject.get("template").toString();
		            	String fsanswer = jsonObject.get("fsanswer").toString();
		            	%>
		            	<tr>
		            		<td><%=id %></td>
		            		<td><%=time %></td>
		            		<td><%=question %></td>
		            		<td><%=value %></td>
		            		<td><textarea readonly style="resize:none;border:none;"><%=template %></textarea></td>
		            		<td><%=subject %></td>
		            		<td><%=predicate %></td>
		            		<td><%=score %></td>
		            		<td><textarea readonly style="resize:none;border:none;"><%=fsanswer %></textarea></td>
		            		
		            	</tr>
					<% }}%>
				</table>
	</div>
	<%@ include file="footer.jsp"%>

</body>
</html>