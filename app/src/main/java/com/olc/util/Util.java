package com.olc.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Util {
    static final String  TAG = "Util";
    /**
     * byte[] Convert to hex string</>
     *
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return "";
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v).toUpperCase();
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static String byteToHexString(byte src) {
        StringBuilder stringBuilder = new StringBuilder("");
        int v = src & 0xFF;
        String hv = Integer.toHexString(v).toUpperCase();
        if (hv.length() < 2) {
            stringBuilder.append(0);
        }
        stringBuilder.append(hv);
        return stringBuilder.toString();
    }


    /**
     * byte[4] to int
     * @param byteArray
     * @return
     */
    public static int bytes4ToInt(byte[] byteArray) {
        int n = byteArray[3] & 0xFF |
                (byteArray[2] & 0xFF) << 8 |
                (byteArray[1] & 0xFF) << 16 |
                (byteArray[0] & 0xFF) << 24;
        CLog.d(TAG, "bytes4ToInt : bytes " + bytesToHexString(byteArray) + "-->int " + n);
        return n;
    }

    /**
     * @param byteArray
     * @return
     */
    public static String bytesToStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        String str = new String(byteArray, StandardCharsets.UTF_8);
        return str;
    }

    /**
     * Hexadecimal string conversion byte[]
     * @param hexString
     * @return
     */
    public static byte[] stringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * @param number
     * @return
     */
    public static byte[] shortToByte(short number) {
        byte[] b = new byte[2];
        b[0] = ((byte) ((number>>8)&0xFF));
        b[1] = ((byte) (number & 0xFF));
        return b;
    }

    /**
     * @param number
     * @return
     */
    public static List<Byte> shortToByteList(short number) {
        List<Byte> list = new ArrayList<>();
        list.add((byte) ((number>>8)&0xFF));
        list.add((byte) (number & 0xFF));
        return list;
    }

    /**
     * @param b
     * @return
     */
    public static short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);
        short s1 = (short) (b[1] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }
}
