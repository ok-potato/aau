import at.jku.risc.uarau.Algorithm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SigmaTest {
    @Test
    public void simple() {
        String problem = "f(a,b) ?= g(a,c,d)";
        String relations = "h {(1,1),(3,2),(4,2)}[0.7] f; h g {(1,1),(3,3)}[0.8]";
        Algorithm.solve(problem, relations, 0.5f);
    }
    
        @Test
    public void pimple() {
        String problem = "f(h(a,b,c,d),b) ?= g(f(a,b),f(b,c),f(c,d))";
        String relations = "h {(1,1),(3,2),(4,2)}[0.7] f; h g {(1,1),(3,3)}[0.8]; c d {}[0.6]";
        Algorithm.solve(problem, relations, 0.5f);
    }
    
    @Test
    public void badSyntax() {
        // terms
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve(")", "", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve("f(", "", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve("f()", "", 1.0f));
        // proximity relations
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve("f() ?= g()", "f g", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve("f() ?= g()", "f g {}", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve("f() ?= g()", "f g {}[]", 1.0f));
        // argument relations
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve("f() ?= g()", "f g {1}[1.0]", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve("f() ?= g()", "f g {(1, )}[1.0]", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve("f() ?= g()", "f g {f}[1.0]", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve("f() ?= g()", "fg {}[1.0]", 1.0f));
    }
    
    @Test
    public void acceptableSyntaxProximityRelations() {
        // no relations
        Algorithm.solve("f(a,b) ?= g(a,c,d)", "", 1.0f);
        Algorithm.solve("f(a,b) ?= g(a,c,d)", " ; ", 1.0f);
        // empty relations
        Algorithm.solve("f() ?= g()", "f g {}[1.0]", 1.0f);
        Algorithm.solve("f() ?= g()", "f g {} [1.0]", 1.0f);
        Algorithm.solve("f() ?= g()", "f  {} g[1.0]", 1.0f);
        Algorithm.solve("f() ?= g()", "  f  [1.0]{}g", 1.0f);
        // alternative arg map syntax
        Algorithm.solve("f(a,b) ?= g(c)", "f g [1.0] {1,1,2,1}", 1.0f);
        Algorithm.solve("f(a,b) ?= g(c)", "f g [1.0] {(1 1)(2,1)}", 1.0f);
        Algorithm.solve("f(a,b) ?= g(c)", "f g [1.0] {1 1 2 1}", 1.0f);
        Algorithm.solve("f(a,b) ?= g(c)", "f g [1.0] {) ) ()() ,,  1, , ))( 1 )))))))))))))2   ,,), 1}", 1.0f);
    }
    
    @Test
    public void idRelation() {
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve("f() ?= g()", "f f [0.9f]{}", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve("f() ?= g()", "f f [0.9f]{}", 0.8f));
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve("f(a) ?= g()", "f f [1.0f]{}", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve("f(a,b) ?= g()", "f f [1.0f]{(1,1),(1,2)}", 1.0f));
        Algorithm.solve("f(a,b) ?= g()", "f f [1.0f]{(1,1),(2,2)}", 1.0f);
    }
}
