import at.jku.risc.uarau.data.Solution;
import at.jku.risc.uarau.util.ANSI;

public abstract class BaseTest {
    protected static void check(Solution solution, String lhs, String rhs, String r, float alpha1, float alpha2) {
        boolean wasEnabled = ANSI.enabled;
        ANSI.enabled = false;
        if (r != null) {
            assert solution.generalization.toString().equals(r);
        }
        ANSI.enabled = wasEnabled;
        
        if (lhs != null) {
            assert solution.lhs != null;
            assert solution.lhs.toString().equals(lhs);
        }
        if (rhs != null) {
            assert solution.rhs != null;
            assert solution.rhs.toString().equals(rhs);
        }
        
        if (alpha1 > 0 || close(alpha1, 0)) { // alpha1 >= 0
            assert close(alpha1, solution.alpha1);
        }
        if (alpha2 > 0 || close(alpha2, 0)) { // alpha2 >= 0
            assert close(alpha2, solution.alpha2);
        }
    }
    
    protected static boolean close(double a, double b) {
        return Math.abs(a - b) < 0.00001f * Math.abs(a + b) / 2;
    }
}
