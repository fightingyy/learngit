<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<%
response.setHeader("Cache-Control","no-store");
response.setHeader("Pragrma","no-cache");
response.setDateHeader("Expires",0);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>添加问题模板</title>
<link rel="shortcut icon" href="image/magnifier3.ico">
<link rel="stylesheet" href="css/main.css" />
<link rel="stylesheet" href="css/tinyselect.css">
<script src="js/jquery.js"></script>
<script src="js/comm.js"></script>
<script src="js/addQuestion.js"></script>
<script src="js/tinyselect.js"></script>

</head>
<body>
	<%@ include file="/IsLogin.jsp"%>
	<div class="body">
		<%@ include file="menu.jsp"%>
		<div id="addtemplate" class="content">

			输入问题模板：<input id="questionTemplate" name="template"
				placeholder="静夜思是谁写的" /><br> 问题的主语：<input id="inputSubject"
				placeholder="静夜思" /><br>
			<div class="selectproperty">
				<span style="float: left;">选择科目及属性：</span> <select id="course"
					class="course">
					<option value="chinese">语文</option>
					<option value="history">历史</option>
					<option value="geo">地理</option>
					<option value="english">英语</option>
					<option value="math">数学</option>
					<option value="chemistry">化学</option>
					<option value="biology">生物</option>
					<option value="politics">政治</option>
					<option value="physics">物理</option>
					<option value="Common">通用</option>
				</select>
				<div class="row" style="float: left; margin-left: 1em;">
					<div class="cell">
						<select id="property">
							<option value="-1">选择属性</option>
						</select>
					</div>
				</div>
				<a id="view" style="float: left;">查看此谓语已有的模板</a><br>
				<br>
			</div>
			<button id="add" class="mybuttom">添加</button>
		</div>

		<%@ include file="footer.jsp"%>
	</div>
</body>
</html>