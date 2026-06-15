package highlighting.antlr;

import java.util.List;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

/// MiniJava Pretty Printer (minimal, stateful)
///
/// Requirements:
/// - Reproduce the whole program (comments and whitespaces are gone).
/// - Ignore whitespace from the input; instead, generate:
///     - indentation for class bodies and blocks,
///     - exactly one line per statement (lines ending in ';').
///
/// Simplification:
/// Everything that is not indentation or line breaks is printed as raw tokens (with a very simple
/// space heuristic). Expression and signature formatting is therefore not "nice", which is
/// acceptable for this exercise.
public final class PrettyPrinterVisitor extends MiniJavaBaseVisitor<Void> {

    private final StringBuilder out = new StringBuilder();
    private final int indentWidth;
    private int currentIndent = 0;
    private boolean atLineStart = true;

    // For simple spacing between tokens:
    private Token lastToken = null;

    public PrettyPrinterVisitor(int indentWidth) {
        this.indentWidth = Math.max(0, indentWidth);
    }

    public String result() {
        return out.toString();
    }

    // ----------------------------------------------------
    // Structural methods – these enforce indentation and "one statement per line"
    // ----------------------------------------------------

    @Override
    public Void visitCompilationUnit(MiniJavaParser.CompilationUnitContext ctx) {
        boolean hasPackage = ctx.packageDecl() != null;
        boolean hasImports = !ctx.importDecl().isEmpty();
        boolean hasTypes = !ctx.typeDecl().isEmpty();

        if (hasPackage) {
            visit(ctx.packageDecl());
            nl();

            if (hasImports || hasTypes) {
                nl();
            }
        }

        for (var importDecl : ctx.importDecl()) {
            visit(importDecl);
            nl();
        }

        if (hasImports && hasTypes) {
            nl();
        }

        for (int i = 0; i < ctx.typeDecl().size(); i++) {
            visit(ctx.typeDecl(i));

            if (i + 1 < ctx.typeDecl().size()) {
                nl();
                nl();
            }
        }

        return null;
    }

    @Override
    public Void visitClassBody(MiniJavaParser.ClassBodyContext ctx) {
        write(" {");
        nl();

        currentIndent++;

        for (var declaration : ctx.classBodyDeclaration()) {
            visit(declaration);

            if (!atLineStart) {
                nl();
            }
        }

        currentIndent--;

        write("}");
        lastToken = null;

        return null;
    }

    @Override
    public Void visitBlock(MiniJavaParser.BlockContext ctx) {
        if (atLineStart) {
            write("{");
        } else {
            write(" {");
        }

        nl();

        currentIndent++;

        for (var statement : ctx.blockStatement()) {
            visit(statement);

            if (!atLineStart) {
                nl();
            }
        }

        currentIndent--;

        write("}");
        lastToken = null;

        return null;
    }

