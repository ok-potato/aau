package at.jku.risc.uarau.data;

import at.jku.risc.uarau.data.term.Term;
import at.jku.risc.uarau.util.ANSI;
import at.jku.risc.uarau.util.Util;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Witness {
    public final Map<Integer, Set<Term>> substitutions;
    
    public Witness(Map<Integer, Set<Term>> substitutions) {
        this.substitutions = Collections.unmodifiableMap(substitutions);
    }
    
    @Override
    public String toString() {
        return Util.str(substitutions.entrySet()
                .stream()
                .flatMap(entry -> Stream.of(ANSI.blue(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList()), " ", "..");
    }
}
