/**
 * 
 */
        var opts = {            
            lines: 13, // 花瓣数目
            length: 20, // 花瓣长度
            width: 10, // 花瓣宽度
            radius: 30, // 花瓣距中心半径
            corners: 1, // 花瓣圆滑度 (0-1)
            rotate: 0, // 花瓣旋转角度
            direction: 1, // 花瓣旋转方向 1: 顺时针, -1: 逆时针
            color: 'black', // 花瓣颜色
            speed: 1, // 花瓣旋转速度
            trail: 60, // 花瓣旋转时的拖影(百分比)
            shadow: false, // 花瓣是否显示阴影
            hwaccel: false, //spinner 是否启用硬件加速及高速旋转            
            className: 'spinner', // spinner css 样式名称
            zIndex: 2e9, // spinner的z轴 (默认是2000000000)
            top: '4em', // spinner 相对父容器Top定位 单位 px
            left: 'auto'// spinner 相对父容器Left定位 单位 px
        };

        var spinner = new Spinner(opts);

        $(document).ready(function () {
        	
        	var userCourse=$("#userInfo").find("#userCourse").text();
        	if(userCourse!="all"&&course!=userCourse){
   				$("#course").val(userCourse);
   			 }
        	
        	$('#excel').change(function() {
        		   $('#photoCover').val($(this).val());
        	});
        	
        	$("#frm").submit(function(){
        		$(this).ajaxSubmit(function(resultJson) {   
        			var realpath = resultJson.response;
                    ajaxRequestData(realpath);
        		});
        		   return false;
        	});
        	
        	$("#course").change(function(){
	   			 
	   			 var course=$(this).val();
	   			
	   			 if(userCourse!="all"&&course!=userCourse){
	   				$("#course").val(userCourse);
	   				alert("您没有测试该学科的权限！");
	   			 }	 
   		 	});
        	
	    	
        });
        
        function ajaxRequestData(realpath){
        	
        	if($("#excel").val() == null || $("#excel").val()==""){
                alert("请选择待测试的文件!");
                return;
            }
        	
            $.ajax({
                type: "POST",
//                secureuri:false,
                dataType: "json",
                url: "TestAction",
                data: {filepath:realpath,
                		course:$("#course option:selected").val(),
                		},
                beforeSend: function (XMLHttpRequest) {
                	
//                	var sessionstatus = XMLHttpRequest.getResponseHeader("sessionstatus");  
//                    if (sessionstatus == "timeout"||sessionstatus==null) {  
//                        // 这里怎么处理在你，这里跳转的登录页面  
//                    	alert("登录超时，请重新登录！");
//                    	window.location.href="./login.jsp";  
//                    } 
                    
                    //异步请求时spinner出现
                	$("#testresult").empty;
                	$("#testresult").html("");
                	$("#error").text("");
                    var target = $("#wait").get(0);
                    $("#answer fieldset").css("display","none");
                    spinner.spin(target);                    
                },
                success: function (data) {
	
                	$("#testresult").empty;
                	$("#answer fieldset").css("display","block");
                	 var obj = $.parseJSON(data);
                	 
                	 $("#box1").css("display","block");
                	 var i=1; 
            		 var total=obj.total;
            		 var right=obj.right;
            		 var noMatch=obj.NoMatch;
            		 var match=total-noMatch;
            		 var rate=right/total;
            		 var time=obj.time;
//            		 var NoKnowledge=obj.NoKnowledge;
            		 $("#testresult").append("<div id='total'>总计共"+total+"道题<br>共答对"+right+"道题<br>匹配到模板"+match+"道题<br>正确率："+rate+"<br>测试所用时间："+time+"<br></div>");
            		 
            		 for(var d in obj){
              			var item=obj[d];
              			if(d=="file")
              				$("#testresult").append("<div id='viewTest'><a target='_blank' href='"+item+"'>查看测试结果</a></div>");
              		 }
            		 spinner.spin();	
                },
                error: function (e, jqxhr, settings, exception) {
               	 	$("#testresult").text("");
               	 	$("#box").empty();
                    $("#error").text("请求发生错误...");
                    //关闭spinner  
                    spinner.spin();
                }
                
            });
        }
       