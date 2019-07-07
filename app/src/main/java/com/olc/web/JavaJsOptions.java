package com.olc.web;
/*

                   _ooOoo_
                  o8888888o
                  88" . "88
                  (| -_- |)
                  O\  =  /O
               ____/`---'\____
             .'  \\|     |//  `.
            /  \\|||  :  |||//  \
           /  _||||| -:- |||||-  \
           |   | \\\  -  /// |   |
           | \_|  ''\---/''  |   |
           \  .-\__  `-`  ___/-. /
         ___`. .'  /--.--\  `. . __
      ."" '<  `.___\_<|>_/___.'  >'"".
     | | :  `- \`.;`\ _ /`;.`/ - ` : | |
     \  \ `-.   \_ __\ /__ _/   .-` /  /
======`-.____`-.___\_____/___.-`____.-'======
                   `=---='
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
         佛祖保佑       永无BUG

*/

import android.app.Activity;
import android.content.Intent;
import android.webkit.JavascriptInterface;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.olc.reader.MainActivity;
import com.olc.web.bean.JsRequest;
import com.olc.web.bean.JsResponse;
import com.olc.web.util.FileOptions;
import com.utils.ui.ToastUtil;



/**
 * 项目名称: z
 * 类描述：
 * 创建时间:2018/11/12 9:31
 *
 * @author tanping
 */
public class JavaJsOptions {

    private  Activity activity;
    Gson gson = new Gson();

    public JavaJsOptions(Activity activity){
        this.activity = activity;
        invokeJava("test");
    }

    @JavascriptInterface
    public String invokeJava(String data){
        JsResponse response  = new JsResponse();
        try {
            JsRequest request=  gson.fromJson(data,JsRequest.class);
            if (request.action.equals(JsRequest.write_file)){
                response = FileOptions.getInstance(activity).writeFile(request.path,request.data);
            }else if (request.action.equals(JsRequest.read_file)){
                response = FileOptions.getInstance(activity).readFile(request.path);
            }else if (request.action.equals(JsRequest.start_qr)){
                Intent intent = new Intent(activity,MainActivity.class);
                activity.startActivityForResult(intent,JsRequest.start_qr_request);
            }
            return gson.toJson(response);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
            response.code = 500;
            response.error = "参数异常";
        }catch (Exception e){
            response.code = 500;
            response.error = "内部错误";
        }

        return gson.toJson(response);

//        try {
//            JsResponse result = FileOptions.getInstance(activity).writeFile("abc1.json", "xxx===谭平最帅");
//            ToastUtil.showToast(activity,result.code+"");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//        return data + " hello world! ";
    }
}
