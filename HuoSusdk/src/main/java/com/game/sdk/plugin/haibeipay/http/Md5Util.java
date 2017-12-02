package com.game.sdk.plugin.haibeipay.http;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by guojb on 2016/8/8.
 */
public class Md5Util {
    public static String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();
            // Create HEX String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String sTmp = Integer.toHexString(0xFF & messageDigest[i]);
                switch (sTmp.length()) {
                    case 0:
                        hexString.append("00");
                        break;
                    case 1:
                        hexString.append("0");
                        hexString.append(sTmp);
                        break;
                    default:
                        hexString.append(sTmp);
                        break;
                }
            }
            return hexString.toString().toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
