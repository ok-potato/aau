package at.jku.risc.stout.uru.print;

import java.io.PrintStream;

public class ErrorPrintStream extends PrintStream {
    final StringBuilder buffer = new StringBuilder();
    
    public ErrorPrintStream() {
        super(System.out);
    }
    
    public void print(int b) {
        buffer.appendCodePoint(b);
        if (b == '\n')
            flush();
    }
    
    public void print(char c) {
        buffer.append(c);
        if (c == '\n')
            flush();
    }
    
    public void print(Object obj) {
        print(String.valueOf(obj));
    }
    
    public void println(Object obj) {
        println(String.valueOf(obj));
    }
    
    public void println(String str) {
        print(str);
        println();
    }
    
    public void print(String str) {
        buffer.append(str);
    }
    
    public void println() {
        print('\n');
    }
    
    public void flush() {
        if (!buffer.isEmpty())
            System.out.print("ERROR: " + buffer);
        buffer.setLength(0);
    }
    
    public void close() {
        flush();
    }
}
