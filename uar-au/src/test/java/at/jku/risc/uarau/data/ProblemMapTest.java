package at.jku.risc.uarau.data;

import at.jku.risc.uarau.AlgorithmTest;
import at.jku.risc.uarau.BaseTest;
import at.jku.risc.uarau.ProximityRelation;
import at.jku.risc.uarau.impl.Parser;
import org.junit.jupiter.api.Test;

public class ProblemMapTest extends BaseTest {
    @Test
    public void flippedRelationsAreSymmetric() {
        assert symmetric(Parser.parseProximityRelation("f g [1.0] {1 1, 3 2, 1 1}"));
        assert symmetric(Parser.parseProximityRelation("f g [1.0] {1 1, 1 2, 1 3}"));
        for (String relation : AlgorithmTest.bigRelations().split(";")) {
            assert symmetric(Parser.parseProximityRelation(relation));
        }
    }
    
    private static boolean symmetric(ProximityRelation forward) {
        ProximityRelation flipped = forward.flipped();
        if (forward.f != flipped.g || forward.g != flipped.f || forward.proximity != flipped.proximity) {
            return false;
        }
        // if f|n maps to g|m, g|m must map to f|n
        for (int fwIdx = 0; fwIdx < forward.argMapping.size(); fwIdx++) {
            for (int bwIdx : forward.argMapping.get(fwIdx)) {
                if (!flipped.argMapping.get(bwIdx).contains(fwIdx)) {
                    return false;
                }
            }
        }
        // same in reverse
        for (int bwIdx = 0; bwIdx < flipped.argMapping.size(); bwIdx++) {
            for (int fwIdx : flipped.argMapping.get(bwIdx)) {
                if (!forward.argMapping.get(fwIdx).contains(bwIdx)) {
                    return false;
                }
            }
        }
        return true;
    }
}
