import at.jku.risc.uarau.data.Solution;
import at.jku.risc.uarau.util.MathUtil;

public abstract class BaseTest {
    protected void check(Solution solution, String A, String S, String r, float alpha1, float alpha2) {
        if (r != null) {
            assert solution.r.toString().equals(r);
        }
        if (alpha1 > 0 || MathUtil.close(alpha1, 0)) {
            assert MathUtil.close(alpha1, solution.alpha1);
        }
        if (alpha2 > 0 || MathUtil.close(alpha2, 0)) {
            assert MathUtil.close(alpha2, solution.alpha2);
        }
    }
}
