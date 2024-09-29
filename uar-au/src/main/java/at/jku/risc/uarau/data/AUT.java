package at.jku.risc.uarau.data;

import at.jku.risc.uarau.GroundTerm;
import at.jku.risc.uarau.Substitution;
import at.jku.risc.uarau.Term;
import at.jku.risc.uarau.util.ANSI;
import at.jku.risc.uarau.util.ArraySet;
import at.jku.risc.uarau.util.Pair;
import at.jku.risc.uarau.util.Util;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AUT {
    public final int var;
    public final ArraySet<GroundTerm> T1, T2;
    
    private Integer hash = null;
    
    public AUT(int var, Queue<GroundTerm> T1, Queue<GroundTerm> T2) {
        this.var = var;
        this.T1 = new ArraySet<>(T1);
        this.T2 = new ArraySet<>(T2);
    }
    
    public AUT(int var, GroundTerm T1, GroundTerm T2) {
        this(var, new ArraySet<>(T1), new ArraySet<>(T2));
    }
    
    public Queue<String> heads() {
        return Stream.concat(T1.stream(), T2.stream())
                .map(t -> t.head)
                .collect(Collectors.toCollection(ArrayDeque::new));
    }
    
    public static Pair<Set<Term>, Set<Term>> applyAll(Queue<AUT> auts, Term baseTerm) {
        Pair<Set<Term>, Set<Term>> applied = new Pair<>(new HashSet<>(), new HashSet<>());
        applied.left.add(baseTerm);
        applied.right.add(baseTerm);
        
        for (AUT aut : auts) {
            applied = aut.apply(applied.left, applied.right);
        }
        return applied;
    }
    
    public Pair<Set<Term>, Set<Term>> apply(Set<Term> Q1, Set<Term> Q2) {
        Set<Term> lhs = new HashSet<>();
        Set<Term> rhs = new HashSet<>();
        
        Q1.forEach(q1 -> T1.forEach(t1 -> lhs.add(new Substitution(var, t1).apply(q1))));
        Q2.forEach(q2 -> T2.forEach(t2 -> rhs.add(new Substitution(var, t2).apply(q2))));
        
        return new Pair<>(Collections.unmodifiableSet(lhs), Collections.unmodifiableSet(rhs));
    }
    
    @Override
    public String toString() {
        String T1_str = Util.str(T1, "  ", "..");
        String T2_str = Util.str(T2, "  ", "..");
        return ANSI.blue(var + " ") + T1_str + ANSI.red(" ?= ") + T2_str;
    }
    
    @Override
    public int hashCode() {
        if (hash == null) {
            hash = var + T1.hashCode() * 31 + T2.hashCode() * 31 * 31;
        }
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AUT)) {
            return false;
        }
        AUT aut = (AUT) obj;
        if (hashCode() != aut.hashCode()) {
            return false;
        }
        return var == aut.var && T1.equals(aut.T1) && T2.equals(aut.T2);
    }
}
