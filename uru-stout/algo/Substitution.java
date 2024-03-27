/*
 * Copyright 2016 Alexander Baumgartner
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package at.jku.risc.stout.uru.algo;

import at.jku.risc.stout.uru.data.Hedge;
import at.jku.risc.stout.uru.data.TermNode;
import at.jku.risc.stout.uru.data.atom.Variable;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Substitution {
    /**
     * Default = "{"
     */
    public static String SIGMA_START = "{";
    /**
     * Default = " -> "
     */
    public static String SIGMA_MAPTO = " -> ";
    /**
     * Default = "} "
     */
    public static String SIGMA_END = "}";
    /**
     * Default = "; "
     */
    public static String MAPPING_SEPARATOR = ", ";
    /**
     * Most likely you want to change {@linkplain Hedge#PRINT_PARAM_START} and
     * {@linkplain Hedge#PRINT_PARAM_END} instead of this variable.
     */
    public static String SIGMA_EMPTY_REPLACEMENT = "()";
    private final Map<Variable, TermNode> mapping;
    
    public Substitution() {
        this.mapping = new HashMap<>();
    }
    
    public Substitution(Map<Variable, TermNode> mapping) {
        this.mapping = mapping;
    }
    
    @Override
    public int hashCode() {
        return mapping.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Substitution)
            return mapping.equals(((Substitution) obj).mapping);
        return false;
    }
    
    /**
     * Add a new mapping of the form: Variable -&gt; Term.
     */
    public void put(Variable fromVar, TermNode toTerm) {
        mapping.put(fromVar, toTerm);
    }
    
    /**
     * Substitution composition which does not add new variables to the mapping.
     * (This is useful for generalization computation of AUPs.)
     */
    public void compose(Variable fromVar, TermNode toTerm) {
        for (Entry<Variable, TermNode> e : mapping.entrySet())
            e.setValue(e.getValue().substitute(fromVar, toTerm));
        if (!mapping.containsKey(fromVar))
            put(fromVar, toTerm);
    }
    
    public void compose(Substitution theta) {
        for (Entry<Variable, TermNode> e : mapping.entrySet())
            e.setValue(e.getValue().apply(theta.mapping));
        for (Entry<Variable, TermNode> e : theta.mapping.entrySet())
            if (!mapping.containsKey(e.getKey()))
                put(e.getKey(), e.getValue());
    }
    
    /**
     * Returns the associated {@linkplain TermNode} for a given
     * {@linkplain Variable} or null if no mapping exists.
     */
    public TermNode get(Variable var) {
        return mapping.get(var);
    }
    
    /**
     * Removes all the mappings from a substitution.
     */
    public void clear() {
        mapping.clear();
    }
    
    public Substitution copy() {
        Substitution ret = new Substitution(new HashMap<Variable, TermNode>(mapping.size()));
        for (Entry<Variable, TermNode> e : mapping.entrySet())
            ret.put(e.getKey(), e.getValue().copy());
        return ret;
    }
    
    public Map<Variable, TermNode> getMapping() {
        return mapping;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(SIGMA_START);
        for (Entry<Variable, TermNode> e : mapping.entrySet()) {
            sb.append(e.getKey().getName());
            sb.append(SIGMA_MAPTO);
            String goal = String.valueOf(e.getValue());
            sb.append(goal.isEmpty() ? SIGMA_EMPTY_REPLACEMENT : goal);
            sb.append(MAPPING_SEPARATOR);
        }
        if (!mapping.isEmpty())
            sb.setLength(sb.length() - 2);
        sb.append(SIGMA_END);
        return sb.toString();
    }
}
