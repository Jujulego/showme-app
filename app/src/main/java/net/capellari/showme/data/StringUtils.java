package net.capellari.showme.data;

/**
 * Created by julien on 06/02/18.
 *
 * Outils !
 */

public class StringUtils {
    // Title case !
    public static String toTitle(String str) {
        return String.valueOf(Character.toTitleCase(str.charAt(0))) + str.substring(1);
    }
}
