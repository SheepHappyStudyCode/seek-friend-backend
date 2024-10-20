package com.yupi.friend.utils;

/**
 * 与哈希算法有关的工具类
 */
public class HashUtils {

    public static String hexToStr(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(String.format("%02x", b));
        }

        return stringBuilder.toString();
    }

    public static byte[] strToHex(String hexString) {
        byte[] byteArray = new byte[hexString.length() / 2];

        for (int i = 0; i < byteArray.length; i++) {
            int index = i * 2;
            int j = Integer.parseInt(hexString.substring(index, index + 2), 16);
            byteArray[i] = (byte) j;
        }

        return byteArray;
    }


}
