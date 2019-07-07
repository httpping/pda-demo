//rem 配置
(function(doc, win){
    var docEl = doc.documentElement,
        resizeEvt = 'orientationchange' in window ? 'orientationchange': 'resize',
        recalc = function(){
            var clientWidth = docEl.clientWidth;
            if (!clientWidth) return;
            if(clientWidth>=750){
                docEl.style.fontSize = 100 + 'px';
            }else{
                docEl.style.fontSize = 100 * (clientWidth / 750) + 'px';
            }
        };
    if (!doc.addEventListener) return;
    win.addEventListener(resizeEvt, recalc, false);
    doc.addEventListener('DOMContentLoaded', recalc, false);
})(document, window);

$(function () {
    //选择下拉
    $('.retrie dt a').click(function(){
        var $t=$(this);
        if($t.hasClass('up')){
            $(".retrie dt a").removeClass('up');
            $('.downlist').hide();
            $('.mask').hide();
        }else{
            $(".retrie dt a").removeClass('up');
            $('.downlist').hide();
            $t.addClass('up');
            $('.downlist').eq($(".retrie dt a").index($(this)[0])).show();
            $('.mask').show();
        }
    });
    $(".area ul li a:contains('"+$('#area').text()+"')").addClass('selected');
    $(".wage ul li a:contains('"+$('#wage').text()+"')").addClass('selected');

});
//判断是否正整数
function isNumber(value) {         //验证是否为数字
    var patrn = /^\+?[1-9][0-9]*$/;
    if (patrn.exec(value) == null || value == "") {
        return false
    } else {
        return true
    }
};
//写入文件
function writefile(filename,filedata){
    console.info('writefile')
	var params ={"action":"write_file","path":filename,"data":filedata};
    var r = tp.invokeJava(JSON.stringify(params));
    if (r != null) {
        var dataobj = JSON.parse(r);
        if (dataobj != null) {
            if (dataobj.code == "200") {
                return "success";
            } else {
                return "系统错误，错误原因是:" + dataobj.error;
            }
        }
    }
}

//读取文件
function readfile(filename){
	/*var result="";
    $.ajax({
        url: filename,
        data: {"t":Math.random(5)},
		   type: "GET",
		   async:false,
		   dataType: "json", 
		   success: function(data) {
			   if(data!=null && data!=""){
				   result= JSON.stringify(data);
			   }
			}	
		});
	return result;	*/

    var params ={"action":"read_file","path":filename};
    var r = tp.invokeJava(JSON.stringify(params));
    if (r != null) {
        var dataobj = JSON.parse(r);
        if (dataobj != null) {
            console.info(JSON.stringify(dataobj))
            if (dataobj.code == 200) {
                return JSON.stringify(dataobj.data);
            } else {
                return null;
            }
        }
    }
    
};


var start_qr = function () {
    var params = { "action": "start_qr", "path": "" };
    var r = tp.invokeJava(JSON.stringify(params));
    return r;
}


function getTitle(){
	var title="";
	var list =readfile(Name_Json);
	var dateObj=JSON.parse(list);
	$.each(dateObj,function(i,item){
		title =item.Name;
	});	
	return title;
};



//获取当前时间，格式YYYY-MM-DD
function getNowFormatDate() {
        var date = new Date();
        var seperator1 = "-";
        var year = date.getFullYear();
        var month = date.getMonth() + 1;
        var strDate = date.getDate();
        if (month >= 1 && month <= 9) {
            month = "0" + month;
        }
        if (strDate >= 0 && strDate <= 9) {
            strDate = "0" + strDate;
        }
        var currentdate = year + seperator1 + month + seperator1 + strDate;
        return currentdate;
}
function getUrlParam(name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)"); //构造一个含有目标参数的正则表达式对象
    var r = window.location.search.substr(1).match(reg); //匹配目标参数
    if (r != null) return unescape(r[2]); return null; //返回参数值
}
function Getuuid() {
    var s = [];
    var hexDigits = "0123456789abcdef";
    for (var i = 0; i < 36; i++) {
        s[i] = hexDigits.substr(Math.floor(Math.random() * 0x10), 1);
    }
    s[14] = "4";  // bits 12-15 of the time_hi_and_version field to 0010
    s[19] = hexDigits.substr((s[19] & 0x3) | 0x8, 1);  // bits 6-7 of the clock_seq_hi_and_reserved to 01
    s[8] = s[13] = s[18] = s[23] = "-";
    var uuid = s.join("");
    return uuid;
};

function setCss(id) {
    var sr = document.getElementById(id);
    var len = sr.value.length;
    setSelectionRange(sr,0,1); //将光标定位到文本最后 
}

function setSelectionRange(input, selectionStart, selectionEnd) {
    if (input.setSelectionRange) {
        input.focus();
        input.setSelectionRange(selectionStart, selectionEnd);
    }
    else if (input.createTextRange) {
        var range = input.createTextRange();
        range.collapse(true);
        range.moveEnd('character', selectionEnd);
        range.moveStart('character', selectionStart);
        range.select();
    }
};

var selectRange = function (start, end) {
    return this.each(function () {
        if (this.setSelectionRange) {
            this.focus();
            this.setSelectionRange(start, end);
        } else if (this.createTextRange) {
            var range = this.createTextRange();
            range.collapse(true);
            range.moveEnd('character', end);
            range.moveStart('character', start);
            range.select();
        }
    });
}; 

