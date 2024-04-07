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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A pool of objects of a certain type. The pooled class has to implement the
 * interface {@linkplain Poolable}. This object pool can be used to recycle
 * objects and avoid memory allocation / garbage collection.
 * 
 * @author Alexander Baumgartner
 */
public class Pool<T extends Poolable> {

	private static HashMap<Class<? extends Poolable>, Pool<? extends Poolable>> pools = new HashMap<Class<? extends Poolable>, Pool<? extends Poolable>>();
	private List<T> freeObjects = new ArrayList<T>();
	private T sample;

	private Pool() {
	}

	/**
	 * Pulls an object out of this {@linkplain Pool} or creates a new object if
	 * the pool is empty.
	 */
	@SuppressWarnings("unchecked")
	public T obtain() {
		int idx = freeObjects.size();
		if (idx == 0)
			sample = (T) sample.newObject();
		else
			sample = freeObjects.remove(idx - 1);
		return sample;
	}

	/**
	 * Calls the method {@linkplain Poolable#cleanUp()} to clean the object and
	 * returns it into this pool.
	 */
	public void free(T obj) {
		obj.cleanUp();
		freeObjects.add(obj);
	}

	/**
	 * Returns a {@linkplain Pool} for a certain type of objects. A new pool
	 * will be created if there is not jet a pool available for the requested
	 * type.
	 */
	public static <T extends Poolable> Pool<T> getPool(T sample) {
		@SuppressWarnings("unchecked")
		Pool<T> p = (Pool<T>) pools.get(sample.getClass());
		if (p == null) {
			p = new Pool<T>();
			p.sample = sample;
			pools.put(sample.getClass(), p);
		}
		return p;
	}

	/**
	 * Removes this pool and let the garbage collector do it's work.
	 */
	public void removePool() {
		pools.remove(getClass());
	}
}
