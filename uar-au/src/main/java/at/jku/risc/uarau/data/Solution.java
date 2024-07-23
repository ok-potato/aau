package at.jku.risc.uarau.data;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Solution {
    public final Set<AUT> E;
    
    public Solution(Collection<AUT> expanded) {
        this.E = Collections.unmodifiableSet(new HashSet<>(expanded));
        assert (this.E.size() == expanded.size());
    }
    
    @Override
    public String toString() {
        return "⭐" + E;
    }
}
