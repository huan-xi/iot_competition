package cn.huse.prepare.util;

public class DataCache {
    private static String token;

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        DataCache.token = token;
    }
}
