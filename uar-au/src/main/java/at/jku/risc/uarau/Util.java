package at.jku.risc.uarau;

import at.jku.risc.uarau.data.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Util {
    static Logger log = LoggerFactory.getLogger(Util.class);
    
    // collection operations
    
    public static <T> Deque<T> copy(Deque<T> original) {
        Deque<T> copy = new ArrayDeque<>(original.size());
        for (T aut : original) {
            copy.push(aut);
        }
        return copy;
    }
    
    // stringify
    
    public static <T> String join(Collection<T> collection) {
        return join(collection, ", ", "..");
    }
    
    public static <T> String join(Collection<T> collection, String separator, String empty) {
        return join(collection, separator, empty, "", "");
    }
    
    public static <T> String join(Collection<T> collection, String separator, String empty, String open, String close) {
        if (collection.isEmpty()) {
            return empty;
        }
        StringJoiner joiner = new StringJoiner(separator);
        collection.forEach(t -> joiner.add(t.toString()));
        return open + joiner + close;
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
            Term t = new Term(head, arguments.toArray(new Term[0]));
            arguments = null;
            log.trace("Parsed term: {}", t);
            return t;
        }
    }
}
