package highlighting.antlr;

import java.util.List;
import java.util.Scanner;

public final class PrettyPrinterDemo {

    private PrettyPrinterDemo() {
        // Utility-Klasse
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Anzahl Leerzeichen pro Einrückstufe eingeben, z.B. 2, 4 oder 8: ");
        int indentWidth = scanner.nextInt();

        List<String> examples =
            List.of(
                """
                class Test{private String name;public String getName(){return name;}}
                """,
                """
                class Control{public String run(){if(condition){return "yes";}else{return "no";}}public String loop(){while(condition){return "loop";}}}
                """,
                """
                class Nested{public String test(){{{return "deep";}}}}
                """);

        for (int i = 0; i < examples.size(); i++) {
            String sourceCode = examples.get(i);

            System.out.println();
            System.out.println("========== Beispiel " + (i + 1) + ": Eingabe ==========");
            System.out.println(sourceCode);

            System.out.println("========== Beispiel " + (i + 1) + ": Pretty Print ==========");
            String prettyPrinted = MiniJavaPrettyPrinter.prettyPrint(sourceCode, indentWidth);
            System.out.println(prettyPrinted);
        }
    }
}
