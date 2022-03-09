package com.giro.config;

public class CryptPassword {

    public static String encrypt(String data, String key) {
        return cryptStep2(cryptStep1(data), key);
    }

    private static String cryptStep1(String data) {
        String str = "";
        String str2 = "";
        for (int i = 0; i < data.length(); i++) {
            str2 = "000" + Character.valueOf(data.substring(i, i + 1).toCharArray()[0]).hashCode();
            str = str + str2.substring(str2.length() - 3);
        }
        return str;
    }

    private static String cryptStep2(String data, String key) {

        String str = "";
        int startIndex = 0;

        for (int i = 0; i < data.length(); i++) {

            int num3 = Integer.parseInt(data.substring(i, i + 1));
            var num4 = Integer.parseInt(key.substring(startIndex, startIndex + 1));
            str = str + Integer.toHexString(num3 ^ num4);

            startIndex++;
            if (startIndex >= key.length()) {
                startIndex = 0;
            }

        }
        return str.toUpperCase();
    }

}
