package at.jku.risc.uarau;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        ProximityMap R = new ProximityMap(new HashSet<>(), 0.5f);
        Algorithm.solve(new Term("f", new Term[0]), new Term("g", new Term[0]), R, 0.5f);
    }
}
