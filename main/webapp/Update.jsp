<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="java.util.*"
	import="com.hp.hpl.jena.query.ResultSet"
	import="com.hp.hpl.jena.query.QuerySolution"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
<head>
<title>更新数据</title>
<link rel="shortcut icon" href="image/magnifier3.ico">
<link rel="stylesheet" href="css/main.css" />
<link rel="stylesheet" type="text/css" href="css/jquery.confirm.css" />

<script src="js/jquery.js"></script>
<script src="js/jquery.form.js"></script>
<script src="js/jquery.confirm.js"></script>
<script src="js/spin-image.js" type="text/javascript"></script>
<script src="js/update.js" type="text/javascript"></script>


</head>
<body>
	<%@ include file="/IsLogin.jsp"%>
	<%@ include file="menu.jsp"%>
	<% String alert=(String)request.getAttribute("alert");
		if(alert!=null&&!alert.equals("")){
	%>
		<script type="text/javascript">
			alert("<%=alert %>");
		</script>
	<%} %>
	<div class="" style="height: auto; min-height: 500px;">
		<table id="new">
			<tr id="th">
				<th style="width: 10%;">科目</th>
				<th colspan=2 style="width: 50%;">概况</th>
				<th colspan=3 style="width: 40%;">操作</th>
			</tr>
		</table>
	</div>


	<%@ include file="footer.jsp"%>
</body>
</html>