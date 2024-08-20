package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util._Data;

import java.util.Collections;
import java.util.Deque;
import java.util.Map;

public class Witness {
    public final Map<Integer, Deque<Term>> substitutions;
    
    public Witness(Map<Integer, Deque<Term>> substitutions) {
        this.substitutions = Collections.unmodifiableMap(substitutions);
    }
    
    @Override
    public String toString() {
        return _Data.str(substitutions.entrySet());
    }
}
