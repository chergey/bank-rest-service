package org.elcer.accounts.app;


import lombok.experimental.UtilityClass;

@UtilityClass
public class TestUtils {

    public static final boolean TEST = isTest();

    private static boolean isTest() {
        try {
            Class.forName("org.junit.Assert");
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }
}
