package highlighting.antlr;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public final class MiniJavaPrettyPrinter {

    private MiniJavaPrettyPrinter() {
        // Utility-Klasse
    }

    public static MiniJavaParser.CompilationUnitContext parse(String sourceCode) {
        var input = CharStreams.fromString(sourceCode);
        var lexer = new MiniJavaLexer(input);
        var tokens = new CommonTokenStream(lexer);
        var parser = new MiniJavaParser(tokens);

        MiniJavaParser.CompilationUnitContext tree = parser.compilationUnit();

        if (parser.getNumberOfSyntaxErrors() > 0) {
            throw new IllegalArgumentException("MiniJava-Code enthält Syntaxfehler.");
        }

        return tree;
    }

    public static String prettyPrint(String sourceCode, int indentWidth) {
        MiniJavaParser.CompilationUnitContext tree = parse(sourceCode);

        PrettyPrinterVisitor visitor = new PrettyPrinterVisitor(indentWidth);
        visitor.visit(tree);

        return visitor.result();
    }
}
