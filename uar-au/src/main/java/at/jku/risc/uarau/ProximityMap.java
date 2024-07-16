package at.jku.risc.uarau;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ProximityMap {
    private static final Logger log = LoggerFactory.getLogger(ProximityMap.class);
    
    private final Map<String, Set<ProximityRelation>> proxClasses = new HashMap<>();
    private final Map<String, Integer> arities = new HashMap<>(); // TODO infer arities, or add them to input?
    
    public ProximityMap(Collection<ProximityRelation> proximityRelations, float lambda) {
        for (ProximityRelation proxRelation : proximityRelations) {
            add(proxRelation, lambda);
        }
    }
    
    public void add(ProximityRelation proxRelation, float lambda) {
        log.debug("Adding proximity relation {}", proxRelation);
        if (proxRelation.proximity < lambda) {
            log.info("Discarding relation with proximity [{}] < λ [{}]", proxRelation.proximity, lambda);
            return;
        }
        Set<ProximityRelation> fClass = proxClass(proxRelation.f);
        Set<ProximityRelation> gClass = proxClass(proxRelation.g);
        fClass.add(proxRelation);
        gClass.add(proxRelation);
    }
    
    public Set<String> commonProximates(Set<Term> T) {
        log.debug("commonProx({})", T);
        Term someT = T.iterator().next();
        T.remove(someT);
        
        Set<String> commonProx = proxClass(someT.head).stream()
                .map(proxRelation -> proxRelation.other(someT.head))
                .collect(Collectors.toSet());
        
        for (Term t : T) {
            Set<String> tProx = proxClass(t.head).stream()
                    .map(proximityRelation -> proximityRelation.other(t.head))
                    .collect(Collectors.toSet());
            commonProx.retainAll(tProx);
        }
        return commonProx;
    }
    
    private Set<ProximityRelation> proxClass(String f) {
        return proxClasses.computeIfAbsent(f, head -> {
            Set<ProximityRelation> proxClass = new HashSet<>();
            // add id-relation
            int arity = arities.get(head);
            List<List<Integer>> mapping = new ArrayList<>(arity);
            for (int i = 0; i < arity; i++) {
                mapping.add(Collections.singletonList(i));
            }
            proxClass.add(new ProximityRelation(f, f, 1.0f, mapping));
            return proxClass;
        });
    }
    
    public ProximityRelation relation(String f, String g) throws NoSuchElementException {
        Set<ProximityRelation> fProxClass = proxClasses.get(f);
        assert (fProxClass != null);
        return fProxClass.stream().filter(pr -> pr.other(f) == g).findFirst().orElseThrow(NoSuchElementException::new);
    }
    
    public int arity(String f) {
        return arities.get(f);
    }
    
    @Override
    public String toString() {
        return "R[" + proxClasses + "]";
    }
}
