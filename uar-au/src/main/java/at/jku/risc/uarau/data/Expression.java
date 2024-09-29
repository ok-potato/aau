package at.jku.risc.uarau.data;

import at.jku.risc.uarau.GroundTerm;
import at.jku.risc.uarau.util.ArraySet;
import at.jku.risc.uarau.util.Util;

import java.util.Queue;

public class Expression {
    public final int var;
    public final ArraySet<GroundTerm> T;
    
    public Expression(int var, Queue<GroundTerm> T) {
        this.var = var;
        this.T = new ArraySet<>(T);
    }
    
    @Override
    public String toString() {
        return String.format("%s in %s", var, Util.str(T, ", ", "[]", "[", "]"));
    }
}
