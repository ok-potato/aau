package at.jku.risc.uarau.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Util {
    
    // *** Collection ***
    
    public static <E> List<E> newList(int size, Function<Integer, E> initializer) {
        List<E> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(initializer.apply(i));
        }
        return list;
    }
    
    public static <E> void pad(Collection<E> collection, int newSize, Supplier<E> supplier) {
        for (int i = collection.size(); i < newSize; i++) {
            collection.add(supplier.get());
        }
    }
    
    public static boolean allUnique(Collection<?> collection) {
        return allUnique(collection, Function.identity());
    }
    
    public static <E, K> boolean allUnique(Collection<E> collection, Function<E, K> extractKey) {
        Set<K> keySet = new HashSet<>();
        for (E element : collection) {
            K key = extractKey.apply(element);
            if (keySet.contains(key)) {
                return false;
            }
            keySet.add(key);
        }
        return true;
    }
    
    public static <E> Collector<E, ?, Queue<E>> toQueue() {
        return Collectors.toCollection(ArrayDeque::new);
    }
    
    public static <E> E getAny(Set<E> set) {
        return set.stream().findFirst().orElseThrow(IllegalArgumentException::new);
    }
    
    // *** String ***
    
    public static String str(Collection<?> collection) {
        return str(collection, ", ", "..");
    }
    
    public static String str(Collection<?> collection, String separator, String empty) {
        return str(collection, separator, empty, "", "");
    }
    
    public static String str(Collection<?> collection, String separator, String empty, String open, String close) {
        if (collection.isEmpty()) {
            return empty;
        }
        StringJoiner joiner = new StringJoiner(separator);
        collection.forEach(t -> joiner.add(t.toString()));
        return open + joiner + close;
    }
}
