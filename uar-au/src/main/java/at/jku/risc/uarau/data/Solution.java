package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.ANSI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Solution {
    static Logger log = LoggerFactory.getLogger(Solution.class);
    
    public final Term r;
    public final Witness sigma1, sigma2;
    public final float alpha1, alpha2;
    
    public Solution(Term r, Witness sigma1, Witness sigma2, float alpha1, float alpha2) {
        this.r = r;
        assert (sigma1 == null) == (sigma2 == null);
        if (sigma1 != null) {
            this.sigma1 = sigma1;
            this.sigma2 = sigma2;
        } else {
            this.sigma1 = null;
            this.sigma2 = null;
        }
        this.alpha1 = alpha1;
        this.alpha2 = alpha2;
        log.info("{}", this);
    }
    
    @Override
    public String toString() {
        String sigma1_str = sigma1 == null ? "" : " 🧿 " + sigma1;
        String sigma2_str = sigma2 == null ? "" : " 🧿 " + sigma2;
        return String.format("🔅 %s%s%s " + ANSI.green("%s %s"), r, sigma1_str, sigma2_str, alpha1, alpha2);
    }
}
