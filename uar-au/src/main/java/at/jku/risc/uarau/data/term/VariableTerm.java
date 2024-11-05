package at.jku.risc.uarau.data.term;

import at.jku.risc.uarau.util.ANSI;
import at.jku.risc.uarau.util.ArraySet;

import java.util.Set;

/**
 * Variable terms are generated during the computation process.
 * <br><br>
 * They can't be used in the problem input, since the problem terms are assumed to be ground.
 * <br>
 * Instead, use {@linkplain MappedVariableTerm} to map your variables to their respective constant representation.
 * <br><br>
 * In the final output, they appear in the solutions terms, together with their corresponding
 * {@linkplain at.jku.risc.uarau.data.Witness} substitutions.
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
        // return String.valueOf(var);
    }
}
