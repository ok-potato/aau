package at.jku.risc.stout.uru;

import at.jku.risc.stout.uru.algo.DebugLevel;
import at.jku.risc.stout.uru.algo.UnificationAlgo;
import at.jku.risc.stout.uru.algo.UnificationProblem;
import at.jku.risc.stout.uru.data.InputParser;
import at.jku.risc.stout.uru.print.ErrorPrintStream;
import at.jku.risc.stout.uru.print.InfoPrintStream;
import at.jku.risc.stout.uru.print.OutPrintStream;
import at.jku.risc.stout.uru.util.ControlledException;

import java.io.PrintStream;

public class UnifyGWT implements EntryPoint {
    
    private static final String PRINT_HEAD = "";
    private static final String PRINT_TAIL = "DONE";
    
    private final PrintStream out = new OutPrintStream();
    private final PrintStream info = new InfoPrintStream();
    private final PrintStream error = new ErrorPrintStream();
    
    public static void main(String[] args) {
        new UnifyGWT().btnClicked();
    }
    
    public void btnClicked() {
        reset();
        System.out.print(PRINT_HEAD);
        try {
            String problemSet = getParameter("problemset");
            DebugLevel debugLevel = DebugLevel.SIMPLE;
            try {
                debugLevel = DebugLevel.valueOf(getParameter("debugmode"));
            } catch (Exception ignored) {
            }
            boolean justify = "true".equals(getParameter("justify"));
            int maxDepth = 1000;
            try {
                maxDepth = Integer.parseInt(getParameter("maxdepth"));
            } catch (Exception ignored) {
            }
            
            info.println("Maximum derivation depth = " + maxDepth);
            info.println("Justify computed unifiers = " + justify);
            info.println("Output format = " + debugLevel);
            info.println();
            info.println("PROBLEM:");
            UnificationProblem problem = new UnificationProblem();
            new InputParser(problem).parseEqSystem(problemSet, info);
            
            new UnificationAlgo(problem, maxDepth).unify(debugLevel, out, error, justify);
        } catch (ControlledException ex) {
            error.println(ex.getMessage() + " (" + ex.getClass().getSimpleName() + ")");
        } catch (Throwable ex) {
            ex.printStackTrace(error);
        }
        info.flush();
        out.flush();
        error.flush();
        System.out.print(PRINT_TAIL);
    }
    
    // GWT Comment Start
    public String getParameter(String name) {
        if ("problemset".equals(name)) {
            return "f(c), f(f(g(a, a)), a, a) =^= c, f(g(b, b, b), b, b, b)";
        }
        if ("debugmode".equals(name)) {
            return DebugLevel.SIMPLE.toString();
        }
        return null;
    }
    
    // GWT Comment END
    private void reset() {
    }
}
