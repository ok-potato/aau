package at.jku.risc.stout.aau;

import at.jku.risc.stout.aau.term.GroundishTerm;
import at.jku.risc.stout.aau.util.ANSI;
import at.jku.risc.stout.aau.util.Data;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * See {@linkplain Solution}
 */
public class Witness {
    public final Map<Integer, Set<GroundishTerm>> substitutions;
    
    public Witness(Map<Integer, Set<GroundishTerm>> substitutions) {
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
