package at.jku.risc.uarau.data;

import java.util.*;

public class Solution {
    public final Term r;
    public final Set<Term> sigma1, sigma2;
    public final float alpha1, alpha2;
    
    public Solution(Term r, Collection<Term> sigma1, Deque<Term> sigma2, float alpha1, float alpha2) {
        this.r = r;
        this.sigma1 = Collections.unmodifiableSet(new HashSet<>(sigma1));
        this.sigma2 = Collections.unmodifiableSet(new HashSet<>(sigma2));
        this.alpha1 = alpha1;
        this.alpha2 = alpha2;
    }
}
