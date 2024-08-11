package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.DataUtil;
import at.jku.risc.uarau.util.Pair;
import at.jku.risc.uarau.util.UnmodifiableDeque;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AUT {
    public final int var;
    public final Deque<Term> T1, T2;
    
    private Integer hash = null;
    
    public AUT(int var, Deque<Term> T1, Deque<Term> T2) {
        this.var = var;
        this.T1 = new UnmodifiableDeque<>(T1);
        this.T2 = new UnmodifiableDeque<>(T2);
    }
    
    public AUT(int var, Term T1, Term T2) {
        this(var, new UnmodifiableDeque<>(T1), new UnmodifiableDeque<>(T2));
    }
    
    public Deque<String> heads() {
        return Stream.concat(T1.stream(), T2.stream())
                .map(t -> t.head)
                .collect(Collectors.toCollection(ArrayDeque::new));
    }
    
    public static Pair<Deque<Term>, Deque<Term>> applyAll(Deque<AUT> auts, Term q1, Term q2) {
        Pair<Deque<Term>, Deque<Term>> applied = new Pair<>(new ArrayDeque<>(), new ArrayDeque<>());
        applied.a.addLast(q1);
        applied.b.addLast(q2);
        for (AUT aut : auts) {
            Pair<Deque<Term>, Deque<Term>> pair = aut.apply(applied.a, applied.b);
            applied.a = pair.a;
            applied.b = pair.b;
        }
        return applied;
    }
    
    public Pair<Deque<Term>, Deque<Term>> apply(Deque<Term> Q1, Deque<Term> Q2) {
        Pair<Deque<Term>, Deque<Term>> pair = new Pair<>(new ArrayDeque<>(), new ArrayDeque<>());
        for (Term baseTerm : Q1) {
            T1.forEach(t -> pair.a.addLast(new Substitution(var, t).apply(baseTerm)));
        }
        for (Term baseTerm : Q2) {
            T2.forEach(t -> pair.b.addLast(new Substitution(var, t).apply(baseTerm)));
        }
        return pair;
    }
    
    @Override
    public String toString() {
        return String.format("➰%s: %s ?= %s", var, DataUtil.joinString(T1, ", ", "{}", "{ ", " }"), DataUtil.joinString(T2, ", ", "{}", "{ ", " }"));
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
