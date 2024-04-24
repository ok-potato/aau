import at.jku.risc.uarau.algorithm.Problem;
import org.junit.jupiter.api.Test;

public class LigmaTest {
    @Test
    public void simple() {
        String problem = "f(a,b) =^= g(a,c,d)";
        String relations = "h {(1,1),(3,2),(4,2)}[0.7] f; h g {(1,1),(3,3)}[0.8]";
        String lambda = "0.5";
        System.out.println(Problem.parse(problem, relations, lambda));
    }
}
