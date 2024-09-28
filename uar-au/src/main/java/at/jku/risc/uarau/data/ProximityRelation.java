package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.ANSI;
import at.jku.risc.uarau.util.Util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProximityRelation {
    public final float proximity;
    public final String f, g;
    public final List<List<Integer>> argRelation;
    
    public ProximityRelation(String f, String g, float proximity, List<List<Integer>> argRelation) {
        this.f = f.intern();
        this.g = g.intern();
        this.proximity = proximity;
        this.argRelation = argRelation;
    }
    
    public ProximityRelation flipped() {
        int flippedSize = argRelation.stream().flatMap(List::stream).max(Comparator.naturalOrder()).orElse(-1) + 1;
        
        List<List<Integer>> flippedArgRelation = Util.newList(flippedSize, i -> new ArrayList<>());
        for (int idx = 0; idx < argRelation.size(); idx++) {
            for (int flippedIdx : argRelation.get(idx)) {
                flippedArgRelation.get(flippedIdx).add(idx);
            }
        }
        return new ProximityRelation(g, f, proximity, flippedArgRelation);
    }
    
    @Override
    public String toString() {
        return "(" + ANSI.red(f + " ► " + g) + argRelationtoString() + " " + proximity + ")";
    }
    
    private String argRelationtoString() {
        StringBuilder sb = new StringBuilder();
        for (List<Integer> args : argRelation) {
            sb.append(Util.str(args, ",", "[]", "[", "]"));
        }
        return sb.toString();
    }
}
