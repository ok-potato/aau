package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.ANSI;
import at.jku.risc.uarau.util.ArraySet;
import at.jku.risc.uarau.util.DataUtil;
import at.jku.risc.uarau.util.Pair;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AUT {
    public final int var;
    public final ArraySet<Term> T1, T2;
    
    private Integer hash = null;
    
    public AUT(int var, Queue<Term> T1, Queue<Term> T2) {
        this.var = var;
        this.T1 = new ArraySet<>(T1);
        this.T2 = new ArraySet<>(T2);
    }
    
    public AUT(int var, Term T1, Term T2) {
        this(var, new ArraySet<>(T1), new ArraySet<>(T2));
    }
    
    public Queue<String> heads() {
        return Stream.concat(T1.stream(), T2.stream())
                .map(t -> t.head)
                .collect(Collectors.toCollection(ArrayDeque::new));
    }
    
    public static Pair<Queue<Term>, Queue<Term>> applyAll(Queue<AUT> auts, Term q1, Term q2) {
        Pair<Queue<Term>, Queue<Term>> applied = new Pair<>(new ArrayDeque<>(), new ArrayDeque<>());
        applied.first.add(q1);
        applied.second.add(q2);
        for (AUT aut : auts) {
            Pair<Queue<Term>, Queue<Term>> pair = aut.apply(applied.first, applied.second);
            applied.first = pair.first;
            applied.second = pair.second;
        }
        return applied;
    }
    
    public Pair<Queue<Term>, Queue<Term>> apply(Queue<Term> Q1, Queue<Term> Q2) {
        Pair<Queue<Term>, Queue<Term>> pair = new Pair<>(new ArrayDeque<>(), new ArrayDeque<>());
        for (Term baseTerm : Q1) {
            T1.forEach(t -> pair.first.add(new Substitution(var, t).apply(baseTerm)));
        }
        for (Term baseTerm : Q2) {
            T2.forEach(t -> pair.second.add(new Substitution(var, t).apply(baseTerm)));
        }
        return pair;
    }
    
    @Override
    public String toString() {
        String T1_str = DataUtil.str(T1, "  ", "..");
        String T2_str = DataUtil.str(T2, "  ", "..");
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
