var code = "";
$(function () {

    layer.open({
        type: 2, content: '检查中...'
    });
    setTimeout(function () { gettypelist(); }, 200);
});



var BindDate = function () {
    var list = readfile(CodeRule_Json);
    var dateObj = JSON.parse(list);   

    if (code != "") {
        $.each(dateObj, function (i, item) {
            if (item.Code == code) {
                $("#txtcode").val(item.Code);
                $("#txtname").val(item.Name);
                $("#seltype").val(item.Typeid)
                $("#txtcodelength").val(item.CodeLen);
                $("#txtclassificationcodelength").val(item.CFCLen);
                $("#txtclassificationcodevalue").val(item.CFCValue);
                $("#txtserlength").val(item.SerLen);
                $("#txtstartcode").val(item.StartCode);
                $("#txtendcode").val(item.EndCode);
            }
        });
    } else {
        $.each(dateObj, function (i, item) {
            if (i === dateObj.length - 1) {
                $("#txtcode").val(parseInt(item.Code) + 1);
            }
        });
    }
}

var gettypelist = function () {
    var list = readfile(AssetType_Json);
    var dateObj = JSON.parse(list);
    var html = "";
    $.each(dateObj, function (i, item) {
        html += "<option value=\"" + item.Code + "\">" + item.Name + "</option>";
    });
    $("#seltype").append(html);
    if (getUrlParam("code") != null && getUrlParam("code") != "") {
        code = getUrlParam("code");
        $(".aui-center-title").html("编辑条码规则");
    };
    BindDate();
    layer.closeAll();
};

var ckok = function () {
    var txtcode = $("#txtcode").val();
    var txtname = $("#txtname").val();
    var txtcodelength = $("#txtcodelength").val();
    var txtclassificationcodelength = $("#txtclassificationcodelength").val();
    var txtclassificationcodevalue = $("#txtclassificationcodevalue").val();
    var txtserlength = $("#txtserlength").val();
    var txtstartcode = $("#txtstartcode").val();
    var txtendcode = $("#txtendcode").val();

    if (txtcode == "") {
        layer.open({ content: '请输入编号' });
        return false;
    }
    if (txtname == "") {
        layer.open({ content: '请输入规则名称' });
        return false;
    }
    if ($("#seltype").val() == "-1") {
        layer.open({ content: '请选择所属类型' });
        return false;
    }
    if (txtclassificationcodelength == "") {
        layer.open({ content: '请输入分类码长度' });
        return false;
    } else {
        if (!isNumber(txtclassificationcodelength)) {
            layer.open({ content: '分类码长度格式错误' });
            return false;
        }
    }
    if (txtclassificationcodevalue == "") {
        layer.open({ content: '请输入分类码固定值' });
        return false;
    }
    if (txtstartcode == "") {
        layer.open({ content: '请输入起始序列号' });
        return false;
    }
    if (txtendcode == "") {
        layer.open({ content: '请输入结束序列号' });
        return false;
    }

    var content = [];
    //获取原来数据列表
    var list = readfile(CodeRule_Json);
    var dateObj = JSON.parse(list);

    if (code != "") {//编辑数据
        $.each(dateObj, function (i, item) {
            if (item.Code == code) {
                if (ishasVlalue(txtname, txtcode)) {
                    item.Code = txtcode;
                    item.Name = txtname;
                    item.Typeid = $("#seltype").val();
                    item.CodeLen = txtcodelength;
                    item.CFCLen = txtclassificationcodelength;
                    item.CFCValue = txtclassificationcodevalue;
                    item.SerLen = txtserlength;
                    item.StartCode = txtstartcode;
                    item.EndCode = txtendcode;
                } else {
                    layer.open({ content: '已存在的规则名称，请重新编辑' });
                    $("#editText_" + id).val("");
                    return false;
                }

            }
        });
        content = dateObj;

    } else {//新增数据
        content = dateObj;
        var arr =
        {
            "Code": txtcode,
            "Name": txtname,
            "Typeid": $("#seltype").val(),
            "CodeLen": txtcodelength,
            "CFCLen": txtclassificationcodelength,
            "CFCValue": txtclassificationcodevalue,
            "SerLen": txtserlength,
            "StartCode": txtstartcode,
            "EndCode": txtendcode,
        };
        if (ishasNewVlalue(txtname)) {
            content.push(arr);
        } else {
            layer.open({ content: '已存在的规则名称，请重新编辑' });
            return false;
        }
    }
    console.info(JSON.stringify(content));
    //json数据写入文件
    var result = writefile("coderulelist.json", JSON.stringify(content));
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
    var list = readfile(CodeRule_Json);
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
};
function ishasNewVlalue(name) {
    var list = readfile(CodeRule_Json);
    var dateObj = JSON.parse(list);
    var ishas = true;
    $.each(dateObj, function (i, item) {
        if (item.Name == name) {
            ishas = false;
        }
    });
    return ishas;
}