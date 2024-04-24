package at.jku.risc.uarau.data;

import java.util.HashSet;
import java.util.Set;

public class ProximityMap {
    private final Set<ProximityRelation> map = new HashSet<>();
    
    public void add(ProximityRelation pr) {
        // TODO unfinished
        assert pr != null;
        
        for (int[] pair : pr.argumentRelation()) {
            assert (pair.length == 2);
        }
        map.add(pr);
    }
    
    @Override
    public String toString() {
        if (map.isEmpty())
            return "{}";
        StringBuilder sb = new StringBuilder(" ");
        for (ProximityRelation pr : map) {
            sb.append(pr.toString()).append("\n       ");
        }
        return STR."{\{sb.delete(sb.length()-8, sb.length()).toString()} }";
    }
}
