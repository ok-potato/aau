package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.ArraySet;
import at.jku.risc.uarau.util.DataUtil;

import java.util.Queue;

public class Expression {
    public final int var;
    public final ArraySet<Term> T;
    
    public Expression(int var, Queue<Term> T) {
        assert var != Term.ANON.var;
        this.var = var;
        this.T = new ArraySet<>(T);
    }
    
    @Override
    public String toString() {
        return String.format("%s in %s", var, DataUtil.str(T, ", ", "[]", "[", "]"));
    }
}
