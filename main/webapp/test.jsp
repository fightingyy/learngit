<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="java.util.*"%>

<!DOCTYPE html>
<html>
<head>
<title>问答测试</title>
<link rel="shortcut icon" href="image/magnifier3.ico">
<link rel="stylesheet" href="css/main.css" />
<!-- <link href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css" rel="stylesheet"> -->
<link href="css/fileinput.css" media="all" rel="stylesheet" type="text/css" />
<script src="js/jquery.js"></script>
<script src="js/jquery.form.js"></script>
<script src="js/spin-image.js" type="text/javascript"></script>
<script src="js/ajaxfileupload.js" type="text/javascript"></script>
<script src="js/test.js" type="text/javascript"></script>
<script src="js/testResult.js" type="text/javascript"></script>
<script src="js/fileinput.js" type="text/javascript"></script>

<style type="text/css">

/* 		input { border:0; padding:0; margin:0; }  */
.div {
	margin: 0 auto;
	width: 100%;
	overflow: hidden;
	padding: 20px 0;
}

.line {
	width: 1000px;
	margin: 0 auto;
}
</style>


</head>
<body>
	<%@ include file="/IsLogin.jsp"%>

	<%@ include file="menu.jsp"%>
	<div id="side">
			<div id="ResultList">
					<p id="before">测试结果列表:</p>
					<ul id="fileList" >
					</ul>
			</div>
<!-- 			<button id="analysis"><a href="analysis">分析测试结果</a></button> -->
	</div>
	<div class="content">
		<div id="question">

			<div class="line">
				<form action="uploadFile" id="frm" enctype="multipart/form-data" method="post" target="frameFile1">
					<select name="course" id="course"
						style="float: left; width: 100px; margin-right: 2px;">
						<option value="chinese">语文</option>
						<option value="history">历史</option>
						<option value="geo">地理</option>
						<option value="english">英语</option>
						<option value="math">数学</option>
						<option value="chemistry">化学</option>
						<option value="biology">生物</option>
						<option value="politics">政治</option>
						<option value="physics">物理</option>
					</select> 
					<input id="excel" name="excel" type="file"  style="display: none">
					<div class="input-append">
						<input id="photoCover" class="input-large" type="text"> 
						<span id="showFile"><a class="btn"onclick="$('input[id=excel]').click();"><img src="./image/open.ico" style="vertical-align:middle;" width="20px"/>&nbsp;浏览</a></span>
					</div>
					<input type="submit" id="test" value="测试">
				</form>
			</div>
			<iframe name='frameFile1' id="frameFile1" style="display: none;"></iframe>
		</div>
		<div id="wait"></div>

		<div id="answer">

			<div style="margin-top:3em;">
				<fieldset style="display: none" class="fieldset">
					<legend>统计测试结果</legend>
					<div id="testresult"></div>
				</fieldset>
				<div id="error"></div>
			</div>		
		</div>


	</div>

	<%@ include file="footer.jsp"%>

</body>
</html>