package com.olc.web.util;
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

import android.content.Context;

import com.google.gson.Gson;
import com.olc.web.bean.JsResponse;
import com.utils.file.FilePathUtils;
import com.utils.ui.ToastUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;


/**
 * 项目名称: z
 * 类描述：
 * 创建时间:2018/11/12 9:52
 *
 * @author tanping
 */
public class FileOptions {

    public String distory ="WmsPda";

    public String root = "/storage/emulated/0";

    Gson gson = new Gson();

    public static  final  String ERROR_REMIND = "没有发现可用的文件存储器";

    public static  final  String ERROR_FILE_WIRTE = "写文件异常";

    private  static  FileOptions  instance;
    private FileOptions(){}

    /**
     * 单例弱引用
     * @return
     */
    public static FileOptions getInstance(Context context){
        if (instance ==null){
            instance =  new FileOptions();
            instance.root = FilePathUtils.getInstance(context).getVaildStoragePriorityOut();
            if (instance.root == null){
                ToastUtil.showToast(context,ERROR_REMIND);
            }
            return  instance;
        }

        return instance;
    }


    public JsResponse writeFile(String path, String data){

        JsResponse jsResponse = new JsResponse();

        if (root == null){
            jsResponse.code = 404;
            jsResponse.error = ERROR_REMIND;
            return jsResponse;
        }


        try {
            File file = new File(root+"/"+distory );
            file.mkdirs();
            file = new File(root+"/"+distory +"/"+ path);
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            bw.write(data);
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
            jsResponse.code = 500;
            jsResponse.error =ERROR_FILE_WIRTE;
            return jsResponse;
        }
        jsResponse.code = 200;
        return jsResponse;

    }

    public JsResponse readFile(String path){

        JsResponse jsResponse = new JsResponse();

        if (root == null){
            jsResponse.code = 404;
            jsResponse.error = ERROR_REMIND;
            return jsResponse;
        }

        try {
            File file = new File(root+"/"+distory +"/"+ path);
            BufferedReader bw = new BufferedReader(new FileReader(file));
            String content = "";
            while (true) {
               String line = bw.readLine();
               if (line!=null){
                   content +=line;
               }else {
                   break;
               }
            }
            bw.close();
            jsResponse.data = content;
            jsResponse.code = 200 ;
            return jsResponse;
        } catch (Exception e) {
            e.printStackTrace();
            jsResponse.code = 500;
            jsResponse.error ="读文件异常";
            return jsResponse;
        }

    }

}
