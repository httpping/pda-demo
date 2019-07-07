/*$.ajax({
    type:'get',
    url:'json/tableData.json',
    dataType: "json",
    data: {},
    success: function (data) {
        var html = template("template-mod",data);

        $('#tableData').append(html);
        operating();
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
}*/
var iCount = 0;
var content = [];
$(function () {
    layer.open({
        type: 2, content: '检查中...'
    });
    setTimeout(function () { setTitle(); gettype(); }, 200);
});
function setTitle() {
    var title = "";
    title = getTitle();
    if (title != "") {
        document.title = getTitle();
    }
    layer.closeAll();
};
//加载资产类型和表规则数据
var gettype = function () {
    var zclist = readfile(AssetType_Json);
    var dateObj = JSON.parse(zclist);
    var zchtml = "";
    $.each(dateObj, function (i, item) {
        zchtml += "<li><label class='flex-parent'><input data-code=" + item.Code + " class=\'checkbox cbzc\' type=\'checkbox\' id=zccb_" + item.Code + " onclick='zcck(this)'><span class='flex-1'>" + item.Name + "</span></label></li>";
    });
    if (zchtml != "") {
        $(".zclist").html(zchtml);
    }


    var tmlist = readfile(CodeRule_Json);
    dateObj = JSON.parse(tmlist);
    var tmhtml = "";
    $.each(dateObj, function (i, item) {
        tmhtml += "<li><label class='flex-parent'><input data-typeid=" + item.Typeid + " data-cfclen=" + item.CFCLen + " data-start=" + item.StartCode + " data-end=" + item.EndCode + " data-code=" + item.Code + " class=\'checkbox cbgz\' type=\'checkbox\' id=gzcb_" + item.Code + "  onclick='gzck(this)'><span class='flex-1'>" + item.Name + "</span></label></li>";
    });
    if (tmhtml != "") {
        $(".tmlist").html(tmhtml);
    }
};
//rfid软件扫描
var scan = function () {
    var scantext = $(".scantext").html();
    if (scantext == "开始扫描") {
        $("#tableData").html("");
        $(".scantext").html("停止扫描");
        $(".showok,.showexit").show();
        //不停的获取内容
        //iCount =setInterval(fun_callback, 1000); 暂停以前方法
        start_qr();

    } else {
        //发送停止扫描事件
        $(".scantext").html("开始扫描");
        clearInterval(iCount);
    }

    

};
//保存数据
var save = function () {

};

var exit = function () {
    window.location.href = "home.html";
};
//选择规则
var gzck = function (obj) {
    var gzcode = $(obj).attr("data-code");
    //遍历json，根据规则id去获取对于的类型，将其勾选上
    if (list != null) {
        var list = readfile(CodeRule_Json);
        var dateObj = JSON.parse(list);
        $.each(dateObj, function (i, item) {
            if (item.Code == gzcode) {
                var selectobj = $("#zccb_" + item.Typeid);
                if (selectobj.length > 0) {
                    //如果元素存在，则勾选上
                    selectobj.attr("checked", "checked");
                }
            }
        })
    }
    if ($("#tableData").html() != "") {
        fun_callback();
    }
};

var zcck = function (obj) {
    if ($("#tableData").html() != "") {
        fun_callback();
    }
}




