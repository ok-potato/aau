package at.jku.risc.uarau.data.term;

import at.jku.risc.uarau.util.ArraySet;

import java.util.Set;

/**
 * VariableTerms can't be used in the problem input, since the problem terms are assumed to be ground
 * <br>
 * Instead, use {@linkplain MappedVariableTerm} to map your variables to the appropriate const representation
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
        return String.valueOf(var);
    }
}
