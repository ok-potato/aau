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
 * This interface defines the two methods which are needed to enable object
 * pooling.
 * 
 * @author Alexander Baumgartner
 */
public interface Poolable {
	/**
	 * This method is called from the {@linkplain Pool} if there is no object
	 * available to recycle.
	 */
	public Poolable newObject();

	/**
	 * This method is called from the {@linkplain Pool} before the object is
	 * returned to the pool.
	 */
	public void cleanUp();
}
