package at.jku.risc.aau;

import at.jku.risc.aau.util.Data;
import at.jku.risc.aau.util.Pair;
import at.jku.risc.aau.util.Panic;
import org.junit.jupiter.api.Test;

import static at.jku.risc.aau.impl.Algorithm.solve;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InitializerTest {
    @Test
    public void arities() {
        solve("f(a()) ?= f(b())", "", 0.5f);
        assertThrows(IllegalArgumentException.class, () -> solve("a() ?= a", "", 0.5f));
        assertThrows(IllegalArgumentException.class, () -> solve("a ?= b()", "a b [0.6]{}", 0.5f));
        assertThrows(IllegalArgumentException.class, () -> solve("a ?= b", "a b [0.6]{}", 0.5f));
        
        assertThrows(IllegalArgumentException.class, () -> solve("a() ?= b(c())", "a b [0.6]{1 1}", 0.5f));
        
        new Problem("a() ?= b()").arities(Data.mapOf(Pair.of("a", 0))).solve();
        assertThrows(IllegalArgumentException.class,
                () -> new Problem("a() ?= b()").arities(Data.mapOf(Pair.of("a", 1))).solve());
        
        new Problem("f(a()) ?= b()").arities(Data.mapOf(Pair.of("h", 2))).proximityRelations("f h [0.5]{1 1}").solve();
        assertThrows(IllegalArgumentException.class,
                () -> new Problem("f(a()) ?= b()").arities(Data.mapOf(Pair.of("h", 1))).proximityRelations("f h [0.5]{1 2}").solve());
    }
    
    @Test
    public void badSyntax() {
        // terms
        assertThrows(Panic.ParseException.class, () -> solve(") ?= g()", "", 1.0f));
        assertThrows(Panic.ParseException.class, () -> solve("f( ?= g()", "", 1.0f));
        assertThrows(Panic.ParseException.class, () -> solve("f()", "", 1.0f));
        assertThrows(Panic.ParseException.class, () -> solve("f(()) ?= g()", "", 1.0f));
        // proximity relations
        assertThrows(Panic.ParseException.class, () -> solve("f() ?= g()", "f g", 1.0f));
        assertThrows(Panic.ParseException.class, () -> solve("f() ?= g()", "f g {}", 1.0f));
        assertThrows(Panic.ParseException.class, () -> solve("f() ?= g()", "f g {}[]", 1.0f));
        assertThrows(Panic.ParseException.class, () -> solve("f() ?= g()", "f g h {}[0.5]", 1.0f));
        assertThrows(Panic.ParseException.class, () -> solve("f() ?= g()", "f , {}[0.5]", 1.0f));
        assertThrows(Panic.ParseException.class, () -> solve("f() ?= g()", ") g {}[0.5]", 1.0f));
        // argument relations
        assertThrows(Panic.ParseException.class, () -> solve("f() ?= g()", "f g {1}[1.0]", 1.0f));
        assertThrows(Panic.ParseException.class, () -> solve("f() ?= g()", "f g {(1, )}[1.0]", 1.0f));
        assertThrows(Panic.ParseException.class, () -> solve("f() ?= g()", "f g {f}[1.0]", 1.0f));
        assertThrows(Panic.ParseException.class, () -> solve("f() ?= g()", "fg {}[1.0]", 1.0f));
    }
    
    @Test
    public void acceptableSyntaxProximityRelations() {
        // no relations
        solve("f(a(), b()) ?= g(a(), c(), d())", "", 1.0f);
        solve("f(a, b) ?= g(a, c, d)", " ; ", 1.0f);
        // empty relations
        solve("f() ?= g()", "f g {}[0.9]", 1.0f);
        solve("f() ?= g()", "f g {} [0.9]", 1.0f);
        solve("f() ?= g()", "f  {} g[0.9]", 1.0f);
        solve("f() ?= g()", "  f  [0.9]{}g", 1.0f);
        // alternative arg map syntax
        solve("f(a(), b()) ?= g(c)", "f g [0.9] {1, 1, 2, 1}", 1.0f);
        solve("f(a, b) ?= g(c())", "f g [0.9] {(1 1)(2, 1)}", 1.0f);
        solve("f(a(), b) ?= g(c())", "f g [0.9] {1 1 2 1}", 1.0f);
        solve("f(a(), b) ?= g(c)", "f g [0.9] {) ) ()() ,,  1, , ))( 1 )))))))))))))2   ,,), 1}", 1.0f);
    }
    
    @Test
    public void idRelations() {
        assertThrows(IllegalArgumentException.class, () -> solve("f(a, b) ?= g()", "f f [1.0f]{(1, 1), (1, 2)}", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> solve("f(a, b) ?= g()", "f f [1.0f]{(1, 1), (2, 2)}", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> solve("a() ?= a()", "a a [0.6]{}", 0.5f));
    }
    
    @Test
    public void duplicateRelations() {
        assertThrows(IllegalArgumentException.class, () -> solve("a() ?= b(c())", "a b [0.6]{} ; a b [0.6]{}", 0.5f));
        assertThrows(IllegalArgumentException.class, () -> solve("a() ?= b(c())", "a b [0.6]{} ; b a [0.6]{}", 0.5f));
    }
    
    @Test
    public void rangeCheck() {
        // alpha
        solve("a() ?= b()", "a b [0] {}", 0.5f);
        assertThrows(IllegalArgumentException.class, () -> solve("a() ?= b()", "a b [-0.000001] {}", 0.5f));
        solve("a() ?= b()", "a b [1] {}", 0.5f);
        assertThrows(IllegalArgumentException.class, () -> solve("a() ?= b()", "a b [1.000001] {}", 0.5f));
        
        // lambda
        assertThrows(IllegalArgumentException.class, () -> solve("a() ?= b()", "a b [0.5] {}", 0.0f));
        solve("a() ?= b()", "a b [0.5] {}", 1.0f);
        assertThrows(IllegalArgumentException.class, () -> solve("a() ?= b()", "a b [0.5] {}", 1.000001f));
    }
}
