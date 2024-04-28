package at.jku.risc.uarau.data;

import org.junit.platform.commons.util.StringUtils;

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
        if (map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder(" ");
        for (ProximityRelation pr : map) {
            sb.append(pr.toString()).append("\n       ");
        }
        return STR."{\{sb.delete(sb.length() - 8, sb.length()).toString()} }";
    }
    
    // relations looks like this:  f g {<arg-map>} [<proximity>]  and are separated by ';'
    // <arg-map> looks like this:  (1,1), (2,3), (3,1), ...  (with surrounding '{}')
    // <proximity> is a float between 0.0 and 1.0            (with surrounding '[]')
    
    public static ProximityMap parse(String map) {
        var proximityMap = new ProximityMap();
        String[] relations = map.split(";");
        for (String relation : relations) {
            if (StringUtils.isNotBlank(relation)) {
                proximityMap.add(ProximityRelation.parse(relation));
            }
        }
        return proximityMap;
    }
}
