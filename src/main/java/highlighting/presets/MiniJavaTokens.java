package highlighting.presets;

import highlighting.regex.Token;
import java.util.List;
import java.util.regex.Pattern;

public final class MiniJavaTokens {

  private MiniJavaTokens() {}

  public static List<Token> defaultTokens() {
    return List.of(
        // Wichtig: Javadoc vor normalem Blockkommentar
        Token.of(Pattern.compile("/\\*\\*[\\s\\S]*?\\*/"), MiniJavaColours.JAVADOC_COMMENT_COLOUR),
        Token.of(
            Pattern.compile("/\\*(?!\\*)[\\s\\S]*?\\*/"), MiniJavaColours.BLOCK_COMMENT_COLOUR),
        Token.of(Pattern.compile("//[^\\r\\n]*"), MiniJavaColours.LINE_COMMENT_COLOUR),

        // Strings mit einfachen Escape-Sequenzen
        Token.of(Pattern.compile("\"([^\"\\\\]|\\\\.)*\""), MiniJavaColours.STRING_LITERAL_COLOUR),

        // Ein Character oder eine Escape-Sequenz wie '\n'
        Token.of(Pattern.compile("'([^'\\\\]|\\\\.)'"), MiniJavaColours.CHAR_LITERAL_COLOUR),

        // Annotationen wie @Override oder @my-annotation
        Token.of(Pattern.compile("@[A-Za-z][A-Za-z-]*"), MiniJavaColours.ANNOTATION_COLOUR),

        // Keywords nicht als Teil anderer Bezeichner
        Token.of(
            Pattern.compile(
                "(?<![A-Za-z0-9_$])(?:package|import|class|public|private|final|return|null|new)(?![A-Za-z0-9_$])"),
            MiniJavaColours.KEYWORD_COLOUR));
  }
}
