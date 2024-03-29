package com.olc.web.util;

import java.util.List;

public class StringUtil {

    public  static boolean  isGif(String url){
        if (url == null)
            return  false;

        int index = url.toLowerCase().indexOf(".gif");
        return index > 0;

    }

    public static boolean isEmpty(String str){
        return str == null || str.trim().equals("");
    }
    public static boolean isNotEmpty(String str){
        return str != null && !str.trim().equals("") && !str.trim().equals("null");
    }




    public static boolean isEmpty(List list){
        return list == null || list.size() == 0;
    }
    public static boolean isNotEmpty(List list){
        return list != null && list.size() != 0;
    }


}