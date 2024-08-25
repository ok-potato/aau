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
    private TNorm t_norm = Math::min;
    private boolean linear = true, witness = true;
    
    public void solve() {
        Algorithm.solve(this.problem, this.proximityRelations, this.lambda, this.t_norm, this.linear, this.witness);
    }
    
    // CONSTRUCTORS
    
    public ProblemConfig(Pair<Term, Term> problem) {
        this.problem = problem;
    }
    
    public ProblemConfig(String problem) {
        this(Parser.parseProblem(problem));
    }
    
    // CHAINING METHODS
    
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
    
    public ProblemConfig t_norm(TNorm t_norm) {
        this.t_norm = t_norm;
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
