import org.junit.jupiter.api.Test;

import static at.jku.risc.uarau.Algorithm.solve;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ParserTest extends BaseTest {
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
        assertThrows(IllegalArgumentException.class, () -> solve("f() ?= g()", "f g h {}[0.5]", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> solve("f() ?= g()", "f , {}[0.5]", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> solve("f() ?= g()", ") g {}[0.5]", 1.0f));
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
