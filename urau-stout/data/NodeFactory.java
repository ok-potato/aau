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

package at.jku.risc.stout.urau.data;

import java.util.Deque;

import at.jku.risc.stout.urau.data.atom.Function;
import at.jku.risc.stout.urau.data.atom.HedgeVar;
import at.jku.risc.stout.urau.data.atom.TermAtom;
import at.jku.risc.stout.urau.data.atom.TermVar;
import at.jku.risc.stout.urau.util.DataStructureFactory;

/**
 * A factory to create {@linkplain TermNode}s, {@linkplain Hedge}s and atomic
 * types ( {@linkplain TermAtom}s).
 * 
 * @author Alexander Baumgartner
 */
public class NodeFactory {

	public static String PREFIX_HedgeVar;
	public static String PREFIX_TermVar;
	public static String PREFIX_Function;
	public static String PREFIX_Constant;
	public static String PREFIX_FreshTermVar = "&";
	public static String SUFFIX_FreshTermVar = "";
	public static String PREFIX_FreshHedgeVar = "#";
	public static String SUFFIX_FreshHedgeVar = "";

	private static long hedgeVarCnt = 0;
	private Deque<Hedge> hStack = DataStructureFactory.$.newDeque();

	public void pushHedge() {
		hStack.push(new Hedge());
	}

	public void addToHedge(TermNode node) {
		hStack.peek().add(node);
	}

	public Hedge popHedge() {
		return hStack.pop();
	}

	public Hedge createHedge(TermNode... nodes) {
		Hedge h = new Hedge();
		for (TermNode termNode : nodes) {
			h.add(termNode);
		}
		return h;
	}

	public TermNode createHedgeVar(String name) {
		if (PREFIX_HedgeVar != null)
			name = PREFIX_HedgeVar + name;
		return new TermNode(new HedgeVar(name), null);
	}

	public TermNode createTermVar(String name) {
		if (PREFIX_TermVar != null)
			name = PREFIX_TermVar + name;
		return new TermNode(new TermVar(name), null);
	}

	public TermNode createFunction(String name, Hedge hedge) {
		if (PREFIX_Function != null)
			name = PREFIX_Function + name;
		return new TermNode(new Function(name), hedge);
	}

	public TermNode createConstant(String name) {
		if (PREFIX_Constant != null)
			name = PREFIX_Constant + name;
		return new TermNode(new Function(name), null);
	}

	public static HedgeVar obtainFreshHedgeVar() {
		StringBuilder name = new StringBuilder();
		if (PREFIX_FreshHedgeVar != null)
			name.append(PREFIX_FreshHedgeVar);
		name.append(++hedgeVarCnt);
		if (SUFFIX_FreshHedgeVar != null)
			name.append(SUFFIX_FreshHedgeVar);
		return new HedgeVar(name.toString());
	}

	public static TermNode obtainFreshHedgeNode() {
		return newNode(obtainFreshHedgeVar());
	}

	public static TermVar obtainFreshTermVar() {
		StringBuilder name = new StringBuilder();
		if (PREFIX_FreshTermVar != null)
			name.append(PREFIX_FreshTermVar);
		name.append(++hedgeVarCnt);
		if (SUFFIX_FreshTermVar != null)
			name.append(SUFFIX_FreshTermVar);
		return new TermVar(name.toString());
	}

	public static TermNode obtainFreshTermVarNode() {
		return newNode(obtainFreshTermVar(), null);
	}

	public static TermNode newNode(TermAtom atom) {
		return newNode(atom, null);
	}

	public static void resetCounter() {
		hedgeVarCnt = 0;
	}

	public static TermNode newNode(TermAtom atom, Hedge hedge) {
		return new TermNode(atom, hedge);
	}
}
