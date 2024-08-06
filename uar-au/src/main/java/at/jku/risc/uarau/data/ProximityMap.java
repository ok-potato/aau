package at.jku.risc.uarau.data;

import at.jku.risc.uarau.util.DataUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ProximityMap {
    private static final Logger log = LoggerFactory.getLogger(ProximityMap.class);
    
    private final Map<String, Map<String, ProximityRelation>> relations = new HashMap<>();
    private final Map<String, Integer> arities;
    private final Set<String> mappedVars = new HashSet<>();
    
    public ProximityMap(Term rhs, Term lhs, Collection<ProximityRelation> proximityRelations, float lambda) {
        // disallow explicit definition of proximity relations, because dealing with them is too annoying
        proximityRelations.forEach(relation -> {
            if (relation.f == relation.g) {
                log.error("Input includes proximity relation of a function onto itself: {}", relation);
                throw new IllegalArgumentException();
            }
        });
        // add flipped relations
        List<ProximityRelation> allProximityRelations = new ArrayList<>(proximityRelations.size() * 2);
        proximityRelations.forEach(relation -> {
            ProximityRelation flipped = relation.flipped();
            assert (symmetric(relation, flipped));
            allProximityRelations.add(relation);
            allProximityRelations.add(flipped);
        });
        // check for duplicates (don't care if they're equivalent)
        for (int i = 0; i < allProximityRelations.size() - 1; i++) {
            for (int k = i + 1; k < allProximityRelations.size(); k++) {
                ProximityRelation first = allProximityRelations.get(i);
                ProximityRelation second = allProximityRelations.get(k);
                if (first.f == second.f && first.g == second.g) {
                    log.error("Duplicate proximity relation found: {} {}", first, second);
                    throw new IllegalArgumentException();
                }
            }
        }
        arities = calculateArities(rhs, lhs, allProximityRelations);
        log.trace("Arities {}", arities);
        // filter out relations with proximity < λ
        allProximityRelations.removeIf(relation -> {
            if (relation.proximity < lambda) {
                log.info("Discarding relation {} with proximity < λ [{}]", relation, lambda);
                return true;
            }
            return false;
        });
        
        for (ProximityRelation relation : allProximityRelations) {
            // if the last argument position of f/g doesn't show up in the relation, we have to pad it accordingly
            for (int i = relation.argRelation.size(); i < arity(relation.f); i++) {
                relation.argRelation.add(new ArrayList<>());
            }
            proximityClass(relation.f).put(relation.g, relation);
        }
        
        log.trace("PR's {}", DataUtils.joinString(allProximityRelations));
    }
    
    private Map<String, Integer> calculateArities(Term rhs, Term lhs, Collection<ProximityRelation> proximityRelations) {
        Map<String, Integer> termArities = new HashMap<>();
        getAritiesFromTerm(rhs, termArities);
        getAritiesFromTerm(lhs, termArities);
        // note: if f/g doesn't show up in a term, we assume its arity equals the max arity found in R
        //   if this assumption is wrong, we're missing some non-relevant positions, and could possibly
        //   misidentify the problem type (CAR where it is in fact UAR / CAM where it is in fact AM)
        //   otherwise, arities of functions would have to be manually specified if they don't appear in a term
        Map<String, Integer> allArities = new HashMap<>(termArities);
        for (ProximityRelation relation : proximityRelations) {
            int relationArity = relation.argRelation.size();
            if (mappedVars.contains(relation.f)) {
                log.error("Non-zero proximity between '{}' and '{}' is not allowed, since variables can only be close to themselves!", relation.f, relation.g);
                throw new IllegalArgumentException();
            }
            Integer termArity = termArities.get(relation.f);
            if (termArity != null && termArity < relationArity) {
                log.error("Arity of '{}' according to proximity relations ({}) exceeds that found in problem terms ({})", relation.f, relationArity, termArity);
                throw new IllegalArgumentException();
            }
            int previousMax = allArities.getOrDefault(relation.f, 0);
            allArities.put(relation.f, Math.max(previousMax, relationArity));
        }
        return Collections.unmodifiableMap(allArities);
    }
    
    private void getAritiesFromTerm(Term t, Map<String, Integer> map) {
        assert (!t.isVar());
        Integer existing = map.get(t.head);
        if (existing != null && existing != t.arguments.length) {
            log.error("Found multiple arities of '{}' in the posed problem!", t.head);
            throw new IllegalArgumentException();
        }
        boolean mappedVar = mappedVars.contains(t.head);
        if (existing != null && !mappedVar && t.mappedVar || mappedVar && !t.mappedVar) {
            log.error("'{}' appears both as a variable and a function/constant symbol!", t.head);
            throw new IllegalArgumentException();
        }
        if (!mappedVar && t.mappedVar) {
            mappedVars.add(t.head);
        }
        map.put(t.head, t.arguments.length);
        for (Term arg : t.arguments) {
            getAritiesFromTerm(arg, map);
        }
    }
    
    private static boolean symmetric(ProximityRelation f_to_g, ProximityRelation g_to_f) {
        if (f_to_g.f != g_to_f.g || f_to_g.g != g_to_f.f || f_to_g.proximity != g_to_f.proximity) {
            return false;
        }
        for (int i = 0; i < f_to_g.argRelation.size(); i++) {
            for (int j : f_to_g.argRelation.get(i)) {
                if (!g_to_f.argRelation.get(j).contains(i)) {
                    return false;
                }
            }
        }
        for (int i = 0; i < g_to_f.argRelation.size(); i++) {
            for (int j : g_to_f.argRelation.get(i)) {
                if (!f_to_g.argRelation.get(j).contains(i)) {
                    return false;
                }
            }
        }
        return true;
    }
    
    // #################################################################################################################
    
    public boolean isMappedVar(String h) {
        return mappedVars.contains(h);
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
        assert (symmetric(relations.get(f).get(g), relations.get(g).get(f)));
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
