package at.jku.risc.uarau;

public class Substitution {
    public final int var;
    public final Term term;
    
    public Substitution(int var, Term term) {
        this.var = var;
        this.term = term;
    }
}
