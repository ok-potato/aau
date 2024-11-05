package at.jku.risc.uarau.data;

import at.jku.risc.uarau.data.term.GroundTerm;
import at.jku.risc.uarau.data.term.MappedVariableTerm;
import at.jku.risc.uarau.util.ArraySet;
import at.jku.risc.uarau.util.Util;
import at.jku.risc.uarau.util.Except;
import at.jku.risc.uarau.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A precomputed view of a given generalization problem.
 * <br>
 * It is used by the {@linkplain at.jku.risc.uarau.Algorithm} to look up information about the problem:
 * <br>
 * <ul>
 *     <li> proximity relations between functions
 *     <li> function arities
 *     <li> 'mapped variable' status of functions
 *     <li> the relations' restriction type
 * </ul>
 */
public class ProximityMap {
    public enum RestrictionType {
        UNRESTRICTED(false, false), CORRESPONDENCE(true, false), MAPPING(false, true), CORRESPONDENCE_MAPPING(true, true);
        
        public final boolean correspondence, mapping;
        
        RestrictionType(boolean correspondence, boolean mapping) {
            this.correspondence = correspondence;
            this.mapping = mapping;
        }
    }
    
    private static final Logger log = LoggerFactory.getLogger(ProximityMap.class);
    
    private final Map<String, Map<String, ProximityRelation>> proximityRelations;
    private final Map<String, Integer> arities;
    private final Set<String> mappedVariables;
    public final RestrictionType restrictionType, theoreticalRestrictionType;
    
    /**
     * Constructs a precomputed view of the problem described by the problem terms, relations and λ-cut.
     */
    public ProximityMap(GroundTerm lhs, GroundTerm rhs, Collection<ProximityRelation> statedRelations, float lambda) {
        // include flipped relations
        List<ProximityRelation> allProximityRelations = new ArrayList<>(statedRelations.size() * 2);
        for (ProximityRelation relation : statedRelations) {
            allProximityRelations.add(relation);
            allProximityRelations.add(relation.flipped());
        }
        enforceValidRelations(allProximityRelations);
        
        Pair<Map<String, Integer>, Set<String>> pair = inferArities(lhs, rhs, allProximityRelations);
        arities = Collections.unmodifiableMap(pair.left);
        mappedVariables = Collections.unmodifiableSet(pair.right);
        
        theoreticalRestrictionType = inferRestriction(allProximityRelations);
        removeBelowLambdaCut(allProximityRelations, lambda);
        restrictionType = inferRestriction(allProximityRelations);
        
        this.proximityRelations = Collections.unmodifiableMap(buildMap(allProximityRelations));
    }
    
    // *** public methods ***
    
    /**
     * True iff the function/constant 'f' was mapped from a variable in the original stated problem.
     */
    public boolean isMappedVariable(String f) {
        return mappedVariables.contains(f);
    }
    
    /**
     * Map of all proximates of function/constant 'f' to their respective proximity relation.
     */
    private Map<String, ProximityRelation> proximityClass(String f) {
        assert proximityRelations.containsKey(f);
        return proximityRelations.get(f);
    }
    
    /**
     * Proximity relation between functions/constants 'f' and 'g'.
     */
    public ProximityRelation proximityRelation(String f, String g) {
        assert proximityRelations.containsKey(g);
        return proximityClass(f).get(g);
    }
    
    /**
     * Arity of the given function/constant 'f'.
     */
    public int arity(String f) {
        assert arities.containsKey(f);
        return arities.get(f);
    }
    
    private static final int PROXIMATES_MEMORY_MAX_SIZE = 3;
    private final Map<ArraySet<String>, ArraySet<String>> proximatesMemory = new HashMap<>();
    
    /**
     * Finds all terms which are proximates of all terms in the given set.
     * <br>
     * Uses some rudimentary memoization, since we can often expect calls on the same sets of terms.
     */
    public ArraySet<String> commonProximates(ArraySet<GroundTerm> terms) {
        assert !terms.isEmpty();
        
        ArraySet<String> heads = terms.map(t -> t.head);
        if (heads.size() <= PROXIMATES_MEMORY_MAX_SIZE && proximatesMemory.containsKey(heads)) {
            return proximatesMemory.get(heads);
        }
        
        Set<String> commonProximates = null;
        for (String head : heads) {
            Stream<String> proximates = proximityClass(head).values().stream().map(rel -> rel.g);
            if (commonProximates != null) {
                commonProximates.retainAll(proximates.collect(Collectors.toList()));
            } else { // first element
                commonProximates = proximates.collect(Collectors.toSet());
            }
        }
        
        ArraySet<String> result = new ArraySet<>(commonProximates, true);
        if (heads.size() <= PROXIMATES_MEMORY_MAX_SIZE) {
            proximatesMemory.put(heads, result);
        }
        return result;
    }
    
    @Override
    public String toString() {
        return Util.str(compactView());
    }
    
