package at.jku.risc.uarau.data;

import java.util.Collections;
import java.util.Set;

public class Expression {
    public final int x;
    public final Set<Term> T;
    
    public Expression(int x, Set<Term> T) {
        this.x = x;
        this.T = Collections.unmodifiableSet(T);
    }
    
    @Override
    public String toString() {
        return String.format("%s in %s", x, T);
    }
}
