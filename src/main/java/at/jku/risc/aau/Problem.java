package at.jku.risc.aau;

import at.jku.risc.aau.impl.Algorithm;
import at.jku.risc.aau.impl.Parser;
import at.jku.risc.aau.term.GroundTerm;
import at.jku.risc.aau.tnorm.CommonTNorms;
import at.jku.risc.aau.tnorm.TNorm;
import at.jku.risc.aau.util.Pair;
import at.jku.risc.aau.util.Panic;

import java.util.*;

/**
 * For a given anti-unification problem, create a {@linkplain Problem} object, and call
 * {@linkplain Problem#solve()} on it to execute the {@linkplain Algorithm}.
 */
public class Problem {
    private final Pair<GroundTerm, GroundTerm> equation;
    private Collection<ProximityRelation> proximityRelations = new HashSet<>();
    private Map<String, Integer> definedArities = new HashMap<>();
    private FuzzySystem customFuzzySystem = null;
    private float lambda = 1.0f;
    private TNorm tNorm = CommonTNorms.minimum;
    private boolean merge = true, witnesses = true;
    
    // *** constructors ***
    
    /**
     * Create a {@linkplain Problem} instance based on a predefined equation.
     * <br><br>
     * Define the problem further via chaining with
     * <br>
     * {@linkplain Problem#proximityRelations(Collection)} || {@linkplain Problem#lambda(float)} || {@linkplain Problem#tNorm(TNorm)} ||
     * {@linkplain Problem#merge(boolean)} || {@linkplain Problem#witnesses(boolean)}
     * <br><br>
     * Finally, call {@linkplain Problem#solve()} to run the {@linkplain Algorithm} against the Problem.
     */
    
    public Problem(Pair<GroundTerm, GroundTerm> equation) {
        this.equation = equation;
    }
    
    /**
     * Create a {@linkplain Problem} instance based on predefined sides of the equation.
     * (See also {@linkplain Problem#Problem(Pair)})
     */
    public Problem(GroundTerm lhs, GroundTerm rhs) {
        this(Pair.of(lhs, rhs));
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
     * Example: "f(x1, a()) ?= g(h(x2))"
     * </code>
     * <br><br>
     * Define the problem further via chaining with
     * <br>
     * {@linkplain Problem#proximityRelations(Collection)} || {@linkplain Problem#lambda(float)} || {@linkplain Problem#tNorm(TNorm)} ||
     * {@linkplain Problem#merge(boolean)} || {@linkplain Problem#witnesses(boolean)}
     * <br><br>
     * Finally, call {@linkplain Problem#solve()} to run the {@linkplain Algorithm} against the Problem.
     */
    public Problem(String equation) {
        this(Parser.parseEquation(equation));
    }
    
    /**
     * Create a {@linkplain Problem} instance from Strings representing each side of the equation
     * (See also {@linkplain Problem#Problem(String)})
     */
    public Problem(String lhs, String rhs) {
        this(Pair.of(Parser.parseTerm(lhs), Parser.parseTerm(rhs)));
    }
    
    public Pair<GroundTerm, GroundTerm> getEquation() {
        return equation;
    }
    
    // *** run ***
    
    /**
     * Run the {@linkplain Algorithm} with the defined problem and its current settings.
     *
     * @return the set of possible {@linkplain Solution}s to the defined problem
     */
    public Set<Solution> solve() {
        return Algorithm.solve(this);
    }
    
    // *** additional parameters ***
    
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
     * <br>
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
     * Argument relations can optionally include '<b>(...)</b>' and/or ' <b>,</b> ' for readability.
     * <br><br>
     * <code>
     * Example 1: "f h [0.9] {(1 1), (1 2)} ; g h [0.5] {(1 2), (2 2), (3 1)}"
     * <br>
     * Example 2: "f h [0.9] { 1 1 1 2 } ; g h [0.5] { 1 2 2 2 3 1 }"
     * </code>
     */
    public Problem proximityRelations(String relations) {
        if (customFuzzySystem != null) {
            throw Panic.arg("Ambiguous problem definition: cannot define both a custom fuzzy system and proximity relations or arities.");
        }
        return proximityRelations(Parser.parseProximityRelations(relations));
    }
    
    public Collection<ProximityRelation> getProximityRelations() {
        return proximityRelations;
    }
    
    /**
     * Explicitly define the arities of some (or all) functions.
     * <br>
     * This is usually unnecessary, except in the case described in the README section on arities.
     */
    public Problem arities(Map<String, Integer> arities) {
        if (customFuzzySystem != null) {
            throw Panic.arg("Ambiguous problem definition: cannot define both a custom fuzzy system and proximity relations or arities.");
        }
        this.definedArities = arities;
        return this;
    }
    
    public Map<String, Integer> getDefinedArities() {
        return this.definedArities;
    }
    
    /**
     * Provide a custom fuzzy logic system.
     * <br>
     * This is only needed if it's impractical to provide all relevant proximity relations upfront.
     * <br><br>
     * If a custom fuzzy system is provided, {@linkplain Problem#proximityRelations(Collection) proximityRelations}
     * and {@linkplain Problem#arities(Map) arities} no longer make sense as inputs.
     */
    public Problem customFuzzySystem(FuzzySystem customFuzzySystem) {
        if (!proximityRelations.isEmpty() || !definedArities.isEmpty()) {
            throw Panic.arg("Ambiguous problem definition: cannot define both a custom fuzzy system and proximity relations or arities.");
        }
        this.customFuzzySystem = customFuzzySystem;
        return this;
    }
    
    public FuzzySystem getCustomFuzzySystem() {
        return customFuzzySystem;
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
            throw Panic.arg("Lambda must be in range [0,1]");
        }
        this.lambda = lambda;
        return this;
    }
    
    public float getLambda() {
        return lambda;
    }
    
    /**
     * Define a custom {@linkplain TNorm#apply(float, float) bi-function} for composing two proximities, i.e.:
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
    
    public TNorm getTNorm() {
        return tNorm;
    }
    
    // *** settings ***
    
    /**
     * Define if the algorithm should try merging compatible solutions.
     * <br>
     * Otherwise, we can skip some computation steps.
     * <br>
     * If this and {@linkplain Problem#witnesses} are both <b>false</b>,
     * the <b>expand</b> step is skipped, which is usually the most expensive.
     *
     * @param merge default: <b>true</b>
     */
    public Problem merge(boolean merge) {
        this.merge = merge;
        return this;
    }
    
    public boolean wantsMerge() {
        return merge;
    }
    
    /**
     * Define if the algorithm should produce witness substitutions.
     * <br>
     * Otherwise, we can skip some computation steps.
     * <br>
     * If this and {@linkplain Problem#merge} are both <b>false</b>,
     * the <b>expand</b> step is skipped, which is usually the most expensive.
     *
     * @param witnesses default: <b>true</b>
     */
    public Problem witnesses(boolean witnesses) {
        this.witnesses = witnesses;
        return this;
    }
    
    public boolean wantsWitnesses() {
        return witnesses;
    }
}
