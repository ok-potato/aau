package at.jku.risc.uarau.data;

import at.jku.risc.uarau.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ProximityMap {
    private static final Logger log = LoggerFactory.getLogger(ProximityMap.class);
    
    private final Map<String, Map<String, ProximityRelation>> relations = new HashMap<>();
    private final Map<String, Integer> arities = new HashMap<>();
    
    private static final String ID_RELATION_VIOLATION = "By definition, the proximity of a function to itself must be 1, with Id argument mapping. {} violates this rule!";
    
    public ProximityMap(Term rhs, Term lhs, Collection<ProximityRelation> proximityRelations, float lambda) {
        // filter out relations with proximity < λ
        proximityRelations = proximityRelations.stream().filter(relation -> {
            if (relation.f == relation.g && relation.proximity < 1.0f) {
                log.error(ID_RELATION_VIOLATION, relation);
                throw new IllegalArgumentException();
            }
            if (relation.proximity < lambda) {
                log.info("Discarding relation {} with proximity < λ [{}]", relation, lambda);
                return false;
            }
            return true;
        }).collect(Collectors.toSet());
        
        calculateArities(rhs, lhs, proximityRelations);
        log.trace("Arities {}", arities);
        for (ProximityRelation relation : proximityRelations) {
            // check id argument relation violations
            if (relation.f == relation.g) {
                if (relation.argRelation.size() != arity(relation.f)) {
                    log.error(ID_RELATION_VIOLATION, relation);
                    throw new IllegalArgumentException();
                }
                for (int i = 0; i < relation.argRelation.size(); i++) {
                    if (relation.argRelation.get(i).size() != 1 || relation.argRelation.get(i).get(0) != i) {
                        log.error(ID_RELATION_VIOLATION, relation);
                        throw new IllegalArgumentException();
                    }
                }
                log.info("It's not necessary to explicitly define functions' proximity to themselves ({})", relation);
            }
            // if the last argument position of f/g doesn't show up in the relation, we have to pad it accordingly
            for (int i = relation.argRelation.size(); i < arity(relation.f); i++) {
                relation.argRelation.add(new ArrayList<>());
            }
            ProximityRelation flipped = relation.flipped();
            assert (symmetric(relation, flipped));
            proximityClass(relation.f).put(relation.g, relation);
            proximityClass(relation.g).put(relation.f, flipped);
        }
        log.trace("PR's {}", Util.joinString(proximityRelations));
    }
    
    private void calculateArities(Term rhs, Term lhs, Collection<ProximityRelation> proximityRelations) {
        Map<String, Integer> termArities = new HashMap<>();
        getAritiesFromTerm(rhs, termArities);
        getAritiesFromTerm(lhs, termArities);
        // note: if f/g doesn't show up in a term, we assume its arity equals the max arity found in R
        //   if this assumption is wrong, we're missing some non-relevant positions, and could possibly
        //   misidentify the problem type (CAR where it is in fact UAR / CAM where it is in fact AM)
        //   otherwise, arities of functions would have to be manually specified if they don't appear in a term
        arities.putAll(termArities);
        for (ProximityRelation relation : proximityRelations) {
            int relationArity = relation.argRelation.size();
            Integer termArity = termArities.get(relation.f);
            if (termArity != null && termArity < relationArity) {
                log.error("Arity of '{}' according to proximity relations ({}) exceeds that found in problem terms ({})", relation.f, relationArity, termArity);
                throw new IllegalArgumentException();
            }
            int previousMax = arities.getOrDefault(relation.f, 0);
            arities.put(relation.f, Math.max(previousMax, relationArity));
        }
    }
    
    private void getAritiesFromTerm(Term t, Map<String, Integer> map) {
        assert (!t.isVar());
        Integer existing = map.get(t.head);
        if (existing != null && existing != t.arguments.length) {
            log.error("Found multiple arities of '{}' in the posed problem!", t.head);
            throw new IllegalArgumentException();
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
    
    public Set<String> commonProximates(Set<String> T) {
        assert (T != null && !T.isEmpty());
        Set<String> commonProximates = null;
        for (String t : T) {
            if (commonProximates == null) {
                commonProximates = proximityClass(t).values().stream().map(pr -> pr.g).collect(Collectors.toSet());
                continue;
            }
            Set<String> tProx = proximityClass(t).values().stream().map(pr -> pr.g).collect(Collectors.toSet());
            commonProximates.retainAll(tProx);
        }
        log.trace("  comProx{} = {}", T, commonProximates);
        return commonProximates;
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
