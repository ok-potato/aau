package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.ArraySet;
import at.jku.risc.uarau.util.Util;
import at.jku.risc.uarau.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProximityMap {
    public enum Restriction {
        UNRESTRICTED(false, false), CORRESPONDENCE(true, false), MAPPING(false, true), CORRESPONDENCE_MAPPING(true, true);
        
        public final boolean correspondence, mapping;
        
        Restriction(boolean correspondence, boolean mapping) {
            this.correspondence = correspondence;
            this.mapping = mapping;
        }
    }
    
    private static final Logger log = LoggerFactory.getLogger(ProximityMap.class);
    
    private final Map<String, Map<String, ProximityRelation>> relations = new HashMap<>();
    private final Map<String, Integer> arities;
    private final Set<String> vars;
    public final Restriction restriction, theoreticalRestriction;
    
    public ProximityMap(Term rhs, Term lhs, Collection<ProximityRelation> proximityRelations, float lambda) {
        // add flipped relations - don't allow declaring identity relations (even if they follow the definition)
        List<ProximityRelation> allProximityRelations = new ArrayList<>(proximityRelations.size() * 2);
        proximityRelations.forEach(relation -> {
            if (relation.f == relation.g) {
                throw new IllegalArgumentException("Identity proximity relation: " + relation);
            }
            allProximityRelations.add(relation);
            allProximityRelations.add(relation.flipped());
        });
        
        // don't allow multiple relations between two functions (even if they're equivalent)
        Set<String> existing = new HashSet<>();
        for (ProximityRelation relation : allProximityRelations) {
            String key = relation.f + "," + relation.g;
            if (existing.contains(key)) {
                String msg = String.format("Multiple proximity relations between '%s' and '%s'", relation.f, relation.g);
                throw new IllegalArgumentException(msg);
            }
            existing.add(key);
        }
        
        Pair<Map<String, Integer>, Set<String>> pair = findArities(rhs, lhs, allProximityRelations);
        arities = Collections.unmodifiableMap(pair.left);
        vars = Collections.unmodifiableSet(pair.right);
        
        theoreticalRestriction = findRestriction(allProximityRelations);
        
        // optimization: remove relations with proximity < λ
        allProximityRelations.removeIf(relation -> {
            if (relation.proximity < lambda) {
                log.info("Discarding relation {} with proximity < λ [{}]", relation, lambda);
                return true;
            }
            return false;
        });
        
        restriction = findRestriction(allProximityRelations);
        
        for (ProximityRelation relation : allProximityRelations) {
            Util.pad(relation.argRelation, arity(relation.f), ArrayList::new);
            proximityClass(relation.f).put(relation.g, relation);
        }
    }
    
    private Restriction findRestriction(Collection<ProximityRelation> proximityRelations) {
        boolean correspondence = proximityRelations.stream()
                .allMatch(relation -> relation.argRelation.stream().noneMatch(List::isEmpty));
        boolean mapping = proximityRelations.stream()
                .allMatch(relation -> relation.argRelation.stream().noneMatch(argRelation -> argRelation.size() > 1));
        if (correspondence) {
            return mapping ? Restriction.CORRESPONDENCE_MAPPING : Restriction.CORRESPONDENCE;
        } else {
            return mapping ? Restriction.MAPPING : Restriction.UNRESTRICTED;
        }
    }
    
    private Pair<Map<String, Integer>, Set<String>> findArities(Term rhs, Term lhs, Collection<ProximityRelation> proximityRelations) {
        //      NOTE: If f/g doesn't show up in a term, we assume its arity equals the maximum arity found in R.
        //      If this assumption is wrong, we're missing some non-relevant positions, and could possibly
        //      misidentify the problem type (CAR where it is in fact UAR / CAM where it is in fact AM).
        //      TODO If this is not acceptable, we need to allow manually defining arities
        Map<String, Integer> termArities = new HashMap<>();
        Set<String> termVars = new HashSet<>();
        findTermArities(rhs, termArities, termVars);
        findTermArities(lhs, termArities, termVars);
        Map<String, Integer> allArities = new HashMap<>(termArities);
        for (ProximityRelation relation : proximityRelations) {
            if (termVars.contains(relation.f)) {
                String msg = String.format("Variable '%s' cannot be close to '%s'", relation.f, relation.g);
                throw new IllegalArgumentException(msg);
            }
            Integer termArity = termArities.get(relation.f);
            if (termArity != null && termArity < relation.argRelation.size()) {
                String msg = String.format("'%s' appears in the problem with arity %s, which is exceeded by argument relation %s", relation.f, termArity, relation);
                throw new IllegalArgumentException(msg);
            }
            int newArity = Math.max(relation.argRelation.size(), allArities.getOrDefault(relation.f, 0));
            allArities.put(relation.f, newArity);
        }
        return new Pair<>(allArities, termVars);
    }
    
    private void findTermArities(Term t, Map<String, Integer> arityMap, Set<String> varSet) {
        assert !t.isVar();
        if (arityMap.containsKey(t.head)) {
            if (arityMap.get(t.head) != t.arguments.size()) {
                throw new IllegalArgumentException("Found multiple arities for '" + t.head + "' in the posed problem");
            }
            if (varSet.contains(t.head) != t.mappedVar) {
                throw new IllegalArgumentException(t.head + " appears as both a variable and a function/const symbol");
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
    
    // *** public methods ***
    
    public boolean isMappedVar(String h) {
        return vars.contains(h);
    }
    
    private final Map<ArraySet<String>, ArraySet<String>> proximatesMemory = new HashMap<>();
    private static final int MAX_SIZE_FOR_PROXIMATES_MEMORY = 2;
    
    public ArraySet<String> commonProximates(ArraySet<Term> T) {
        assert !T.isEmpty();
        ArraySet<String> heads = T.map(t -> t.head);
        
        if (heads.size() <= MAX_SIZE_FOR_PROXIMATES_MEMORY && proximatesMemory.containsKey(heads)) {
            return proximatesMemory.get(heads);
        }
        
        Set<String> commonProximates = null;
        
        for (String t_head : heads) {
            Stream<String> t_prox = proximityClass(t_head).values().stream().map(rel -> rel.g);
            if (commonProximates == null) {
                commonProximates = t_prox.collect(Collectors.toSet());
            } else {
                commonProximates.retainAll(t_prox.collect(Collectors.toList()));
            }
        }
        ArraySet<String> result = new ArraySet<>(commonProximates, true);
        
        if (heads.size() <= MAX_SIZE_FOR_PROXIMATES_MEMORY) {
            proximatesMemory.put(heads, result);
        }
        return result;
    }
    
    private Map<String, ProximityRelation> proximityClass(String f) {
        return relations.computeIfAbsent(f, head -> {
            // initialize with id-relation
            assert arities.containsKey(head);
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
        assert relations.containsKey(f) && relations.containsKey(g);
        return proximityClass(f).get(g);
    }
    
    public int arity(String f) {
        assert arities.containsKey(f);
        return arities.get(f);
    }
    
    @Override
    public String toString() {
        return toString("");
    }
    
    public String toString(String prefix) {
        StringBuilder sb = new StringBuilder();
        for (String k : relations.keySet()) {
            sb.append(prefix).append(Util.str(relations.get(k).values(), " ", ".."));
        }
        return sb.toString();
    }
}
