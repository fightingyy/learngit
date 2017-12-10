/**
 * 
 */
$(document).ready(function () {
	
	$('#course').change(function() {
		var course=$("#course option:selected").val();
		listFile(course);
	});
	var userCourse=$("#userInfo").find("#userCourse").text();
	if(userCourse!="all"&&course!=userCourse){
		listFile(userCourse);
	}
	else listFile("chinese");
});

function listFile(course){
	
	$.ajax({
        type: "POST",
        secureuri:false,
        dataType: "json",
        url: "showResult",
        data: {course:course,},
        timeout:30000,
        beforeSend: function () {            
        	$("#fileList").empty();                
        },
        success: function (data) {

        	 var list = $.parseJSON(data).list;
        	 var path = $.parseJSON(data).path;
        	 for(var d in list){
        		 $("#fileList").append("<li><a target='_blank' href='"+path+"\\resources\\TestResults\\"+course+"\\"+list[d]+"'>"+list[d]+"</a></li>");
        	 }	
        },
        error: function (e, jqxhr, settings, exception) {
       	 	
        }
        
    });
}
