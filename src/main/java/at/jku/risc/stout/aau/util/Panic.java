package at.jku.risc.stout.aau.util;

/**
 * Throw common exceptions with formatted messages
 */
public class Panic {
    public static IllegalArgumentException arg(String message, Object... args) {
        return new IllegalArgumentException(String.format(message, args));
    }
    
    public static IllegalStateException state(String message, Object... args) {
        return new IllegalStateException(String.format(message, args));
    }
    
    public static ParseException parse(String message, Object... args) {
        return new ParseException(String.format(message, args));
    }
    
    public static class ParseException extends RuntimeException {
        public ParseException(String message) {
            super(message);
        }
    }
}