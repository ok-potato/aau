package at.jku.risc.uarau.data;

import at.jku.risc.uarau.data.term.FunctionTerm;
import at.jku.risc.uarau.data.term.GroundTerm;
import at.jku.risc.uarau.data.term.Term;
import at.jku.risc.uarau.data.term.VariableTerm;
import at.jku.risc.uarau.util.Panic;
import at.jku.risc.uarau.util.Util;

import java.util.ArrayDeque;
import java.util.Queue;

// TODO documentation
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
            return GroundTerm.force(term);
        } catch (UnsupportedOperationException e) {
            throw new IllegalArgumentException("Couldn't force cast: " + term, e);
        }
    }
    
    public Term apply(Term term) {
        if (term instanceof VariableTerm) {
            return ((VariableTerm) term).var == this.var ? this.substitute : term;
        }
        if (term instanceof GroundTerm) {
            return term;
        }
        if (!(term instanceof FunctionTerm)) {
            throw Panic.state("Unknown Term type used in substitution: %s", term.getClass());
        }
        FunctionTerm functionTerm = (FunctionTerm) term;
        return new FunctionTerm(functionTerm.head, Util.mapList(functionTerm.arguments, this::apply));
    }
    
    @Override
    public String toString() {
        return String.format("🔅%s►%s", var, substitute);
    }
}
