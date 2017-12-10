/**
 * 
 */

$(document).ready(function() {
	
	$('body').click(function(e){
		  var tar = e.target;
		  if($(tar).hasClass('delete')){
		    $(tar).parents('.item-row').remove();
		    if ($(".delete").length < 2) $(".delete").hide();
		  }
	});
		   
	 $("#modify").click(function(){
		 
		 var i=0;
		 
		 var jsonObj={};
		 var table=$('#items');
		 var course=$('#c').attr("title"); 
		 course=change(course);
		 table.find("tr.item-row").each(function(){
			 if($(this).is(':visible')){
				 var pattern=new Object();
				 
				 pattern.content=$(this).find(".pcontent").text();
				 pattern.subject=$(this).find(".subject").text();
				 pattern.value=$(this).find(".value").text();
				 pattern.type=$(this).find(".type").text();
				 pattern.usage=$(this).find(".usage").text();
				 pattern.myclass=$(this).find(".class").text();
				 pattern.priority=$(this).find(".priority").text();
				 pattern.course=course;	 
				 
				 jsonObj[i]=pattern;
				 i++;
			 }
		 });
		 

		 modify(JSON.stringify(jsonObj));
	 });

});



function modify(jsonObj){
	
	$.ajax({
        type: "POST",
        dataType: "json",
        url: "merge",
        data: {"jsonObj":jsonObj},		
        timeout:6000,
        beforeSend: function (XMLHttpRequest) {
        	
//        	var sessionstatus = XMLHttpRequest.getResponseHeader("sessionstatus");  
//            if (sessionstatus == "timeout"||sessionstatus==null) {  
//                // 这里怎么处理在你，这里跳转的登录页面  
//            	alert("登录超时，请重新登录！");
//            	window.location.href="./login.jsp";  
//            } 
        	var tr=$('#items').find("tr");
        	if(tr.length>2){
				for(var i=2;i<tr.length-1;i++){
					tr.eq(i).remove();
				}        
        	}
        },
        success: function (data) {
        	alert("模板合并完成");
        }
        });
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
