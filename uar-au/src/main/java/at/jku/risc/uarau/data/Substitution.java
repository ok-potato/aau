package at.jku.risc.uarau.data;

import java.util.ArrayDeque;
import java.util.Queue;

public class Substitution {
    public final int var;
    public final Term substitute;
    
    public Substitution(int var, Term substitute) {
        assert var != Term.ANON.var;
        this.var = var;
        this.substitute = substitute;
    }
    
    public static Term applyAll(Queue<Substitution> substitutions, int baseVariable) {
        if (substitutions.isEmpty()) {
            return new Term(baseVariable);
        }
        substitutions = new ArrayDeque<>(substitutions);
        Term term = substitutions.remove().substitute;
        for (Substitution substitution : substitutions) {
            term = substitution.apply(term);
        }
        return term;
    }
    
    public Term apply(Term term) {
        if (term.var == this.var) {
            return this.substitute;
        }
        if (term.isVar() || term.mappedVar) {
            return term;
        }
        Term[] arguments = new Term[term.arguments.size()];
        for (int i = 0; i < term.arguments.size(); i++) {
            arguments[i] = apply(term.arguments.get(i));
        }
        return new Term(term.head, arguments);
    }
    
    @Override
    public String toString() {
        return String.format("🔅%s►%s", var, substitute);
    }
}
