import at.jku.risc.uarau.data.Solution;
import at.jku.risc.uarau.util.MathUtil;

public abstract class BaseTest {
    protected void check(Solution solution, String sigma1, String sigma2, String r, float alpha1, float alpha2) {
        if (r != null) {
            assert solution.r.toString(false).equals(r);
        }
        
        if (sigma1 != null) {
            assert solution.sigma1 != null;
            assert solution.sigma1.toString().equals(sigma1);
        }
        if (sigma2 != null) {
            assert solution.sigma2 != null;
            assert solution.sigma2.toString().equals(sigma2);
        }
        
        if (alpha1 > 0 || MathUtil.close(alpha1, 0)) { // alpha1 >= 0
            assert MathUtil.close(alpha1, solution.alpha1);
        }
        if (alpha2 > 0 || MathUtil.close(alpha2, 0)) { // alpha2 >= 0
            assert MathUtil.close(alpha2, solution.alpha2);
        }
    }
}
