package at.jku.risc.stout.aau.term;

import at.jku.risc.stout.aau.Solution;
import at.jku.risc.stout.aau.Witness;
import at.jku.risc.stout.aau.util.ANSI;

import java.util.Collections;
import java.util.Set;

/**
 * {@linkplain VariableTerm VariableTerms} are terms to be substituted.
 * <br>
 * If enabled, {@linkplain Solution Solutions} supply
 * {@linkplain Witness#substitutions Witness.substitutions}
 * for each variable in their generalization.
 * <br><br>
 * See {@linkplain MappedVariableTerm} for representing variables in the problem statement.
 */
public class VariableTerm implements Term {
    public static final VariableTerm VAR_0 = new VariableTerm(0);
    public final int var;
    
    public VariableTerm(int var) {
        this.var = var;
    }

    @Override
    public String head() {
        return Integer.toString(var);
    }

    @Override
    public Set<Integer> namedVariables() {
        return Collections.singleton(var);
    }
    
    @Override
    public int hashCode() {
        return var;
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof VariableTerm)) {
            return false;
        }
        return var == ((VariableTerm) other).var;
    }
    
    @Override
    public String toString() {
        return ANSI.blue(var);
    }
}
