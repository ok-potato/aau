package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.DataUtils;

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
        return DataUtils.joinString(substitutions.entrySet());
    }
}
