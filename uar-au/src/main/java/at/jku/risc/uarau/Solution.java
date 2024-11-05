package at.jku.risc.uarau;

import at.jku.risc.uarau.data.Witness;
import at.jku.risc.uarau.data.term.Term;
import at.jku.risc.uarau.util.ANSI;

public class Solution {
    public final Term r;
    public final Witness lhs, rhs;
    public final float alpha1, alpha2;
    
    public Solution(Term r, Witness lhs, Witness rhs, float alpha1, float alpha2) {
        this.r = r;
        assert (lhs == null) == (rhs == null);
        if (lhs != null) {
            assert (lhs.substitutions.isEmpty() == rhs.substitutions.isEmpty());
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
        StringBuilder sb = new StringBuilder().append(r);
        if (lhs == null) {
            sb.append(ANSI.red("  ∅"));
        } else if (!lhs.substitutions.isEmpty()) {
            sb.append(ANSI.yellow("  LHS.. ")).append(lhs).append(ANSI.yellow("  RHS.. ")).append(rhs);
        }
        sb.append(ANSI.yellow("  α..", alpha1, alpha2));
        return sb.toString();
    }
}
