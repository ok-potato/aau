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

import java.util.Set;

public class UnificationEquation {
    public static String PRINT_EQ_SEPARATOR = " =? ";
    public TermNode left, right;
    public long derivationDepth;
    private UnificationEquation next;
    
    public UnificationEquation(TermNode left, TermNode right) {
        this.left = left;
        this.right = right;
    }
    
    public UnificationEquation getNext() {
        return next;
    }
    
    public void setNext(UnificationEquation next) {
        this.next = next;
    }
    
    public void apply(Substitution theta) {
        left = left.apply(theta.getMapping());
        right = right.apply(theta.getMapping());
    }
    
    public void apply(Variable fromVar, TermNode toTerm) {
        left = left.substitute(fromVar, toTerm);
        right = right.substitute(fromVar, toTerm);
    }
    
    public UnificationEquation copy() {
        UnificationEquation cpy = new UnificationEquation(left.copy(), right.copy());
        cpy.next = next;
        cpy.derivationDepth = derivationDepth;
        return cpy;
    }
    
    @Override
    public int hashCode() {
        return (left.hashCode() + 1) ^ right.hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UnificationEquation)
            return left.equals(((UnificationEquation) obj).left) && right.equals(((UnificationEquation) obj).right);
        return false;
    }
    
    @Override
    public String toString() {
        return left.toString() + PRINT_EQ_SEPARATOR + right.toString();
    }
    
    public void collectHedgeVars(Set<HedgeVar> atoms) {
        left.collectHedgeVars(atoms);
        right.collectHedgeVars(atoms);
    }
    
    public void swap() {
        TermNode left = this.left;
        this.left = this.right;
        this.right = left;
    }
    
    public void incDerivationDepth() {
        derivationDepth++;
    }
}
