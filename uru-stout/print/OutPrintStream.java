package at.jku.risc.stout.uru.print;

import java.io.PrintStream;

public class OutPrintStream extends PrintStream {
    final StringBuilder buffer = new StringBuilder();
    long count = 0L;
    
    public OutPrintStream() {
        super(System.out);
    }
    
    public void print(int b) {
        buffer.appendCodePoint(b);
        if (b == '\n') {
            count++;
            flush();
        }
    }
    
    public void print(char c) {
        buffer.append(c);
        if (c == '\n') {
            count++;
            flush();
        }
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
        String lineNum = "000000" + count + ": ";
        if (!buffer.isEmpty())
            System.out.print(lineNum.substring(lineNum.length() - 8) + buffer);
        buffer.setLength(0);
    }
    
    public void close() {
        flush();
        count = 0L;
    }
}
