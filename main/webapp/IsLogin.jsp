<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<% 
		String user=(String)session.getAttribute("user");	
		int test=0;
		int add=0;
		int modify=0;
		String course="";
		if(user==null||user.equals("")){				
	%>
<p id="extnav">
	<a href="login.jsp"><img src="../KnowledgeQA/image/login.gif"
		width=80px /></a><br />
</p>
	<%	}else{ 
		test=Integer.parseInt(session.getAttribute("test").toString());
		add=Integer.parseInt(session.getAttribute("add").toString());
		modify=Integer.parseInt(session.getAttribute("modify").toString());
		course=(String)session.getAttribute("course");
%>
<p id="extnav">
	<span style="margin-right: 0.5em;">当前用户：<%=user %></span>|<span
		style="float: right; margin-left: 0.5em;"><a href="logout"
		style="color: white;">退出</a></span>
</p>
<div id="userInfo" style="display: none;">
	<span id="Test"><%=test %></span> <span id="Add"><%=add %></span> <span
		id="Modify"><%=modify %></span> <span id="userCourse"><%=course %></span>
</div>
<%} %>

<% String message=(String)request.getAttribute("message"); 
 if(message!=null&&!message.equals("")){
%>
<script type="text/javascript">
		alert("<%=message %>");
	</script>
<% }%>