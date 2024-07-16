import prev.uarau.algorithm.Algorithm;
import prev.uarau.algorithm.Problem;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class PrevTest {
    @Test
    public void simple() {
        String problem = "f(a,b) =^= g(a,c,d)";
        String relations = "h {(1,1),(3,2),(4,2)}[0.7] f; h g {(1,1),(3,3)}[0.8]";
        String lambda = "0.5";
        Problem p = Problem.parse(problem, relations, lambda);
        System.out.println(p);
        Algorithm.solve(p);
    }
    
    @Test
    public void badSyntax() {
        assertThrows(IllegalArgumentException.class, () -> Problem.parse(")", "", "1.0"));
        assertThrows(IllegalArgumentException.class, () -> Problem.parse("f(", "", "1.0"));
        assertThrows(IllegalArgumentException.class, () -> Problem.parse("f()", "", "1.0"));
        assertThrows(IllegalArgumentException.class, () -> Problem.parse("f() =^= g()", "f ~ g", "1.0"));
        assertThrows(IllegalArgumentException.class, () -> Problem.parse("f() =^= g()", "f ~ g {}", "1.0"));
        assertThrows(IllegalArgumentException.class, () -> Problem.parse("f() =^= g()", "f ~ g {}[]", "1.0"));
        assertThrows(Exception.class, () -> Problem.parse("f() =^= g()", "f ~ g {(}[1.0]", "1.0"));
    }
    
    @Test
    public void noMappings() {
        Problem.parse("f(a,b) =^= g(a,c,d)", "", "1.0");
        Problem.parse("f(a,b) =^= g(a,c,d)", " ; ", "1.0");
        Problem.parse("f() =^= g()", "f ~ g {}[1.0]", "1.0");
    }
}
