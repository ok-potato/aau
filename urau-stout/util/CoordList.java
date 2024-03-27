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

package at.jku.risc.stout.urau.util;

import java.util.Arrays;

/**
 * A pooled list of x,y coordinates.
 * 
 * @author Alexander Baumgartner
 */
public class CoordList extends TinyList<int[]> implements Poolable {
	private int[][] coords = new int[32][];
	private int size = 0;

	private static final Pool<CoordList> coordListPool = Pool
			.getPool(new CoordList());

	private CoordList() {
	}

	public static CoordList obtainList() {
		return coordListPool.obtain();
	}

	public boolean contains(int x, int y) {
		for (int i = size - 1; i >= 0; i--) {
			int[] c = coords[i];
			if (c[0] == x && c[1] == y)
				return true;
		}
		return false;
	}

	public void add(int x, int y) {
		if (size == coords.length)
			coords = Arrays.copyOf(coords, size + (size >> 1));
		int[] c = coords[size];
		if (c == null)
			coords[size] = c = new int[2];
		c[0] = x;
		c[1] = y;
		size++;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public int[] get(int i) {
		return coords[i];
	}

	@Override
	public void removeLast() {
		size--;
	}

	/**
	 * Cleans up the entire memory from the current object pool.
	 */
	public static void fullCleanUp() {
		coordListPool.removePool();
	}

	/**
	 * Clean up the references and return the object to the pool.
	 */
	public void free() {
		coordListPool.free(this);
	}

	@Override
	public Poolable newObject() {
		return new CoordList();
	}

	@Override
	public void cleanUp() {
		size = 0;
	}
}