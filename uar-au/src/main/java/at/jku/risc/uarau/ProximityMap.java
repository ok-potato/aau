package at.jku.risc.uarau;

import java.util.*;
import java.util.stream.Collectors;

public class ProximityMap {
    private final Map<String, Set<ProximityRelation>> proxClasses = new HashMap<>();
    private final Map<String, Integer> arities = new HashMap<>(); // TODO infer arities, or add them to input?
    
    public ProximityMap(Collection<ProximityRelation> proximityRelations, float lambda) {
        for (ProximityRelation proxRelation : proximityRelations) {
            add(proxRelation, lambda);
        }
    }
    
    public void add(ProximityRelation proxRelation, float lambda) {
        if (proxRelation.proximity < lambda) {
            System.out.printf("Discarding relation with proximity [%s] < λ [%s]%n", proxRelation.proximity, lambda);
            return;
        }
        Set<ProximityRelation> fClass = proxClasses.computeIfAbsent(proxRelation.f, _ignored -> new HashSet<>());
        Set<ProximityRelation> gClass = proxClasses.computeIfAbsent(proxRelation.g, _ignored -> new HashSet<>());
        fClass.add(proxRelation);
        gClass.add(proxRelation);
    }
    
    public Set<ProximityRelation> prox(String f) {
        return proxClasses.get(f);
    }
    
    public Set<String> commonProximates(Set<Term> T) {
        Term someT = T.iterator().next();
        T.remove(someT);
        
        Set<String> commonProx = prox(someT.head).stream()
                .map(proxRelation -> proxRelation.other(someT.head))
                .collect(Collectors.toSet());
        
        for (Term t : T) {
            Set<String> tProx = prox(t.head).stream()
                    .map(proximityRelation -> proximityRelation.other(t.head))
                    .collect(Collectors.toSet());
            commonProx.retainAll(tProx);
        }
        return commonProx;
    }
    
    public ProximityRelation relation(String f, String g) throws NoSuchElementException {
        Set<ProximityRelation> fProxClass = proxClasses.get(f);
        if (fProxClass == null) {
            throw new NoSuchElementException();
        }
        return fProxClass.stream().filter(pr -> pr.other(f) == g).findFirst().orElseThrow(NoSuchElementException::new);
    }
    
    public int arity(String f) {
        return arities.get(f);
    }
}
