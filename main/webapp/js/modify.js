


$(document).ready(function() {
	
	var userCourse=$("#userInfo").find("#userCourse").text();
	if(userCourse!="all"){
		$("#course").val(userCourse);
		$("#selectproperty").tinyselect({ dataUrl: "js/"+userCourse+".json" , dataParser: dataParserA });
    }
	else 
		$("#selectproperty").tinyselect({ dataUrl: "js/chinese.json" , dataParser: dataParserA });
	
	$("#course").change(function(){
		var course=$("#course option:selected").val();
		$(".tinyselect").remove();
		var url="js/"+course+".json";
		$("#property").tinyselect({ dataUrl: url , dataParser: dataParserA });
		$("#selectproperty").tinyselect({ dataUrl: url , dataParser: dataParserA });
	 });
	 $("#import").click(function(){
		 ajaxImport();
	 });

	 $('input').click(function(){
	    $(this).select();
	 });
	 $('body').click(function(e){
		  var tar = e.target;
		  
		  if($(tar).hasClass('delete')){
			 
			  var tr=$(tar).parents("tr");
			  var id=tr.find(".item-name").text();
			  
			  if(id!="X"){
				  $.confirm({
						'title'		: '删除确认',
						'message'	: '是否删除此模板？',
						'buttons'	: {
							'确定'	: {
								'class'	: 'blue',
								'action': function(){
									 var course=tr.find("#Course").text(); 
									 ajaxDelete(id);
									 $(tar).parents('.item-row').remove();
									    if ($(".delete").length < 2) $(".delete").hide();
								}
							},
							'取消'	: {
								'class'	: 'gray',
								'action': function(){}	
							}
						}
					});
			  }
			  else{
				  $(tar).parents('.item-row').remove();
				    if ($(".delete").length < 2) $(".delete").hide();
			  }
		  }
	});
	   
	 $("#addrow").click(function(){
	    $(".item-row:last").after('<tr class="item-row"><td class="item-name"><div class="delete-wpr"><a class="delete" href="javascript:;" title="Remove row">X</a></div><textarea class="id"></textarea></td><td><textarea class="pcontent">输入正则模板</textarea></td><td><textarea class="subject">true</textarea></td><td><textarea class="value">false</textarea></td><td><div class="row" style="float:left;margin-left:1em;"><div class="cell"><textarea id="property" class="type"></textarea></div></div></td><td><textarea class="class"></textarea></td><td><textarea class="usage">data</textarea></td><td><textarea class="priority">2</textarea></td></tr>');
	    if ($(".delete").length > 0) $(".delete").show();
	    
	  });
	 $("#selectproperty").bind('change', function() { 
		   var property=$("#tinyselect").attr("name");
		   selectProperty(property);
	   });
	 
	 $("#modify").click(function(){
		 
		 var i=0;
		 
		 var jsonObj={};
		 var table=$('#items');

		 table.find("tr.item-row").each(function(){
			 if($(this).is(':visible')){
				 var pattern=new Object();
				 pattern.id=$(this).find(".item-name").text().replace("X","");
				 pattern.content=$(this).find(".pcontent").val();
				 pattern.subject=$(this).find(".subject").val();
				 pattern.value=$(this).find(".value").val();
				 pattern.type=$(this).find(".type").val();
				 pattern.usage=$(this).find(".usage").val();
				 pattern.myclass=$(this).find(".class").val();
				 pattern.priority=$(this).find(".priority").val();
				 pattern.course=$("#course option:selected").val();
				 if(pattern.id=="")pattern.id="-1";
				 if(pattern.content!=""&&pattern.type!=""){
					 jsonObj[i]=pattern;
					 i++;
				 }
			 }
		 });
		 

		 modify(JSON.stringify(jsonObj));
		 ajaxImport(); 
	 });
	 $("#course").change(function(){
		 var course=$(this).val();
		 if(userCourse!="all"&&course!=userCourse){
			 $("#course").val(userCourse);
			 alert("您没有修改该学科模板的权限！");
		 }	 
	 });
});

