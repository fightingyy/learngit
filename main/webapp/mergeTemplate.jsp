<%@ page language="java" contentType="text/html; charset=UTF-8" import="net.sf.json.JSONObject" import="java.util.*"
	 pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>整合模板</title>
<link rel='stylesheet' type='text/css' href='css/style1.css' />
<link rel="shortcut icon" href="image/magnifier3.ico">
<link rel="stylesheet" href="css/main.css" />
<link rel="stylesheet" href="css/tinyselect.css">
<link rel='stylesheet' type='text/css' href='css/print.css'
	media="print" />
<script type='text/javascript' src='js/jquery.js'></script>
<script type='text/javascript' src='js/merge.js'></script>
<script src="js/tinyselect.js"></script>
</head>
<body>
	<%@ include file="/IsLogin.jsp"%>
	<%@ include file="menu.jsp"%>
	<div id="page-wrap">
		<div>
			
			<button id="modify" class="mybuttom">提交</button>
			<% String c=(String)request.getAttribute("course");
			   if(c!=null&&!c.equals("")){
			%>
			<font style="font-size:1.1em;" id="c" title="<%=c %>">以下是<%=c %>已导出的模板，请删除无用的模板：</font>
			<%} %>
		</div>
		<div>
			<table id="items" class="table">
				<tr>
					<th width="5%">pattern_id</th>
					<th width="45%">content</th>
					<th width="5%">subject</th>
					<th width="5%">value</th>
					<th width="20%">type</th>
					<th width="5%">class</th>
					<th width="5%">usage</th>
					<th width="5%">priority</th>
				</tr>
				
				<% JSONObject result=(JSONObject)request.getAttribute("result");  
					if(result!=null){
						Iterator its = result.keys();  
			        	List<String> keyListstr = new ArrayList<String>(); 
			        	while(its.hasNext()){ 
							JSONObject cell=(JSONObject)result.get(its.next());
							Iterator it= result.keys();
							int id=cell.getInt("id");
							String content=(String)cell.get("content");
							String subject=(String)cell.get("subject");
							String value=(String)cell.get("value");
							String type=(String)cell.get("type");
							String myclass=(String)cell.get("myclass");
							String usage=(String)cell.get("usage");
							int priority=cell.getInt("priority");
					%>
					<tr class='item-row'> 
						<td class='item-name'>
							<div class='delete-wpr'>
							<a class='delete' href='javascript:;' title='Remove row'>X</a>
							</div><%=id %>
						</td>
						<td class="pcontent"><textarea><%=content %></textarea></td> 
						<td class="subject"><%=subject %></td> 
						<td class="value"><%=value %></td> 
						<td class="type"><%=type %></td> 
						<td class="class"><%=myclass %></td> 
						<td class="usage"><%=usage %></td> 
						<td class="priority"><%=priority %></td> 
					</tr>
				
				<% }
			        	}%>
				
			</table>
		</div>
	</div>
	<%@ include file="footer.jsp"%>
</body>
</html>