    @Override
    public Void visitStatement(MiniJavaParser.StatementContext ctx) {
        int startType = ctx.getStart().getType();

        if (ctx.block() != null) {
            visit(ctx.block());
            return null;
        }

        if (startType == MiniJavaLexer.RETURN) {
            write("return");

            MiniJavaParser.ExpressionContext expression = firstExpression(ctx);
            if (expression != null) {
                write(" ");
                visit(expression);
            }

            write(";");
            lastToken = null;
            return null;
        }

        if (startType == MiniJavaLexer.IF) {
            write("if");
            write("(");

            MiniJavaParser.ExpressionContext expression = firstExpression(ctx);
            if (expression != null) {
                visit(expression);
            }

            write(")");

            List<MiniJavaParser.StatementContext> statements = childStatements(ctx);
            MiniJavaParser.StatementContext thenStatement = statements.get(0);

            if (thenStatement.block() != null) {
                visit(thenStatement);
            } else {
                nl();
                currentIndent++;
                visit(thenStatement);
                currentIndent--;
            }

            if (statements.size() > 1) {
                MiniJavaParser.StatementContext elseStatement = statements.get(1);

                if (thenStatement.block() == null && !atLineStart) {
                    nl();
                }

                if (elseStatement.block() != null) {
                    write(" else");
                    visit(elseStatement);
                } else {
                    write("else");
                    nl();
                    currentIndent++;
                    visit(elseStatement);
                    currentIndent--;
                }
            }

            lastToken = null;
            return null;
        }

        if (startType == MiniJavaLexer.WHILE) {
            write("while");
            write("(");

            MiniJavaParser.ExpressionContext expression = firstExpression(ctx);
            if (expression != null) {
                visit(expression);
            }

            write(")");

            List<MiniJavaParser.StatementContext> statements = childStatements(ctx);
            MiniJavaParser.StatementContext body = statements.get(0);

            if (body.block() != null) {
                visit(body);
            } else {
                nl();
                currentIndent++;
                visit(body);
                currentIndent--;
            }

            lastToken = null;
            return null;
        }

        MiniJavaParser.ExpressionContext expression = firstExpression(ctx);
        if (expression != null) {
            visit(expression);
            write(";");
            lastToken = null;
        }

        return null;
    }

    // ---------------- helper methods ----------------

    private static MiniJavaParser.ExpressionContext firstExpression(
        MiniJavaParser.StatementContext ctx) {
        List<MiniJavaParser.ExpressionContext> expressions =
            ctx.getRuleContexts(MiniJavaParser.ExpressionContext.class);

        if (expressions.isEmpty()) {
            return null;
        }

        return expressions.get(0);
    }

    private static List<MiniJavaParser.StatementContext> childStatements(
        MiniJavaParser.StatementContext ctx) {
        return ctx.getRuleContexts(MiniJavaParser.StatementContext.class);
    }

    private void indent() {
        if (atLineStart) {
            out.repeat(" ", Math.max(0, indentWidth * currentIndent));
            atLineStart = false;
        }
    }

    private void write(String s) {
        if (s == null || s.isEmpty()) return;
        indent();
        out.append(s);
    }

    private void nl() {
        out.append('\n');
        atLineStart = true;
        lastToken = null; // Reset spacing context at the beginning of a line
    }

    private void writeln(String s) {
        write(s);
        nl();
    }

    // --------------- token output + basic spacing ---------------

    @Override
    public Void visitTerminal(TerminalNode node) {
        Token t = node.getSymbol();

        if (t.getType() == Token.EOF) {
            return null;
        }

        String text = t.getText();

        if (lastToken != null) {
            int prevType = lastToken.getType();
            int curType = t.getType();

            // Simple heuristic: insert a space between "word-like" tokens
            if (needsSpaceBetween(prevType, curType)) write(" ");
        }

        write(text);
        lastToken = t;
        return null;
    }

    private boolean needsSpaceBetween(int prevType, int curType) {
        return isWordLike(prevType) && isWordLike(curType);
    }

    private boolean isWordLike(int type) {
        return type == MiniJavaLexer.IDENTIFIER
            || type == MiniJavaLexer.STRING_LITERAL
            || type == MiniJavaLexer.CHAR_LITERAL
            || type == MiniJavaLexer.NULL
            || type == MiniJavaLexer.PACKAGE
            || type == MiniJavaLexer.IMPORT
            || type == MiniJavaLexer.CLASS
            || type == MiniJavaLexer.PUBLIC
            || type == MiniJavaLexer.PRIVATE
            || type == MiniJavaLexer.FINAL
            || type == MiniJavaLexer.RETURN
            || type == MiniJavaLexer.NEW
            || type == MiniJavaLexer.IF
            || type == MiniJavaLexer.ELSE
            || type == MiniJavaLexer.WHILE
            || type == MiniJavaLexer.EXTENDS
            || type == MiniJavaLexer.IMPLEMENTS;
    }
}
