package com.olc.web.bean;
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

/**
 * 项目名称: z
 * 类描述：
 * 创建时间:2018/11/12 9:58
 *
 * @author tanping
 */
public class JsResponse {

    /**
     * 200 成功
     */
    public int code = 200;

    /**
     * 错误 提醒
     */
    public String error ;

    /**
     * 结果
     */
    public String data;


    public boolean issuccess(){
        if (code == 200){
            return true;
        }


        return false;
    }
}
