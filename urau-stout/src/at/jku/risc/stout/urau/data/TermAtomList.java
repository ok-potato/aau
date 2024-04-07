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

import java.util.Arrays;

import at.jku.risc.stout.urau.data.atom.TermAtom;
import at.jku.risc.stout.urau.util.Pool;
import at.jku.risc.stout.urau.util.Poolable;
import at.jku.risc.stout.urau.util.TinyList;

/**
 * A list of {@linkplain TermAtom}s which is {@linkplain Poolable}. This list in
 * combination with object pooling can be used to avoid memory allocation and
 * garbage collection.
 * 
 * @author Alexander Baumgartner
 */
public class TermAtomList extends TinyList<TermAtom> implements Poolable {
	private TermAtom[] atoms = new TermAtom[32];
	private int size = 0;

	private static final Pool<TermAtomList> termListPool = Pool
			.getPool(new TermAtomList());

	private TermAtomList() {
	}

	public static TermAtomList obtainList() {
		return termListPool.obtain();
	}

	public void add(TermAtom atom) {
		if (size == atoms.length)
			atoms = Arrays.copyOf(atoms, size + (size >> 1));
		atoms[size] = atom;
		size++;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public TermAtom get(int i) {
		return atoms[i];
	}

	@Override
	public void removeLast() {
		atoms[--size] = null;
	}

	/**
	 * Cleans up the entire memory from the current object pool.
	 */
	public static void fullCleanUp() {
		termListPool.removePool();
	}

	/**
	 * Clean up the references and return the object to the pool.
	 */
	public void free() {
		termListPool.free(this);
	}

	@Override
	public Poolable newObject() {
		return new TermAtomList();
	}

	@Override
	public void cleanUp() {
		for (int i = size - 1; i >= 0; i--)
			atoms[i] = null;
		size = 0;
	}
}