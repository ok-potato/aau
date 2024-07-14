package at.jku.risc.uarau.algorithm;

import at.jku.risc.uarau.data.Function;
import at.jku.risc.uarau.data.Term;
import at.jku.risc.uarau.data.Variable;

public class Problem {
    Term lhs, rhs;
    ProximityMap R;
    float lambda;
    public TNorm tNorm = Math::min;
    
    public Problem(Term lhs, Term rhs, ProximityMap R, float lambda) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.R = R;
        this.lambda = lambda;
        
        calcArities(this.lhs);
        calcArities(this.rhs);
        validateArities();
    }
    
    private void calcArities(Term t) {
        if (t instanceof Variable) {
            return;
        }
        
        Function f = (Function) t;
        int arity = f.arguments.size();
        
        if (R.arities.containsKey(f.head) && R.arities.get(f.head) != arity) {
            throw new IllegalStateException(String.format("Function symbol %s(...) shows up with multiple arities!", f.head));
        }
        R.arities.put(f.head, arity);
        
        for (Term arg : f.arguments) {
            calcArities(arg);
        }
    }
    
    private void validateArities() {
        for (String head : R.arities.keySet()) {
            R.proximityClass(head);
        }
    }
    
    public static Problem parse(String problem, String relations, String lambda) {
        String[] problemTerms = problem.split("=\\^=");
        if (problemTerms.length != 2) {
            throw new IllegalArgumentException("Problem must contain two terms separated by '=^='");
        }
        float lambdaParsed = Float.parseFloat(lambda);
        if (lambdaParsed < 0.0f || lambdaParsed > 1.0f) {
            throw new IllegalArgumentException("Lambda must be in range [0,1]");
        }
        return new Problem(Term.parse(problemTerms[0]), Term.parse(problemTerms[1]), ProximityMap.parse(relations, lambdaParsed), lambdaParsed);
    }
    
    @Override
    public String toString() {
        return String.format("Problem:\n  %s =^= %s\n  λ: %s\n  R: %s\n", lhs, rhs, lambda, R);
    }
}
