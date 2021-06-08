package com.noid.powermeter.Model;

import java.util.Arrays;

public class UUIDs {
    public static final String UUID_NOTIFY = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static final String UUID_SERVICE = "0000ffe0-0000-1000-8000-00805f9b34fb";

    public static byte[] concat(byte[] bArr, byte[] bArr2) {
        if (bArr != null && bArr2 != null) {
            byte[] copyOf = Arrays.copyOf(bArr, bArr.length + bArr2.length);
            System.arraycopy(bArr2, 0, copyOf, bArr.length, bArr2.length);
            return copyOf;
        }
        return null;
    }

    public static String bytesToHexString(byte[] bArr) {
        StringBuilder sb = new StringBuilder();
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

}
