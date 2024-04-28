package at.jku.risc.uarau.algorithm;

import at.jku.risc.uarau.data.ProximityMap;
import at.jku.risc.uarau.data.Term;

public class Problem {
    Term lhs, rhs;
    ProximityMap R;
    float lambda;
    
    public Problem(Term lhs, Term rhs, ProximityMap r, float lambda) {
        this.lhs = lhs;
        this.rhs = rhs;
        R = r;
        this.lambda = lambda;
    }
    
    public static Problem parse(String problem, String relations, String lambda) {
        String[] problemTerms = problem.split("=\\^=");
        if (problemTerms.length != 2) {
            throw new IllegalArgumentException("Problem must contain two terms separated by '=^='");
        }
        return new Problem(Term.parse(problemTerms[0]), Term.parse(problemTerms[1]), ProximityMap.parse(relations), Float.parseFloat(lambda));
    }
    
    @Override
    public String toString() {
        return STR."Problem:\n  \{lhs}  =^=  \{rhs}\n  R: \{R}\n  λ: \{lambda}";
    }
}
