package at.jku.risc.uarau.util;

public class _Math {
    public static boolean close(double a, double b) {
        return Math.abs(a - b) < 0.00001f * Math.abs(a + b) / 2;
    }
    
    // this has nothing to do with anything, but it's kind of cool
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
