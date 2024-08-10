package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.DataUtils;
import at.jku.risc.uarau.util.Pair;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AUT {
    public final int var;
    public final Set<Term> T1, T2;
    
    private Integer hash = null;
    
    public AUT(int var, Set<Term> T1, Set<Term> T2) {
        this.var = var;
        this.T1 = Collections.unmodifiableSet(T1);
        this.T2 = Collections.unmodifiableSet(T2);
    }
    
    public AUT(int var, Term T1, Term T2) {
        this(var, Collections.singleton(T1), Collections.singleton(T2));
    }
    
    public Set<String> heads() {
        return Stream.concat(T1.stream(), T2.stream()).map(t -> t.head).collect(Collectors.toSet());
    }
    
    public static Pair<Deque<Term>, Deque<Term>> applyAll(Deque<AUT> auts, Term q1, Term q2) {
        Pair<Deque<Term>, Deque<Term>> applied = new Pair<>(new ArrayDeque<>(), new ArrayDeque<>());
        applied.a.add(q1);
        applied.b.add(q2);
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
            for (Term t : T1) {
                pair.a.addLast(Substitution.apply(new Substitution(var, t), baseTerm));
            }
        }
        for (Term baseTerm : Q2) {
            for (Term t : T2) {
                pair.b.addLast(Substitution.apply(new Substitution(var, t), baseTerm));
            }
        }
        return pair;
    }
    
    @Override
    public String toString() {
        return String.format("➰%s: %s ?= %s", var, DataUtils.joinString(T1, ", ", "{}", "{ ", " }"), DataUtils.joinString(T2, ", ", "{}", "{ ", " }"));
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
