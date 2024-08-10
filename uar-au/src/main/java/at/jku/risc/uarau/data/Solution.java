package at.jku.risc.uarau.data;

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
        return String.format("🔅 %s 🧿%s 🧿%s ⚫ %s, %s", r, sigma1, sigma2, alpha1, alpha2);
    }
}
