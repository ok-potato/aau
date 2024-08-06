package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.DataUtils;

import java.util.Collections;
import java.util.Set;

public class AUT {
    public final int var;
    public final Set<Term> T1, T2;
    
    private Integer hash = null;
    
    public AUT(int var, Set<Term> T1, Set<Term> T2) {
        this.var = var;
        this.T1 = Collections.unmodifiableSet(T1);
        this.T2 = Collections.unmodifiableSet(T2);
    }
    
    public AUT(int var, Term T1, Term T2) {
        this(var, Collections.singleton(T1), Collections.singleton(T2));
    }
    
    @Override
    public String toString() {
        return String.format("➰%s: %s ?= %s", var, DataUtils.joinString(T1, ", ", "{}", "{ ", " }"), DataUtils.joinString(T2, ", ", "{}", "{ ", " }"));
    }
    
    @Override
    public int hashCode() {
        if (hash == null) {
            hash = var + T1.hashCode() * 31 + T2.hashCode() * 31 * 31;
        }
        return hash;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AUT)) {
            return false;
        }
        AUT aut = (AUT) obj;
        if (hashCode() != aut.hashCode()) {
            return false;
        }
        return var == aut.var && T1.equals(aut.T1) && T2.equals(aut.T2);
    }
}