function modify(jsonObj){
	
	$.ajax({
        type: "POST",
        dataType: "json",
        url: "modify",
        data: {"jsonObj":jsonObj},
        timeout:6000,
        beforeSend: function (XMLHttpRequest) {
        	
        	var sessionstatus = XMLHttpRequest.getResponseHeader("sessionstatus");  
            if (sessionstatus == "timeout") {  
                // 这里怎么处理在你，这里跳转的登录页面  
            	alert("登录超时，请重新登录！");
            	window.location.href="./login.jsp";  
            } 
            
        	var tr=$('#items').find("tr");
        	if(tr.length>2){
				for(var i=2;i<tr.length-1;i++){
					tr.eq(i).remove();
				}        
        	}
        },
        success: function (data) {
        	
        	alert("模板修改成功！");
        }
        });
}
function ajaxImport(){
	var course=$("#course option:selected").val();
    $.ajax({
        type: "POST",
        dataType: "json",
        url: "import",
        data: {course:course,},		
        timeout:6000,
        beforeSend: function () {
        	var tr=$('#items').find("tr");
        	if(tr.length>2){
				for(var i=2;i<tr.length-1;i++){
					tr.eq(i).remove();
				}        
        	}
        },
        success: function (data) {
        	if(data=="{}")
        		alert("所选学科还没有任何模板");
        	else{
	        	var obj=$.parseJSON(data);
	        	var i=0;
	        	for(var d in obj){
	        		var pattern = obj[d];
//	        		if(i==1000) break;
	        		if(d==0){
		        		var tr1=$('#items').find("tr").eq(1); 
		        		tr1.find('textarea.id').remove();
		        		tr1.find('td.item-name').text(pattern.id);
		        		tr1.find('textarea.pcontent').text(pattern.content);
		        		tr1.find('textarea.subject').text(pattern.subject);
		        		tr1.find('textarea.value').text(pattern.value);
		        		tr1.find('textarea.type').text(pattern.type);
		        		tr1.find('textarea.class').text(pattern.myclass);
		        		tr1.find('textarea.usage').text(pattern.usage);
		        		tr1.find('textarea.priority').text(pattern.priority);
	        		}
	        		else{
	        			var text=addText(pattern.id,pattern.content,pattern.subject,pattern.value,pattern.type,pattern.myclass,pattern.usage,pattern.priority);
	        			$(".item-row:last").after(text);
	        		}
	        		i++;
	        	}
        	}
        	 
        },
        error: function (e, jqxhr, settings, exception) {
        	
            $("#imformation").text("请求发生错误...");
        
        }
    })
}
function ajaxDelete(id){
	 $.ajax({
		 type: "POST",
	        dataType: "json",
	        url: "delete",
	        data: {id:id,
	        		course:$("#course option:selected").val()},
	        timeout:600000,
	        success: function (data) {
//	        	 alert("模板删除成功！");
	        },
	        error: function (e, jqxhr, settings, exception) {
	        	
	            alert("请求发生错误...");
	        
	        }
	    })
}
function addText(id,content,subject,value,type,myclass,usage,priority){
	
	var text="<tr class='item-row'>" +
			"<td class='item-name'><div class='delete-wpr'><a class='delete' href='javascript:;' title='Remove row'>X</a></div>"+id+"</td>" +
			"<td><textarea class='pcontent'>"+content+"</textarea></td>" +
			"<td><textarea class='subject'>"+subject+"</textarea></td>" +
			"<td><textarea class='value'>"+value+"</textarea></td>" +
			"<td><textarea class='type'>"+type+"</textarea></td>" +
			"<td><textarea class='class'>"+myclass+"</textarea></td>" +
			"<td><textarea class='usage'>"+usage+"</textarea></td>" +
			"<td><textarea class='priority'>"+priority+"</textarea></td>" +
			"</tr>";
	return text;
	
}
function dataParserA(data, selected) {
	retval = [ { val: "-1" , text: "选择属性" } ];

	data.forEach(function(v){
		if(selected == "-1" && v.val == 3)
			v.selected = true;
		retval.push(v); 
	});

	return retval;
}

function selectProperty(property){
	var type=$('#items').find("tr").find("textarea.type");
	type.each(function(){
		if(property==$(this).text()){
			$(this).closest("tr").show();
		}
		else $(this).closest("tr").hide();
	});
}

function comfirm() {
	
}

