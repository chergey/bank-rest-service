package org.elcer.accounts.app;


import lombok.experimental.UtilityClass;

@UtilityClass
public class TestUtils {

    public static final boolean TEST = isTest();

    private static boolean isTest() {
        try {
            Class.forName("org.junit.Test");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("org.junit.jupiter.api.Test");
            } catch (ClassNotFoundException e2) {
                return false;
            }
        }
        return true;
    }

}
