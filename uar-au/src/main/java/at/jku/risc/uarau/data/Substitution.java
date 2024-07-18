package at.jku.risc.uarau.data;

import at.jku.risc.uarau.data.Term;

public class Substitution {
    public final int var;
    public final Term term;
    
    public Substitution(int var, Term term) {
        this.var = var;
        this.term = term;
    }
    
    @Override
    public String toString() {
        return String.format("🔅%s►%s", var, term);
    }
}
