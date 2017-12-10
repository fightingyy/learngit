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
            top: 'auto', // spinner 相对父容器Top定位 单位 px
            left: 'auto'// spinner 相对父容器Left定位 单位 px
        };

        var spinner = new Spinner(opts);

        $(document).ready(function () {
            $("#search").bind("click", function () {
                ajaxRequestData();
                
            });
            
//            $("body").on('click','#other',function(){
//            	otherResult();
//            });
        })
        
        function ajaxRequestData(){
            $.ajax({
                type: "POST",
                dataType: "json",
                url: "answer",
                data: {inputQuestion : $("#inputQuestion").val(),
                		course:$("#course option:selected").val()},
                timeout:600000,
                beforeSend: function () {
                    //异步请求时spinner出现
                	$("#imformation").text("");
                    var target = $("#wait").get(0);
                    $("#answerd fieldset").css("display","none");
                    spinner.spin(target);                    
                },
                success: function (data) {
	
                	 var json = $.parseJSON(data);
                	 $("#citiao").empty();
                	 $("#imformation").text("");
                	 $("#result").text("");
                	 $("#wordList").empty();
                	 var subject="";
                	 
                	 $("#answerd fieldset").css("display","block");
                	 var uniArray=new Array();
                	 var count=0;
                	 for(var i in json){
                		 var obj=json[i];
                		 var value=obj.value;
                		 var title=obj.subject;
                		 var predicate=obj.predicate;
                		 var score=obj.score;
              			 var regex = new RegExp("^http://(.*)?");
              			 var re = new RegExp("<br>","g"); 
              			 
              			 if(score<1.5&&count==0&&obj.message==""&&score!=0){
//              				$("#result").append("<br><hr style='width:5%;height:2pt;background-color: #441379;margin-right: 0.1em;float:left;margin-top:0.7em;'><span style='font-size:18px;float:left;'>以下是可能的答案</span><hr style='width:71%;height:2pt;background-color: #441379;margin-left: 1em;float:right;margin-top:0.7em;'>");
              				$("#result").append("<img src='./image/may.jpg' style='width:95%;margin-top:0.8em;margin-right:1.5em;'/><br>");
              				count++;
              			 }
                		 if(title!=""&&uniArray.indexOf (title)<0){
                			 uniArray.push(title);
                			 var temp=title.replace("（非直接查询出的结果，经后续处理得到的答案）","");
                			 subject+="<a id='other' href='AddAction?subjectName="+temp+"&course="+$("#course option:selected").val()+"'>"+temp+"<a/>&nbsp;&nbsp;&nbsp;";
                		 }
                			 
                		 
                		 if(obj.message!=null&&obj.message=="您提的问题暂时没有可用的模板，您可以自己添加模板"){
                    		 $("#imformation").text(obj.message+"：");
                    		 $("#imformation").append("<a href='./QuestionTemplate.jsp'>添加问题模板</a><br><br>");
                    		 continue;
                    	 }
                		 else if(obj.message!=null&&obj.message!=""){
                			 $("#imformation").text(obj.message);
                			 continue;
                			 }
                		 else if(obj.attention!=null&&obj.attention!="")
               				$("#result").append("<div>"+obj.attention+"</div><br><hr style='height:2pt;background-color: #441379;margin-right: 1.5em;'>");


              			 if(predicate!="") {
              				 if(!regex.test(value.replace(re,""))||predicate=="出处"){
        
          						$("#result").append("<div class='list'><b>"+title+"--"+predicate+":</b>"+value+"</div>");
              					
                 	         }
                 	         else {
                 	        	if(value.indexOf("<br><br>")>-1){
                 	        		var array=value.split("<br><br>")
                 	        		for(var t=0;t<array.length;t++){
                 	        			var image=array[t].replace(re,"").replace("&nbsp;&nbsp;","").replace(" ","");
                 	        			$("#result").append("<div class='list'> <b>"+title+"--"+predicate+":</b><image id='i' src='"+image+"'></div>");

             	        			}
             	        		}	
                 	        	else if(value.indexOf("getjpg")>-1||value.indexOf("getpng")>-1){
                 	        		$("#result").append("<div class='list'> <b>"+title+"--"+predicate+":</b><image id='i' src='"+value.replace(re,"")+"' /><br></div>");
                 	        	}
                 	        }
              			}
              			else if(value!=""){
              				var index=value.indexOf("（非直接查询出的结果，经后续处理得到的答案）");
              				if(index>0){
              					var s=value.replace("（非直接查询出的结果，经后续处理得到的答案）<br>","").replace("<br>","");
              					$("#result").append("<div class='list'><b>"+s+"</b>（非直接查询出的结果，经后续处理得到的答案）</div>");
              				}
              				else $("#result").append("<div class='list'>"+value+"</div>");
              			}
              			else if(title!=""){
              				
              				$("#result").append("<div class='list'>"+title+"</div>");
              			}
                	 }
                	 
                	 if(subject!=""){
                		 array=obj.subject.split("|");
                		
                		 $("#wordList").append("<p id='citiao'>相关词条：");
                		 for(var i=0;i<array.length;i++){
                			 if(array[i]!=null&&array[i]!="")
                				 $("#citiao").append(subject);
                		 }
                		 
                	 }
                    //关闭spinner  
                    spinner.spin();
                    show();
                },
                error: function (e, jqxhr, settings, exception ) {
                	$("#imformation").text("");
               	 	$("#result").text("");
               	 	$("#box").empty();
               	 	$("#answerd fieldset").css("display","block");
                    $("#imformation").text("请求连接超时...");
                    //关闭spinner  
                    spinner.spin();
                }
            })
        }
        function show(){  
        	var length=100;
        	$(".list").each(function(){
                var text = $(this).html();
                if(text.length<200) return ;
                var newBox = document.createElement("div");//创建一个新的div对象。  
                var btn = document.createElement("a");//创建一个新的a对象。  
                if(text.indexOf("--")>0&&text.indexOf(":")>0){
                	 var array = text.split(":");
                	 text="<b>"+array[0]+":</b>"+array[1];
                }
                	
                newBox.innerHTML = text.substring(0,length);
                btn.innerHTML = text.length > length ? " ...显示全部" : "";
                btn.href = "###"; 
                btn.onclick = function(){ 
                  if(btn.innerHTML == " ...显示全部")  
                  {  
                    btn.innerHTML = " 收起";  
                    newBox.innerHTML = text; 
                    newBox.append(btn);
                  }  
                  else  
                  {  
                    btn.innerHTML = " ...显示全部";  
                    newBox.innerHTML = text.substring(0,length); 
                    newBox.append(btn);
                  }  
                }  
                $(this).text("");
                $(this).append(newBox);
                newBox.append(btn);
        	});
              
          } 
        
        
  