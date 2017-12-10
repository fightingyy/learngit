<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="java.util.*"%>
<%@taglib prefix="s" uri="/struts-tags"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="shortcut icon" href="image/magnifier3.ico">
<title>登录</title>
<link href="css/styles.css" type="text/css" media="screen"
	rel="stylesheet" />
<link href="css/jquery-ui-1.8.16.custom.css" rel="stylesheet">

</head>

<body id="login">
	<% String message=(String)request.getAttribute("message");
			if(message!=null&&!message.equals("")){ %>
	<script type="text/javascript">alert("<%=message %>")</script>
	<%} %>
	<div id="wrappertop"></div>
	<div id="wrapper">
		<div id="content">
			<div id="header">
				<h1>
					<a href=""> <img src="image/magnifier3.png" height="50"
						width="50" alt="logo"></a>
				</h1>
			</div>
			<div id="darkbanner" class="banner320">
				<h2>KnowledgeQA</h2>
			</div>
			<div id="darkbannerwrap"></div>
			<form name="form1" method="post" action="login">
				<fieldset class="form">
					<p>
						<label class="loginlabel" for="user_name"> 账号:</label> <input
							class="logininput ui-keyboard-input ui-widget-content ui-corner-all"
							name="userName" id="user_name" type="text" value="" />
					</p>
					<p>
						<label class="loginlabel" for="password"> 密码:</label> <span>
							<input
							class="logininput ui-keyboard-input ui-widget-content ui-corner-all"
							name="password" id="user_password" type="password" />
						</span>
					</p>
					<button id="loginbtn" type="submit" class="positive" name="submit">
						<img src="image/key.png" alt="Announcement" />登录
					</button>
					<ul id="forgottenpassword">
						<!-- 	                    <li class="boldtext">|</li> -->
						<!-- 	                    <li> -->
						<!-- 	                        <input id="remember" type="checkbox" name="remember" id="rememberMe"> -->
						<!-- 							<label for="rememberMe">记住密码</label></li> -->
					</ul>
				</fieldset>
			</form>
		</div>
	</div>
	<div id="wrapperbottom_branding"></div>
</body>
</html>