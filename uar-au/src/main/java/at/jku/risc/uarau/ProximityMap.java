package at.jku.risc.uarau;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ProximityMap {
    private static final Logger log = LoggerFactory.getLogger(ProximityMap.class);
    
    private final Map<String, Set<ProximityRelation>> proxClasses = new HashMap<>();
    private final Map<String, Integer> arities = new HashMap<>();
    
    public ProximityMap(Term rhs, Term lhs, Collection<ProximityRelation> proximityRelations, float lambda) {
        proximityRelations = proximityRelations.stream().filter(relation -> {
            if (relation.proximity < lambda) {
                log.info("Discarding relation {} with proximity < λ [{}]", relation, lambda);
                return false;
            }
            return true;
        }).collect(Collectors.toSet());
        calcArities(rhs, lhs, proximityRelations);
        if (log.isDebugEnabled()) {
            log.debug("Calculated arities: {}", arities);
            log.debug("Adding proximity relations {}", proximityRelations);
        }
        for (ProximityRelation relation : proximityRelations) {
            Set<ProximityRelation> fClass = proxClass(relation.f);
            Set<ProximityRelation> gClass = proxClass(relation.g);
            fClass.add(relation);
            gClass.add(relation);
        }
    }
    
    private void calcArities(Term rhs, Term lhs, Collection<ProximityRelation> proximityRelations) {
        calcArities(rhs);
        calcArities(lhs);
        for (ProximityRelation relation : proximityRelations) {
            int prevF = arities.getOrDefault(relation.f, 0);
            arities.put(relation.f, Math.max(prevF, relation.get(relation.f).size()));
            int prevG = arities.getOrDefault(relation.g, 0);
            arities.put(relation.g, Math.max(prevG, relation.get(relation.g).size()));
        }
    }
    
    private void calcArities(Term t) {
        assert (!t.isVar());
        int prev = arities.getOrDefault(t.head, 0);
        arities.put(t.head, Math.max(prev, t.arguments.length));
        for (Term arg : t.arguments) {
            calcArities(arg);
        }
    }
    
    public Set<String> commonProximates(Set<String> T) {
        assert (T != null && !T.isEmpty());
        Set<String> commonProx = null;
        for (String t : T) {
            if (commonProx == null) {
                commonProx = proxClass(t).stream().map(relation -> relation.other(t)).collect(Collectors.toSet());
                continue;
            }
            Set<String> tProx = proxClass(t).stream().map(relation -> relation.other(t)).collect(Collectors.toSet());
            commonProx.retainAll(tProx);
        }
        if (log.isDebugEnabled()) {
            log.debug("common proximates for {}: {} (according to {})", T, commonProx, this);
        }
        return commonProx;
    }
    
    private Set<ProximityRelation> proxClass(String f) {
        return proxClasses.computeIfAbsent(f, head -> {
            Set<ProximityRelation> proxClass = new HashSet<>();
            // initialize with id-relation
            assert (arities.containsKey(head));
            int arity = arities.get(head);
            List<List<Integer>> mapping = new ArrayList<>(arity);
            for (int i = 0; i < arity; i++) {
                mapping.add(Collections.singletonList(i));
            }
            proxClass.add(new ProximityRelation(f, f, 1.0f, mapping));
            return proxClass;
        });
    }
    
    public ProximityRelation proxRelation(String f, String g) throws NoSuchElementException {
        assert (proxClasses.containsKey(f) && proxClasses.containsKey(g));
        assert ((proxClasses.get(f).stream().anyMatch(relation -> relation.other(f) == g))
                && (proxClasses.get(g).stream().anyMatch(relation -> relation.other(g) == f)));
        Set<ProximityRelation> fProxClass = proxClasses.get(f);
        return fProxClass.stream().filter(pr -> pr.other(f) == g).findFirst().orElseThrow(NoSuchElementException::new);
    }
    
    public int arity(String f) {
        assert (arities.containsKey(f));
        return arities.get(f);
    }
    
    @Override
    public String toString() {
        return "R[" + proxClasses + "]";
    }
}
