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

package at.jku.risc.stout.uru.data;

import at.jku.risc.stout.uru.data.atom.HedgeVar;
import at.jku.risc.stout.uru.data.atom.TermAtom;
import at.jku.risc.stout.uru.data.atom.Variable;

import java.util.Map;
import java.util.Set;

/**
 * A term node consists of a {@linkplain TermAtom} and a {@linkplain Hedge}.
 *
 * @author Alexander Baumgartner
 */
public class TermNode {
    public static final TermNode empty = new TermNode(null, null);
    private TermAtom atom;
    private Hedge hedge;
    
    /**
     * Instantiates a new term node. null values are transformed to unique
     * objects which represent the null value. So that no
     * {@linkplain NullPointerException} can occur.
     *
     * @see TermAtom#nullAtom
     * @see Hedge#nullHedge
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
     * @see TermAtom#nullAtom
     */
    private void setAtom(TermAtom atom) {
        this.atom = atom == null ? TermAtom.nullAtom : atom;
    }
    
    public Hedge getHedge() {
        return hedge;
    }
    
    /**
     * Null values are transformed to unique objects which represent the null
     * value. So that no {@linkplain NullPointerException} can occur.
     *
     * @see Hedge#nullHedge
     */
    private void setHedge(Hedge hedge) {
        this.hedge = hedge == null ? Hedge.nullHedge : hedge;
    }
    
    /**
     * Tests whether the atom of this term node is the null atom.
     */
    public boolean isNullAtom() {
        return atom == TermAtom.nullAtom;
    }
    
    @Override
    public boolean equals(Object other) {
        if (other instanceof TermNode otherNode) {
            if (atom.equals(otherNode.atom)) {
                if (hedge == otherNode.hedge) {
                    return true;
                }
                return hedge.equals(otherNode.hedge);
            }
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        return atom.hashCode() ^ hedge.hashCode();
    }
    
    public TermNode copy() {
        return new TermNode(atom.copy(), hedge.copy());
    }
    
    /**
     * Substitutes one {@linkplain Variable} by a {@linkplain TermNode}
     * recursively and returns the new {@linkplain TermNode}.
     */
    public TermNode substitute(Variable from, TermNode to) {
        for (int i = hedge.size() - 1; i >= 0; i--) {
            hedge.replace(i, hedge.get(i).substitute(from, to));
        }
        return atom.substitute(from, to, this);
    }
    
    /**
     * Applies a substitution recursively and returns the new
     * {@linkplain TermNode}.
     */
    public TermNode apply(Map<Variable, TermNode> sigma) {
        for (int i = hedge.size() - 1; i >= 0; i--) {
            hedge.replace(i, hedge.get(i).apply(sigma));
        }
        return atom.apply(sigma, this);
    }
    
    public Set<HedgeVar> collectHedgeVars(final Set<HedgeVar> atoms) {
        final TraverseCallBack collectAtoms = new TraverseCallBack() {
            @Override
            public boolean exec(TermAtom atom, Hedge hedge) {
                if (atom instanceof HedgeVar) {
                    atoms.add((HedgeVar) atom);
                }
                return false;
            }
        };
        traverse(collectAtoms);
        return atoms;
    }
    
    /**
     * Traverses the term tree and executes the callback function at every node.
     *
     * @see TraverseCallBack
     */
    public boolean traverse(TraverseCallBack callBack) {
        if (callBack.exec(atom, hedge)) {
            return true;
        }
        for (int i = 0; i < hedge.size(); i = i + 1) {
            if (hedge.get(i).traverse(callBack)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean occurs(final TermAtom atomOcc) {
        return traverse(new TraverseCallBack() {
            @Override
            public boolean exec(TermAtom atom, Hedge hedge) {
                return atom == atomOcc;
            }
        });
    }
    
    /**
     * @Override public void print(Writer out) throws IOException {
     * if (isNullAtom() && hedge.size() == 1) {
     * hedge.get(0).print(out);
     * } else {
     * atom.print(out);
     * hedge.print(out);
     * }
     * }
     */
    
    @Override
    public String toString() {
        if (isNullAtom() && hedge.size() == 1) {
            return hedge.get(0).toString();
        } else {
            return atom.toString() + hedge.toString();
        }
    }
    
    /**
     * This class may be used to
     * {@linkplain TermNode#traverse(TraverseCallBack) traverse} a term tree and
     * execute an arbitrary operation on every node.
     *
     * @author Alexander Baumgartner
     */
    public static abstract class TraverseCallBack {
        /**
         * Propagation will stop as soon as this method returns true.
         */
        public abstract boolean exec(TermAtom atom, Hedge hedge);
    }
}
