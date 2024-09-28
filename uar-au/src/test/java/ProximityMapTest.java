import at.jku.risc.uarau.Parser;
import at.jku.risc.uarau.data.ProximityRelation;
import org.junit.jupiter.api.Test;

public class ProximityMapTest extends BaseTest {
    @Test
    public void flippedRelationsAreSymmetric() {
        assert symmetric(Parser.parseProximityRelation("f g [1.0] {1 1, 3 2, 1 1}"));
        assert symmetric(Parser.parseProximityRelation("f g [1.0] {1 1, 1 2, 1 3}"));
    }
    
    private static boolean symmetric(ProximityRelation forward) {
        ProximityRelation flipped = forward.flipped();
        if (forward.f != flipped.g || forward.g != flipped.f || forward.proximity != flipped.proximity) {
            return false;
        }
        // if f|n maps to g|m, g|m must map to f|n
        for (int fwIdx = 0; fwIdx < forward.argRelation.size(); fwIdx++) {
            for (int bwIdx : forward.argRelation.get(fwIdx)) {
                if (!flipped.argRelation.get(bwIdx).contains(fwIdx)) {
                    return false;
                }
            }
        }
        // same in reverse
        for (int bwIdx = 0; bwIdx < flipped.argRelation.size(); bwIdx++) {
            for (int fwIdx : flipped.argRelation.get(bwIdx)) {
                if (!forward.argRelation.get(fwIdx).contains(bwIdx)) {
                    return false;
                }
            }
        }
        return true;
    }
}
