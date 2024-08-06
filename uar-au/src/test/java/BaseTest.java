import at.jku.risc.uarau.data.AUT;
import at.jku.risc.uarau.data.Config;
import at.jku.risc.uarau.data.Substitution;
import at.jku.risc.uarau.data.Term;
import at.jku.risc.uarau.util.MathUtils;
import org.junit.platform.commons.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class BaseTest {
    protected void check(Config solution, String A, String S, String r, float alpha1, float alpha2) {
        if (A != null) {
            List<String> A_auts = new ArrayList<>(Arrays.asList(A.split("\\s*;\\s*")));
            A_auts.removeIf(StringUtils::isBlank);
            assert (solution.A.size() == A_auts.size());
            assert (solution.A.stream().map(AUT::toString).allMatch(A_auts::contains));
        }
        if (S != null) {
            List<String> S_auts = new ArrayList<>(Arrays.asList(S.split("\\s*;\\s*")));
            S_auts.removeIf(StringUtils::isBlank);
            assert (solution.S.size() == S_auts.size());
            assert (solution.S.stream().map(AUT::toString).allMatch(S_auts::contains));
        }
        if (r != null) {
            assert (Substitution.apply(solution.substitutions, Term.VAR_0).toString().equals(r));
        }
        if (alpha1 > 0 || MathUtils.close(alpha1, 0)) {
            assert (MathUtils.close(alpha1, solution.alpha1));
        }
        if (alpha2 > 0 || MathUtils.close(alpha2, 0)) {
            assert (MathUtils.close(alpha2, solution.alpha2));
        }
    }
}
