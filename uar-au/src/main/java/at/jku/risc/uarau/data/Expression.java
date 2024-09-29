package at.jku.risc.uarau.data;

import at.jku.risc.uarau.data.term.GroundTerm;
import at.jku.risc.uarau.util.ArraySet;
import at.jku.risc.uarau.util.Util;

public class Expression {
    public final int var;
    public final ArraySet<GroundTerm> T;
    
    public Expression(int var, ArraySet<GroundTerm> T) {
        this.var = var;
        this.T = T;
    }
    
    @Override
    public String toString() {
        return String.format("%s in %s", var, Util.str(T, ", ", "[]", "[", "]"));
    }
}
