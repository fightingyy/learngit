/**
 * 
 */
$(document).ready(function(){
	ajaxRequestData();
	
	$('body').click(function(e){
		  var tar = e.target;
		  if($(tar).hasClass('updateTerm')){
			  var tr=$(tar).parents("tr");
			  var course=tr.find("#Course").text();
			  ajaxUpdate("updateTerm",course);
		 }
		 else if($(tar).hasClass('updateTemplate')){
			 
			 $.confirm({
					'title'		: '更新确认',
					'message'	: '是否更新模板？',
					'buttons'	: {
						'确定'	: {
							'class'	: 'blue',
							'action': function(){
								var tr=$(tar).parents("tr");
								 var course=tr.find("#Course").text();
								 ajaxUpdate("updateTemplate",course);
							}
						},
						'取消'	: {
							'class'	: 'gray',
							'action': function(){}	
						}
					}
				});
			
		 }
		 else if($(tar).hasClass('importTemplate')){
			
		 }
		 
		});
});

function ajaxUpdate(url,course){
	$.ajax({
        type: "POST",
        dataType: "json",
        url: url,
        data:{"course":course,},
        timeout:600000,
        complete : function(XMLHttpRequest, textStatus) {  
            // 通过XMLHttpRequest取得响应头，sessionstatus  
            var sessionstatus = XMLHttpRequest.getResponseHeader("sessionstatus");  
            if (sessionstatus == "timeout") {  
                // 这里怎么处理在你，这里跳转的登录页面  
            	alert("登录超时，请重新登录！");
            	window.location.href="./login.jsp";  
            }  
        },
        success: function (data) {
        	alert("更新完毕！");      	 
        },
        error: function (e, jqxhr, settings, exception) {
        	
            alert.text("请求发生错误...");
        
        }
    })
}
function ajaxRequestData(){
	
    $.ajax({
        type: "POST",
        dataType: "json",
        url: "show",
        timeout:600000,
        success: function (data) {
        	var obj = $.parseJSON(data);
        	for(var item in obj){
        		
        		var courseObj=obj[item];
        		$("#new").append("<tr><td id='Course'>"+item+"</td><td>术语数量："+courseObj.termCount+"</td><td>模板数量："+courseObj.patternCount+"</td><td class='opration'><a class='updateTerm' href='#'>更新术语</a></td><td class='opration'><a class='updateTemplate' href='#'>更新模板</a></td><td class='opration'><a class='importTemplate' href='showExcel?course="+change(item)+"'>整合模板</a></td></tr>");
        	}
        	 
        },
        error: function (e, jqxhr, settings, exception) {
        	
            alert("请求发生错误...");
        
        }
    })
}

function change(item){
	var course;
	
	switch(item){
	case "语文": course="chinese";break;
	case "地理": course="geo";break;
	case "数学": course="math";break;
	case "英语": course="english";break;
	case "化学": course="chemistry";break;
	case "历史": course="history";break;
	case "生物": course="biology";break;
	case "政治": course="politics";break;
	case "物理":course="physics";break;	
	}
	
	return course;
}