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

import at.jku.risc.stout.uru.data.Hedge;
import at.jku.risc.stout.uru.data.TermNode;
import at.jku.risc.stout.uru.data.atom.HedgeVar;
import at.jku.risc.stout.uru.data.atom.TermAtom;
import at.jku.risc.stout.uru.data.atom.TermVar;
import at.jku.risc.stout.uru.data.atom.Variable;

import java.io.PrintStream;
import java.util.*;
import java.util.Map.Entry;

public class UnificationAlgo {
    private final UnifSystem system;
    private final long maxDerivationDepth;
    private final Set<Substitution> result = new HashSet<>();
    
    private boolean maxDepthReached = false;
    
    public UnificationAlgo(UnificationProblem problem, long maxDerivationDepth) {
        this.system = new UnifSystem(problem, new Substitution());
        this.maxDerivationDepth = maxDerivationDepth;
        while (!system.problem.isEmpty()
                && system.problem.getFirst().left.equals(system.problem.getFirst().right)) {
            system.problem.remove();
        }
    }
    
    public Set<Substitution> unify(DebugLevel debugLevel, PrintStream debugOut, PrintStream debugError, boolean justify) {
        // Projection
        final Stack<UnifSystem> systems = projection(system);
        
        while (!systems.isEmpty()) {
            UnifSystem sys = systems.pop();
            while (!sys.problem.isEmpty()) {
                UnificationEquation eq = sys.problem.remove();
                if (eq.derivationDepth >= maxDerivationDepth) {
                    // Max derivation depth reached ===>> BOT
                    sys.problem.add(eq);
                    maxDepthReached = true;
                    error("Derivation depth " + maxDerivationDepth + " reached", debugLevel, debugError);
                    break;
                }
                eq.incDerivationDepth();
                TermAtom leftA = eq.left.getAtom();
                TermAtom rightA = eq.right.getAtom();
                Hedge leftH = eq.left.getHedge();
                Hedge rightH = eq.right.getHedge();
                if (isClash(leftH, rightH)) {
                    sys.problem.add(eq);
                    error("Irreparable difference encountered", debugLevel, debugError);
                    break;
                }
                if (!leftA.equals(rightA)) {
                    if (rightA instanceof TermVar && !(leftA instanceof TermVar)) {
                        // Orient 1
                        eq.swap();
                        sys.problem.add(eq);
                        debug("Orient 1", sys, debugLevel, debugOut, false);
                    } else if (leftA instanceof TermVar && rightA != TermAtom.nullAtom) {
                        if (eq.right.occurs(leftA)) {
                            // Individual Variable Occurrence Check
                            sys.problem.add(eq);
                            error("Individual Variable Occurrence Check", debugLevel, debugError);
                            break;
                        } else {
                            // Solve
                            sys.apply((TermVar) leftA, eq.right);
                            debug("Solve", sys, debugLevel, debugOut, false);
                        }
                    } else {
                        // No rule applicable ===>> BOT
                        sys.problem.add(eq);
                        error("Symbol Clash", debugLevel, debugError);
                        break;
                    }
                } else if (leftH.equals(rightH)) {
                    // Trivial
                    debug("Trivial", sys, debugLevel, debugOut, false);
                } else if (rightH.isEmpty() || leftH.isEmpty()) {
                    // No rule applicable ===>> BOT
                    sys.problem.add(eq);
                    error("Empty", debugLevel, debugError);
                    break;
                } else if (leftH.get(0).getAtom() instanceof HedgeVar) {
                    if (leftH.get(0).getAtom() == rightH.get(0).getAtom()) {
                        // Sequence Variable Elimination 1
                        leftH.getSequence().remove(0);
                        rightH.getSequence().remove(0);
                        sys.problem.add(eq);
                        debug("Sequence Variable Elimination 1", sys, debugLevel, debugOut, false);
                    } else {
                        HedgeVar leftVar = (HedgeVar) leftH.get(0).getAtom();
                        if (!rightH.get(0).occurs(leftVar)) {
                            // Widening 1
                            UnifSystem sysNew = sys.copy();
                            UnificationEquation eqNew = eq.copy();
                            List<TermNode> leftSeq = eqNew.left.getHedge().getSequence();
                            TermNode x = leftSeq.remove(0);
                            Hedge widen = new Hedge();
                            widen.add(eqNew.right.getHedge().getSequence().remove(0)); // t
                            widen.add(x);
                            sysNew.problem.add(eqNew);
                            sysNew.apply(leftVar, new TermNode(null, widen));
                            leftSeq.add(0, x);
                            systems.push(sysNew);
                            debug("Widening 1", sysNew, debugLevel, debugOut, true);
                            if (rightH.get(0).getAtom() instanceof HedgeVar) {
                                // Widening 2
                                sysNew = sys.copy();
                                eqNew = eq.copy();
                                List<TermNode> rightSeq = eqNew.right.getHedge().getSequence();
                                TermNode y = rightSeq.remove(0);
                                widen = new Hedge();
                                widen.add(eqNew.left.getHedge().getSequence().remove(0)); // x
                                widen.add(y);
                                sysNew.problem.add(eqNew);
                                sysNew.apply((Variable) y.getAtom(), new TermNode(null, widen));
                                rightSeq.add(0, y);
                                systems.push(sysNew);
                                debug("Widening 2", sysNew, debugLevel, debugOut, true);
                            }
                            // Sequence Variable Elimination 2
                            leftH.getSequence().remove(0);
                            TermNode t = rightH.getSequence().remove(0);
                            sys.problem.add(eq);
                            sys.apply(leftVar, t);
                            debug("Sequence Variable Elimination 2", sys, debugLevel, debugOut, false);
                        } else {
                            // No rule applicable ===>> BOT
                            sys.problem.add(eq);
                            error("Sequence Variable Occurrence Check", debugLevel, debugError);
                            break;
                        }
                    }
                } else if (rightH.get(0).getAtom() instanceof HedgeVar) {
                    // Orient 2
                    eq.swap();
                    sys.problem.add(eq);
                    debug("Orient 2", sys, debugLevel, debugOut, false);
                } else {
                    int idx = idxHedgeVar(leftH, rightH);
                    if (idx != -1) {
                        // Partial Decomposition 1
                        for (int i = 0; i < idx; i++) {
                            UnificationEquation eqDecomposition = new UnificationEquation(leftH.get(i), rightH.get(i));
                            eqDecomposition.derivationDepth = eq.derivationDepth;
                            sys.problem.add(eqDecomposition);
                        }
                        eq.left = new TermNode(leftA, leftH.subHedge(idx, leftH.size()));
                        eq.right = new TermNode(rightA, rightH.subHedge(idx, rightH.size()));
                        sys.problem.add(eq);
                        debug("Partial Decomposition 1", sys, debugLevel, debugOut, false);
                    } else if (leftH.size() == rightH.size()) {
                        // Total Decomposition
                        for (int i = leftH.size() - 1; i >= 0; i--) {
                            UnificationEquation eqDecomposition = new UnificationEquation(leftH.get(i), rightH.get(i));
                            eqDecomposition.derivationDepth = eq.derivationDepth;
                            sys.problem.add(eqDecomposition);
                        }
                        if (leftA != TermAtom.nullAtom || leftH.size() != 1) {
                            debug("Total Decomposition", sys, debugLevel, debugOut, false); // Non-trivial
                        }
                    } else {
                        // No rule applicable ===>> BOT
                        sys.problem.add(eq);
                        error("Arity Disagreement", debugLevel, debugOut);
                        break;
                    }
                }
            }
            // Empty problem set = solution found
            if (sys.problem.isEmpty()) {
                if (justify) {
                    UnificationProblem problemOrig = system.problem.copy();
                    while (!problemOrig.isEmpty()) {
                        UnificationEquation eqOriginal = problemOrig.remove();
                        eqOriginal.apply(sys.sigma);
                        if (!eqOriginal.left.equals(eqOriginal.right)) {
                            return null;
                        }
                    }
                }
                result.add(sys.sigma);
            }
        }
        return result;
    }
    
