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

import at.jku.risc.stout.urau.data.TermNode;

/**
 * A hedge variable can be substituted by a single {@linkplain TermNode} or a
 * hedge which is represented by a {@linkplain TermNode} with a null-atom
 * {@linkplain TermNode#isNullAtom()}.
 * 
 * @author Alexander Baumgartner
 */
public class HedgeVar extends Variable {
	public HedgeVar(String name) {
		super(name);
	}
}
