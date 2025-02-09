package at.jku.risc.aau.impl;

import at.jku.risc.aau.term.*;
import at.jku.risc.aau.util.Data;
import at.jku.risc.aau.util.Panic;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Usually part of a chain of substitutions which will later be applied to a term.
 */
public class Substitution {
    public final int var;
    public final Term substitute;
    
    public Substitution(int var, Term substitute) {
        this.var = var;
        this.substitute = substitute;
    }
    
    public static Term applyAll(Queue<Substitution> substitutions, Term baseTerm) {
        if (substitutions.isEmpty()) {
            return baseTerm;
        }
        substitutions = new ArrayDeque<>(substitutions);
        Term term = baseTerm;
        for (Substitution substitution : substitutions) {
            term = substitution.apply(term);
        }
        return term;
    }
    
    /**
     * Type level assertion that there are no unsubstituted variables (besides ANON) remaining after substitution
     */
    public static GroundishTerm applyAll_forceGroundish(Queue<Substitution> substitutions, Term baseTerm) {
        Term term = applyAll(substitutions, baseTerm);
        try {
            return GroundishTerm.force(term);
        } catch (UnsupportedOperationException e) {
            throw new IllegalArgumentException("Couldn't force cast: " + term, e);
        }
    }
    
    public Term apply(Term term) {
        if (term instanceof VariableTerm) {
            return ((VariableTerm) term).var == this.var ? this.substitute : term;
        }
        if (term instanceof GroundishTerm) {
            return term;
        }
        if (!(term instanceof FunctionTerm)) {
            throw Panic.state("Unknown Term type used in substitution: %s", term.getClass());
        }
        FunctionTerm functionTerm = (FunctionTerm) term;
        return new FunctionTerm(functionTerm.head(), Data.mapToList(functionTerm.arguments(), this::apply));
    }
    
    @Override
    public String toString() {
        return String.format("🔅%s►%s", var, substitute);
    }
}
