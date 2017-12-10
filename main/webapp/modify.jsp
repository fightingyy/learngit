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
<title>编辑模板</title>
<link rel='stylesheet' type='text/css' href='css/style1.css' />
<link rel="shortcut icon" href="image/magnifier3.ico">
<link rel="stylesheet" href="css/main.css" />
<link rel="stylesheet" href="css/tinyselect.css">
<link rel='stylesheet' type='text/css' href='css/print.css'media="print" />
<link rel="stylesheet" type="text/css" href="css/jquery.confirm.css" />

<script type='text/javascript' src='js/jquery.js'></script>
<script type='text/javascript' src='js/modify.js'></script>
<script src="js/tinyselect.js"></script>
<script src="js/jquery.confirm.js"></script>

</head>
<body>
	<%@ include file="/IsLogin.jsp"%>
	<%@ include file="menu.jsp"%>
	
	<div id="page-wrap">
		<div class="line">
			<span style="float: left;">选择学科：</span> <select id="course"
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
			<button id="import" style="float: left;font-size:16px;padding:0 0.2em;">导入已有模板</button>

			<div style="float: left; margin-left: 3em;">
				<span style="float: left;">根据属性筛选模板：</span>
				<div class="row" style="float: left;">
					<div class="cell">
						<select id="selectproperty">
							<option value="-1">选择属性</option>
						</select>
					</div>
				</div>
			</div>
			<button id="modify" class="mybuttom">提交</button>
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
				<tr class="item-row">
					<td class="item-name"><div class="delete-wpr">
							<textarea class="id">1</textarea>
						</div></td>
					<td><textarea class="pcontent">输入正则模板</textarea></td>
					<td><textarea class="subject">true</textarea></td>
					<td><textarea class="value">false</textarea></td>
					<td><textarea class="type"></textarea></td>
					<td><textarea class="class">null</textarea></td>
					<td><textarea class="usage">data</textarea></td>
					<td><textarea class="priority">2</textarea></td>
				</tr>

				<tr id="hiderow">
					<td colspan="8"><a id="addrow" href="javascript:;"
						title="Add a row">Add a row</a></td>
				</tr>
			</table>
		</div>
	</div>
	<%@ include file="footer.jsp"%>
</body>
</html>