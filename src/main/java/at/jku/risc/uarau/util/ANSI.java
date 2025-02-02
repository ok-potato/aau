package at.jku.risc.uarau.util;

import java.util.Arrays;

/**
 * Utility class for colorful logging.
 * <br>
 * Can be toggled globally with {@linkplain ANSI#enabled ANSI.enabled}
 */
public class ANSI {
    public static boolean enabled = true;
    
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    
    public static String red(Object... o) {
        return color(RED, o);
    }
    
    public static String green(Object... o) {
        return color(GREEN, o);
    }
    
    public static String yellow(Object... o) {
        return color(YELLOW, o);
    }
    
    public static String blue(Object... o) {
        return color(BLUE, o);
    }
    
    public static String regular(Object... o) {
        return Data.str(Arrays.asList(o));
    }
    
    private static String color(String color, Object... o) {
        return enabled ? color + Data.str(Arrays.asList(o)) + RESET : Data.str(Arrays.asList(o));
    }
}
