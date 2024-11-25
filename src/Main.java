import java.io.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1)  {
            System.out.println("expected jbran [file]");
            System.exit(1);
        }

        if (Arrays.asList(args).contains("-r") || Arrays.asList(args).contains("--repl")) {
            var scanner = new Scanner(System.in);
            var codeAcc = new StringBuilder();
            while (true) {
                var input = scanner.next();
                input = input.trim();
                if (Objects.equals(input, "!!exit")) {
                    break;
                } else if (Objects.equals(input, "!!show")) {
                    System.out.printf("'%s'\n", codeAcc.toString());
                } else if (input.startsWith("!!")) {
                    System.out.printf("invalid command '%s'\n", input.substring(2));
                } else {
                    if (!input.trim().isEmpty()) codeAcc.append(input.trim());
                    var i = new Interpreter();
                    try {
                        i.interpret(codeAcc.toString());
                        System.out.println();
                        System.out.println(i.dump());
                    } catch (InterpreterException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
            scanner.close();
        } else {
            String path = args[0];

            String content = "";
            try (Scanner s = new Scanner(new File(path))) {
                StringBuilder builder = new StringBuilder();
                while (s.hasNextLine()) builder.append(s.nextLine());
                content = builder.toString();
            } catch (IOException e) {
                if (e.getClass().equals(FileNotFoundException.class)) {
                    System.out.printf("file '%s' does not exist\n", path);
                } else {
                    System.out.printf("unable to read file '%s'\n", path);
                }
                System.exit(1);
            }

            var interpreter = new Interpreter();
            try {
                interpreter.interpret(content);
            } catch (InterpreterException e) {
                System.out.println(e.getMessage());
                System.exit(1);
            }
        }
    }
}