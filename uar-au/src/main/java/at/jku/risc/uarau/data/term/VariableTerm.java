package at.jku.risc.uarau.data.term;

import at.jku.risc.uarau.data.Solution;
import at.jku.risc.uarau.util.ANSI;
import at.jku.risc.uarau.util.ArraySet;

import java.util.Set;

/**
 * {@linkplain VariableTerm VariableTerms} are terms to be substituted.
 * <br>
 * If enabled, {@linkplain Solution Solutions} supply
 * {@linkplain at.jku.risc.uarau.data.Witness#substitutions Witness.substitutions}
 * for each variable in their generalization.
 * <br><br>
 * See {@linkplain MappedVariableTerm} for representing variables in the problem statement.
 */
public class VariableTerm implements Term {
    public static final int VAR_0 = 0;
    public final int var;
    
    public VariableTerm(int var) {
        this.var = var;
    }
    
    @Override
    public Set<Integer> v_named() {
        return ArraySet.singleton(var);
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
