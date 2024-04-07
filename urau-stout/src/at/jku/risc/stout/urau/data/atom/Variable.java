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

import java.util.Map;

import at.jku.risc.stout.urau.data.TermNode;

/**
 * This is the base class for different types of variables.
 * 
 * @author Alexander Baumgartner
 */
public abstract class Variable extends TermAtom {

	public Variable(String name) {
		super(name);
	}

	@Override
	public TermNode substitute(Variable from, TermNode to, TermNode thisNode) {
		if (equals(from))
			return to.clone();
		return thisNode;
	}

	@Override
	public TermNode apply(Map<Variable, TermNode> sigma, TermNode thisNode) {
		TermNode to = sigma.get(this);
		if (to != null)
			return to.clone();
		return thisNode;
	}

	@Override
	public Variable clone() {
		return (Variable) super.clone();
	}
}
