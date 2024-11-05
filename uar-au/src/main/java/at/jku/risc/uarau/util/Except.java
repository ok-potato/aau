package at.jku.risc.uarau.util;

/**
 * Make throwing common exceptions with formatted messages slightly less verbose.
 */
public class Except {
    public static IllegalArgumentException argument(String message, Object... args) {
        return new IllegalArgumentException(String.format(message, args));
    }
    
    public static IllegalStateException state(String message, Object... args) {
        return new IllegalStateException(String.format(message, args));
    }
}
