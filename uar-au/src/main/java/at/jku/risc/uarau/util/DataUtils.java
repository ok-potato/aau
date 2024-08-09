package at.jku.risc.uarau.util;

import at.jku.risc.uarau.data.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

public class DataUtils {
    static Logger log = LoggerFactory.getLogger(DataUtils.class);
    
    // collection operations
    
    public static <T> void pad(List<T> list, Supplier<T> supplier, int newSize) {
        for (int i = list.size(); i < newSize; i++) {
            list.add(supplier.get());
        }
    }
    
    public static <T> Deque<T> copyDeque(Deque<T> original) {
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
    
    // build
    
    public static class TermBuilder {
        public String head;
        public List<Term> arguments = new ArrayList<>();
        
        public TermBuilder(String head) {
            this.head = head;
        }
        
        public Term build() {
            assert (arguments != null);
            Term t = new Term(head, arguments);
            arguments = null;
            log.trace("Parsed term: {}", t);
            return t;
        }
    }
}
