package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.ImplicitSet;
import at.jku.risc.uarau.util._Data;

import java.util.Queue;

public class Expression {
    public final int var;
    public final Queue<Term> T;
    
    public Expression(int var, Queue<Term> T) {
        assert var != Term.ANON.var;
        this.var = var;
        this.T = new ImplicitSet<>(T);
    }
    
    @Override
    public String toString() {
        return String.format("%s in %s", var, _Data.str(T, ", ", "[]", "[", "]"));
    }
}
