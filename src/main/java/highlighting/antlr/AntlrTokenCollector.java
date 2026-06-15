package highlighting.antlr;

import highlighting.core.HighlightRegion;
import highlighting.core.SyntaxHighlighter;
import highlighting.presets.MiniJavaColours;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

public class AntlrTokenCollector extends SyntaxHighlighter {

    @Override
    public List<HighlightRegion> collectMatches(String text) {
        List<HighlightRegion> regions = new ArrayList<>();

        CharStream input = CharStreams.fromString(text);
        MiniJavaLexer lexer = new MiniJavaLexer(input);

        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        tokenStream.fill();

        List<Token> tokens = tokenStream.getTokens();

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);

            if (token.getType() == Token.EOF) {
                continue;
            }

            String tokenText = token.getText();
            Color colour = null;

            if ("@".equals(tokenText)) {
                addRegion(regions, token, MiniJavaColours.ANNOTATION_COLOUR);

                if (i + 1 < tokens.size()) {
                    Token next = tokens.get(i + 1);

                    if (next.getType() != Token.EOF
                        && next.getStartIndex() == token.getStopIndex() + 1
                        && isIdentifier(next)) {
                        addRegion(regions, next, MiniJavaColours.ANNOTATION_COLOUR);
                    }
                }

                continue;
            }

            if (isKeyword(tokenText)) {
                colour = MiniJavaColours.KEYWORD_COLOUR;
            } else if (isStringLiteral(token)) {
                colour = MiniJavaColours.STRING_LITERAL_COLOUR;
            } else if (isCharLiteral(token)) {
                colour = MiniJavaColours.CHAR_LITERAL_COLOUR;
            } else if (isJavadocComment(token)) {
                colour = MiniJavaColours.JAVADOC_COMMENT_COLOUR;
            } else if (isBlockComment(token)) {
                colour = MiniJavaColours.BLOCK_COMMENT_COLOUR;
            } else if (isLineComment(token)) {
                colour = MiniJavaColours.LINE_COMMENT_COLOUR;
            }

            if (colour != null) {
                addRegion(regions, token, colour);
            }
        }

        return regions;
    }

    private static void addRegion(List<HighlightRegion> regions, Token token, Color colour) {
        int start = token.getStartIndex();
        int end = token.getStopIndex() + 1;

        if (start >= 0 && end > start) {
            regions.add(new HighlightRegion(start, end, colour));
        }
    }

    private static boolean isKeyword(String text) {
        switch (text) {
            case "class":
            case "public":
            case "static":
            case "void":
            case "main":
            case "String":
            case "extends":
            case "return":
            case "int":
            case "boolean":
            case "if":
            case "else":
            case "while":
            case "true":
            case "false":
            case "this":
            case "new":
            case "length":
                return true;
            default:
                return false;
        }
    }

    private static boolean isIdentifier(Token token) {
        String name = symbolicName(token);

        return "IDENTIFIER".equals(name) || "ID".equals(name);
    }

    private static boolean isStringLiteral(Token token) {
        String text = token.getText();
        String name = symbolicName(token);

        return name.contains("STRING") || startsAndEndsWith(text, "\"");
    }

    private static boolean isCharLiteral(Token token) {
        String text = token.getText();
        String name = symbolicName(token);

        return name.contains("CHAR") || startsAndEndsWith(text, "'");
    }

    private static boolean isLineComment(Token token) {
        String text = token.getText();
        String name = symbolicName(token);

        return name.contains("LINE_COMMENT") || text.startsWith("//");
    }

    private static boolean isBlockComment(Token token) {
        String text = token.getText();
        String name = symbolicName(token);

        return !isJavadocComment(token)
            && (name.contains("BLOCK_COMMENT")
            || name.contains("MULTILINE_COMMENT")
            || text.startsWith("/*"));
    }

    private static boolean isJavadocComment(Token token) {
        String text = token.getText();
        String name = symbolicName(token);

        return name.contains("JAVADOC") || text.startsWith("/**");
    }

    private static boolean startsAndEndsWith(String text, String delimiter) {
        return text != null && text.length() >= 2 && text.startsWith(delimiter) && text.endsWith(delimiter);
    }

    private static String symbolicName(Token token) {
        String name = MiniJavaLexer.VOCABULARY.getSymbolicName(token.getType());

        if (name == null) {
            return "";
        }

        return name.toUpperCase(Locale.ROOT);
    }
}
