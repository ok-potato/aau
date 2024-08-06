package at.jku.risc.uarau.util;

public class MathUtils {
    public static boolean close(double a, double b) {
        System.out.println("double");
        return Math.abs(a - b) < 0.00001f * Math.abs(a + b) / 2;
    }
    
    public static long parseBits(String s) {
        s = s.replaceAll("_", "");
        if (s.length() > 64 || s.chars().anyMatch(c -> c != '1' && c != '0')) {
            throw new NumberFormatException();
        }
        if (s.length() < 64 || s.charAt(0) == '0') {
            return Long.parseLong(s, 2);
        }
        // two's compliment
        s = s.replaceAll("0", "_").replaceAll("1", "0").replaceAll("_", "1");
        return -(Long.parseLong(s, 2) + 1);
    }
}
