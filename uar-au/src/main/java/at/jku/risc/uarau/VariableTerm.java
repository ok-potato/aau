package at.jku.risc.uarau;

import at.jku.risc.uarau.util.ArraySet;

import java.util.Set;

public class VariableTerm implements Term {
    public static final int VAR_0 = 0;
    public final int var;
    
    VariableTerm(int var) {
        this.var = var;
    }
    
    @Override
    public Set<Integer> v_named() {
        return new ArraySet<>(var);
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
