package at.jku.risc.uarau.util;

public class MathUtil {
    public static boolean close(double a, double b) {
        return Math.abs(a - b) < 0.00001f * Math.abs(a + b) / 2;
    }
}
