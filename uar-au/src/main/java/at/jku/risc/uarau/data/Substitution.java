package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.DataUtils;

import java.util.Deque;

public class Substitution {
    public final int var;
    public final Term term;
    
    public Substitution(int var, Term term) {
        assert (var != Term.ANON.var);
        this.var = var;
        this.term = term;
    }
    
    public static Term apply(Deque<Substitution> substitutions, int baseVariable) {
        if (substitutions.isEmpty()) {
            return new Term(baseVariable);
        }
        substitutions = DataUtils.copyAccurate(substitutions);
        Term t = substitutions.removeFirst().term;
        while (!substitutions.isEmpty()) {
            t = apply(t, substitutions.pop());
        }
        return t;
    }
    
    public static Term apply(Term t, Substitution substitution) {
        if (t.var == substitution.var) {
            return substitution.term;
        }
        if (t.isVar() || t.mappedVar) {
            return t;
        }
        Term[] arguments = new Term[t.arguments.length];
        for (int i = 0; i < t.arguments.length; i++) {
            arguments[i] = apply(t.arguments[i], substitution);
        }
        return new Term(t.head, arguments);
    }
    
    @Override
    public String toString() {
        return String.format("🔅%s►%s", var, term);
    }
}
