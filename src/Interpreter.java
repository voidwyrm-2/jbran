import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Stack;

public class Interpreter {
    private final int[] tape;
    private int tapePtr;

    Interpreter() {
        this.tape = new int[30000];
        this.tapePtr = 0;
    }

    public final void interpret(String text) throws InterpreterException {
        var chars = text.chars().toArray();

        record Jpos(int ln, int col, int idx) {};

        HashMap<Integer, Integer> jumpMap = new HashMap<>();
        Stack<Jpos> jumpStack = new Stack<>();
        int ln = 1;
        int col = 0;
        for (int i = 0; i < chars.length; i++) {
            col++;
            if (chars[i] == '[') {
                jumpStack.push(new Jpos(ln, col, i));
            } else if (chars[i] == ']') {
                if (jumpStack.empty()) throw new InterpreterException(String.format("error on line %d, col %d: ']' missing matching '['", ln, col));
                var pos = jumpStack.pop();
                jumpMap.put(pos.idx, i);
                jumpMap.put(i, pos.idx);
            } else if (chars[i] == '\n') {
                ln++;
                col = 0;
            }
        }

        if (!jumpStack.empty()) {
            var pos = jumpStack.pop();
            throw new InterpreterException(String.format("error on line %d, col %d: '[' missing matching ']'", pos.ln, pos.col));
        }

        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case '+':
                    if (tape[tapePtr] == 255) {
                        tape[tapePtr] = 0;
                    } else {
                        tape[tapePtr]++;
                    }
                    break;
                case '-':
                    if (tape[tapePtr] == 0) {
                        tape[tapePtr] = 255;
                    } else {
                        tape[tapePtr]--;
                    }
                    break;
                case '>':
                    if (tapePtr + 1 >= tape.length) {
                        tapePtr = 0;
                    } else {
                        tapePtr++;
                    }
                    break;
                case '<':
                    if (tapePtr - 1 < 0) {
                        tapePtr = tape.length - 1;
                    } else {
                        tapePtr--;
                    }
                    break;
                case '[':
                    if (tape[tapePtr] == 0) i = jumpMap.get(i);
                    break;
                case ']':
                    if (tape[tapePtr] != 0) i = jumpMap.get(i);
                    break;
                case '.':
                    System.out.print((char)tape[tapePtr]);
                    break;
                case ',': {
                    try {
                        var b = System.in.read();
                        tape[tapePtr] = Math.min(b, 0);
                    } catch (IOException _) {
                        tape[tapePtr] = 0;
                    }
                }
                    break;
            }
        }
    }

    public final void setTape(int i, byte b) {
        tape[i] = b;
    }

    public final void setTape(int i, int b) {
        this.setTape(i, (byte)b);
    }

    public final byte getTape(int i) {
        return (byte)tape[i];
    }

    public final void setPtr(int i) {
        tapePtr = Math.min(Math.max(i, tape.length - 1), 0);
    }

    public final int getPtr() {
        return tapePtr;
    }

    public final String dump(int limit) {
        StringBuilder out = new StringBuilder();
        int zcount = 0;
        for (int i = 0; i < limit && i < tape.length; i++) {
            if (tape[i] == 0) {
                zcount++;
            } else {
                if (zcount != 0) out.append(String.format("...%d %s... ", zcount, zcount == 1 ? "zero" : "zeroes"));
                out.append(String.format("%d ", tape[i]));
                zcount = 0;
            }
        }

        if (zcount != 0) {
            out.append(String.format("...%d %s... ", zcount, zcount == 1 ? "zero" : "zeroes"));
        }

        return "[" + out.toString().trim() + "]";
    }

    public final String dump() {
        return this.dump(30000);
    }
}
