package at.jku.risc.uarau.data;

import org.junit.platform.commons.util.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProximityMap {
    public final Map<ProximityRelation, ProximityRelation> relations = new HashMap<>();
    private final Map<String, Set<ProximityRelation>> classes = new HashMap<>();
    private boolean isReady = false;
    
    public void add(ProximityRelation pr) {
        assert pr != null;
        
        for (int[] pair : pr.argumentRelation()) {
            assert (pair.length == 2);
        }
        relations.put(pr, pr);
    }
    
    public ProximityMap ready(float lambda) {
        assert !isReady;
        relations.entrySet().removeIf(e -> e.getKey().proximity() < lambda);
        for (var pr : relations.keySet()) {
            Set<ProximityRelation> fClass = classes.computeIfAbsent(pr.f(), k -> new HashSet<>());
            Set<ProximityRelation> gClass = classes.computeIfAbsent(pr.g(), k -> new HashSet<>());
            fClass.add(pr);
            gClass.add(pr);
        }
        isReady = true;
        return this;
    }
    
    public float proximity(Term s, Term t) {
        assert isReady;
        if (s.equals(t)) {
            return 1.0f;
        }
        if (!(s instanceof Function) || !(t instanceof Function)) {
            return 0.0f;
        }
        ProximityRelation pr = relations.get(new ProximityRelation(s.head, t.head, 0.0f, null));
        if (pr == null) {
            return 0.0f;
        }
        return pr.proximity();
    }
    
    @Override
    public String toString() {
        if (relations.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder(" ");
        for (ProximityRelation pr : relations.keySet()) {
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
