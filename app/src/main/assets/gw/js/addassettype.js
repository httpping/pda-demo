var content = [];
var EndObj = null;
var code = "";
$(function(){
	layer.open({
        type: 2, content: '检查中...'
    });
    setTimeout(function () { getAssetTypeList(); }, 200);
});

function getAssetTypeList(){
	//先判断是否有数据文件存储
	var list =readfile(AssetType_Json);
	var dateObj=JSON.parse(list);
    content.push(JSON.stringify(dateObj));
    if (getUrlParam("code") != null && getUrlParam("code") != "") {
        code = getUrlParam("code");
        $(".aui-center-title").html("编辑资产类型");
    }
    BindDate();
    layer.closeAll();
	
};

var BindDate = function () {
    var list = readfile(AssetType_Json);
    if (list != null) {
        var dateObj = JSON.parse(list);
        content.push(JSON.stringify(dateObj));
        if (code != "") {
            $.each(dateObj, function (i, item) {
                if (item.Code == code) {
                    $("#typecode").val(item.Code);
                    $("#typename").val(item.Name);
                    $("#typebak").val(item.Bak)
                }
            });
        } else {
            $.each(dateObj, function (i, item) {
                if (i === dateObj.length - 1) {
                    $("#typecode").val(parseInt(item.Code) + 1);
                }
            });
        }
    }
    else {
        $("#typecode").val(1);
    }
    
}

var ckok = function () {
    var txtcode = $("#typecode").val();
    var typename = $("#typename").val();
    var typebak = $("#typebak").val();

    if (txtcode == "") {
        layer.open({ content: '系统错误，无法生成资产类型编码' });
        return false;
    }
    if (typename == "") {
        layer.open({ content: '请输入资产类型' });
        return false;
    }
    var list = readfile(AssetType_Json);

    if(list!=null){

    }
    var dateObj = JSON.parse(list);
    if (code != "") {//编辑数据


    } else {//新增数据
        content = dateObj;
        var arr =
        {
            "Code": txtcode,
            "Name": typename,
            "Bak": typebak
        };
        if (ishasNewVlalue(typename)) {
            content.push(arr);
        } else {
            layer.open({ content: '已存在的规则名称，请重新编辑' });
            return false;
        }
    }

    console.info(JSON.stringify(content));

    var result = writefile("assettype.json", JSON.stringify(content));
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
};

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
function ishasNewVlalue(name) {
    var list = readfile(AssetType_Json);
    if (list != null) {
        var dateObj = JSON.parse(list);
        var ishas = true;
        $.each(dateObj, function (i, item) {
            if (item.Name == name) {
                ishas = false;
            }
        });
        return ishas;
    }
    return true;
}

