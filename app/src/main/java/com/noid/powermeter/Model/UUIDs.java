package com.noid.powermeter.Model;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class UUIDs {
    public static int LOCALE = 0;
    public static final String UUID_NOTIFY = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static final String UUID_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";

    public static String doubleToString(double d) {
        return new DecimalFormat("0.00").format(d);
    }

    public static <T> byte[] concat(byte[] bArr, byte[] bArr2) {
        byte[] copyOf = Arrays.copyOf(bArr, bArr.length + bArr2.length);
        System.arraycopy(bArr2, 0, copyOf, bArr.length, bArr2.length);
        return copyOf;
    }

    public static String bytesToHexString(byte[] bArr) {
        StringBuilder sb = new StringBuilder("");
        if (bArr == null || bArr.length <= 0) {
            return null;
        }
        for (byte b : bArr) {
            String hexString = Integer.toHexString(b & 255);
            if (hexString.length() < 2) {
                sb.append(0);
            }
            sb.append(hexString);
        }
        return sb.toString();
    }

    public static String getTime() {
        return new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new Date(System.currentTimeMillis()));
    }

    public static String getTimeDay() {
        return new SimpleDateFormat("yyyy年MM月dd日").format(new Date(System.currentTimeMillis()));
    }

    public static String getTimeMonth() {
        return new SimpleDateFormat("yyyy年MM月").format(new Date(System.currentTimeMillis()));
    }

    public static String millisToTimeMonth(long j) {
        return new SimpleDateFormat("yyyy年MM月").format(new Date(j * 1000));
    }

    public static String millisToTimeDay(long j) {
        return new SimpleDateFormat("yyyy年MM月dd日").format(new Date(j * 1000));
    }

    public static String millisToTime(long j) {
        return new SimpleDateFormat("MM月dd日 HH:mm:ss").format(new Date(j * 1000));
    }

    public static byte[] long2bytes(long j) {
        return new byte[]{(byte) ((int) (j & 255)), (byte) ((int) ((j & 65280) >> 8)), (byte) ((int) ((j & 16711680) >> 16)), (byte) ((int) ((j & -16777216) >> 24))};
    }
}
