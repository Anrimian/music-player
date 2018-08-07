package com.github.anrimian.simplemusicplayer.domain.utils;


public class TextUtils {

    /**
     * copied from android package for using in non-device specific layers
     */
    public static boolean equals(CharSequence a, CharSequence b) {
        if (a == b) return true;
        int length;
        if (a != null && b != null && (length = a.length()) == b.length()) {
            if (a instanceof String && b instanceof String) {
                return a.equals(b);
            } else {
                for (int i = 0; i < length; i++) {
                    if (a.charAt(i) != b.charAt(i)) return false;
                }
                return true;
            }
        }
        return false;
    }

    /**
     * copied from android package for using in non-device specific layers
     */
    public static boolean isEmpty(CharSequence s) {
        return s == null || s.length() == 0;
    }

    //oauth2://mrmobile/v1/authorization#access_token=aab6118993c009530a1bb4d9f728a51b53c0d508&expires_in=3600&token_type=Bearer&scope=phone&state=state
    public static String between(String source, String start, String end) {
        int startIndex = indexOfEnd(source, start);
        return source.substring(startIndex, source.indexOf(end, startIndex));
    }

    private static int indexOfEnd(String text, String source) {
        return text.indexOf(source) + source.length();
    }
}
