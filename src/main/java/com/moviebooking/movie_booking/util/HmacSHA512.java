package com.moviebooking.movie_booking.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class HmacSHA512 {

    public static String hash(String key, String data) throws Exception {

        Mac mac = Mac.getInstance("HmacSHA512");

        SecretKeySpec secretKey =
                new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");

        mac.init(secretKey);

        byte[] rawHmac = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hex = new StringBuilder();

        for (byte b : rawHmac) {
            String s = Integer.toHexString(0xff & b);
            if (s.length() == 1) hex.append('0');
            hex.append(s);
        }

        return hex.toString();
    }
}