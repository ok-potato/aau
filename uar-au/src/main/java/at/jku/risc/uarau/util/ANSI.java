package at.jku.risc.uarau.util;

public class ANSI {
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    
    public static boolean ansi = false;
    
    public static String red(Object o) {
        return ansi ? RED + o + RESET : o.toString();
    }
    
    public static String green(Object o) {
        return ansi ? GREEN + o + RESET : o.toString();
    }
    
    public static String yellow(Object o) {
        return ansi ? YELLOW + o + RESET : o.toString();
    }
    
    public static String blue(Object o) {
        return ansi ? BLUE + o + RESET : o.toString();
    }
}
