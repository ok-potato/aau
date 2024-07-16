package at.jku.risc.uarau;

import java.util.ArrayDeque;
import java.util.Deque;

public class Util {
    public static <T> Deque<T> copy(Deque<T> original) {
        Deque<T> copy = new ArrayDeque<>(original.size());
        for (T aut : original) {
            copy.push(aut);
        }
        return copy;
    }
}
