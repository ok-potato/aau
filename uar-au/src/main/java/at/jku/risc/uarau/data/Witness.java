package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.ANSI;
import at.jku.risc.uarau.util.DataUtil;

import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Witness {
    public final Map<Integer, Queue<Term>> substitutions;
    
    public Witness(Map<Integer, Queue<Term>> substitutions) {
        this.substitutions = Collections.unmodifiableMap(substitutions);
    }
    
    @Override
    public String toString() {
        return DataUtil.str(substitutions.entrySet()
                .stream()
                .flatMap(entry -> Stream.of(ANSI.blue(entry.getKey()), entry.getValue()))
                .collect(Collectors.toList()), " ", "..");
    }
}
