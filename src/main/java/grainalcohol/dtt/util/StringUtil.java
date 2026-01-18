package grainalcohol.dtt.util;

public class StringUtil {
    public static String warp(String str, String warper) {
        if (str == null || warper == null) return null;
        return warper + str + warper;
    }

    public static String warp(String str) {
        return warp(str, "'");
    }
}
