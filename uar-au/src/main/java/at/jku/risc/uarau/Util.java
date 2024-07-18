package at.jku.risc.uarau;

import at.jku.risc.uarau.data.Term;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class Util {
    static Logger log = LoggerFactory.getLogger(Util.class);
    
    public static <T> Deque<T> copy(Deque<T> original) {
        Deque<T> copy = new ArrayDeque<>(original.size());
        for (T aut : original) {
            copy.push(aut);
        }
        return copy;
    }
    
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
