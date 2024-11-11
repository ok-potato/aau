package at.jku.risc.uarau.data;

import at.jku.risc.uarau.data.term.Term;
import at.jku.risc.uarau.util.ANSI;

/**
 * The output of the {@linkplain at.jku.risc.uarau.Algorithm Algorithm} is a set of all possible
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
            assert (lhs.substitutions.keySet().equals(rhs.substitutions.keySet()));
            this.lhs = lhs;
            this.rhs = rhs;
        } else {
            this.lhs = null;
            this.rhs = null;
        }
        this.alpha1 = alpha1;
        this.alpha2 = alpha2;
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
