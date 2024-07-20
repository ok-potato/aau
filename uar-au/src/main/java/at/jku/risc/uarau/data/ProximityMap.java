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
                if (relation.f_to_g.size() != arity(relation.f)) {
                    log.error(ID_RELATION_VIOLATION, relation);
                    throw new IllegalArgumentException();
                }
                for (int i = 0; i < relation.f_to_g.size(); i++) {
                    if (relation.f_to_g.get(i).size() != 1 || relation.f_to_g.get(i).get(0) != i) {
                        log.error(ID_RELATION_VIOLATION, relation);
                        throw new IllegalArgumentException();
                    }
                }
                log.info("It's not necessary to explicitly define functions' proximity to themselves ({})", relation);
            }
            // if the last argument position of f/g doesn't show up in the relation, we have to pad it accordingly
            for (int i = relation.f_to_g.size(); i < arity(relation.f); i++) {
                relation.f_to_g.add(new ArrayList<>());
            }
            for (int i = relation.g_to_f.size(); i < arity(relation.g); i++) {
                relation.g_to_f.add(new ArrayList<>());
            }
            Set<ProximityRelation> fClass = proxClass(relation.f);
            Set<ProximityRelation> gClass = proxClass(relation.g);
            fClass.add(relation);
            gClass.add(relation);
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
            int minF = relation.f_to_g.size();
            int minG = relation.g_to_f.size();
            
            Integer termArityF = termArities.get(relation.f);
            Integer termArityG = termArities.get(relation.g);
            if (termArityF != null && termArityF < minF) {
                log.error("Arity of '{}' according to proximity relations ({}) exceeds that found in problem terms ({})", relation.f, minF, termArityF);
                throw new IllegalArgumentException();
            }
            if (termArityG != null && termArityG < minG) {
                log.error("Arity of '{}' according to proximity relations ({}) exceeds that found in problem terms ({})", relation.g, minG, termArityG);
                throw new IllegalArgumentException();
            }
            int prevF = arities.getOrDefault(relation.f, 0);
            int prevG = arities.getOrDefault(relation.g, 0);
            arities.put(relation.f, Math.max(prevF, minF));
            arities.put(relation.g, Math.max(prevG, minG));
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
        log.trace("  comProx{} = {}", T, commonProx);
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
    
    public ProximityRelation getProximityRelation(String f, String g) {
        assert (proxClasses.containsKey(f) && proxClasses.containsKey(g));
        assert (!Collections.disjoint(proxClasses.get(f), proxClasses.get(g)));
        return proxClasses.get(f).stream().filter(pr -> pr.other(f) == g).findFirst().get();
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
        if (proxClasses.isEmpty()) {
            return "💢";
        }
        StringBuilder sb = new StringBuilder();
        for (String k : proxClasses.keySet()) {
            sb.append(String.format("%s💢%s ", prefix, k));
            proxClasses.get(k).forEach(pr -> sb.append(pr).append(" "));
        }
        return sb.substring(0, sb.length() - 1);
    }
}
