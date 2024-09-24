package at.jku.risc.uarau;

import at.jku.risc.uarau.data.ProximityRelation;
import at.jku.risc.uarau.data.Solution;
import at.jku.risc.uarau.data.Term;
import at.jku.risc.uarau.util.Pair;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Problem {
    private final Pair<Term, Term> equation;
    private Collection<ProximityRelation> proximityRelations = new HashSet<>();
    private float lambda = 1.0f;
    private TNorm tNorm = Math::min;
    private boolean merge = true, witness = true;
    
    /**
     * Run the {@link Algorithm} on the defined problem.
     * <br>
     * The {@link Problem} is not consumed, and can be further modified for additional calls to this method.
     * @return the calculated solutions according to the problem definition
     */
    public Set<Solution> solve() {
        return Algorithm.solve(this.equation, this.proximityRelations, this.lambda, this.tNorm, this.merge, this.witness);
    }
    
    // *** constructors ***
    
    /**
     * Create a {@link Problem} instance which can be solved through the {@link Algorithm}
     * <br><br>
     * Define the problem equation via instantiated objects.
     * <br><br>
     * Define the problem further via chaining with
     * {@link Problem#proximityRelations(Collection)}, {@link Problem#lambda(float)}, {@link Problem#tNorm(TNorm)},
     * {@link Problem#merge(boolean)} and {@link Problem#witness(boolean)}
     */
    
    public Problem(Pair<Term, Term> equation) {
        this.equation = equation;
    }
    
    /**
     * Create a {@link Problem} instance which can be solved through the {@link Algorithm}
     * <br><br>
     * Define the problem equation via String representation.
     * <br>
     * The equation consists of two sides, separated by "?="
     * <br>
     * Each side of the equation is a term, written in typical syntax.
     * <br>
     * Function/variable symbols cannot include whitespace, or the characters '(', ')', ','
     * <br><br>
     * Example equation: "f(a, b) ?= g(h(c))"
     * <br><br>
     * Define the problem further via chaining with
     * {@link Problem#proximityRelations(Collection)}, {@link Problem#lambda(float)}, {@link Problem#tNorm(TNorm)},
     * {@link Problem#merge(boolean)} and {@link Problem#witness(boolean)}
     * <br><br>
     * @see Problem#Problem(Pair)
     */
    public Problem(String equation) {
        this(Parser.parseEquation(equation));
    }
    
    // *** chaining methods ***
    
    /**
     * Define proximity relations via instantiated objects.
     * <br><br>
     * For each pair of functions f and g with proximity > 0, a {@link ProximityRelation} is needed,
     * which describes the proximity, as well as the argument relation from f to g.
     * <br>
     * The proximity relation is symmetrical, therefore it is necessary and sufficient to define the relation in one of the two possible directions.
     * <br><br>
     * A proximity relation with proximity < lambda doesn't factor into the calculation.
     * Any relation which is not defined is assumed to be of proximity = 0 (and thus below any lambda cut).
     */
    public Problem proximityRelations(Collection<ProximityRelation> relations) {
        this.proximityRelations = relations;
        return this;
    }
    
    /**
     * Define proximity relations via String representation, according to the following format:
     * <ul>
     *     <li> proximity relations are separated by semicolon ( ; )
     *     <li> a proximity relation follows the format "f g [prox] { f_arg_1 g_arg_1, f_arg_2 g_arg_2, ... }"
     *     <li> "prox" is the proximity between functions f and g, in the range [0,1]
     *     <li> "f_arg_n" and "g_arg_n" are argument positions in f and g respectively, which map onto the other
     * </ul>
     * Example of a valid set of proximity relation: "f h [0.4] { 1 1, 1 2 } ; g h [0.5] { 1 2, 2 2, 3 1 }"
     * <br><br>
     * @see Problem#proximityRelations(Collection)
     */
    public Problem proximityRelations(String relations) {
        return proximityRelations(Parser.parseProximityRelations(relations));
    }
    
    /**
     * Define the lambda-cut within the range [0,1]
     * <br>
     * i.e. the minimum value a proximity needs to be counted as 'close'.
     *
     * @param lambda default: 1.0
     */
    public Problem lambda(float lambda) {
        if (lambda < 0.0f || lambda > 1.0f) {
            throw new IllegalArgumentException("Lambda must be in range [0,1]");
        }
        this.lambda = lambda;
        return this;
    }
    
    /**
     * Define the lambda-cut via String representation.
     * @see Problem#lambda(float)
     */
    public Problem lambda(String lambda) {
        return lambda(Parser.parseProximity(lambda));
    }
    
    /**
     * Define a custom bi-function to serve as the T-norm, i.e. the function with which to combine two proximities.
     * <br>
     * To ensure correctness, the function must follow the
     * <a href="https://en.wikipedia.org/wiki/T-norm">definition of T-norm</a>
     *
     * @param tNorm default: {@link Math#min(float, float)}
     */
    public Problem tNorm(TNorm tNorm) {
        this.tNorm = tNorm;
        return this;
    }
    
    /**
     * Define if the algorithm should try merging compatible solutions.
     * <br>
     * When set to false, some computation steps can be skipped.
     * When this and {@link Problem#witness} are both set to 'false', the 'expand' step is skipped, which is generally the most expensive.
     *
     * @param merge default: true
     */
    public Problem merge(boolean merge) {
        this.merge = merge;
        return this;
    }
    
    /**
     * Define if the algorithm should produce witness substitutions.
     * <br>
     * When set to false, some computation steps can be skipped.
     * When this and {@link Problem#merge} are both set to 'false', the 'expand' step is skipped, which is generally the most expensive.
     *
     * @param witness default: true
     */
    public Problem witness(boolean witness) {
        this.witness = witness;
        return this;
    }
}
