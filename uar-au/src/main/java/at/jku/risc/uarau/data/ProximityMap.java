package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.DataUtil;
import at.jku.risc.uarau.util.ImmutableSet;
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
        // add flipped relations - don't allow declaring identity relations, even if they follow the definition
        List<ProximityRelation> allProximityRelations = new ArrayList<>(proximityRelations.size() * 2);
        proximityRelations.forEach(relation -> {
            if (relation.f == relation.g) {
                log.error("Identity proximity relation: {}", relation);
                throw new IllegalArgumentException();
            }
            allProximityRelations.add(relation);
            allProximityRelations.add(relation.flipped());
        });
        
        // don't allow multiple relations (even if they're equivalent)
        Set<String> existing = new HashSet<>();
        for (ProximityRelation relation : allProximityRelations) {
            String key = relation.f + "," + relation.g;
            if (existing.contains(key)) {
                log.error("Multiple proximity relations between {} {}", relation.f, relation.g);
                throw new IllegalArgumentException();
            }
            existing.add(key);
        }
        
        Pair<Map<String, Integer>, Set<String>> pair = findArities(rhs, lhs, allProximityRelations);
        arities = Collections.unmodifiableMap(pair.first);
        vars = Collections.unmodifiableSet(pair.second);
        log.trace("Arities {}", arities);
        
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
            DataUtil.pad(relation.argRelation, ArrayList::new, arity(relation.f));
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
        //      Otherwise, arities of functions would have to be manually specified if they don't appear in a term.
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
        assert !t.isVar(); // can only have mapped vars
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
    
    // --- public
    
    public boolean isMappedVar(String h) {
        return vars.contains(h);
    }
    
    private final Map<ImmutableSet<String>, ImmutableSet<String>> proximatesMemory = new HashMap<>();
    private static final int MAX_SIZE_FOR_PROXIMATES_MEMORY = 5;
    
    public ImmutableSet<String> commonProximates(ImmutableSet<Term> T) {
        assert !T.isEmpty();
        ImmutableSet<String> heads = T.map(t -> t.head);
        
        if (heads.size() < MAX_SIZE_FOR_PROXIMATES_MEMORY && proximatesMemory.containsKey(heads)) {
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
        ImmutableSet<String> result = new ImmutableSet<>(commonProximates, true);
        
        if (heads.size() < MAX_SIZE_FOR_PROXIMATES_MEMORY) {
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
        if (relations.isEmpty()) {
            return prefix + "💢";
        }
        StringBuilder sb = new StringBuilder();
        for (String k : relations.keySet()) {
            sb.append(String.format("%s💢 %s ", prefix, k));
            sb.append(DataUtil.str(relations.get(k).values(), " ", ".."));
        }
        return sb.substring(0, sb.length() - 1);
    }
}
