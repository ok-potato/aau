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

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import at.jku.risc.stout.urau.data.atom.TermAtom;
import at.jku.risc.stout.urau.data.atom.Variable;
import at.jku.risc.stout.urau.util.DataStructureFactory;
import at.jku.risc.stout.urau.util.Printable;

/**
 * A {@linkplain Hedge} is a sequence of {@linkplain TermNode}s.
 * 
 * @author Alexander Baumgartner
 */
public class Hedge extends Printable implements Cloneable {

	public static String PRINT_PARAM_START = "(";
	public static String PRINT_PARAM_SEPARATOR = ", ";
	public static String PRINT_PARAM_END = ")";
	private List<TermNode> sequence = DataStructureFactory.$.newList();

	public Hedge substitute(Variable x, TermNode t) {
		for (int i = sequence.size() - 1; i >= 0; i--)
			sequence.set(i, sequence.get(i).substitute(x, t));
		return this;
	}

	public Hedge apply(Map<Variable, TermNode> sigma) {
		for (int i = sequence.size() - 1; i >= 0; i--)
			sequence.set(i, sequence.get(i).apply(sigma));
		return this;
	}

	public boolean isEmpty() {
		return sequence.size() == 0;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof Hedge))
			return false;
		Hedge oH = (Hedge) other;
		if (this.size() != oH.size())
			return false;
		for (int i = this.size() - 1; i >= 0; i--)
			if (!this.sequence.get(i).equals(oH.sequence.get(i)))
				return false;
		return true;
	}

	public void add(TermNode t) {
		sequence.add(t);
	}

	public TermNode get(int i) {
		return sequence.get(i);
	}

	public int size() {
		return sequence.size();
	}

	@Override
	public Hedge clone() {
		try {
			Hedge clone = (Hedge) super.clone();
			clone.sequence = DataStructureFactory.$.newList(sequence.size());
			for (int i = 0, n = sequence.size(); i < n; i++)
				clone.sequence.add(sequence.get(i).clone());
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	public TermAtomList top() {
		TermAtomList list = TermAtomList.obtainList();
		for (int i = 0, n = sequence.size(); i < n; i++)
			list.add(sequence.get(i).getAtom());
		return list;
	}

	public List<TermNode> getSequence() {
		return sequence;
	}

	@Override
	public void print(Writer out) throws IOException {
		print(out, true);
	}

	public void print(Writer out, boolean isBlock) throws IOException {
		if (sequence.size() > 0) {
			if (isBlock)
				out.append(PRINT_PARAM_START);
			for (int i = 0, n = sequence.size(); i < n; i++) {
				if (i > 0)
					out.append(PRINT_PARAM_SEPARATOR);
				sequence.get(i).print(out);
			}
			if (isBlock)
				out.append(PRINT_PARAM_END);
		}
	}

	public String toHedgeString() {
		StringBuilder sb = new StringBuilder();
		sb.append(PRINT_PARAM_START);
		for (int i = 0, n = sequence.size(); i < n; i++) {
			if (i > 0)
				sb.append(PRINT_PARAM_SEPARATOR);
			sb.append(sequence.get(i));
		}
		sb.append(PRINT_PARAM_END);
		return sb.toString();
	}

	public String toHedgePartString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0, n = sequence.size(); i < n; i++) {
			if (i > 0)
				sb.append(PRINT_PARAM_SEPARATOR);
			sb.append(sequence.get(i));
		}
		return sb.toString();
	}

	/**
	 * Replaces the {@linkplain TermNode} at the given position with a new
	 * {@linkplain TermNode} if the {@linkplain TermAtom} of the new node is not
	 * null. Otherwise the old {@linkplain TermNode} will be replaced by the
	 * given sequence.
	 * 
	 * @return Returns the amount of nodes inserted instead of the old one.
	 */
	public int replace(int idx, TermNode toTermOrHedge) {
		if (toTermOrHedge.isNullAtom()) {
			List<TermNode> oSequence = toTermOrHedge.getHedge().sequence;
			int size = oSequence.size();
			switch (size) {
			case 0:
				sequence.remove(idx);
				return 0;
			case 1:
				sequence.set(idx, oSequence.get(0));
				return 1;
			default:
				sequence.set(idx, oSequence.get(0));
				sequence.addAll(idx + 1, oSequence.subList(1, size));
				return size;
			}
		} else {
			sequence.set(idx, toTermOrHedge);
			return 1;
		}
	}
}
