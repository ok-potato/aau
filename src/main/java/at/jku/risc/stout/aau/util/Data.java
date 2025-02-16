package at.jku.risc.stout.aau.util;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Useful collection operations (partially inspired by Kotlin's stdlib)
 */
public class Data {
    
    // *** Instantiations, mappings ***
    
    @SafeVarargs
    public static <K, V> Map<K, V> mapOf(Pair<K, V>... pairs) {
        Map<K, V> map = new HashMap<>();
        for (Pair<K, V> pair : pairs) {
            map.put(pair.left, pair.right);
        }
        return map;
    }
    
    public static <E> List<E> list(int size, Function<Integer, E> initializer) {
        List<E> list = new ArrayList<>(size);
        for (int idx = 0; idx < size; idx++) {
            list.add(initializer.apply(idx));
        }
        return list;
    }
    
    public static <E, M> List<M> mapToList(Collection<E> original, Function<E, M> mapping) {
        return original.stream()
                .map(mapping)
                .collect(Collectors.toCollection(() -> new ArrayList<>(original.size())));
    }
    
    public static <E, M> Queue<M> mapToQueue(Collection<E> original, Function<E, M> mapping) {
        return original.stream()
                .map(mapping)
                .collect(Collectors.toCollection(() -> new ArrayDeque<>(original.size())));
    }
    
    public static <E> void pad(Collection<E> collection, int newSize, Supplier<E> filler) {
        for (int idx = collection.size(); idx < newSize; idx++) {
            collection.add(filler.get());
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
    
    public static <E, T> Set<E> permute(Set<E> set, Collection<Set<T>> steps, BiFunction<E, T, E> mapping) {
        for (Set<T> transforms : steps) {
            set = set.stream().flatMap(e -> transforms.stream().map(t -> mapping.apply(e, t))).collect(Collectors.toSet());
        }
        return set;
    }
    
    public static <E> E getAny(Set<E> set) {
        for (E e : set) {
            return e;
        }
        throw Panic.arg("No element present");
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
    
    // *** toStrings ***
    
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
    
    private static final String LOG_NEWLINE = "\n      ";
    
    public static String log(String title, Collection<?> collection) {
        return title + str(collection, LOG_NEWLINE, "", LOG_NEWLINE, "");
    }
}
