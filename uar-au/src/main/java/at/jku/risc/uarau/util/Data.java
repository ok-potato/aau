package at.jku.risc.uarau.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Useful collection operations
 */
public class Data {
    
    // *** Instantiations, mappings ***
    
    public static <E> List<E> list(int size, Function<Integer, E> initializer) {
        List<E> list = new ArrayList<>(size);
        for (int idx = 0; idx < size; idx++) {
            list.add(initializer.apply(idx));
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
        for (int idx = collection.size(); idx < newSize; idx++) {
            collection.add(supplier.get());
        }
    }
    
    // *** Checks, retrievals ***
    
    public static <E> boolean isSet(Collection<E> collection) {
        Set<E> occurred = new HashSet<>();
        for (E element : collection) {
            if (occurred.contains(element)) {
                return false;
            }
            occurred.add(element);
        }
        return true;
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
    
    public static String str(Collection<?> collection) {
        return str(collection, " ", "..", "", "");
    }
    
    public static String str(Collection<?> collection, String separator, String empty, String open, String close) {
        if (collection.isEmpty()) {
            return empty;
        }
        StringJoiner joiner = new StringJoiner(separator);
        collection.forEach(t -> joiner.add(t.toString()));
        return open + joiner + close;
    }
    
    private static final String LOG_NEWLINE = "\n        ";
    
    public static String log(String title, Collection<?> collection) {
        return title + str(collection, LOG_NEWLINE, "", LOG_NEWLINE, "");
    }
}
