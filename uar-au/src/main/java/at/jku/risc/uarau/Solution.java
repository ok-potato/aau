package at.jku.risc.uarau;

import at.jku.risc.uarau.impl.Algorithm;
import at.jku.risc.uarau.impl.Substitution;
import at.jku.risc.uarau.term.GroundTerm;
import at.jku.risc.uarau.term.Term;
import at.jku.risc.uarau.util.ANSI;
import at.jku.risc.uarau.util.Data;
import at.jku.risc.uarau.util.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The output of the {@linkplain Algorithm Algorithm} is a set of all possible
 * {@linkplain Solution Solutions}.
 * <br>
 * Each {@linkplain Solution} gives a unique, minimal {@linkplain Solution#generalization} for the problem terms.
 * <br><br>
 * With {@linkplain at.jku.risc.uarau.Problem#witnesses(boolean) Problem.witnesses(boolean)}
 * enabled, every variable in the {@linkplain Solution#generalization}
 * will have an associated entry of possible {@linkplain Witness#substitutions} per side.
 * <br>
 * If one of the possible substitutions is applied for each variable,
 * you get a term which approximates the respective side of the posed problem, proving that the
 * {@linkplain Solution#generalization} is indeed valid.
 * <br><br>
 * {@linkplain Solution#alpha1} and {@linkplain Solution#alpha2} are the proximity of the
 * {@linkplain Solution#generalization} to the left- and right-hand problem terms respectively
 * (if you choose one of the optimal {@linkplain Witness#substitutions} for each variable).
 */
public class Solution {
    public final Term generalization;
    public final Witness lhs, rhs;
    public final float alpha1, alpha2;
    
    public Solution(Term generalization, Witness lhs, Witness rhs, float alpha1, float alpha2) {
        this.generalization = generalization;
        assert (lhs == null) == (rhs == null);
        if (lhs != null) {
            assert lhs.substitutions.keySet().equals(rhs.substitutions.keySet());
            this.lhs = lhs;
            this.rhs = rhs;
        } else {
            this.lhs = null;
            this.rhs = null;
        }
        this.alpha1 = alpha1;
        this.alpha2 = alpha2;
    }
    
    // TODO document
    public Pair<Set<GroundTerm>, Set<GroundTerm>> enumerate() {
        return new Pair<>(enumerateSide(lhs.substitutions), enumerateSide(rhs.substitutions));
    }
    
    private Set<GroundTerm> enumerateSide(Map<Integer, Set<Term>> substitutions) {
        List<Set<Substitution>> steps = substitutions.entrySet().stream()
                .map(entry -> entry.getValue().stream()
                        .map(term -> new Substitution(entry.getKey(), term))
                        .collect(Collectors.toSet()))
                .collect(Collectors.toList());
        
        Set<Term> generalization = Collections.singleton(this.generalization);
        Set<Term> unchaste = Data.permute(generalization, steps, ((term, substitution) -> substitution.apply(term)));
        return unchaste.stream().map(GroundTerm::force).collect(Collectors.toSet());
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append(generalization);
        if (lhs == null) {
            sb.append(ANSI.red("  ∅")).append(ANSI.yellow("  α..", alpha1, alpha2));
        } else {
            sb.append(ANSI.yellow("  LHS (α=" + alpha1 + ")..  "))
                    .append(lhs)
                    .append(ANSI.yellow("  RHS (α=" + alpha2 + ")..  "))
                    .append(rhs);
        }
        return sb.toString();
    }
}
