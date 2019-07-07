var content = [];
$(function () {
    layer.open({
        type: 2, content: '检查中...'
    });
    setTimeout(function () { setTitle(); getlist(); }, 200);
});

function setTitle() {
    var title = "";
    title = getTitle();
    if (title != "") {
        document.title = getTitle();
    }
    layer.closeAll();
}
function getlist() {
    var list = readfile(CodeRule_Json);
    var dateObj = JSON.parse(list);
    content.push(JSON.stringify(dateObj));
    var html = "";
    $.each(dateObj, function (i, item) {
        html += "<tr class='trlist' id=trlist_" + item.Code + ">";
        html += "<td>" + (i + 1) + "</td>";
        html += "<td class=\'editVal\' data-name=" + item.Name + " data-typeid=" + item.Typeid + " data-codelen=" + (item.CodeLen == "" ? "0" : item.CodeLen) + " data-cfclen=" + (item.CFCLen == "" ? "0" : item.CFCLen) + " data-cfcvalue=" + item.CFCValue + " data-serlen=" + (item.SerLen == "" ? "0" : item.SerLen)
            + "data-startcode=" + item.StartCode + " data-endcode=" + item.EndCode + " data-id=" + item.Code + " id=td_" + item.Code + ">" + item.Name + "<br/>" + item.StartCode + "至" + item.EndCode + "</td>";
        html += "<td>";
        html += "<input type=\'button\' id=btn_" + item.Code + " data-id=" + item.Code + " onclick='edit(this)' value=\'编辑\'  class=\'edit\' />&nbsp;&nbsp;";
        html += "<a href=\'javascript:void(0);\' data-id=" + item.Code + " onclick='del(this)' class=\'del\'>删除</a>";
        html += "</td>";
        html += "</tr>";
    });
    $("#tableData").html(html);
}

var edit = function (obj) {
    var id = $(obj).attr("data-id");
    window.location.href = "AddCodeRule.html?code=" + id;
};

var del = function (obj) {
    var id = $(obj).attr("data-id");
    layer.open({
        content: '您确定删除该项？'
           , btn: ['确认', '取消']
   , yes: function (index) {
       $("#trlist_" + id).remove();
       writeJoson();
       layer.closeAll();
       return false;
       layer.open({
           content: '操作成功'
           , btn: '确认'
           , yes: function (index) { window.location.href = "assetType.html" }
       });


   }
    });
}


//判断是否重复
function ishasVlalue(name, code) {
    var list = readfile(AssetType_Json);
    var dateObj = JSON.parse(list);
    var ishas = true;
    $.each(dateObj, function (i, item) {
        if (code != item.Code) {
            if (item.Name == name) {
                ishas = false;
            }
        }
    });
    return ishas;
}
//重新写入json
function writeJoson() {
    var endContent = [];
    $.each($(".editVal"), function (i,item) {
        var id = $(item).attr("data-id");
        var arr =
        {
            "Code": $(item).attr("data-id"),
            "Name": $(item).attr("data-name"),
            "Typeid": $("#seltype").val(),
            "CodeLen": $(item).attr("data-codelen") == "0" ? "" : $(item).attr("data-codelen"),
            "CFCLen": $(item).attr("data-cfclen") == "0" ? "" : $(item).attr("data-cfclen"),
            "CFCValue": $(item).attr("data-cfcvalue"),
            "SerLen": $(item).attr("data-serlen") == "0" ? "" : $(item).attr("data-serlen"),
            "StartCode": $(item).attr("data-startcode"),
            "EndCode": $(item).attr("data-endcode"),
        };
        endContent.push(arr);
    });
    console.info(JSON.stringify(endContent));//保存文件
}