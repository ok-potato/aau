package at.jku.risc.stout.urau;

import at.jku.risc.stout.urau.algo.*;
import at.jku.risc.stout.urau.data.EquationSystem;
import at.jku.risc.stout.urau.data.InputParser;
import at.jku.risc.stout.urau.data.MalformedTermException;
import at.jku.risc.stout.urau.data.atom.Variable;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class TestPaperExample {
    
    public static void main(String[] args) throws MalformedTermException, IOException, IllegalAlignmentException {
        Reader in1 = new StringReader("a,a,a"); // Use FileReader instead
        Reader in2 = new StringReader("a,a,a,a"); // Use FileReader instead
        boolean iterateAll = true;
        
        RigidityFnc rFnc = new RigidityFncSubsequence().setMinLen(3);
        EquationSystem eqSys = new EquationSystem();
        new InputParser(eqSys).parseHedgeEquation(in1, in2);
        
        new AntiUnify(rFnc, eqSys) {
            public void callback(AntiUnifySystem res, Variable var) {
                System.out.println(res.getSigma().get(var));
            }
        }.antiUnify(iterateAll);
    }
}
