package com.asifapps.oxforddigitalattendance.Utils;

public class QrDecoder {
    public static String[] decodeQr(String code) {
        String[] data;

        try {
            data = code.split(",");

            if (data.length > 2)
                return data;

            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
