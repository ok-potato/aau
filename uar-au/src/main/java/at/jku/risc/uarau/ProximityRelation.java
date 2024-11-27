package at.jku.risc.uarau;

import at.jku.risc.uarau.util.ANSI;
import at.jku.risc.uarau.util.ArraySet;
import at.jku.risc.uarau.util.Data;
import at.jku.risc.uarau.util.Panic;

import java.util.*;

/**
 * A set of {@linkplain ProximityRelation ProximityRelations} is part of the {@linkplain Problem#proximityRelations(Collection) Problem input}.
 * Each relation is defined on two functions {@linkplain ProximityRelation#f} and {@linkplain ProximityRelation#g}.
 * <br><br>
 * Since these relations are by definition symmetric, the input should contain only one "direction" per pair of function symbols.
 * If <i>neither</i> direction is provided, the proximity is assumed to be 0.
 * <br>
 * The identity relation of every function is also predefined, so should also not be provided.
 * <br><br>
 * The modelling/validation of this occurs in {@linkplain at.jku.risc.uarau.impl.PredefinedFuzzySystem PredefinedFuzzySystem}.
 */
public class ProximityRelation {
    public final String f, g;
    public final float proximity;
    public final List<Set<Integer>> argMapping;
    
    public ProximityRelation(String f, String g, float proximity, List<Set<Integer>> argMapping) {
        this.f = f.intern();
        this.g = g.intern();
        if (proximity < 0.0f || proximity > 1.0f) {
            throw Panic.arg("Proximity outside of range [0,1]: %s", proximity);
        }
        this.proximity = proximity;
        this.argMapping = argMapping;
    }
    
    public ProximityRelation flipped() {
        int flippedSize = argMapping.stream().flatMap(Set::stream).max(Comparator.naturalOrder()).orElse(-1) + 1;
        
        List<List<Integer>> flippedArgs = Data.list(flippedSize, idx -> new ArrayList<>());
        for (int idx = 0; idx < argMapping.size(); idx++) {
            for (int flippedIdx : argMapping.get(idx)) {
                flippedArgs.get(flippedIdx).add(idx);
            }
        }
        return new ProximityRelation(g, f, proximity, Data.mapList(flippedArgs, relation -> ArraySet.of(relation, true)));
    }
    
    @Override
    public String toString() {
        return String.format("(%s%s %s)", ANSI.green(f + " " + g), argRelationtoString(), proximity);
    }
    
    private String argRelationtoString() {
        if (argMapping.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder(" ");
        for (Set<Integer> args : argMapping) {
            sb.append(Data.str(args, " ", "[]", "[", "]"));
        }
        return sb.toString();
    }
}