    private boolean isClash(Hedge leftHedge, Hedge rightHedge) {
        Map<TermAtom, Integer> leftHeadVars = leftHedge.headVars();
        Map<TermAtom, Integer> rightHeadVars = rightHedge.headVars();
        boolean hasHedgeVar = false;
        for (TermAtom x : leftHeadVars.keySet()) {
            if (x instanceof HedgeVar) {
                hasHedgeVar = true;
                break;
            }
        }
        if (!hasHedgeVar) {
            for (TermAtom x : rightHeadVars.keySet()) {
                if (x instanceof HedgeVar) {
                    hasHedgeVar = true;
                    break;
                }
            }
            if (!hasHedgeVar) {
                return false;
            }
        }
        // has hedge variable
        Map<TermAtom, Integer> leftHeadFunctions = leftHedge.headFunctions();
        Map<TermAtom, Integer> rightHeadFunctions = rightHedge.headFunctions();
        
        if (multiSetContainsAll(leftHeadVars, rightHeadVars)) {
            if (!multiSetContainsAll(rightHeadFunctions, leftHeadFunctions)) {
                return true;
            }
        }
        if (multiSetContainsAll(rightHeadVars, leftHeadVars)) {
            if (!multiSetContainsAll(leftHeadFunctions, rightHeadFunctions)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean multiSetContainsAll(Map<TermAtom, Integer> checkContainment, Map<TermAtom, Integer> elementsToCheck) {
        for (Entry<TermAtom, Integer> element : elementsToCheck.entrySet()) {
            Integer contained = checkContainment.get(element.getKey());
            if (contained == null || contained < element.getValue()) {
                return false;
            }
        }
        return true;
    }
    
    private void debug(String msg, UnifSystem sys, DebugLevel debugLevel, PrintStream out, boolean newBranch) {
        if (debugLevel == DebugLevel.PROGRESS && out != null) {
            out.println("  ==>  " + msg + " ==>");
            if (newBranch) {
                out.println("  New branch generated!");
            }
        }
        debug(debugLevel, out, sys);
    }
    
    private Stack<UnifSystem> projection(UnifSystem sys) {
        Stack<UnifSystem> systems = new Stack<>();
        systems.push(sys.copy());
        Set<HedgeVar> varSet = sys.problem.collectHedgeVars();
        HedgeVar[] vars = new HedgeVar[varSet.size()];
        int n = 1 << varSet.toArray(vars).length;
        for (int counter = 1; counter < n; counter++) {
            UnifSystem sysProj = sys.copy();
            systems.push(sysProj);
            for (int j = 0; j < vars.length; j++) {
                if ((counter & (1 << j)) != 0) {
                    sysProj.eliminateSeqVariable(vars[j]);
                }
            }
        }
        return systems;
    }
    
    private void debug(DebugLevel debugLevel, PrintStream out, UnifSystem sys) {
        if (debugLevel == DebugLevel.PROGRESS && out != null) {
            out.println("  System: " + sys);
            out.println();
        }
    }
    
    private void error(String msg, DebugLevel debugLevel, PrintStream out) {
        if (debugLevel == DebugLevel.PROGRESS && out != null) {
            out.println("  ==>  " + msg);
            out.println();
        }
    }
    
    private int idxHedgeVar(Hedge leftHedge, Hedge rightHedge) {
        for (int n = Math.min(leftHedge.size(), rightHedge.size()), i = 0; i < n; i++) {
            if (leftHedge.get(i).getAtom() instanceof HedgeVar || rightHedge.get(i).getAtom() instanceof HedgeVar) {
                return i;
            }
        }
        return -1;
    }
}
