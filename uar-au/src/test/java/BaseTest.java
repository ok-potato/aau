import at.jku.risc.uarau.data.Solution;

public abstract class BaseTest {
    protected static void check(Solution solution, String sigma1, String sigma2, String r, float alpha1, float alpha2) {
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
