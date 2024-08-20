package at.jku.risc.uarau.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class _Data {
    // collection operations
    
    public static <T> void pad(List<T> list, Supplier<T> supplier, int newSize) {
        for (int i = list.size(); i < newSize; i++) {
            list.add(supplier.get());
        }
    }
    
    public static <T> Deque<T> conjunction(Deque<T> a, Deque<T> b) {
        Deque<T> s = new ArrayDeque<>(a.size() + b.size());
        s.addAll(a);
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
    
    public static<T> Collector<T, ?, ArrayDeque<T>> toDeque() {
        return Collectors.toCollection(ArrayDeque::new);
    }
    
    public static <T> T getAny(Set<T> set) {
        return set.stream().findFirst().orElseThrow(IllegalArgumentException::new);
    }
    
    // stringify
    
    public static <T> String str(Collection<T> collection) {
        return str(collection, ", ", "..");
    }
    
    public static <T> String str(Collection<T> collection, String separator, String empty) {
        return str(collection, separator, empty, "", "");
    }
    
    public static <T> String str(Collection<T> collection, String separator, String empty, String open, String close) {
        if (collection.isEmpty()) {
            return empty;
        }
        StringJoiner joiner = new StringJoiner(separator);
        collection.forEach(t -> joiner.add(t.toString()));
        return open + joiner + close;
    }
}
