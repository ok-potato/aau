package at.jku.risc.uarau;

import at.jku.risc.uarau.data.ProximityRelation;
import at.jku.risc.uarau.data.Term;
import at.jku.risc.uarau.util.Pair;

import java.util.Collection;
import java.util.HashSet;

public class ProblemConfig {
    private final Pair<Term, Term> problem;
    private Collection<ProximityRelation> proximityRelations = new HashSet<>();
    private float lambda = 1.0f;
    private TNorm tNorm = Math::min;
    private boolean linear = true, witness = true;
    
    public void solve() {
        Algorithm.solve(this.problem, this.proximityRelations, this.lambda, this.tNorm, this.linear, this.witness);
    }
    
    // *** constructors ***
    
    public ProblemConfig(Pair<Term, Term> problem) {
        this.problem = problem;
    }
    
    public ProblemConfig(String problem) {
        this(Parser.parseProblem(problem));
    }
    
    // *** chaining methods ***
    
    public ProblemConfig proximityRelations(Collection<ProximityRelation> relations) {
        this.proximityRelations = relations;
        return this;
    }
    
    public ProblemConfig proximityRelations(String relations) {
        this.proximityRelations = Parser.parseProximityRelations(relations);
        return this;
    }
    
    public ProblemConfig lambda(float lambda) {
        this.lambda = lambda;
        return this;
    }
    
    public ProblemConfig tNorm(TNorm tNorm) {
        this.tNorm = tNorm;
        return this;
    }
    
    public ProblemConfig linear(boolean linear) {
        this.linear = linear;
        return this;
    }
    
    public ProblemConfig witness(boolean witness) {
        this.witness = witness;
        return this;
    }
}
