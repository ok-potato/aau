package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.ANSI;
import at.jku.risc.uarau.util.ArraySet;
import at.jku.risc.uarau.util.Util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class ProximityRelation {
    public final float proximity;
    public final String f, g;
    public final List<Set<Integer>> argRelation;
    
    public ProximityRelation(String f, String g, float proximity, List<Set<Integer>> argRelation) {
        this.f = f.intern();
        this.g = g.intern();
        this.proximity = proximity;
        this.argRelation = argRelation;
    }
    
    public ProximityRelation flipped() {
        int flippedSize = argRelation.stream().flatMap(Set::stream).max(Comparator.naturalOrder()).orElse(-1) + 1;
        
        List<List<Integer>> flippedArgs = Util.newList(flippedSize, i -> new ArrayList<>());
        for (int idx = 0; idx < argRelation.size(); idx++) {
            for (int flippedIdx : argRelation.get(idx)) {
                flippedArgs.get(flippedIdx).add(idx);
            }
        }
        return new ProximityRelation(g, f, proximity, Util.mapList(flippedArgs, relation -> new ArraySet<>(relation, true)));
    }
    
    @Override
    public String toString() {
        return "(" + ANSI.red(f + " ► " + g) + " " + argRelationtoString() + " " + proximity + ")";
    }
    
    private String argRelationtoString() {
        StringBuilder sb = new StringBuilder();
        for (Set<Integer> args : argRelation) {
            sb.append(Util.str(args, ",", "[]", "[", "]"));
        }
        return sb.toString();
    }
}
