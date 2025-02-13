package at.jku.risc.aau.tnorm;

/**
 * Based on <a href="https://en.wikipedia.org/wiki/T-norm#Prominent_examples">this Wikipedia section on prominent TNorms</a>
 * as of 1/31/2025
 */
public class CommonTNorms {
    public static final TNorm minimum = Math::min;
    public static final TNorm product = (a, b) -> a * b;
    public static final TNorm lukasiewicz = (a, b) -> Math.max(0, a + b - 1);
    public static final TNorm drastic = (a, b) -> a == 1 ? b : b == 1 ? a : 0;
    public static final TNorm nilpotentMinimum = (a, b) -> a + b > 1 ? Math.min(a, b) : 0;
    public static final TNorm hamacherProduct = (a, b) -> a == 0 && b == 0 ? 0 : (a*b) / (a + b - a*b);
}
