package at.jku.risc.uarau;

import at.jku.risc.uarau.impl.Algorithm;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.slf4j.Log4jLogger;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.Set;

public class AlgorithmTest {
    public static String bigEquation() { // |f| = 2  |g| = 3  |h| = 3
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
    
    public static String bigRelations() {
        return "h f [0.7] {1 1, 2 1, 3 2)} ; c d [0.6] {} ; b c [0.9] {} ; f g [0.9] {1 2, 2 3, 2 1)}";
    }
    
    @Test
    public void small() {
        String equation = "f(a, b) ?= g(a, c, d)";
        String relations = "h f [0.7] {1 1, 3 2} ; h g [0.8] {1 1, 3 3} ; f g [0.1] {1 1, 2 1}";
        
        Problem problem = new Problem(equation).proximityRelations(relations).lambda(0.5f);
        TestUtils.verify(problem);
    }
    
    @Test
    public void medium() {
        String equation = "f(h(a, b(), c(), d()), b()) ?= g(f(a, b()), b(), c())";
        String relations = "h f [0.7] {1 1, 3 2, 4 2} ; h g [0.8] {1 1, 3 3} ; c d [0.6] {}";
        
        Problem problem = new Problem(equation).proximityRelations(relations).lambda(0.5f);
        TestUtils.verify(problem);
    }
    
    @Test
    public void big() {
        String equation = bigEquation();
        String relations = bigRelations();
        
        Problem problem = new Problem(equation).proximityRelations(relations).lambda(0.5f).merge(true).witnesses(false);
        TestUtils.verify(problem);
    }
    
    // @Test
    public void benchmark() {
        String problem = bigEquation();
        String relations = bigRelations();
        
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
    
    // *** examples from the paper ***
    
    @Test
    public void example5() {
        String relations = "a b [0.9]{} ; b c [0.8]{} ; h f [0.7]{1 1, 1 2} ; h g [0.6]{1 1}";
        
        Problem problem1 = new Problem("f(a(), c()) ?= g(a())").proximityRelations(relations).lambda(0.5f);
        Set<Solution> solutions1 = TestUtils.verify(problem1);
        assert solutions1.size() == 1;
        // TODO TestUtils.check(Data.getAny(solutions1), "..", "..", "h(b())", 0.7f, 0.6f);
        
        Problem problem2 = new Problem("f(a(), d()) ?= g(a())").proximityRelations(relations).lambda(0.5f);
        Set<Solution> solutions2 = TestUtils.verify(problem2);
        assert solutions2.size() == 1;
        // TODO TestUtils.check(Data.getAny(solutions2), null, null, "0", 1.0f, 1.0f);
    }
    
    @Test
    public void example6() {
        String equation = "f(a(), b()) ?= g(a(), c(), d())";
        String relations = "b c [0.5] {} ; c d [0.6] {} ; h f [0.7] {1 1, 3 2, 4 2} ; h g [0.8] {1 1, 3 3}";
        Problem problem = new Problem(equation).proximityRelations(relations).lambda(0.5f);
        TestUtils.verify(problem);
        
        String relationsSol = "b c [0.5] {} ; c d [0.6] {} ; h f [0.7] {1 1, 1 2, 4 2} ; h g [0.8] {1 1, 3 3}";
        Problem problemSol = new Problem(equation).proximityRelations(relationsSol).lambda(0.5f);
        TestUtils.verify(problemSol);
    }
    
    @Test
    public void example7() {
        String equation = "p(f1(a), g1(b)) ?= p(f2(a), g2(b))";
        String relations = "f1 h1 [0.6] {1 1} ; f2 h2 [0.7] {1 1} ; g1 h1 [0.8] {1 2} ; g2 h2 [0.9] {1 2}";
        Problem problem = new Problem(equation).proximityRelations(relations).lambda(0.5f);
        TestUtils.verify(problem);
    }
    
    @Test
    public void variableTerms() {
        TestUtils.verify(new Problem("a() ?= b()").lambda(0.5f));
        TestUtils.verify(new Problem("a ?= b()").lambda(0.5f));
        TestUtils.verify(new Problem("a ?= b").lambda(0.5f));
    }
}
