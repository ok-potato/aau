import at.jku.risc.uarau.Algorithm;
import at.jku.risc.uarau.ProblemConfig;
import at.jku.risc.uarau.data.Solution;
import at.jku.risc.uarau.util.DataUtil;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static at.jku.risc.uarau.Algorithm.solve;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class SigmaTest extends BaseTest {
    @Test
    public void small() {
        String problem = "f(a, b) ?= g(a, c, d)";
        String relations = "h {1 1, 3 2}[0.7] f ; h g {1 1, 3 3}[0.8] ; f g {1 1, 2 1}[0.1]";
        solve(problem, relations, 0.5f);
    }
    
    @Test
    public void medium() {
        String problem = "f(h(a, b(), c(), d()), b()) ?= g(f(a, b()), b(), c())";
        String relations = "h {1 1, 3 2, 4 2}[0.7] f; h g {1 1, 3 3}[0.8]; c d {}[0.6]";
        solve(problem, relations, 0.5f);
    }
    
    @Test
    public void big() { // |f| = 2  |g| = 3  |h| = 3
        String l_h1 = "h( a(), b(), f(a(),b()) )";
        String l_g1 = String.format("g( c(), d(), %s )", l_h1);
        String l_h2 = String.format("h( f(c(),d()), %s, c() )", l_g1);
        String lhs = String.format("f( %s, b() )", l_h2);
        
        String r_g1 = "g( a(), c(), f(a(),b()) )";
        String r_h1 = String.format("h( g(c(),e(),a()), %s, d() )", r_g1);
        String r_f1 = String.format("f( %s, c() )", r_h1);
        String rhs = String.format("g( g(a(),b(),d()), %s, d() )", r_f1);
        
        String problem = String.format("%s ?= %s", lhs, rhs);
        String relations = "h f {1 1, 2 1, 3 2)}[0.7] ; c d {}[0.6] ; b c {}[0.9] ; f g [0.9] {1 2, 2 3, 2 1)}";
        
        new ProblemConfig(problem).proximityRelations(relations).lambda(0.5f).linear(false).witness(false).solve();
    }
    
    @Test
    public void example5() {
        String relations = "a b [0.9]{} ; b c [0.8]{} ; h f [0.7]{1 1, 1 2} ; h g [0.6]{1 1}";
        
        Set<Solution> solutions1 = Algorithm.solve("f(a(), c()) ?= g(a())", relations, 0.5f);
        assert solutions1.size() == 1;
        check(DataUtil.getAny(solutions1), "..", "..", "h(b())", 0.7f, 0.6f);
        
        Set<Solution> solutions2 = Algorithm.solve("f(a(), d()) ?= g(a())", relations, 0.5f);
        assert solutions2.size() == 1;
        check(DataUtil.getAny(solutions2), null, null, "0", 1.0f, 1.0f);
    }
    
    @Test
    public void example6() {
        String problem = "f(a(), b()) ?= g(a(), c(), d())";
        String relations = "b c [0.5]{} ; c d [0.6]{} ; h f [0.7]{1 1, 3 2, 4 2} ; h g [0.8]{1 1, 3 3}";
        solve(problem, relations, 0.5f);
        
        String relationsSol = "b c [0.5]{} ; c d [0.6]{} ; h f [0.7]{1 1, 1 2, 4 2} ; h g [0.8]{1 1, 3 3}";
        solve(problem, relationsSol, 0.5f);
    }
    
    @Test
    public void example7() {
        String problem = "p(f1(a), g1(b)) ?= p(f2(a), g2(b))";
        String relations = "f1 h1 [0.6]{1 1} ; f2 h2 [0.7]{1 1} ; g1 h1 [0.8]{1 2} ; g2 h2 [0.9]{1 2}";
        solve(problem, relations, 0.5f);
    }
    
    @Test
    public void variableTerms() {
        solve("a() ?= b()", "", 0.5f);
        solve("a ?= b()", "", 0.5f);
        solve("a() ?= b", "", 0.5f);
        solve("a ?= b", "", 0.5f);
    }
    
    @Test
    public void parseArities() {
        solve("f(a()) ?= f(b())", "", 0.5f);
        assertThrows(IllegalArgumentException.class, () -> solve("a() ?= a", "", 0.5f));
        assertThrows(IllegalArgumentException.class, () -> solve("a ?= b()", "a b [0.6]{}", 0.5f));
        assertThrows(IllegalArgumentException.class, () -> solve("a() ?= a()", "a a [0.6]{}", 0.5f));
        assertThrows(IllegalArgumentException.class, () -> solve("a() ?= b(c())", "a b [0.6]{1 1}", 0.5f));
        assertThrows(IllegalArgumentException.class, () -> solve("a() ?= b(c())", "a b [0.6]{} ; a b [0.6]{}", 0.5f));
    }
    
    @Test
    public void badSyntax() {
        // terms
        assertThrows(IllegalArgumentException.class, () -> solve(") ?= g()", "", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> solve("f( ?= g()", "", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> solve("f()", "", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> solve("f(()) ?= g()", "", 1.0f));
        // proximity relations
        assertThrows(IllegalArgumentException.class, () -> solve("f() ?= g()", "f g", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> solve("f() ?= g()", "f g {}", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> solve("f() ?= g()", "f g {}[]", 1.0f));
        // argument relations
        assertThrows(IllegalArgumentException.class, () -> solve("f() ?= g()", "f g {1}[1.0]", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> solve("f() ?= g()", "f g {(1, )}[1.0]", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> solve("f() ?= g()", "f g {f}[1.0]", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> solve("f() ?= g()", "fg {}[1.0]", 1.0f));
    }
    
    @Test
    public void acceptableSyntaxProximityRelations() {
        // no relations
        solve("f(a(), b()) ?= g(a(), c(), d())", "", 1.0f);
        solve("f(a, b) ?= g(a, c, d)", " ; ", 1.0f);
        // empty relations
        solve("f() ?= g()", "f g {}[1.0]", 1.0f);
        solve("f() ?= g()", "f g {} [1.0]", 1.0f);
        solve("f() ?= g()", "f  {} g[1.0]", 1.0f);
        solve("f() ?= g()", "  f  [1.0]{}g", 1.0f);
        // alternative arg map syntax
        solve("f(a(), b()) ?= g(c)", "f g [1.0] {1, 1, 2, 1}", 1.0f);
        solve("f(a, b) ?= g(c())", "f g [1.0] {(1 1)(2, 1)}", 1.0f);
        solve("f(a(), b) ?= g(c())", "f g [1.0] {1 1 2 1}", 1.0f);
        solve("f(a(), b) ?= g(c)", "f g [1.0] {) ) ()() ,,  1, , ))( 1 )))))))))))))2   ,,), 1}", 1.0f);
    }
    
    @Test
    public void idRelation() {
        assertThrows(IllegalArgumentException.class, () -> solve("f() ?= g()", "f f [0.9f]{}", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> solve("f() ?= g()", "f f [0.9f]{}", 0.8f));
        assertThrows(IllegalArgumentException.class, () -> solve("f(a) ?= g()", "f f [1.0f]{}", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> solve("f(a, b) ?= g()", "f f [1.0f]{(1, 1), (1, 2)}", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> solve("f(a, b) ?= g()", "f f [1.0f]{(1, 1), (2, 2)}", 1.0f));
    }
}
