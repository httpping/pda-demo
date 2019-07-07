package com.olc.web.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class EnDecode {

    private static char[][][] m_checkTable=new char [][][]{{{  '1', '7', '4', '0', '9', '8', '2', '5', '6', '3' }, { '2', '1', '6', '8', '5', '7', '9', '4', '3', '0' }, { '2', '8', '6', '0', '4', '5', '9', '1', '3', '7' }, { '8', '2', '9', '1', '3', '5', '4', '0', '7', '6' }, { '3', '6', '1', '5', '4', '2', '0', '9', '7', '8' }, { '1', '8', '2', '6', '0', '3', '5', '9', '4', '7' }, { '8', '3', '5', '1', '2', '0', '6', '4', '9', '7' }, { '7', '8', '3', '1', '0', '4', '9', '6', '5', '2' }, { '5', '3', '7', '4', '8', '0', '6', '1', '9', '2' }, { '2', '8', '9', '0', '7', '1', '5', '6', '4', '3' }, { '9', '2', '4', '6', '1', '8', '7', '5', '0', '3' }, { '9', '1', '8', '5', '3', '2', '6', '7', '4', '0' }, { '8', '3', '9', '6', '7', '5', '2', '4', '1', '0' }, { '3', '6', '4', '9', '2', '0', '5', '8', '7', '1' }, { '2', '5', '7', '4', '9', '3', '0', '8', '1', '6' }, { '1', '7', '4', '0', '9', '8', '2', '5', '6', '3' } }, { { '4', '0', '7', '3', '5', '1', '6', '9', '8', '2' }, { '1', '6', '8', '3', '4', '9', '2', '0', '7', '5' }, { '6', '7', '3', '8', '4', '9', '2', '5', '0', '1' }, { '5', '4', '0', '2', '6', '1', '7', '3', '8', '9' }, { '0', '3', '8', '5', '7', '9', '4', '1', '6', '2' }, { '0', '3', '5', '6', '7', '1', '9', '2', '4', '8' }, { '5', '8', '3', '2', '4', '1', '9', '0', '6', '7' }, { '4', '6', '3', '5', '2', '8', '1', '0', '7', '9' }, { '1', '0', '8', '5', '7', '9', '3', '4', '2', '6' }, { '1', '7', '9', '0', '3', '4', '5', '2', '8', '6' }, { '8', '0', '7', '4', '3', '1', '6', '9', '2', '5' }, { '5', '9', '3', '2', '8', '7', '4', '1', '6', '0' }, { '5', '8', '0', '1', '3', '2', '7', '9', '4', '6' }, { '0', '9', '3', '8', '7', '6', '5', '4', '1', '2' }, { '5', '3', '2', '6', '8', '9', '4', '1', '0', '7' }, { '4', '0', '7', '3', '5', '1', '6', '9', '8', '2' } }, { { '6', '0', '2', '4', '3', '8', '7', '9', '5', '1' }, { '4', '3', '6', '9', '2', '1', '8', '0', '5', '7' }, { '0', '1', '4', '8', '9', '3', '5', '7', '2', '6' }, { '9', '3', '8', '1', '0', '4', '5', '7', '6', '2' }, { '1', '4', '9', '5', '2', '3', '6', '8', '7', '0' }, { '4', '9', '6', '0', '3', '2', '5', '8', '1', '7' }, { '5', '7', '8', '2', '0', '9', '6', '1', '4', '3' }, { '5', '1', '8', '4', '0', '7', '2', '9', '3', '6' }, { '7', '8', '3', '6', '4', '1', '9', '5', '0', '2' }, { '3', '2', '4', '6', '9', '0', '1', '5', '8', '7' }, { '7', '9', '5', '4', '0', '3', '1', '2', '8', '6' }, { '9', '8', '5', '6', '0', '7', '2', '4', '1', '3' }, { '2', '1', '6', '3', '8', '0', '5', '4', '7', '9' }, { '3', '4', '7', '8', '1', '6', '2', '5', '0', '9' }, { '2', '4', '9', '1', '5', '7', '3', '0', '8', '6' }, { '6', '0', '2', '4', '3', '8', '7', '9', '5', '1' } }, { { '7', '0', '8', '1', '3', '6', '2', '4', '5', '9' }, { '5', '6', '3', '0', '8', '2', '9', '7', '4', '1' }, { '9', '0', '2', '8', '3', '1', '4', '7', '5', '6' }, { '6', '0', '2', '8', '9', '4', '7', '3', '1', '5' }, { '4', '9', '6', '0', '8', '2', '7', '1', '3', '5' }, { '1', '5', '6', '8', '9', '7', '0', '3', '4', '2' }, { '3', '9', '4', '8', '5', '1', '2', '6', '7', '0' }, { '2', '6', '1', '7', '5', '3', '4', '0', '8', '9' }, { '0', '9', '3', '5', '4', '2', '7', '6', '1', '8' }, { '1', '7', '8', '6', '9', '2', '5', '4', '0', '3' }, { '7', '8', '3', '1', '2', '5', '6', '4', '9', '0' }, { '6', '8', '0', '1', '9', '5', '3', '4', '7', '2' }, { '9', '1', '2', '6', '7', '0', '5', '8', '3', '4' }, { '9', '8', '4', '7', '5', '6', '0', '1', '3', '2' }, { '1', '8', '3', '5', '7', '4', '6', '2', '0', '9' }, { '6', '0', '2', '4', '3', '8', '7', '9', '5', '1' } }, { { '1', '7', '4', '0', '9', '8', '2', '5', '6', '3' }, { '2', '1', '6', '8', '5', '7', '9', '4', '3', '0' }, { '2', '8', '6', '0', '4', '5', '9', '1', '3', '7' }, { '8', '2', '9', '1', '3', '5', '4', '0', '7', '6' }, { '3', '6', '1', '5', '4', '2', '0', '9', '7', '8' }, { '1', '8', '2', '6', '0', '3', '5', '9', '4', '7' }, { '8', '3', '5', '1', '2', '0', '6', '4', '9', '7' }, { '7', '8', '3', '1', '0', '4', '9', '6', '5', '2' }, { '5', '3', '7', '4', '8', '0', '6', '1', '9', '2' }, { '2', '8', '9', '0', '7', '1', '5', '6', '4', '3' }, { '9', '2', '4', '6', '1', '8', '7', '5', '0', '3' }, { '9', '1', '8', '5', '3', '2', '6', '7', '4', '0' }, { '8', '3', '9', '6', '7', '5', '2', '4', '1', '0' }, { '3', '6', '4', '9', '2', '0', '5', '8', '7', '1' }, { '2', '5', '7', '4', '9', '3', '0', '8', '1', '6' }, { '1', '7', '4', '0', '9', '8', '2', '5', '6', '3' }  }};

    public static String DecryptTagData(String dtagdata)
    {
        if ((dtagdata == null) || (dtagdata.length() < 0x18))
        {
            return dtagdata;
        }
        if (dtagdata.length() >= 0x18)
        {
            dtagdata = dtagdata.substring(0, 0x18);
        }
        dtagdata = dtagdata.toUpperCase();
        if (dtagdata.indexOf("EB90") > 0)
        {
            return dtagdata;
        }
        if (dtagdata.indexOf("EB90")<0){
            return dtagdata;
        }

        ArrayList list = new ArrayList();
        for (int i = 4; i < 0x17; i += 2)
        {
            //byte num5 =Convert.ToByte(dtagdata.substring(i, 2), 0x10);
            //byte num5 =Byte.parseByte(dtagdata.substring(i, i + 2), 0x10);
            byte num5 =(byte)Integer.valueOf(dtagdata.substring(i, i + 2), 16).byteValue();

            list.add(num5);
        }
        byte num = (byte) list.get(8);
        byte num2 = (byte) list.get(9);
        //if (num != ~num2)
        //if((byte)list.get(8)+(byte)list.get(9)!=0xff)
        if(Integer.valueOf(Integer.toHexString(num+num2& 0xFF),16)!=0xff)
        {
            System.out.println("两个校验位出错--非取反关系！");
            return null;
        }
        num = (byte) list.get(0);
        for (int j = 1; j < 8; j++)
        {
            num2 = (byte) list.get(j);
            num = (byte) (num ^ num2);
        }
        if (num != ((byte) list.get(8)))
        {
            System.out.println("校验出错--校验位不是异或的结果！");
            return null;
        }
        String str = "";
        for (int k = 0; k < 8; k++)
        {
            num2 = (byte) list.get(k);
            num = (byte)~num2;
            num = (byte) (num ^ 0x55);
            // new String(num,0x10)Convert.ToString(num, 0x10)
            //String.valueOf(num);
            String str4 =Integer.toHexString(num& 0xFF);
            if (str4.length() == 1)
            {
                str4 = "0" + str4;
            }
            str = str + str4;
        }
        if ((str == null) || (str.length() != 0x10))
        {
            System.out.println("出错！算出来不是16位！");
            return null;
        }
        int num3 =Integer.parseInt(str.substring(0, 1));
        if ((num3 > 4) || (num3 < 0))
        {
            System.out.println("出错！算出来查表的号是 " + num3);
            return null;
        }
        String str2 = "";
        boolean flag = true;
        for (int m = 1; m < str.length(); m++)
        {
            for (int n = 0; n < 10; n++)
            {
                if (String.valueOf(m_checkTable[num3][m][n]).equals(str.substring(m,m+1)))
                {
                    str2 = str2 + String.valueOf(n);
                    flag = false;
                    break;
                }
            }
            if (flag)
            {
                break;
            }
        }
        if (flag)
        {
            System.out.println("出错！查对照表出错！");
            return null;
        }
        return str2;
    }


    public static String EncryptTagData(String tagdata, Boolean isOld)
    {
        if (tagdata == null)
        {
            return null;
        }
        int length = tagdata.length();
        if (length != 15)
        {
            return null;
        }
        int num2 = new Random(4).nextInt();
        String str = null;
        if (isOld)
        {
            str = "0" + tagdata;
        }
        else
        {
            for (int m = 1; m <= length; m++)
            {
                try
                {
                    char ch = tagdata.charAt(m - 1);
                    str = str + String.valueOf(m_checkTable[num2][ m][Integer.valueOf(ch)]);
                }
                catch(Exception ex)
                {
                    return null;
                }
            }
            str = String.valueOf(num2) + str;
        }
        ArrayList list = new ArrayList();
        for (int i = 0; i < str.length(); i += 2)
        {
            int num3 =Integer.getInteger(str.substring(i, i+1)) * 0x10;
            int num4 =Integer.getInteger(str.substring(i + 1, i + 1+1));
            int num9 = (num3 + num4) ^ 0x55;
            byte num10 = (byte) ~num9;
            list.add(num10);
        }
        if (list.size() != 8)
        {
            System.out.println("!!!!!!!!!!!!!!!!!!!");
        }
        byte num5 = (byte) list.get(0);
        for (int j = 1; j < 8; j++)
        {
            byte num6 = (byte) list.get(j);
            num5 = (byte) (num5 ^ num6);
        }
        list.add(num5);
        num5 = (byte)~num5;
        list.add(num5);
        String str2 = "";
        for (int k = 0; k < list.size(); k++)
        {
            // Convert.ToString((byte) list.get(k), 0x10)
            String str4 =String.valueOf((byte) list.get(k));
            if (str4.length() == 1)
            {
                str4 = "0" + str4;
            }
            str2 = str2 + str4;
        }
        return ("EB90" + str2.toUpperCase());
    }

    /**
     * 字符串转换成十六进制值
     * @param bin String 我们看到的要转换成十六进制的字符串
     * @return
     */
    public static String bin2hex(String bin) {
        char[] digital = "0123456789ABCDEF".toCharArray();
        StringBuffer sb = new StringBuffer("");
        byte[] bs = bin.getBytes();
        int bit;
        for (int i = 0; i < bs.length; i++) {
            bit = (bs[i] & 0x0f0) >> 4;
            sb.append(digital[bit]);
            bit = bs[i] & 0x0f;
            sb.append(digital[bit]);
        }
        return sb.toString();
    }

    /**
     * 十六进制转换字符串
     * @param hex String 十六进制
     * @return String 转换后的字符串
     */
    public static String hex2bin(String hex) {
        String digital = "0123456789ABCDEF";
        char[] hex2char = hex.toCharArray();
        byte[] bytes = new byte[hex.length() / 2];
        int temp;
        for (int i = 0; i < bytes.length; i++) {
            temp = digital.indexOf(hex2char[2 * i]) * 16;
            temp += digital.indexOf(hex2char[2 * i + 1]);
            bytes[i] = (byte) (temp & 0xff);
        }
        return new String(bytes);
    }

    /**
     * java字节码转字符串
     * @param b
     * @return
     */
    public static String byte2hex(byte[] b) { //一个字节的数，

        // 转成16进制字符串

        String hs = "";
        String tmp = "";
        for (int n = 0; n < b.length; n++) {
            //整数转成十六进制表示

            tmp = (java.lang.Integer.toHexString(b[n] & 0XFF));
            if (tmp.length() == 1) {
                hs = hs + "0" + tmp;
            } else {
                hs = hs + tmp;
            }
        }
        tmp = null;
        return hs.toUpperCase(); //转成大写

    }

    /**
     * 字符串转java字节码
     * @param b
     * @return
     */
    public static byte[] hex2byte(byte[] b) {
        if ((b.length % 2) != 0) {
            throw new IllegalArgumentException("长度不是偶数");
        }
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个进制字节

            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        b = null;
        return b2;
    }

    public static void main(String[] args) {
        String encrypt=EncryptTagData("EB90BBCFAAFEBB2",false);

        System.out.println(encrypt);

        String decrypt=DecryptTagData("EB90BBCFAAFEBB2FF3A2E51A");

        System.out.println(decrypt);
    }


}
