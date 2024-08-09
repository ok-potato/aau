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
        List<List<Integer>> flippedArgRelation = new ArrayList<>(flipSize);
        DataUtils.pad(flippedArgRelation, ArrayList::new, flipSize);
        // invert mapping
        for (int fwIdx = 0; fwIdx < argRelation.size(); fwIdx++) {
            for (int flippedIdx : argRelation.get(fwIdx)) {
                flippedArgRelation.get(flippedIdx).add(fwIdx);
            }
        }
        ProximityRelation flipped = new ProximityRelation(g, f, proximity, flippedArgRelation);
        assert (symmetric(this, flipped));
        return flipped;
    }
    
    private static boolean symmetric(ProximityRelation fg, ProximityRelation gf) {
        if (fg.f != gf.g || fg.g != gf.f || fg.proximity != gf.proximity) {
            return false;
        }
        for (int fg_idx = 0; fg_idx < fg.argRelation.size(); fg_idx++) {
            for (int gf_idx : fg.argRelation.get(fg_idx)) {
                if (!gf.argRelation.get(gf_idx).contains(fg_idx)) {
                    return false;
                }
            }
        }
        for (int gf_idx = 0; gf_idx < gf.argRelation.size(); gf_idx++) {
            for (int fg_idx : gf.argRelation.get(gf_idx)) {
                if (!fg.argRelation.get(fg_idx).contains(gf_idx)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        return String.format("(%s►%s %s %s)", f, g, proximity, DataUtils.mapString(argRelation));
    }
}
