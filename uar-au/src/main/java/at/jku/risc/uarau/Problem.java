package at.jku.risc.uarau;

import at.jku.risc.uarau.data.ProximityRelation;
import at.jku.risc.uarau.data.Solution;
import at.jku.risc.uarau.data.term.GroundTerm;
import at.jku.risc.uarau.util.Pair;
import at.jku.risc.uarau.util.Util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Problem {
    private final Pair<GroundTerm, GroundTerm> equation;
    private Collection<ProximityRelation> proximityRelations = new HashSet<>();
    private float lambda = 1.0f;
    private TNorm tNorm = Math::min;
    private boolean merge = true, witness = true;
    
    /**
     * Run the {@linkplain Algorithm} on the defined problem.
     * <br>
     * The Problem is not consumed, so it can be modified and reused for subsequent calculations if desired.
     *
     * @return the set of possible solutions to the defined problem
     */
    public Set<Solution> solve() {
        return Algorithm.solve(this.equation, this.proximityRelations, this.lambda, this.tNorm, this.merge, this.witness);
    }
    
    // *** constructors ***
    
    /**
     * Create a {@linkplain Problem} instance based on a predefined equation.
     * <br><br>
     * Define the problem further via chaining with
     * <br>
     * {@linkplain Problem#proximityRelations(Collection)} || {@linkplain Problem#lambda(float)} || {@linkplain Problem#tNorm(TNorm)} ||
     * {@linkplain Problem#merge(boolean)} || {@linkplain Problem#witness(boolean)}
     * <br><br>
     * Finally, call {@linkplain Problem#solve()} to run the {@linkplain Algorithm} against the Problem.
     */
    
    public Problem(Pair<GroundTerm, GroundTerm> equation) {
        this.equation = equation;
    }
    
    /**
     * Create a {@linkplain Problem} instance based on an equation in String representation:
     * <br><br>
     * The equation consists of two sides, separated by "?="
     * <br>
     * Each side of the equation is a term, written in typical syntax.
     * <br>
     * function/variable names can't contain whitespace or '( ) ,'
     * <br><br>
     * <code>
     *     Example: "f(x1, a()) ?= g(h(x2))"
     * </code>
     * <br><br>
     * Define the problem further via chaining with
     * <br>
     * {@linkplain Problem#proximityRelations(Collection)} || {@linkplain Problem#lambda(float)} || {@linkplain Problem#tNorm(TNorm)} ||
     * {@linkplain Problem#merge(boolean)} || {@linkplain Problem#witness(boolean)}
     * <br><br>
     * Finally, call {@linkplain Problem#solve()} to run the {@linkplain Algorithm} against the Problem.
     */
    public Problem(String equation) {
        this(Parser.parseEquation(equation));
    }
    
    // *** chaining methods ***
    
    /**
     * Define the set of (non-zero) <b>proximity relations</b>
     * <br><br>
     * For each function pair <b>f, g</b> with a <b>proximity > 0</b>, include a {@linkplain ProximityRelation},
     * either <b>f</b>-><b>g</b>, or <b>g</b>-><b>f</b>
     * <br>
     * (not both, though)
     * <br><br>
     * A proximity relation that isn't in the set is assumed to be of <b>proximity = 0</b>, and thus below all possible lambda-cuts,
     * which means it can't affect the calculation.
     *<br>
     * (If you have a <b>fixed lambda</b>, you can also omit relations with <b>proximity < lambda</b> for similar reasons)
     */
    public Problem proximityRelations(Collection<ProximityRelation> relations) {
        this.proximityRelations = relations;
        return this;
    }
    
    /**
     * Define the set of <b>proximity relations</b> (see {@linkplain Problem#proximityRelations(Collection)}) via String representation:
     * <ul>
     *     <li> proximity relations are separated by ' <b>;</b> ', and follow the format
     *     <br>
     *     <code>
     *          f g [prox] { f_1 g_1 f_2 g_2 ... }
     *     </code> where...
     *     <li> <b>prox</b> is the proximity between functions <b>f</b> and <b>g</b>, in the range [0,1]
     *     <li> <b>f_n</b> and <b>g_n</b> are <b>1-indexed</b> argument positions of <b>f</b> and <b>g</b> respectively, which map onto each other
     * </ul>
     * Argument relations can optionally include '<b>(...)</b>' and ' <b>,</b> ' for readability.
     * <br><br>
     * <code>
     * Example 1: "f h [0.9] { (1 1), (1 2) } ; g h [0.5] { (1 2), (2 2), (3 1) }"
     * <br>
     * Example 2: "f h [0.9] { 1 1 1 2 } ; g h [0.5] { 1 2 2 2 3 1 }"
     * </code>
     */
    public Problem proximityRelations(String relations) {
        return proximityRelations(Parser.parseProximityRelations(relations));
    }
    
    /**
     * Define the lambda-cut within the range [0,1]
     * <br>
     * The lambda-cut defines the threshold above which proximites are considered 'close'.
     *
     * @param lambda default: <b>1.0</b>
     */
    public Problem lambda(float lambda) {
        if (lambda < 0.0f || lambda > 1.0f) {
            throw Util.except("Lambda must be in range [0,1]");
        }
        this.lambda = lambda;
        return this;
    }
    
    /**
     * Define a custom bi-function for composing two proximities, i.e.:
     * <pre>{@code a~c = tNorm(a~b, b~c)}</pre>
     * The function must follow the definition of
     * <a href="https://en.wikipedia.org/wiki/T-norm">triangular norm</a>
     *
     * @param tNorm default: <b>{@linkplain Math#min(float, float)}</b>
     */
    public Problem tNorm(TNorm tNorm) {
        this.tNorm = tNorm;
        return this;
    }
    
    /**
     * Define if the algorithm should try merging compatible solutions.
     * <br>
     * Otherwise, we can skip some computation steps.
     * <br>
     * If this and {@linkplain Problem#witness} are both <b>false</b>,
     * the <b>expand</b> step is skipped, which is usually the most expensive.
     *
     * @param merge default: <b>true</b>
     */
    public Problem merge(boolean merge) {
        this.merge = merge;
        return this;
    }
    
    /**
     * Define if the algorithm should produce witness substitutions.
     * <br>
     * Otherwise, we can skip some computation steps.
     * <br>
     * If this and {@linkplain Problem#merge} are both <b>false</b>,
     * the <b>expand</b> step is skipped, which is usually the most expensive.
     *
     * @param witness default: <b>true</b>
     */
    public Problem witness(boolean witness) {
        this.witness = witness;
        return this;
    }
}
