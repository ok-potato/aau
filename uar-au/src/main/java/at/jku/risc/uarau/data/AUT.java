package at.jku.risc.uarau.data;

import java.util.HashSet;
import java.util.Set;

// Anti-Unification Triple
public class AUT {
    public final Variable variable;
    public final Set<Term> T1, T2;
    
    public AUT(Variable variable, Set<Term> T1, Set<Term> T2) {
        this.variable = variable;
        this.T1 = T1;
        this.T2 = T2;
    }
    
    public AUT(Variable variable, Term t1, Term t2) {
        this.variable = variable;
        this.T1 = new HashSet<>();
        T1.add(t1);
        this.T2 = new HashSet<>();
        T2.add(t2);
    }
}
