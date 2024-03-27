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

package at.jku.risc.stout.urau.data.atom;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import at.jku.risc.stout.urau.data.TermNode;
import at.jku.risc.stout.urau.util.Printable;

/**
 * Base class for all the atomic types like function symbols and variables.
 * 
 * @author Alexander Baumgartner
 */
public abstract class TermAtom extends Printable implements
		Comparable<TermAtom>, Cloneable {
	private String name;
	private Class<? extends TermAtom> clazz = getClass();

	public TermAtom(String name) {
		this.name = name.intern();
	}

	public String getName() {
		return name;
	}

	public boolean equals(TermAtom obj) {
		// We only need to compare the pointers, because of calling intern
		// at construction time
		return name == obj.name && clazz == obj.clazz;
	}

	@Override
	public TermAtom clone() {
		try {
			return (TermAtom) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public int compareTo(TermAtom o) {
		String n1 = this.name, n2 = o.name;
		if (n1 == n2) {
			Class<? extends TermAtom> c1 = clazz;
			Class<? extends TermAtom> c2 = o.clazz;
			if (c1 == c2)
				return 0;
			return c1.getSimpleName().compareTo(c2.getSimpleName());
		}
		return n1.compareTo(n2);
	}

	@Override
	public void print(Writer out) throws IOException {
		out.append(getName());
	}

	public TermNode substitute(Variable from, TermNode to, TermNode thisNode) {
		return thisNode;
	}

	public TermNode apply(Map<Variable, TermNode> sigma, TermNode thisNode) {
		return thisNode;
	}
}
