import at.jku.risc.uarau.Algorithm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PrevTest {
    @Test
    public void simple() {
        String problem = "f(a,b) ?= g(a,c,d)";
        String relations = "h {(1,1),(3,2),(4,2)}[0.7] f; h g {(1,1),(3,3)}[0.8]";
        float lambda = 0.5f;
        Algorithm.solve(problem, relations, lambda);
    }
    
    @Test
    public void badSyntax() {
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve(")", "", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve("f(", "", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve("f()", "", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve("f() ?= g()", "f ~ g", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve("f() ?= g()", "f ~ g {}", 1.0f));
        assertThrows(IllegalArgumentException.class, () -> Algorithm.solve("f() ?= g()", "f ~ g {}[]", 1.0f));
        assertThrows(Exception.class, () -> Algorithm.solve("f() ?= g()", "f ~ g {(}[1.0]", 1.0f));
    }
    
    @Test
    public void noMappings() {
        Algorithm.solve("f(a,b) ?= g(a,c,d)", "", 1.0f);
        Algorithm.solve("f(a,b) ?= g(a,c,d)", " ; ", 1.0f);
        Algorithm.solve("f() ?= g()", "f ~ g {}[1.0]", 1.0f);
    }
}
