package org.elcer.accounts.utils;

import lombok.experimental.UtilityClass;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

@UtilityClass
public final class RandomUtils {

    public static int getGtZeroRandom() {
        return ThreadLocalRandom.current().nextInt(100, 1000);
    }

    public static int getGtZeroRandomL() {
        return ThreadLocalRandom.current().nextInt(100, 10000);
    }


    public static int getGtZeroRandom(int hbound) {
        return ThreadLocalRandom.current().nextInt(1, hbound);
    }

    public static int getGtZeroRandom(int lbound, int hbound) {
        return ThreadLocalRandom.current().nextInt(lbound, hbound);
    }

    private static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String lower = upper.toLowerCase(Locale.ROOT);
    private static final char[] alphanum = (upper + lower).toCharArray();


    public String nextString(int len) {
        final char[] buf = new char[len];
        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = alphanum[ThreadLocalRandom.current().nextInt(alphanum.length)];
        return new String(buf);
    }

}
