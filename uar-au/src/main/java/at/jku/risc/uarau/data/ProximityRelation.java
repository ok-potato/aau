package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.DataUtils;

import java.util.ArrayList;
import java.util.Collection;
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
        int flipSize = argRelation.stream().flatMap(Collection::stream).max(Comparator.naturalOrder()).orElse(-1) + 1;
        // pad
        List<List<Integer>> flip = new ArrayList<>(flipSize);
        for (int i = 0; i < flipSize; i++) {
            flip.add(new ArrayList<>());
        }
        // invert mapping
        for (int fwIdx = 0; fwIdx < argRelation.size(); fwIdx++) {
            for (int flipIdx : argRelation.get(fwIdx)) {
                flip.get(flipIdx).add(fwIdx);
            }
        }
        return new ProximityRelation(g, f, proximity, flip);
    }
    
    @Override
    public String toString() {
        return String.format("(%s►%s %s %s)", f, g, proximity, DataUtils.mapString(argRelation));
    }
}
