package at.jku.risc.uarau.util;

public class ANSI {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    
    public static String red(Object o) {
        return RED + o + RESET;
    }
    
    public static String green(Object o) {
        return GREEN + o + RESET;
    }
    
    public static String yellow(Object o) {
        return YELLOW + o + RESET;
    }
    
    public static String blue(Object o) {
        return BLUE + o + RESET;
    }
}
