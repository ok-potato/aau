package at.jku.risc.uarau.impl;

import at.jku.risc.uarau.term.FunctionTerm;
import at.jku.risc.uarau.term.GroundTerm;
import at.jku.risc.uarau.term.Term;
import at.jku.risc.uarau.term.VariableTerm;
import at.jku.risc.uarau.util.Data;
import at.jku.risc.uarau.util.Panic;

import java.util.ArrayDeque;
import java.util.Queue;

// TODO document
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
    
    public static GroundTerm applyAllForceGroundTerm(Queue<Substitution> substitutions, Term baseTerm) {
        Term term = applyAll(substitutions, baseTerm);
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
        return new FunctionTerm(functionTerm.head, Data.mapList(functionTerm.arguments, this::apply));
    }
    
    @Override
    public String toString() {
        return String.format("🔅%s►%s", var, substitute);
    }
}
