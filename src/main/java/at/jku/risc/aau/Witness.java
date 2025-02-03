package at.jku.risc.aau;

import at.jku.risc.aau.term.Term;
import at.jku.risc.aau.util.ANSI;
import at.jku.risc.aau.util.Data;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * See {@linkplain Solution}
 */
public class Witness {
    public final Map<Integer, Set<Term>> substitutions;
    
    public Witness(Map<Integer, Set<Term>> substitutions) {
        this.substitutions = Collections.unmodifiableMap(substitutions);
    }
    
    @Override
    public String toString() {
        return Data.str(substitutions.entrySet()
                .stream()
                .flatMap(entry -> Stream.of(ANSI.blue(entry.getKey()), Data.str(entry.getValue())))
                .collect(Collectors.toList()));
    }
}