    /**
     * Convenience method for logging.
     */
    public List<String> compactView() {
        List<String> view = new ArrayList<>();
        Set<String> listed = new HashSet<>();
        for (String k : proximityRelations.keySet()) {
            if (listed.contains(k)) {
                continue;
            }
            listed.add(k);
            List<ProximityRelation> list = proximityRelations.get(k)
                    .values()
                    .stream()
                    .filter(relation -> !listed.contains(relation.g))
                    .collect(Collectors.toList());
            if (!list.isEmpty()) {
                view.add(Util.str(list));
            }
        }
        return view;
    }
    
    public List<String> fullView() {
        return proximityRelations.values().stream().map(map -> Util.str(map.values())).collect(Collectors.toList());
    }
    
    // *** used only during construction ***
    
    /**
     * Enforces that there be no duplicate relations or identity relations.
     */
    private void enforceValidRelations(Collection<ProximityRelation> proximityRelations) {
        Set<String> existing = new HashSet<>();
        for (ProximityRelation relation : proximityRelations) {
            if (relation.f == relation.g) {
                throw Except.argument("Identity proximity relation: %s", relation);
            }
            String key = relation.f + "," + relation.g;
            if (existing.contains(key)) {
                throw Except.argument("Multiple proximity relations between '%s' and '%s'", relation.f, relation.g);
            }
            existing.add(key);
        }
    }
    
    // TODO Do we want/need to allow manually defining arities?
    
    /**
     * Infers function arities from their occurrences in the problem terms and proximity relations.
     * <br>
     * Note: this limits our knowledge of non-relevant positions to those of functions that appear in the problem.
     */
    private Pair<Map<String, Integer>, Set<String>> inferArities(GroundTerm lhs, GroundTerm rhs, Collection<ProximityRelation> proximityRelations) {
        Map<String, Integer> termArities = new HashMap<>();
        Set<String> mappedVariables = new HashSet<>();
        
        inferAritiesFromTerm(lhs, termArities, mappedVariables);
        inferAritiesFromTerm(rhs, termArities, mappedVariables);
        
        Map<String, Integer> arities = new HashMap<>(termArities);
        for (ProximityRelation relation : proximityRelations) {
            if (mappedVariables.contains(relation.f)) {
                throw Except.argument("Variable '%s' can't be close to '%s'", relation.f, relation.g);
            }
            if (termArities.containsKey(relation.f) && termArities.get(relation.f) < relation.argRelation.size()) {
                throw Except.argument("'%s' has a higher arity in its argument relation %s than in the equation", relation.f, relation);
            }
            arities.put(relation.f, Math.max(relation.argRelation.size(), arities.getOrDefault(relation.f, 0)));
        }
        return new Pair<>(arities, mappedVariables);
    }
    
    /**
     * Recursively infers arities from a term and all its sub-terms.
     */
    private void inferAritiesFromTerm(GroundTerm term, Map<String, Integer> arities, Set<String> mappedVariables) {
        if (arities.containsKey(term.head)) {
            if (arities.get(term.head) != term.arguments.size()) {
                throw Except.argument("'%s' appears in the posed problem with multiple arities", term.head);
            }
            if (mappedVariables.contains(term.head) != term instanceof MappedVariableTerm) {
                throw Except.argument("%s appears as both a variable and a function/const symbol", term.head);
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
        boolean correspondence = Util.all(relations, relation -> Util.none(relation.argRelation, Set::isEmpty));
        boolean mapping = Util.all(relations, relation -> Util.none(relation.argRelation, argRel -> argRel.size() > 1));
        if (correspondence) {
            return mapping ? RestrictionType.CORRESPONDENCE_MAPPING : RestrictionType.CORRESPONDENCE;
        } else {
            return mapping ? RestrictionType.MAPPING : RestrictionType.UNRESTRICTED;
        }
    }
    
    /**
     * <b>Optimisation:</b> removes all relations below the lambda-cut, since they can't contribute to solutions.
     */
    private void removeBelowLambdaCut(Collection<ProximityRelation> proximityRelations, float lambda) {
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
     * From it, we can retrieve the {@linkplain ProximityMap#proximityClass} of 'f' with <b>proximityRelations.get(f)</b>,
     * <br>
     * and the {@linkplain ProximityMap#proximityRelation(String, String)} of 'f' and 'g' with <b>proximityRelations.get(f).get(g)</b>.
     */
    private Map<String, Map<String, ProximityRelation>> buildMap(Collection<ProximityRelation> relations) {
        Map<String, Map<String, ProximityRelation>> map = new HashMap<>();
        // initialize each proximity class with the identity relation
        for (String f : arities.keySet()) {
            List<Set<Integer>> mapping = Util.list(arities.get(f), ArraySet::singleton);
            Map<String, ProximityRelation> proximityClass = new HashMap<>();
            proximityClass.put(f, new ProximityRelation(f, f, 1.0f, mapping));
            map.put(f, proximityClass);
        }
        // add all relations that were computed from the stated relations
        for (ProximityRelation relation : relations) {
            Util.pad(relation.argRelation, arity(relation.f), Collections::emptySet);
            map.get(relation.f).put(relation.g, relation);
        }
        return map;
    }
}
