package at.jku.risc.uarau;

import at.jku.risc.uarau.term.GroundTerm;
import at.jku.risc.uarau.util.ArraySet;

import java.util.Collections;
import java.util.List;

/**
 * A precomputed view of a given generalization problem.
 * <br>
 * It is used by the {@linkplain at.jku.risc.uarau.impl.Algorithm Algorithm} to look up information about the problem:
 * <br>
 * <ul>
 *     <li> proximity classes / relations of functions
 *     <li> function arities
 *     <li> the relations' overall restriction type
 * </ul>
 */
public interface FuzzySystem {
    
    ProximityRelation proximityRelation(String f, String g);
    
    ArraySet<String> commonProximates(ArraySet<GroundTerm> f);
    
    int arity(String f);
    
    RestrictionType restrictionType();
    
    default RestrictionType practicalRestrictionType() {
        return restrictionType();
    }
    
    default List<String> fullView() {
        return Collections.emptyList();
    }
    
    default List<String> compactView() {
        return fullView();
    }
    
    enum RestrictionType {
        UNRESTRICTED(false, false),
        CORRESPONDENCE(true, false),
        MAPPING(false, true),
        CORRESPONDENCE_MAPPING(true, true);
        
        public final boolean correspondence, mapping;
        
        RestrictionType(boolean correspondence, boolean mapping) {
            this.correspondence = correspondence;
            this.mapping = mapping;
        }
    }
}
