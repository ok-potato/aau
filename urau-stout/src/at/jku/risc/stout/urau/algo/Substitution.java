/*
 * Copyright 2012 Alexander Baumgartner
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

package at.jku.risc.stout.urau.algo;

import java.util.Map;
import java.util.Map.Entry;

import at.jku.risc.stout.urau.data.Hedge;
import at.jku.risc.stout.urau.data.TermNode;
import at.jku.risc.stout.urau.data.atom.Variable;
import at.jku.risc.stout.urau.util.DataStructureFactory;

/**
 * This class represents a substitution, which is a mapping from variables to
 * terms.<br>
 * It is used inside the rule based system {@linkplain AntiUnifySystem} to
 * compute generalizations for given {@linkplain AntiUnifyProblem}s. In
 * this case, the left hand side of the mapping is the generalization variable
 * of an anti-unification problem (AUP) and the right hand side is the
 * generalization computed so far.
 * 
 * @author Alexander Baumgartner
 */
public class Substitution implements Cloneable {
	private Map<Variable, TermNode> mapping = DataStructureFactory.$.newMap();

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
	public static String SIGMA_END = "} ";
	/**
	 * Default = "; "
	 */
	public static String RAN_PRINT_SEPARATOR = "; ";
	/**
	 * Most likely you want to change {@linkplain Hedge#PRINT_PARAM_START} and
	 * {@linkplain Hedge#PRINT_PARAM_END} instead of this variable.
	 */
	public static String SIGMA_EMPTY_REPLACEMENT = Hedge.PRINT_PARAM_START
			+ Hedge.PRINT_PARAM_END;

	/**
	 * Add a new mapping of the form: Variable -&gt; Term
	 */
	public void put(Variable fromVar, TermNode toTerm) {
		mapping.put(fromVar, toTerm);
	}

	/**
	 * Substitution composition which does not add new variables to the mapping.
	 * (This is useful for generalization computation of AUPs.)
	 */
	public void composeInRange(Variable fromVar, TermNode toTerm) {
		for (Entry<Variable, TermNode> e : mapping.entrySet()) {
			e.setValue(e.getValue().substitute(fromVar, toTerm));
		}
	}

	/**
	 * Returns the associated {@linkplain TermNode} for a given
	 * {@linkplain Variable} or null if no mapping exists.
	 */
	public TermNode get(Variable var) {
		return mapping.get(var);
	}

	public Map<Variable, TermNode> getMapping() {
		return mapping;
	}

	/**
	 * Removes all the mappings from a substitution.
	 */
	public void clear() {
		mapping.clear();
	}

	@Override
	public Substitution clone() {
		Substitution clone = new Substitution();
		for (Entry<Variable, TermNode> e : mapping.entrySet())
			clone.mapping.put(e.getKey(), e.getValue().clone());
		return clone;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Entry<Variable, TermNode> e : mapping.entrySet()) {
			sb.append(SIGMA_START);
			sb.append(e.getKey().getName());
			sb.append(SIGMA_MAPTO);
			String goal = String.valueOf(e.getValue());
			sb.append(goal.length() == 0 ? SIGMA_EMPTY_REPLACEMENT : goal);
			sb.append(SIGMA_END);
		}
		return sb.toString();
	}

	/**
	 * Only prints the range of the mapping, which is actually useful to display
	 * a computed generalization of an AUP without the generalization variable
	 * (which presents the most general generalization).
	 */
	public String toRanString() {
		StringBuilder sb = new StringBuilder();
		for (Entry<Variable, TermNode> e : mapping.entrySet()) {
			if (sb.length() > 0)
				sb.append(RAN_PRINT_SEPARATOR);
			String goal = String.valueOf(e.getValue());
			sb.append(goal.length() == 0 ? SIGMA_EMPTY_REPLACEMENT : goal);
		}
		return sb.toString();
	}
}