//app调用显示页面内容
var fun_callback = function () {
    console.info('ssfff')
    content = [];//清空数组
    var list = readfile(Collect_Json);
    if (list != null) {
        var dateObj = JSON.parse(list);
        $.each(dateObj, function (i, item) {
            if (item.RfidCode != null && item.RfidCode!= "") {
                //获取到的序列号是item.RfidCode
                //先判断条码规则是否有选中
                var islasetCK, isselect = false;
                islasetCK = $("input[type=checkbox].cbgz").is(':checked');
                isselect = $("input[type=checkbox].cbzc").is(':checked');
                if (islasetCK) {//如果条码规则有选中
                    $.each($(".cbgz"), function (z, zi) {
                        if ($(this).is(':checked')) {
                            var startcode = $(this).attr("data-start");
                            var endcode = $(this).attr("data-end");
                            var cfclen = $(this).attr("data-cfclen");
                            var typyid = $(this).attr("data-typeid");
                            var tmpcode = getcode(cfclen, item.RfidCode);
                            var typename = getTypeName(typyid);
                            //console.info("===>返回则typename是：" + typename);
                            if (isHasCode(startcode, endcode, getcode(cfclen, item.RfidCode))) {//判断是否属于区间
                                //判断是否已经存在
                                content.push({ "code": item.RfidCode, "typename": typename })
                            }
                        }
                    });
                }
                //如果类型有被选中
                if (isselect) {
                    $.each($(".cbzc"), function (z, zi) {
                        if ($(this).is(':checked')) {
                            var typyid = $(this).attr("data-code");
                            //去查找规则表，获取所有规则的typeid
                            var gzlist = readfile(CodeRule_Json);
                            if (gzlist != null) {
                                var gzObj = JSON.parse(gzlist);
                                $.each(gzObj, function (a, ai) {
                                    if (ai.Typeid == typyid) {
                                        var startcode = ai.StartCode;
                                        var endcode = ai.EndCode;
                                        var cfclen = ai.CFCLen;
                                        var tmpcode = getcode(cfclen, item.RfidCode);
                                        if (isHasCode(startcode, endcode, getcode(cfclen, item.RfidCode))) {//判断是否属于区间
                                            //判断是否已经存在
                                            content.push({ "code": item.RfidCode, "typename": getTypeName(typyid) })
                                        }
                                    }

                                })

                            };
                        }
                    });
                }
                if (!isselect && !islasetCK) {
                    var alllist = readfile(CodeRule_Json);
                    if (alllist != null) {
                        var allObj = JSON.parse(alllist);
                        $.each(allObj, function (a, ai) {
                            var startcode = ai.StartCode;
                            var endcode = ai.EndCode;
                            var cfclen = ai.CFCLen;
                            var tmpcode = getcode(cfclen, item.RfidCode);
                            if (isHasCode(startcode, endcode, getcode(cfclen, item.RfidCode))) {//判断是否属于区间
                                //判断是否已经存在
                                content.push({ "code": item.RfidCode, "typename": getTypeName(ai.Typeid) })
                            }
                        })

                    };
                }
            }
        })
    };

    console.info("content是:" + JSON.stringify(content));

    var nhtml = "";    
    if (content.length > 0) {
        $.each(content, function (i, item) {
            console.info("code是:" + item.code + "的数据--是否有" + !isHasHtml(item.code));
            if (!isHasHtml(item.code)) {
                nhtml += "<tr data-code=" + item.code + " class='trlist'>";
                nhtml += "                <td>" + (i + 1) + "</td>";
                nhtml += "                <td>" + item.code + "</td>";
                nhtml += "                <td>" + item.typename + "</td>";
                nhtml += "                <td>";
                nhtml += "                    <a href=\'javascript:void(0);\' class=\'del\'>删除</a>";
                nhtml += "                </td>";
                nhtml += "              </tr>";
            }
        });
        if (nhtml != "") {
            $("#tableData").html(nhtml);
        }
    } else {
        $("#tableData").html("");
    }
};

var getcode = function (len, code) {
    code = code.substring(0, len);
    return code;
};
//判断是否在区间范围
var isHasCode=function(startcode,endcode,code)
{
    if (startcode.length < code.length) {
        startcode = PrefixInteger(startcode, (code.length - startcode.length));
    }
    if (endcode.length < code.length) {
        endcode = PrefixInteger(endcode, (code.length - endcode.length));
    }
    if (endcode <= code <= startcode) {
        return true;
    }
    return false;
}
//获取规则类型名称
var getTypeName = function(typeid){
    var list = readfile(AssetType_Json);
    var dateObj = JSON.parse(list);
    var typename = "";
    $.each(dateObj, function (i, item) {
        if (item.Code == typeid) {
            typename= item.Name;
        }
    });
    return typename;
}
//左补0
function PrefixInteger(num, length) {
    return (Array(length).join('0') + num).slice(-length);
}
//判断在页面中是否存在
var isHasHtml = function (incode) {
    var ishas = false;
    $.each($(".trlist"), function () {
        if ($(this).attr("data-code") == incode) {
            ishas=true;
        }
    })
    return ishas;
}