/*$.ajax({
    type:'get',
    url:'json/tableData-2.json',
    dataType: "json",
    data: {},
    success: function (data) {
        var html = template("template-mod",data);

        $('#tableData').append(html);
        operating();
        edit();
    }
});
//表格操作
function operating() {
    $("#tableOperating").on('click', '.del', function () {
        //删除当前表格行
        $(this).closest('tr').remove();
        //循环表格重排序号
        $('#tableOperating table tbody tr').each(function () {
            var num = $(this).index() + 1;
            $(this).find('td:eq(0)').text(num)
        })
    });
}
//点击编辑按钮可以编辑表格
function edit() {
    $(".edit").click(function () {
        str = $(this).val() == "编辑" ? "确定" : "编辑";
        $(this).val(str); // 按钮被点击后，在“编辑”和“确定”之间切换
        $(this).parent().siblings("td.editVal").each(function () { // 获取当前行的其他单元格
            obj_text = $(this).find("input:text"); // 判断单元格下是否有文本框
            if (!obj_text.length) // 如果没有文本框，则添加文本框使之可以编辑
                $(this).html("<input type='text' id='editText' value='" + $(this).text() + "'>");
            else // 如果已经存在文本框，则将其显示为文本框修改的值
                $(this).html(obj_text.val());
        });
    });
}*/

var content=[];
$(function(){
	layer.open({
        type: 2, content: '检查中...'
    });
    setTimeout(function () { setTitle(); getlist();}, 200);
});

function setTitle(){
	var title="";
	title=getTitle();
	if(title!=""){
		document.title = getTitle(); 		
	}
    layer.closeAll();
}
function getlist(){
	var list =readfile(AssetType_Json);
	var dateObj=JSON.parse(list);
	content.push(JSON.stringify(dateObj));
	var html="";
	$.each(dateObj,function(i,item){
		html+="<tr class='trlist' id=trlist_"+item.Code+">";
		html+="<td>"+(i+1)+"</td>";
		html+="<td class=\'editVal\' data-bak="+item.Bak+" data-id="+item.Code+" id=td_"+item.Code+"><p id=p_"+item.Code+">"+item.Name+"</p></td>";
		html+="<td>";
        html += "<input type=\'button\' id=btn_" + item.Code + " data-id=" + item.Code + " onclick='edit(this)' value=\'编辑\' value=" + item.Name +"  class=\'edit\' />&nbsp;&nbsp;";
		html+="<a href=\'javascript:void(0);\' data-id="+item.Code+" onclick='del(this)' class=\'del\'>删除</a>";
		html+="</td>";
		html+="</tr>";
	});
	$("#tableData").html(html);
}

var edit=function(obj){
	var id=$(obj).attr("data-id");
	/*if($("#btn_"+id).val()=="编辑"){
		var text =$("#td_"+id).find("p").html();
		$("#td_"+id).find("p").hide();
		var nhtml ="<input type=\'text\' class='editText_New' id=\'editText_"+id+"\' value=\'"+text+"\'>";
		$("#td_"+id).html(nhtml);
		$("#btn_"+id).val("确定");
	}else{
		//("#td_"+id).find("p").show();
		var text = $("#editText_"+id).val();
		if(text!=""){
			//判断名称是否重复
			if(ishasVlalue(text,id))
			{
				var nhtml ="<p id=p_"+id+">"+text+"</p>";
				$("#td_"+id).html(nhtml);
				$("#btn_"+id).val("编辑");
				writeJoson();
			}else{
				layer.open({ content: '已存在的资产名称，请重新编辑'});
				$("#editText_"+id).val("");
				return false;
			}
			//修改原json
		}else{
			layer.open({ content: '请输入资产类型名称'});
			return false;
		}	
	}*/
    window.location.href = "addassettype.html?code=" + id;
};

var del=function(obj){
	var id=$(obj).attr("data-id");
	layer.open({
        content: '您确定删除该项？'
           , btn: ['确认', '取消']
   , yes: function (index)
	   {
		   $("#trlist_"+id).remove();
		   writeJoson();
       layer.closeAll();
		   layer.open({
                   content: '操作成功'
               , btn: '确认'
               , yes: function (index) { window.location.href = "assetType.html" }
            });
		   
		   
	   }
  });
}


//判断是否重复
function ishasVlalue(name,code){
	var list =readfile(AssetType_Json);
	var dateObj=JSON.parse(list);
	var ishas = true;
	$.each(dateObj,function(i,item){
		if(code!=item.Code){
			if(item.Name==name){
				ishas=false;
			}
		}
	});
	return ishas;
}
//重新写入json
function writeJoson(){
	console.info("============写入json")
	var endContent=[];
	$.each($(".editVal"),function(){
			   var id=$(this).attr("data-id");
			   var arr  =
				 {
					 "Code" : id,
					 "Name" : $("#p_"+id).html(),
					 "Bak" : $(this).attr("data-bak")
				 };
				endContent.push(arr);
		   });
    console.info(JSON.stringify(endContent));//保存文件
}



