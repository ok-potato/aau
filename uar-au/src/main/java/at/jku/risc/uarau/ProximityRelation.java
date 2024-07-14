package at.jku.risc.uarau;

import java.util.ArrayList;
import java.util.List;

public class ProximityRelation {
    public final float proximity;
    public final String f, g;
    private final List<List<Integer>> f_to_g;
    private final List<List<Integer>> g_to_f;
    
    public ProximityRelation(String f, String g, float proximity, List<List<Integer>> argRelation) {
        this.f = f.intern();
        this.g = g.intern();
        this.proximity = proximity;
        this.f_to_g = argRelation;
        this.g_to_f = reverse(argRelation);
    }
    
    private List<List<Integer>> reverse(List<List<Integer>> map) {
        List<List<Integer>> reversed = new ArrayList<>();
        for (int mapFrom = 0; mapFrom < map.size(); mapFrom++) {
            for (int mapTo : map.get(mapFrom)) {
                for (int maxIdx = reversed.size() - 1; maxIdx < mapTo; maxIdx++) {
                    reversed.add(new ArrayList<>());
                }
                reversed.get(mapTo).add(mapFrom);
            }
        }
        return reversed;
    }
    
    public List<Integer> get(String from, int index) {
        assert (from == this.f || from == this.g);
        List<List<Integer>> map = from == this.f ? f_to_g : g_to_f;
        assert (map.size() > index); // TODO this assertion might fail, since I only pad to max map index
        return map.get(index);
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
}
