package at.jku.risc.uarau;

import at.jku.risc.uarau.util.Util;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class Substitution {
    public final int var;
    public final Term substitute;
    
    public Substitution(int var, Term substitute) {
        this.var = var;
        this.substitute = substitute;
    }
    
    public static Term applyAll(Queue<Substitution> substitutions, int baseVariable) {
        if (substitutions.isEmpty()) {
            return new VariableTerm(baseVariable);
        }
        substitutions = new ArrayDeque<>(substitutions);
        Term term = substitutions.remove().substitute;
        for (Substitution substitution : substitutions) {
            term = substitution.apply(term);
        }
        return term;
    }
    
    public static GroundTerm applyAllForceGroundTerm(Queue<Substitution> substitutions, int baseVariable) {
        Term term = applyAll(substitutions, baseVariable);
        try {
            return forceGroundTerm(term);
        } catch (UnsupportedOperationException e) {
            throw new IllegalArgumentException("Couldn't force cast: " + term, e);
        }
    }
    
    private static GroundTerm forceGroundTerm(Term term) {
        if (term instanceof GroundTerm) {
            return (GroundTerm) term;
        }
        if (!(term instanceof FunctionTerm)) {
            throw new UnsupportedOperationException(
                    "Tried force-casting sub-term: " + term + " of type " + term.getClass().getSimpleName());
        }
        FunctionTerm functionTerm = (FunctionTerm) term;
        List<GroundTerm> arguments = Util.newList(functionTerm.arguments.size(), i -> forceGroundTerm(functionTerm.arguments.get(i)));
        return new GroundTerm(functionTerm.head, arguments);
    }
    
    public Term apply(Term term) {
        if (term instanceof VariableTerm) {
            return ((VariableTerm) term).var == this.var ? this.substitute : term;
        }
        if (term instanceof GroundTerm) {
            return term;
        }
        if (!(term instanceof FunctionTerm)) {
            throw new IllegalStateException("Unknown Term type used in substitution: " + term.getClass());
        }
        FunctionTerm functionTerm = (FunctionTerm) term;
        List<Term> arguments = Util.newList(functionTerm.arguments.size(), i -> apply(functionTerm.arguments.get(i)));
        return new FunctionTerm(functionTerm.head, arguments);
    }
    
    @Override
    public String toString() {
        return String.format("🔅%s►%s", var, substitute);
    }
}
