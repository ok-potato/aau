/*
 * Copyright 2016 Alexander Baumgartner
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

package at.jku.risc.stout.uru.algo;

import at.jku.risc.stout.uru.data.TermNode;
import at.jku.risc.stout.uru.data.atom.HedgeVar;
import at.jku.risc.stout.uru.data.atom.Variable;

import java.util.HashSet;
import java.util.Set;

public class UnificationProblem {
    private UnificationEquation first, last;
    
    public boolean isEmpty() {
        return first == null;
    }
    
    public void add(UnificationEquation eq) {
        eq.setNext(null);
        if (first == null) {
            first = last = eq;
        } else {
            last.setNext(eq);
            last = eq;
        }
    }
    
    public UnificationEquation remove() {
        UnificationEquation eq = first;
        first = eq.getNext();
        return eq;
    }
    
    public void apply(Variable fromVar, TermNode toTerm) {
        for (UnificationEquation eq = first; eq != null; eq = eq.getNext())
            eq.apply(fromVar, toTerm);
    }
    
    public UnificationProblem copy() {
        UnificationProblem cpy = new UnificationProblem();
        if (first != null) {
            cpy.first = first.copy();
            cpy.last = cpy.first;
            for (; cpy.last.getNext() != null; cpy.last = cpy.last.getNext())
                cpy.last.setNext(cpy.last.getNext().copy());
        }
        return cpy;
    }
    
    public UnificationEquation getFirst() {
        return first;
    }
    
    @Override
    public boolean equals(Object obj) {
        for (UnificationEquation eq = first; eq != null; eq = eq.getNext())
            if (!eq.left.equals(eq.right))
                return false;
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (UnificationEquation eq = first; eq != null; eq = eq.getNext())
            sb.append(eq).append(", ");
        if (!isEmpty())
            sb.setLength(sb.length() - 2);
        sb.append('}');
        return sb.toString();
    }
    
    public Set<HedgeVar> collectHedgeVars() {
        Set<HedgeVar> atoms = new HashSet<>();
        for (UnificationEquation eq = first; eq != null; eq = eq.getNext())
            eq.collectHedgeVars(atoms);
        return atoms;
    }
}
