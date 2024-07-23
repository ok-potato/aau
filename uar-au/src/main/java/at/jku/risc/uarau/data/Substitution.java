package at.jku.risc.uarau.data;

import java.util.Deque;

public class Substitution {
    public final int var;
    public final Term term;
    
    public Substitution(int var, Term term) {
        this.var = var;
        this.term = term;
    }
    
    public static Term apply(Deque<Substitution> subs) {
        assert(!subs.isEmpty());
        Term t = subs.getFirst().term;
        while(!subs.isEmpty()) {
            t = apply(t, subs.pop());
        }
        return t;
    }
    
    public static Term apply(Term t, Substitution substitution) {
        if (t.var == substitution.var) {
            return substitution.term;
        }
        if (t.isVar()) {
            return t;
        }
        for (int i = 0; i < t.arguments.length; i++) {
            t.arguments[i] = apply(t.arguments[i], substitution);
        }
        return t;
    }
    
    @Override
    public String toString() {
        return String.format("🔅%s►%s", var, term);
    }
}
