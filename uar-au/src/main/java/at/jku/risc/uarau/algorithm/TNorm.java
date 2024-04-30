package at.jku.risc.uarau.algorithm;

public interface TNorm {
    default float apply(float a, float b) {
        return Math.min(a, b);
    }
}
