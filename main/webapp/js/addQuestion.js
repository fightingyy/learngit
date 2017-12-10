
       $(document).ready(function(){
    	   
    	   var userCourse=$("#userInfo").find("#userCourse").text();
    	   if(userCourse!="all"){
    		   $("#course").val(userCourse);
    		   $("#property").tinyselect({ dataUrl: "js/"+userCourse+".json" , dataParser: dataParserA });
    	   }
    	   else 
      			$("#property").tinyselect({ dataUrl: "js/chinese.json" , dataParser: dataParserA });
    	   
    	   $("#course").change(function(){
    		   var course=$("#course option:selected").val();
    		   $(".tinyselect").remove();
    		   var url="js/"+course+".json";
    		   $("#property").tinyselect({ dataUrl: url , dataParser: dataParserA });
    		   
  			   var course=$(this).val();
  			   if(userCourse!="all"&&course!=userCourse){
  				   $("#course").val(userCourse);
  				   alert("您没有添加该学科模板的权限！");
  			   }
  				   
    		 });
    	   $('#view').click(function() {		
				ajaxView();
			});
    	   
    	   $('#add').click(function() {
   			ajaxRequestData();
   			});
		});
       
       function dataParserA(data, selected) {
			retval = [ { val: "-1" , text: "选择属性" } ];

			data.forEach(function(v){
				if(selected == "-1" && v.val == 3)
					v.selected = true;
				retval.push(v); 
			});

			return retval;
		}

		function dataParserB(data, selected) {
			retval = [ { val: "-1" , text: "选择属性" } ];
			data.forEach(function(v){ retval.push(v); });
			return retval;
		}
		
		function ajaxRequestData(){
			console.log($("#tinyselect").attr("name"));
            $.ajax({
                type: "POST",
                dataType: "json",
                url: "AddQuestionTemplate",
                data: {template : $("#questionTemplate").val(),
                		course:$("#course option:selected").val(),
                		subject:$("#inputSubject").val(),
                		
                		inputSubject:$("#inputSubject").val(),
                		type:$("#tinyselect").attr("name"),
                		},
        		beforeSend: function (XMLHttpRequest) {
                	
//                	var sessionstatus = XMLHttpRequest.getResponseHeader("sessionstatus");  
//                    if (sessionstatus == "timeout"||sessionstatus==null) {  
//                        // 这里怎么处理在你，这里跳转的登录页面  
//                    	alert("登录超时，请重新登录！");
//                    	window.location.href="./login.jsp";  
//                    }  
                },		
                timeout:6000,
                success: function (data) {
                	alert("模板添加成功");
                	 
                },
                error: function (e, jqxhr, settings, exception) {
                	
                    $("#imformation").text("请求发生错误...");
                
                }
            })
        }
		
		function ajaxView(){

            $.ajax({
                type: "POST",
                dataType: "json",
                url: "ViewTemplate",
                data: {course:$("#course option:selected").val(),
                	type:$("#tinyselect").attr("name"),
                	},

                success: function (data) {
                	var obj = $.parseJSON(data);
                	var items="";
                	var i=1;
                	
                	for(var d in obj){
             			var item=obj[d];
             			items=items+i+"、"+item+"\r\n";
             			i++;
                	}
                	alert(items);
                	 
                },
                error: function (e, jqxhr, settings, exception) {
                	
                    $("#imformation").text("请求发生错误...");
                
                }
            })
        }
		
        
        
        
  