package at.jku.risc.uarau.data;

import at.jku.risc.uarau.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class ProximityRelation {
    Logger log = LoggerFactory.getLogger(ProximityRelation.class);
    
    public final float proximity;
    public final String f, g;
    public final List<List<Integer>> f_to_g;
    public final List<List<Integer>> g_to_f;
    
    public ProximityRelation(String f, String g, float proximity, List<List<Integer>> argRelation) {
        this.f = f.intern();
        this.g = g.intern();
        this.proximity = proximity;
        this.f_to_g = argRelation;
        this.g_to_f = flip(argRelation);
        assert (isSymmetric());
    }
    
    private boolean isSymmetric() {
        for (int i = 0; i < f_to_g.size(); i++) {
            final int i_but_cooler = i;
            if (f_to_g.get(i).stream().anyMatch(j -> g_to_f.get(j).stream().noneMatch(v -> v == i_but_cooler))) {
                return false;
            }
        }
        for (int i = 0; i < g_to_f.size(); i++) {
            final int i_but_cooler = i;
            if (g_to_f.get(i).stream().anyMatch(j -> f_to_g.get(j).stream().noneMatch(v -> v == i_but_cooler))) {
                return false;
            }
        }
        return true;
    }
    
    private List<List<Integer>> flip(List<List<Integer>> map) {
        int maxToIdx = map.stream().flatMap(Collection::stream).max(Comparator.naturalOrder()).orElse(-1) + 1;
        List<List<Integer>> reversed = new ArrayList<>(maxToIdx);
        for (int i = 0; i < maxToIdx; i++) {
            reversed.add(new ArrayList<>());
        }
        
        for (int mapFrom = 0; mapFrom < map.size(); mapFrom++) {
            for (int mapTo : map.get(mapFrom)) {
                reversed.get(mapTo).add(mapFrom);
            }
        }
        return reversed;
    }
    
    public List<List<Integer>> get(String f) {
        assert (f == this.f || f == this.g);
        return f == this.f ? f_to_g : g_to_f;
    }
    
    public String other(String f) {
        assert (f == this.f || f == this.g);
        return f == this.f ? this.g : this.f;
    }
    
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ProximityRelation)) {
            return false;
        }
        ProximityRelation otherRelation = (ProximityRelation) other;
        if (f == otherRelation.f) {
            return g == otherRelation.g;
        }
        if (f == otherRelation.g) {
            return g == otherRelation.f;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return f.hashCode() + g.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("(%s►%s %s %s)", f, g, proximity, Util.mapString(f_to_g));
    }
}
