package at.jku.risc.uarau.impl;

import at.jku.risc.uarau.FuzzySystem;
import at.jku.risc.uarau.ProximityRelation;
import at.jku.risc.uarau.term.GroundTerm;
import at.jku.risc.uarau.term.MappedVariableTerm;
import at.jku.risc.uarau.util.ArraySet;
import at.jku.risc.uarau.util.Data;
import at.jku.risc.uarau.util.Pair;
import at.jku.risc.uarau.util.Panic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class PredefinedFuzzySystem implements FuzzySystem {
    
    private static final Logger log = LoggerFactory.getLogger(PredefinedFuzzySystem.class);
    
    private final Map<String, Map<String, ProximityRelation>> proximityMap;
    private final Map<String, Integer> arities;
    private final RestrictionType restrictionType, practicalRestrictionType;
    
    /**
     * Constructs a precomputed view of the problem described by the problem terms, relations and λ-cut.
     */
    public PredefinedFuzzySystem(GroundTerm lhs, GroundTerm rhs, Collection<ProximityRelation> statedRelations, float lambda) {
        // include flipped relations
        List<ProximityRelation> allProximityRelations = new ArrayList<>(statedRelations.size() * 2);
        for (ProximityRelation relation : statedRelations) {
            allProximityRelations.add(relation);
            allProximityRelations.add(relation.flipped());
        }
        enforceValidRelations(allProximityRelations);
        
        this.arities = Collections.unmodifiableMap(inferArities(lhs, rhs, allProximityRelations));
        
        this.restrictionType = inferRestriction(allProximityRelations);
        removeProximitesBelowLambda(allProximityRelations, lambda);
        this.practicalRestrictionType = inferRestriction(allProximityRelations);
        
        this.proximityMap = Collections.unmodifiableMap(buildMap(allProximityRelations));
    }
    
    // *** public methods ***
    
    @Override
    public RestrictionType restrictionType() {
        return restrictionType;
    }
    
    @Override
    public RestrictionType practicalRestrictionType() {
        return practicalRestrictionType;
    }
    
    /**
     * Map of all proximates of function/constant 'f' to their respective proximity relation.
     * <br>
     * <b>Undefined</b> for ANON.
     */
    private Map<String, ProximityRelation> proximityClass(String f) {
        assert proximityMap.containsKey(f);
        return proximityMap.get(f);
    }
    
    /**
     * Proximity relation between functions/constants 'f' and 'g'.
     * <br>
     * <b>Undefined</b> if either side is ANON.
     */
    @Override
    public ProximityRelation proximityRelation(String f, String g) {
        assert proximityMap.containsKey(g);
        return proximityClass(f).get(g);
    }
    
    /**
     * Arity of the given function/constant 'f'.
     * <br>
     * <b>Undefined</b> for ANON.
     */
    @Override
    public int arity(String f) {
        assert arities.containsKey(f);
        return arities.get(f);
    }
    
    private static final int PROXIMATES_MEMORY_MAX_SIZE = 3;
    private final Map<ArraySet<String>, ArraySet<String>> proximatesMemory = new HashMap<>();
    
    /**
     * Finds all terms which are proximates of all terms in the given set.
     * <br>
     * <b>Undefined</b> for sets containing ANON.
     * <br><br>
     * Uses some rudimentary memoization, since we can often expect calls on the same sets of terms.
     */
    @Override
    public ArraySet<String> commonProximates(ArraySet<GroundTerm> terms) {
        assert !terms.isEmpty();
        
        ArraySet<String> heads = terms.map(t -> t.head);
        if (heads.size() <= PROXIMATES_MEMORY_MAX_SIZE && proximatesMemory.containsKey(heads)) {
            return proximatesMemory.get(heads);
        }
        
        Set<String> commonProximates = null;
        for (String head : heads) {
            Stream<String> proximates = proximityClass(head).values().stream().map(relation -> relation.g);
            if (commonProximates != null) {
                commonProximates.retainAll(proximates.collect(Collectors.toList()));
            } else { // first element
                commonProximates = proximates.collect(Collectors.toSet());
            }
        }
        
        ArraySet<String> result = ArraySet.of(commonProximates, true);
        if (heads.size() <= PROXIMATES_MEMORY_MAX_SIZE) {
            proximatesMemory.put(heads, result);
        }
        return result;
    }
    
    @Override
    public String toString() {
        return Data.str(compactView());
    }
    
    /**
     * Convenience method for logging.
     */
    public List<String> compactView() {
        List<String> view = new ArrayList<>();
        Set<String> listed = new HashSet<>();
        for (String k : proximityMap.keySet()) {
            if (listed.contains(k)) {
                continue;
            }
            listed.add(k);
            List<ProximityRelation> list = proximityMap.get(k)
                    .values()
                    .stream()
                    .filter(relation -> !listed.contains(relation.g))
                    .collect(Collectors.toList());
            if (!list.isEmpty()) {
                view.add(Data.str(list));
            }
        }
        return view;
    }
    
    public List<String> fullView() {
        return proximityMap.values().stream().map(map -> Data.str(map.values())).collect(Collectors.toList());
    }
    
    // *** private methods used during construction ***
    
    /**
     * Enforces that there be no duplicate relations or identity relations.
     */
    private void enforceValidRelations(Collection<ProximityRelation> proximityRelations) {
        for (ProximityRelation relation : proximityRelations) {
            if (relation.f == relation.g) {
                throw Panic.arg("Identity proximity relation: %s", relation);
            }
        }
        
        Set<Pair<String, String>> existing = new HashSet<>();
        for (ProximityRelation relation : proximityRelations) {
            Pair<String, String> key = new Pair<>(relation.f, relation.g);
            if (existing.contains(key)) {
                throw Panic.arg("Multiple proximity relations defined between '%s' and '%s'", relation.f, relation.g);
            }
            existing.add(key);
        }
    }
    
    /**
     * Infers function arities from their occurrences in the problem terms and proximity relations.
     * <br>
     * Note: this limits our knowledge of non-relevant positions to those of functions that appear in the problem.
     */
    private Map<String, Integer> inferArities(GroundTerm lhs, GroundTerm rhs, Collection<ProximityRelation> proximityRelations) {
        Map<String, Integer> termArities = new HashMap<>();
        Set<String> mappedVariables = new HashSet<>();
        
        inferAritiesFromTerm(lhs, termArities, mappedVariables);
        inferAritiesFromTerm(rhs, termArities, mappedVariables);
        
        Map<String, Integer> arities = new HashMap<>(termArities);
        for (ProximityRelation relation : proximityRelations) {
            if (mappedVariables.contains(relation.f)) {
                throw Panic.arg("Variable '%s' can't be close to '%s'", relation.f, relation.g);
            }
            if (termArities.containsKey(relation.f) && termArities.get(relation.f) < relation.argMapping.size()) {
                throw Panic.arg("'%s' has a higher arity in its argument relation %s than in the equation",
                        relation.f,
                        relation);
            }
            arities.put(relation.f, Math.max(relation.argMapping.size(), arities.getOrDefault(relation.f, 0)));
        }
        return arities;
    }
    
    /**
     * Recursively infers arities from a term and all its sub-terms.
     */
    private void inferAritiesFromTerm(GroundTerm term, Map<String, Integer> arities, Set<String> mappedVariables) {
        if (arities.containsKey(term.head)) {
            if (arities.get(term.head) != term.arguments.size()) {
                throw Panic.arg("'%s' appears in the posed problem with multiple arities", term.head);
            }
            if (mappedVariables.contains(term.head) != term instanceof MappedVariableTerm) {
                throw Panic.arg("%s appears as both a variable and a function/const symbol", term.head);
            }
        } else { // first occurrence
            arities.put(term.head, term.arguments.size());
            if (term instanceof MappedVariableTerm) {
                mappedVariables.add(term.head);
            }
        }
        for (GroundTerm arg : term.arguments) {
            inferAritiesFromTerm(arg, arities, mappedVariables);
        }
    }
    
    /**
     * Infers the problem's {@linkplain RestrictionType} from the given relations.
     * <br><br>
     * We don't actually need to use any of the optimisations mentioned in the paper -
     * <br>
     * however, we still get information about the kind of the generated generalization set.
     */
    private RestrictionType inferRestriction(Collection<ProximityRelation> relations) {
        boolean correspondence = Data.all(relations, relation -> Data.none(relation.argMapping, Set::isEmpty));
        boolean mapping = Data.all(relations, relation -> Data.none(relation.argMapping, argRel -> argRel.size() > 1));
        if (correspondence) {
            return mapping ? RestrictionType.CORRESPONDENCE_MAPPING : RestrictionType.CORRESPONDENCE;
        } else {
            return mapping ? RestrictionType.MAPPING : RestrictionType.UNRESTRICTED;
        }
    }
    
    /**
     * <b>Optimisation:</b> removes all relations below the lambda-cut, since they can't contribute to solutions.
     */
    private void removeProximitesBelowLambda(Collection<ProximityRelation> proximityRelations, float lambda) {
        proximityRelations.removeIf(relation -> {
            if (relation.proximity < lambda) {
                log.info("Discarding relation {} with proximity < λ [{}]", relation, lambda);
                return true;
            }
            return false;
        });
    }
    
    /**
     * Creates map representation of the given proximity relations.
     * <br><br>
     * From it, we can retrieve the {@linkplain PredefinedFuzzySystem#proximityClass} of 'f' with <b>proximityRelations.get(f)</b>,
     * <br>
     * and the {@linkplain PredefinedFuzzySystem#proximityRelation(String, String)} of 'f' and 'g' with <b>proximityRelations.get(f).get(g)</b>.
     */
    private Map<String, Map<String, ProximityRelation>> buildMap(Collection<ProximityRelation> relations) {
        Map<String, Map<String, ProximityRelation>> map = new HashMap<>();
        // initialize each proximity class with the identity relation
        for (String f : arities.keySet()) {
            List<Set<Integer>> mapping = Data.list(arities.get(f), ArraySet::singleton);
            Map<String, ProximityRelation> proximityClass = new HashMap<>();
            proximityClass.put(f, new ProximityRelation(f, f, 1.0f, mapping));
            map.put(f, proximityClass);
        }
        // add all relations that were computed from the stated relations
        for (ProximityRelation relation : relations) {
            Data.pad(relation.argMapping, arity(relation.f), Collections::emptySet);
            map.get(relation.f).put(relation.g, relation);
        }
        return map;
    }
}
