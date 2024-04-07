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

import java.util.Arrays;

import at.jku.risc.stout.urau.algo.AlignmentList.Alignment;
import at.jku.risc.stout.urau.algo.AlignmentList.Alignment.AlignmentAtom;
import at.jku.risc.stout.urau.data.Equation;
import at.jku.risc.stout.urau.data.atom.TermAtom;
import at.jku.risc.stout.urau.util.Pool;
import at.jku.risc.stout.urau.util.Poolable;
import at.jku.risc.stout.urau.util.TinyList;

/**
 * A list of {@linkplain Alignment} elements which is pooled. Use the method
 * {@linkplain AlignmentList#obtainList()} to obtain a list from the
 * {@linkplain Pool}.
 * 
 * @author Alexander Baumgartner
 */
public class AlignmentList extends TinyList<Alignment> implements Poolable {
	private Alignment[] alignment = new Alignment[8];
	private int size = 0;

	private static final Pool<AlignmentList> alignPool = Pool
			.getPool(new AlignmentList());

	private AlignmentList() {
	}

	/**
	 * @see Pool#obtain()
	 */
	public static AlignmentList obtainList() {
		return alignPool.obtain();
	}

	@Override
	public int size() {
		return size;
	}

	/**
	 * Returns an alignment of this list.
	 */
	@Override
	public Alignment get(int i) {
		return alignment[i];
	}

	/**
	 * Removes the last element of this list of alignments.
	 */
	@Override
	public void removeLast() {
		alignment[--size].reset();
	}

	/**
	 * Cleans up the entire memory from the current object pool.
	 */
	public static void fullCleanUp() {
		alignPool.removePool();
	}

	/**
	 * @see Pool#free(Poolable)
	 */
	public void free() {
		alignPool.free(this);
	}

	/**
	 * Increments the list by one and returns the new {@linkplain Alignment}
	 * from this {@linkplain AlignmentList}.
	 * 
	 * @return An {@linkplain Alignment}, which can be filled with
	 *         {@linkplain AlignmentAtom}s
	 */
	public Alignment nextAlignment() {
		int size = this.size;
		if (size == alignment.length)
			alignment = Arrays.copyOf(alignment, size + (size >> 1));
		Alignment next = alignment[size];
		if (next == null)
			alignment[size] = next = new Alignment();
		this.size++;
		return next;
	}

	@Override
	public Poolable newObject() {
		return new AlignmentList();
	}

	@Override
	public void cleanUp() {
		for (int i = size - 1; i >= 0; i--)
			alignment[i].reset();
		size = 0;
	}

	/**
	 * An {@linkplain Alignment} is a list of {@linkplain AlignmentAtom}s.
	 * 
	 * @author Alexander Baumgartner
	 */
	public class Alignment extends TinyList<AlignmentAtom> {
		private AlignmentAtom[] atoms = new AlignmentAtom[16];
		private int size = 0;

		private Alignment() {
		}

		@Override
		public int size() {
			return size;
		}

		@Override
		public AlignmentAtom get(int i) {
			return atoms[i];
		}

		@Override
		public void removeLast() {
			atoms[--size].atom = null;
		}

		/**
		 * Adds a new {@linkplain AlignmentAtom} to this {@linkplain Alignment}.
		 */
		public void addAtom(TermAtom atom, int idxLeft, int idxRight) {
			int size = this.size;
			if (size == atoms.length)
				atoms = Arrays.copyOf(atoms, size + (size >> 1));
			AlignmentAtom next = atoms[size];
			if (next == null)
				atoms[size] = next = new AlignmentAtom();
			next.set(atom, idxLeft, idxRight);
			this.size++;
		}

		/**
		 * Resets the alignment to the initial state.
		 */
		public void reset() {
			for (int i = size - 1; i >= 0; i--)
				atoms[i].atom = null;
			size = 0;
		}

		/**
		 * An {@linkplain AlignmentAtom} consists of an {@linkplain TermAtom}
		 * and two indexes for the positions of the atom inside the two hedges
		 * of an {@linkplain Equation}.
		 * 
		 * @author Alexander Baumgartner
		 */
		public class AlignmentAtom {
			public TermAtom atom;
			public int idxLeft, idxRight;

			private AlignmentAtom() {
			}

			public void set(TermAtom atom, int idxLeft, int idxRight) {
				this.atom = atom;
				this.idxLeft = idxLeft;
				this.idxRight = idxRight;
			}

			@Override
			public String toString() {
				return (atom == null ? "*null*" : atom.getName()) + "("
						+ idxLeft + "," + idxRight + ")";
			}
		}
	}
}