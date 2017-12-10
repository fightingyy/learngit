<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
<head>
<title>知识记忆类问答</title>
<link rel="shortcut icon" href="image/magnifier3.ico">
<link rel="stylesheet" href="css/main.css" />

<script src="js/jquery.js"></script>
<script src="js/jquery.form.js"></script>
<script src="js/spin-image.js" type="text/javascript"></script>
<script src="js/index.js" type="text/javascript"></script>

</head>
<body>
	<div class="body">
		<%@ include file="/IsLogin.jsp"%>
		<%@ include file="menu.jsp"%>
		<div class="content">
			<div id="question">
				<div>
					<s:form action="#" id="ask" onsubmit="return false;">

						<s:select id="course"
							list="#{'history':'历史','geo':'地理','math':'数学','english':'英语','chemistry':'化学','biology':'生物','politics':'政治','physics':'物理'}" label="学科"
							headerKey="chinese" headerValue="语文"></s:select>
						<s:textfield name="inputQuestion" id="inputQuestion"
							placeholder="《静夜思》的作者是谁?" />
						<s:submit id="search" value="查询" />
					</s:form>
				</div>
			</div>
			<div id="wait"></div>
			<div id="dashboard">

				<div id="answerd">
					<fieldset class="fieldset">
	   					<legend>答案</legend>
						<div id="result"></div>
						<div id="imformation"></div>
						<div id="wordList"></div>
					</fieldset>
					
				</div>
			</div>
		</div>
		<%@ include file="footer.jsp"%>
	</div>
</body>
</html>