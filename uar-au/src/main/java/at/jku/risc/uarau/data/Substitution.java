package at.jku.risc.uarau.data;

import java.util.ArrayDeque;
import java.util.Deque;

public class Substitution {
    public final int var;
    public final Term term;
    
    public Substitution(int var, Term term) {
        assert var != Term.ANON.var;
        this.var = var;
        this.term = term;
    }
    
    public static Term applyAll(Deque<Substitution> substitutions, int baseVariable) {
        if (substitutions.isEmpty()) {
            return new Term(baseVariable);
        }
        substitutions = new ArrayDeque<>(substitutions);
        Term term = substitutions.removeFirst().term;
        while (!substitutions.isEmpty()) {
            term = apply(substitutions.pop(), term);
        }
        return term;
    }
    
    public static Term apply(Substitution substitution, Term term) {
        if (term.var == substitution.var) {
            return substitution.term;
        }
        if (term.isVar() || term.mappedVar) {
            return term;
        }
        Term[] arguments = new Term[term.arguments.size()];
        for (int i = 0; i < term.arguments.size(); i++) {
            arguments[i] = apply(substitution, term.arguments.get(i));
        }
        return new Term(term.head, arguments);
    }
    
    @Override
    public String toString() {
        return String.format("🔅%s►%s", var, term);
    }
}
