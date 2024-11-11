package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.ANSI;
import at.jku.risc.uarau.util.ArraySet;
import at.jku.risc.uarau.util.Util;

import java.util.*;

/**
 * {@linkplain ProximityRelation} represents one of the directions of the proximity relation between
 * {@linkplain ProximityRelation#f} to {@linkplain ProximityRelation#g}.
 * <br><br>
 * The set of proximity relations for a given {@linkplain at.jku.risc.uarau.Problem Problem}
 * is modeled by {@linkplain ProblemMap}.
 */
public class ProximityRelation {
    final String f, g;
    public final float proximity;
    public final List<Set<Integer>> argMapping;
    
    public ProximityRelation(String f, String g, float proximity, List<Set<Integer>> argMapping) {
        this.f = f.intern();
        this.g = g.intern();
        this.proximity = proximity;
        this.argMapping = argMapping;
    }
    
    public ProximityRelation flipped() {
        int flippedSize = argMapping.stream().flatMap(Set::stream).max(Comparator.naturalOrder()).orElse(-1) + 1;
        
        List<List<Integer>> flippedArgs = Util.list(flippedSize, idx -> new ArrayList<>());
        for (int idx = 0; idx < argMapping.size(); idx++) {
            for (int flippedIdx : argMapping.get(idx)) {
                flippedArgs.get(flippedIdx).add(idx);
            }
        }
        return new ProximityRelation(g, f, proximity, Util.mapList(flippedArgs, relation -> new ArraySet<>(relation, true)));
    }
    
    @Override
    public String toString() {
        return String.format("(%s%s %s)", ANSI.blue(f + " " + g), argRelationtoString(), proximity);
    }
    
    private String argRelationtoString() {
        if (argMapping.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(" ");
        for (Set<Integer> args : argMapping) {
            sb.append(Util.str(args, " ", "[]", "[", "]"));
        }
        return sb.toString();
    }
}
