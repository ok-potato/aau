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
import java.util.Map;

import at.jku.risc.stout.urau.data.atom.TermAtom;
import at.jku.risc.stout.urau.data.atom.Variable;
import at.jku.risc.stout.urau.util.Printable;

/**
 * A term node consists of a {@linkplain TermAtom} and a {@linkplain Hedge}.
 * 
 * @author Alexander Baumgartner
 */
public class TermNode extends Printable implements Cloneable {
	private TermAtom atom;
	private Hedge hedge;
	public static final TermAtom nullAtom = new TermAtom("") {
		@Override
		public void print(Writer out) throws IOException {
		}

		@Override
		public TermAtom clone() {
			return this;
		}
	};
	public static final Hedge nullHedge = new Hedge() {
		@Override
		public void add(TermNode t) {
			throw new IllegalAccessError("Illegal null-hedge modification");
		}

		@Override
		public Hedge clone() {
			return this;
		}
	};

	/**
	 * Instantiates a new term node. null values are transformed to unique
	 * objects which represent the null value. So that no
	 * {@linkplain NullPointerException} can occur.
	 * 
	 * @see TermNode#nullAtom
	 * @see TermNode#nullHedge
	 */
	public TermNode(TermAtom atom, Hedge hedge) {
		setAtom(atom);
		setHedge(hedge);
	}

	public TermAtom getAtom() {
		return atom;
	}

	/**
	 * Null values are transformed to unique objects which represent the null
	 * value. So that no {@linkplain NullPointerException} can occur.
	 * 
	 * @see TermNode#nullAtom
	 */
	public void setAtom(TermAtom atom) {
		this.atom = atom == null ? nullAtom : atom;
	}

	public Hedge getHedge() {
		return hedge;
	}

	/**
	 * Null values are transformed to unique objects which represent the null
	 * value. So that no {@linkplain NullPointerException} can occur.
	 * 
	 * @see TermNode#nullHedge
	 */
	public void setHedge(Hedge hedge) {
		this.hedge = hedge == null ? nullHedge : hedge;
	}

	/**
	 * Tests whether the atom of this term node is the null atom.
	 */
	public boolean isNullAtom() {
		return atom == nullAtom;
	}

	@Override
	public boolean equals(Object other) {
		if (!(other instanceof TermNode))
			return false;
		TermNode oNode = (TermNode) other;
		if (atom.equals(oNode.atom)) {
			if (hedge == oNode.hedge)
				return true;
			if (hedge != null)
				return hedge.equals(oNode.hedge);
		}
		return false;
	}

	@Override
	public TermNode clone() {
		return new TermNode(atom.clone(), hedge.clone());
	}

	/**
	 * Substitutes one {@linkplain Variable} by a {@linkplain TermNode}
	 * recursively and returns the new {@linkplain TermNode}.
	 */
	public TermNode substitute(Variable from, TermNode to) {
		for (int i = hedge.size() - 1; i >= 0; i--)
			hedge.replace(i, hedge.get(i).substitute(from, to));
		return atom.substitute(from, to, this);
	}

	/**
	 * Applies a substitution recursively and returns the new
	 * {@linkplain TermNode}.
	 */
	public TermNode apply(Map<Variable, TermNode> sigma) {
		for (int i = hedge.size() - 1; i >= 0; i--)
			hedge.replace(i, hedge.get(i).apply(sigma));
		return atom.apply(sigma, this);
	}

	@Override
	public void print(Writer out) throws IOException {
		atom.print(out);
		hedge.print(out);
	}

	/**
	 * Traverses the term tree and executes the callback function at every node.
	 * 
	 * @see TraverseCallBack
	 */
	public boolean traverse(TraverseCallBack callBack) {
		if (callBack.exec(atom, hedge))
			return true;
		for (int i = 0, n = hedge.size(); i < n; i++)
			if (hedge.get(i).traverse(callBack))
				return true;
		return false;
	}

	/**
	 * This class may be used to
	 * {@linkplain TermNode#traverse(TraverseCallBack) traverse} a term tree and
	 * execute an arbitrary operation on every node.
	 * 
	 * @author Alexander Baumgartner
	 */
	public abstract class TraverseCallBack {
		/**
		 * Propagation will stop as soon as this method returns true.
		 */
		public abstract boolean exec(TermAtom atom, Hedge hedge);
	}
}
