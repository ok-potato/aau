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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * This factory class defines which data structures to use. The complexity of
 * the algorithm will depend on the used data structures. Hash based structures
 * ({@linkplain HashMap}, {@linkplain HashSet}) have better average case
 * complexity and tree based structures ({@linkplain TreeMap},
 * {@linkplain TreeSet}) have better worst case complexity in Java's standard
 * implementation. Of course you are free to provide your own data structures
 * with better computational behavior here ;)<br>
 * You can simply change the public static instance field which is called
 * {@linkplain DataStructureFactory#$} to your own implementation of
 * {@linkplain DataStructureFactory} (override some of the methods). The data
 * structures which are used by default are {@linkplain ArrayList},
 * {@linkplain ArrayDeque}, {@linkplain HashSet} and {@linkplain HashMap}.
 * 
 * @author Alexander Baumgartner
 */
public class DataStructureFactory {
	public static DataStructureFactory $ = new DataStructureFactory();

	public <T> Set<T> newSet() {
		return new HashSet<T>();
	}

	public <T> Set<T> newSet(Collection<T> toCopy) {
		return new HashSet<T>(toCopy);
	}

	public <T> Set<T> newSet(int size) {
		return new HashSet<T>(size);
	}

	public <T> List<T> newList() {
		return new ArrayList<T>();
	}

	public <T> List<T> newList(Collection<T> toCopy) {
		return new ArrayList<T>(toCopy);
	}

	public <T> List<T> newList(int size) {
		return new ArrayList<T>(size);
	}

	public <T> Deque<T> newDeque() {
		return new ArrayDeque<T>();
	}

	public <T> Deque<T> newDeque(Collection<T> toCopy) {
		return new ArrayDeque<T>(toCopy);
	}

	public <T> Deque<T> newDeque(int size) {
		return new ArrayDeque<T>(size);
	}

	public <K, V> Map<K, V> newMap() {
		return new HashMap<K, V>();
	}

	public <K, V> Map<K, V> newMap(Map<K, V> toCopy) {
		return new HashMap<K, V>(toCopy);
	}

	public <K, V> Map<K, V> newMap(int size) {
		return new HashMap<K, V>(size);
	}
}
