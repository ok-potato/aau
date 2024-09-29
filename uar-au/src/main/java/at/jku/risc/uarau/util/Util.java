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
    
    public static <E, M> List<M> mapList(List<E> original, Function<E, M> mappingFunction) {
        return original.stream()
                .map(mappingFunction)
                .collect(Collectors.toCollection(() -> new ArrayList<>(original.size())));
    }
    
    public static <E, M> Queue<M> mapQueue(Queue<E> original, Function<E, M> mappingFunction) {
        return original.stream()
                .map(mappingFunction)
                .collect(Collectors.toCollection(() -> new ArrayDeque<>(original.size())));
    }
    
    public static <E> void pad(Collection<E> collection, int newSize, Supplier<E> supplier) {
        for (int i = collection.size(); i < newSize; i++) {
            collection.add(supplier.get());
        }
    }
    
    public static <E> boolean allUnique(Collection<E> collection) {
        Set<E> occurred = new HashSet<>();
        for (E element : collection) {
            if (occurred.contains(element)) {
                return false;
            }
            occurred.add(element);
        }
        return true;
    }
    
    public static <E> Collector<E, ?, Queue<E>> toQueue() {
        return Collectors.toCollection(ArrayDeque::new);
    }
    
    public static <E> E getAny(Set<E> set) {
        return set.stream().findFirst().orElseThrow(IllegalArgumentException::new);
    }
    
    public static <E> boolean any(Collection<E> collection, Function<E, Boolean> check) {
        return collection.stream().anyMatch(check::apply);
    }
    
    public static <E> boolean all(Collection<E> collection, Function<E, Boolean> check) {
        return collection.stream().allMatch(check::apply);
    }
    
    public static <E> boolean none(Collection<E> collection, Function<E, Boolean> check) {
        return collection.stream().noneMatch(check::apply);
    }
    
    // *** String ***
    
    public static IllegalArgumentException argException(String message, Object... args) {
        return new IllegalArgumentException(String.format(message, args));
    }
    
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
