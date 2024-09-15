package at.jku.risc.uarau.data;

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
                .flatMap(entry -> Stream.of("\u001B[33m" + entry.getKey() + ":\u001B[0m", entry.getValue()))
                .collect(Collectors.toList()), " ", "..");
    }
}
