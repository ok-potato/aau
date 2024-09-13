package at.jku.risc.uarau.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class DataUtil {
    
    // collection ops
    
    public static <ELEMENT> void pad(Collection<ELEMENT> collection, Supplier<ELEMENT> supplier, int newSize) {
        for (int i = collection.size(); i < newSize; i++) {
            collection.add(supplier.get());
        }
    }
    
    public static boolean allUnique(Collection<?> collection) {
        return allUnique(collection, Function.identity());
    }
    
    public static <ELEMENT, KEY> boolean allUnique(Collection<ELEMENT> collection, Function<ELEMENT, KEY> extractKey) {
        Set<KEY> keySet = new HashSet<>();
        for (ELEMENT element : collection) {
            KEY key = extractKey.apply(element);
            if (keySet.contains(key)) {
                return false;
            }
            keySet.add(key);
        }
        return true;
    }
    
    public static <ELEMENT> Collector<ELEMENT, ?, Queue<ELEMENT>> toQueue() {
        return Collectors.toCollection(ArrayDeque::new);
    }
    
    public static <ELEMENT> ELEMENT getAny(Set<ELEMENT> set) {
        return set.stream().findFirst().orElseThrow(IllegalArgumentException::new);
    }
    
    // string ops
    
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
