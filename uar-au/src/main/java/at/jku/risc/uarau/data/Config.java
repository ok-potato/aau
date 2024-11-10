package at.jku.risc.uarau.data;

import at.jku.risc.uarau.data.term.GroundTerm;
import at.jku.risc.uarau.data.term.VariableTerm;
import at.jku.risc.uarau.util.ANSI;
import at.jku.risc.uarau.util.ArraySet;
import at.jku.risc.uarau.util.Util;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * {@linkplain Config}s are mutable representations of program states, where:
 * <ul>
 *     <li> {@linkplain Config#substitutions} is the substitutions needed to arrive at that state
 *     <li> {@linkplain Config#A} is the remaining set of sub-terms which might be generalizable
 *     <li> {@linkplain Config#S} is the set of fully generalized sub-terms
 * </ul>
 * When branching occurs, a {@linkplain Config#copy()} is made for each possible rule application.
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
        A.add(new AUT(freshVar(), ArraySet.singleton(T1), ArraySet.singleton(T2)));
    }
    
    private Config(Config original) {
        this(original, original.S);
    }
    
    public Config copy() {
        return new Config(this);
    }
    
    private Config(Config original, Queue<AUT> S) {
        this.A = new ArrayDeque<>(original.A);
        this.S = new ArrayDeque<>(S);
        this.substitutions = new ArrayDeque<>(original.substitutions);
        this.alpha1 = original.alpha1;
        this.alpha2 = original.alpha2;
        this.freshVar = original.freshVar;
    }
    
    public Config copyWithNewS(Queue<AUT> S) {
        return new Config(this, S);
    }
    
    public int freshVar() {
        return freshVar++;
    }
    
    public int peekVar() {
        return freshVar + 1;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append(Substitution.applyAll(substitutions, VariableTerm.VAR_0));
        if (!A.isEmpty()) {
            sb.append(ANSI.yellow("  A.. ")).append(Util.str(A));
        }
        if (!S.isEmpty()) {
            sb.append(ANSI.yellow("  S.. ")).append(Util.str(S));
        }
        sb.append(ANSI.yellow("  α..", alpha1, alpha2));
        
        return sb.toString();
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
