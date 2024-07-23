package at.jku.risc.uarau.data;

import java.util.Collections;
import java.util.Set;

public class Expression {
    public final int x;
    public final Set<Term> T;
    
    public Expression(int x, Set<Term> t) {
        this.x = x;
        T = Collections.unmodifiableSet(t);
    }
    
    @Override
    public String toString() {
        return String.format("%s : %s", x, T);
    }
}
