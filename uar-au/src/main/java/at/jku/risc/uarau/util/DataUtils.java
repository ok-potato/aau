package at.jku.risc.uarau.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class DataUtils {
    // collection operations
    
    public static <T> void pad(List<T> list, Supplier<T> supplier, int newSize) {
        for (int i = list.size(); i < newSize; i++) {
            list.add(supplier.get());
        }
    }
    
    public static <T> Set<T> conjunction(Set<T> a, Set<T> b) {
        Set<T> s = new HashSet<>(a);
        s.addAll(b);
        return s;
    }
    
    public static <T> boolean unique(Collection<T> collection) {
        return unique(collection, Function.identity());
    }
    
    public static <T, Key> boolean unique(Collection<T> collection, Function<T, Key> extractKey) {
        Set<Key> keySet = new HashSet<>();
        for (T t : collection) {
            Key key = extractKey.apply(t);
            if (keySet.contains(key)) {
                return false;
            }
            keySet.add(key);
        }
        return true;
    }
    
    public static <T> Deque<T> newArrayDeque(Deque<T> original) {
        Deque<T> copy = new ArrayDeque<>(original.size());
        for (T aut : original) {
            copy.addLast(aut);
        }
        return copy;
    }
    
    public static <T> T getAny(Set<T> set) {
        return set.stream().findFirst().orElseThrow(IllegalArgumentException::new);
    }
    
    // stringify
    
    public static <T> String joinString(Collection<T> collection) {
        return joinString(collection, ", ", "..");
    }
    
    public static <T> String joinString(Collection<T> collection, String separator, String empty) {
        return joinString(collection, separator, empty, "", "");
    }
    
    public static <T> String joinString(Collection<T> collection, String separator, String empty, String open, String close) {
        if (collection.isEmpty()) {
            return empty;
        }
        StringJoiner joiner = new StringJoiner(separator);
        collection.forEach(t -> joiner.add(t.toString()));
        return open + joiner + close;
    }
    
    public static <T> String mapString(List<List<T>> map) {
        StringBuilder sb = new StringBuilder("{");
        for (Collection<T> c : map) {
            sb.append("[");
            String valueSeparator = "";
            for (T t : c) {
                sb.append(valueSeparator).append(t);
                valueSeparator = ",";
            }
            sb.append("]");
        }
        return sb.append("}").toString();
    }
}
