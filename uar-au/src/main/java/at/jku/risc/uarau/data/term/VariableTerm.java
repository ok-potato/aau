package at.jku.risc.uarau.data.term;

import at.jku.risc.uarau.data.Solution;
import at.jku.risc.uarau.util.ANSI;
import at.jku.risc.uarau.util.ArraySet;

import java.util.Set;

/**
 * See {@linkplain MappedVariableTerm} for representing variables in the problem statement.
 * <br><br>
 * Variable terms are terms which can be substituted.
 * In a {@linkplain Solution}, the possible substitutions for each variable
 * are given by the corresponding {@linkplain at.jku.risc.uarau.data.Witness Witness}.
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
