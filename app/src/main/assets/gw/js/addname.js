var content=[];
var EndObj=null;
$(function(){
	layer.open({
        type: 2, content: '检查中...'
    });
    setTimeout(function () { setTitle(); }, 200);
});

function setTitle(){
	var title="";
	title=getTitle();
	if(title!=""){
		document.title = getTitle(); 
		$("#txtname").val(title);
		content=JSON.stringify(title);
	}
	layer.closeAll();
}


var ckok=function(){
	var Name=$("#txtname").val();
		if(Name==""){
			layer.open({ content: '请输入系统名称'});
			return false;
		}
		var arr  =
		 {
			 "Name" : $('#txtname').val()
		 };
	content=arr;
    console.info(JSON.stringify(content));//保存文件
    var result = writefile("name.json", JSON.stringify(content));
    if (result == "success") {
        layer.open({
            content: '操作成功'
            , btn: '确认'
            , yes: function (index) { window.location.href = "home.html" }
        });
    } else {
        layer.open({ content: result });
        return false;
    }
	
}

