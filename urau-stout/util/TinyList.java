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

/**
 * Tiny abstract list. All implementors have to provide O(1) complexity for all
 * the abstract methods!!!
 * 
 * @author Alexander Baumgartner
 */
public abstract class TinyList<V> {
	/**
	 * guarantees complexity O(1) by definition
	 */
	public abstract int size();

	/**
	 * guarantees complexity O(1) by definition
	 */
	public abstract V get(int i);

	/**
	 * guarantees complexity O(1) by definition
	 */
	public abstract void removeLast();

	/**
	 * guarantees complexity O(1) by definition
	 */
	public V getLast() {
		return get(size() - 1);
	}

	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (int i = 0, n = size(); i < n; i++) {
			if (i != 0)
				sb.append(',');
			sb.append(get(i));
		}
		sb.append(']');
		return sb.toString();
	}
}
