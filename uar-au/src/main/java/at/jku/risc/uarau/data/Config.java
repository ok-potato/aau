package at.jku.risc.uarau.data;

import at.jku.risc.uarau.data.term.GroundTerm;
import at.jku.risc.uarau.data.term.VariableTerm;
import at.jku.risc.uarau.util.ANSI;
import at.jku.risc.uarau.util.Util;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Function;

/**
 * mutable
 */
public class Config {
    public final Queue<AUT> A, S;
    public final Queue<Substitution> substitutions;
    
    public float alpha1, alpha2;
    
    private int freshVar;
    
    public Config(GroundTerm T1, GroundTerm T2) {
        A = new ArrayDeque<>();
        S = new ArrayDeque<>();
        substitutions = new ArrayDeque<>();
        alpha1 = 1.0f;
        alpha2 = 1.0f;
        freshVar = 0;
        A.add(new AUT(freshVar(), T1, T2));
    }
    
    private Config(Config original) {
        this(original, original.S);
    }
    
    private Config(Config original, Queue<AUT> S) {
        this.A = new ArrayDeque<>(original.A);
        this.S = new ArrayDeque<>(S);
        this.substitutions = new ArrayDeque<>(original.substitutions);
        this.alpha1 = original.alpha1;
        this.alpha2 = original.alpha2;
        this.freshVar = original.freshVar;
    }
    
    public Config copy() {
        return new Config(this);
    }
    
    public Config mapSolutions(Function<Config, Queue<AUT>> mappingFunction) {
        return new Config(this, mappingFunction.apply(this));
    }
    
    public int freshVar() {
        return freshVar++;
    }
    
    public int peekVar() {
        return freshVar + 1;
    }
    
    @Override
    public String toString() {
        String A_str = Util.str(A, " ", "", "", " ⚫ ");
        String S_str = Util.str(S, " ", ANSI.blue(".."));
        String r_str = Substitution.applyAll(substitutions, VariableTerm.VAR_0).toString();
        return String.format("%s ⚫ %s%s " + ANSI.green("%s %s"), r_str, A_str, S_str, alpha1, alpha2);
    }
    
    @Override
    public int hashCode() {
        return A.hashCode();
    }
    
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Config)) {
            return false;
        }
        Config that = (Config) object;
        return that.A.size() == A.size() && that.S.size() == S.size() && that.A.containsAll(A) && that.S.containsAll(S);
    }
}
