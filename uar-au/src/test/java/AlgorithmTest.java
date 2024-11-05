import at.jku.risc.uarau.Algorithm;
import at.jku.risc.uarau.Problem;
import at.jku.risc.uarau.Solution;
import at.jku.risc.uarau.util.Util;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.slf4j.Log4jLogger;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Set;

import static at.jku.risc.uarau.Algorithm.solve;

public class AlgorithmTest extends BaseTest {
    @Test
    public void small() {
        String problem = "f(a, b) ?= g(a, c, d)";
        String relations = "h f [0.7] {1 1, 3 2} ; h g [0.8] {1 1, 3 3} ; f g [0.1] {1 1, 2 1}";
        solve(problem, relations, 0.5f);
    }
    
    @Test
    public void medium() {
        String problem = "f(h(a, b(), c(), d()), b()) ?= g(f(a, b()), b(), c())";
        String relations = "h f [0.7] {1 1, 3 2, 4 2} ; h g [0.8] {1 1, 3 3} ; c d [0.6] {}";
        solve(problem, relations, 0.5f);
    }
    
    @Test
    public void big() {
        String problem = bigProblem();
        String relations = "h f [0.7] {1 1, 2 1, 3 2)} ; c d [0.6] {} ; b c [0.9] {} ; f g [0.9] {1 2, 2 3, 2 1)}";
        
        new Problem(problem).proximityRelations(relations).lambda(0.5f).merge(true).witnesses(false).solve();
    }
    
    // @Test
    public void benchmark() {
        String problem = bigProblem();
        String relations = "h f [0.7] {1 1, 2 1, 3 2)} ; c d [0.6] {} ; b c [0.9] {} ; f g [0.9] {1 2, 2 3, 2 1)}";
        
        try { // disable logging
            Log4jLogger algorithmLoggerImpl = (Log4jLogger) LoggerFactory.getLogger(Algorithm.class);
            Field loggerField = algorithmLoggerImpl.getClass().getDeclaredField("logger");
            loggerField.setAccessible(true);
            ((Logger) loggerField.get(algorithmLoggerImpl)).setLevel(Level.ERROR);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        
        long startTime = System.currentTimeMillis();
        for (int idx = 0; idx < 50; idx++) {
            new Problem(problem).proximityRelations(relations).lambda(0.5f).merge(true).witnesses(false).solve();
            new Problem(problem).proximityRelations(relations).lambda(0.5f).merge(true).witnesses(true).solve();
        }
        System.out.println(System.currentTimeMillis() - startTime);
    }
    
    private String bigProblem() { // |f| = 2  |g| = 3  |h| = 3
        String l_h1 = "h( a(), b(), f(a(),b()) )";
        String l_g1 = String.format("g( c(), d(), %s )", l_h1);
        String l_h2 = String.format("h( f(c(),d()), %s, c() )", l_g1);
        String lhs = String.format("f( %s, b() )", l_h2);
        
        String r_g1 = "g( a(), c(), f(a(),b()) )";
        String r_h1 = String.format("h( g(c(),e(),a()), %s, d() )", r_g1);
        String r_f1 = String.format("f( %s, c() )", r_h1);
        String rhs = String.format("g( g(a(),b(),d()), %s, d() )", r_f1);
        
        return String.format("%s ?= %s", lhs, rhs);
    }
    
    // *** examples from the paper ***
    
    @Test
    public void example5() {
        String relations = "a b [0.9]{} ; b c [0.8]{} ; h f [0.7]{1 1, 1 2} ; h g [0.6]{1 1}";
        
        Set<Solution> solutions1 = Algorithm.solve("f(a(), c()) ?= g(a())", relations, 0.5f);
        assert solutions1.size() == 1;
        check(Util.getAny(solutions1), "..", "..", "h(b())", 0.7f, 0.6f);
        
        Set<Solution> solutions2 = Algorithm.solve("f(a(), d()) ?= g(a())", relations, 0.5f);
        assert solutions2.size() == 1;
        check(Util.getAny(solutions2), null, null, "0", 1.0f, 1.0f);
    }
    
    @Test
    public void example6() {
        String problem = "f(a(), b()) ?= g(a(), c(), d())";
        String relations = "b c [0.5] {} ; c d [0.6] {} ; h f [0.7] {1 1, 3 2, 4 2} ; h g [0.8] {1 1, 3 3}";
        solve(problem, relations, 0.5f);
        
        String relationsSol = "b c [0.5] {} ; c d [0.6] {} ; h f [0.7] {1 1, 1 2, 4 2} ; h g [0.8] {1 1, 3 3}";
        solve(problem, relationsSol, 0.5f);
    }
    
    @Test
    public void example7() {
        String problem = "p(f1(a), g1(b)) ?= p(f2(a), g2(b))";
        String relations = "f1 h1 [0.6] {1 1} ; f2 h2 [0.7] {1 1} ; g1 h1 [0.8] {1 2} ; g2 h2 [0.9] {1 2}";
        solve(problem, relations, 0.5f);
    }
    
    @Test
    public void variableTerms() {
        solve("a() ?= b()", "", 0.5f);
        solve("a ?= b()", "", 0.5f);
        solve("a ?= b", "", 0.5f);
    }
}
