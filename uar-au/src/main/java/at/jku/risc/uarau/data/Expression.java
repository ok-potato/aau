package at.jku.risc.uarau.data;

import java.util.Collections;
import java.util.Set;

public class Expression {
    public final int var;
    public final Set<Term> T;
    
    public Expression(int var, Set<Term> T) {
        this.var = var;
        this.T = Collections.unmodifiableSet(T);
    }
    
    @Override
    public String toString() {
        return String.format("%s in %s", var, T);
    }
}
