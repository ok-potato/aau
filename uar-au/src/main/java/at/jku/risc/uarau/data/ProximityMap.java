package at.jku.risc.uarau.data;

import at.jku.risc.uarau.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ProximityMap {
    private static final Logger log = LoggerFactory.getLogger(ProximityMap.class);
    
    public final Map<String, Set<ProximityRelation>> proxClasses = new HashMap<>();
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
        log.trace("Arities {}", arities);
        for (ProximityRelation relation : proximityRelations) {
            for (int i = relation.get(relation.f).size(); i < arity(relation.f); i++) {
                relation.get(relation.f).add(new ArrayList<>());
            }
            for (int i = relation.get(relation.g).size(); i < arity(relation.g); i++) {
                relation.get(relation.g).add(new ArrayList<>());
            }
            Set<ProximityRelation> fClass = proxClass(relation.f);
            Set<ProximityRelation> gClass = proxClass(relation.g);
            fClass.add(relation);
            gClass.add(relation);
        }
        log.trace("PR's {}", Util.joinString(proximityRelations));
    }
    
    private void calcArities(Term rhs, Term lhs, Collection<ProximityRelation> proximityRelations) {
        Map<String, Integer> termArities = new HashMap<>();
        calcArities(rhs, termArities);
        calcArities(lhs, termArities);
        arities.putAll(termArities);
        
        for (ProximityRelation relation : proximityRelations) {
            int minF = relation.get(relation.f).size();
            int minG = relation.get(relation.g).size();
            
            Integer termArityF = termArities.get(relation.f);
            Integer termArityG = termArities.get(relation.g);
            if (termArityF != null && termArityF < minF) {
                log.error("Arity of '{}' according to proximity relations ({}) exceeds that found in problem declaration ({})", relation.f, minF, termArityF);
                throw new IllegalArgumentException();
            }
            if (termArityG != null && termArityG < minG) {
                log.error("Arity of '{}' according to proximity relations ({}) exceeds that found in problem declaration ({})", relation.g, minG, termArityG);
                throw new IllegalArgumentException();
            }
            int prevF = arities.getOrDefault(relation.f, 0);
            int prevG = arities.getOrDefault(relation.g, 0);
            arities.put(relation.f, Math.max(prevF, minF));
            arities.put(relation.g, Math.max(prevG, minG));
        }
    }
    
    private void calcArities(Term t, Map<String, Integer> map) {
        assert (!t.isVar());
        Integer existing = map.get(t.head);
        if (existing != null && existing != t.arguments.length) {
            log.error("Found multiple arities of '{}' in problem declaration", t.head);
            throw new IllegalArgumentException();
        }
        map.put(t.head, t.arguments.length);
        for (Term arg : t.arguments) {
            calcArities(arg, map);
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
        log.trace("comProx{} = {}", T, commonProx);
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
        assert ((proxClasses.get(f).stream().anyMatch(relation -> relation.other(f) == g)) && (proxClasses.get(g)
                .stream()
                .anyMatch(relation -> relation.other(g) == f)));
        Set<ProximityRelation> fProxClass = proxClasses.get(f);
        return fProxClass.stream().filter(pr -> pr.other(f) == g).findFirst().orElseThrow(NoSuchElementException::new);
    }
    
    public int arity(String f) {
        assert (arities.containsKey(f));
        return arities.get(f);
    }
    
    @Override
    public String toString() {
        return toString("");
    }
    
    public String toString(String prepender) {
        if (proxClasses.isEmpty()) {
            return "💢";
        }
        StringBuilder sb = new StringBuilder();
        for (String k : proxClasses.keySet()) {
            sb.append(String.format("%s💢%s ", prepender, k));
            proxClasses.get(k).forEach(pr -> sb.append(pr).append(" "));
        }
        return sb.substring(0, sb.length() - 1);
    }
}
