package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.DataUtils;
import at.jku.risc.uarau.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ProximityMap {
    private static final Logger log = LoggerFactory.getLogger(ProximityMap.class);
    
    private final Map<String, Map<String, ProximityRelation>> relations = new HashMap<>();
    private final Map<String, Integer> arities;
    private final Set<String> vars;
    
    public ProximityMap(Term rhs, Term lhs, Collection<ProximityRelation> relations, float lambda) {
        // add flipped relations - don't allow declaring identity relations, even if they follow the definition
        List<ProximityRelation> allRelations = new ArrayList<>(relations.size() * 2);
        relations.forEach(relation -> {
            if (relation.f == relation.g) {
                log.error("Identity proximity relation: {}", relation);
                throw new IllegalArgumentException();
            }
            allRelations.add(relation);
            allRelations.add(relation.flipped());
        });
        
        // don't allow duplicates, even if they're equivalent
        Map<String, ProximityRelation> existing = new HashMap<>();
        for (ProximityRelation pr : allRelations) {
            String key = pr.f + "," + pr.g;
            if (existing.containsKey(key)) {
                log.error("Duplicate proximity relation: {} {}", existing.get(key), pr);
                throw new IllegalArgumentException();
            }
            existing.put(key, pr);
        }
        
        Pair<Map<String, Integer>, Set<String>> pair = findArities(rhs, lhs, allRelations);
        arities = Collections.unmodifiableMap(pair.a);
        vars = Collections.unmodifiableSet(pair.b);
        log.trace("Arities {}", arities);
        
        // optimization: remove relations with proximity < λ
        allRelations.removeIf(relation -> {
            if (relation.proximity < lambda) {
                log.info("Discarding relation {} with proximity < λ [{}]", relation, lambda);
                return true;
            }
            return false;
        });
        
        for (ProximityRelation relation : allRelations) {
            DataUtils.pad(relation.argRelation, ArrayList::new, arity(relation.f));
            proximityClass(relation.f).put(relation.g, relation);
        }
        log.trace("PR's {}", DataUtils.joinString(allRelations));
    }
    
    private Pair<Map<String, Integer>, Set<String>> findArities(Term rhs, Term lhs, Collection<ProximityRelation> proximityRelations) {
        // note: if f/g doesn't show up in a term, we assume its arity equals the maximum arity found in R
        //       if this assumption is wrong, we're missing some non-relevant positions, and could possibly
        //       misidentify the problem type (CAR where it is in fact UAR / CAM where it is in fact AM)
        //       otherwise, arities of functions would have to be manually specified if they don't appear in a term
        Map<String, Integer> termArities = new HashMap<>();
        Set<String> termVars = new HashSet<>();
        findTermArities(rhs, termArities, termVars);
        findTermArities(lhs, termArities, termVars);
        Map<String, Integer> allArities = new HashMap<>(termArities);
        for (ProximityRelation relation : proximityRelations) {
            if (termVars.contains(relation.f)) {
                log.error("'{}' cannot be close to '{}', since it is a variable", relation.f, relation.g);
                throw new IllegalArgumentException();
            }
            Integer termArity = termArities.get(relation.f);
            if (termArity != null && termArity < relation.argRelation.size()) {
                log.error("'{}' appears in the problem with arity {}, which argument relation {} exceeds", relation.f, termArity, relation);
                throw new IllegalArgumentException();
            }
            int newArity = Math.max(relation.argRelation.size(), allArities.getOrDefault(relation.f, 0));
            allArities.put(relation.f, newArity);
        }
        return new Pair<>(allArities, termVars);
    }
    
    private void findTermArities(Term t, Map<String, Integer> arityMap, Set<String> varSet) {
        assert (!t.isVar()); // can only have mapped vars
        if (arityMap.containsKey(t.head)) {
            if (arityMap.get(t.head) != t.arguments.size()) {
                log.error("Found multiple arities of '{}' in the posed problem", t.head);
                throw new IllegalArgumentException();
            }
            if (varSet.contains(t.head) != t.mappedVar) {
                log.error("'{}' appears as both a variable and a function/constant symbol", t.head);
                throw new IllegalArgumentException();
            }
        } else {
            arityMap.put(t.head, t.arguments.size());
            if (t.mappedVar) {
                varSet.add(t.head);
            }
        }
        for (Term arg : t.arguments) {
            findTermArities(arg, arityMap, varSet);
        }
    }
    
    // #################################################################################################################
    
    public boolean isMappedVar(String h) {
        return vars.contains(h);
    }
    
    public Map<Set<String>, Set<String>> proximatesMemory = new HashMap<>();
    
    public Set<String> commonProximates(Set<String> T) {
        assert (T != null && !T.isEmpty());
        Set<String> proximates = null;
        if (T.size() < 5 && proximatesMemory.containsKey(T)) {
            proximates = proximatesMemory.get(T);
        } else {
            for (String t : T) {
                if (proximates == null) {
                    proximates = proximityClass(t).values().stream().map(rel -> rel.g).collect(Collectors.toSet());
                    continue;
                }
                Set<String> t_prox = proximityClass(t).values().stream().map(rel -> rel.g).collect(Collectors.toSet());
                proximates.retainAll(t_prox);
            }
            if (T.size() < 5) {
                proximatesMemory.put(T, proximates);
            }
        }
        log.trace("  commonProximates{} = {}", T, proximates);
        return proximates;
    }
    
    private Map<String, ProximityRelation> proximityClass(String f) {
        return relations.computeIfAbsent(f, head -> {
            // initialize with id-relation
            assert (arities.containsKey(head));
            int arity = arities.get(head);
            List<List<Integer>> mapping = new ArrayList<>(arity);
            for (int i = 0; i < arity; i++) {
                mapping.add(Collections.singletonList(i));
            }
            Map<String, ProximityRelation> proximityClass = new HashMap<>();
            proximityClass.put(f, new ProximityRelation(f, f, 1.0f, mapping));
            return proximityClass;
        });
    }
    
    public ProximityRelation proximityRelation(String f, String g) {
        assert (relations.containsKey(f) && relations.containsKey(g));
        return proximityClass(f).get(g);
    }
    
    public int arity(String f) {
        assert (arities.containsKey(f));
        return arities.get(f);
    }
    
    @Override
    public String toString() {
        return toString("");
    }
    
    public String toString(String prefix) {
        if (relations.isEmpty()) {
            return "💢";
        }
        StringBuilder sb = new StringBuilder();
        for (String k : relations.keySet()) {
            sb.append(String.format("%s💢%s ", prefix, k));
            relations.get(k).values().forEach(pr -> sb.append(pr).append(" "));
        }
        return sb.substring(0, sb.length() - 1);
    }
}
