import static org.junit.jupiter.api.Assertions.*;

import highlighting.presets.MiniJavaTokens;
import highlighting.regex.Token;
import java.util.List;
import org.junit.jupiter.api.Test;

class MiniJavaTokensTest {

  private static final List<Token> TOKENS = MiniJavaTokens.defaultTokens();

  private static final Token JAVADOC_COMMENT = TOKENS.get(0);
  private static final Token BLOCK_COMMENT = TOKENS.get(1);
  private static final Token LINE_COMMENT = TOKENS.get(2);
  private static final Token STRING = TOKENS.get(3);
  private static final Token CHARACTER = TOKENS.get(4);
  private static final Token ANNOTATION = TOKENS.get(5);
  private static final Token KEYWORD = TOKENS.get(6);

  private static List<String> matches(Token token, String text) {
    return token.test(text).stream()
        .map(region -> text.substring(region.start(), region.end()))
        .toList();
  }

  @Test
  void stringLiteralMatchesAtBeginningMiddleAndEnd() {
    assertEquals(List.of("\"hello\""), matches(STRING, "\"hello\" int x;"));
    assertEquals(List.of("\"hello\""), matches(STRING, "String s = \"hello\";"));
    assertEquals(List.of("\"hello\""), matches(STRING, "return \"hello\""));
  }

  @Test
  void stringLiteralMatchesMultipleStrings() {
    assertEquals(List.of("\"a\"", "\"b\""), matches(STRING, "\"a\" + \"b\""));
  }

  @Test
  void stringLiteralDoesNotMatchUnclosedString() {
    assertTrue(matches(STRING, "\"not closed").isEmpty());
  }

  @Test
  void stringLiteralMayContainCommentLikeText() {
    assertEquals(
        List.of("\"this is not // a comment\""),
        matches(STRING, "String s = \"this is not // a comment\";"));

    assertEquals(
        List.of("\"this is not /* a comment */\""),
        matches(STRING, "String s = \"this is not /* a comment */\";"));
  }

  @Test
  void characterLiteralMatchesSingleCharacter() {
    assertEquals(List.of("'a'"), matches(CHARACTER, "'a'"));
    assertEquals(List.of("'x'"), matches(CHARACTER, "char c = 'x';"));
  }

  @Test
  void characterLiteralMatchesEscapeSequence() {
    assertEquals(List.of("'\\n'"), matches(CHARACTER, "char c = '\\n';"));
  }

  @Test
  void characterLiteralDoesNotMatchMultipleCharacters() {
    assertTrue(matches(CHARACTER, "'ab'").isEmpty());
  }

  @Test
  void keywordsMatchAsWholeWords() {
    assertEquals(
        List.of("public", "class", "return", "null", "new"),
        matches(KEYWORD, "public class Test { return null; new Test(); }"));
  }

  @Test
  void keywordsDoNotMatchInsideIdentifiers() {
    assertTrue(matches(KEYWORD, "className").isEmpty());
    assertTrue(matches(KEYWORD, "myreturnValue").isEmpty());
    assertTrue(matches(KEYWORD, "newObject").isEmpty());
    assertTrue(matches(KEYWORD, "notnull").isEmpty());
  }

  @Test
  void keywordMatchesAtBeginningMiddleAndEnd() {
    assertEquals(List.of("class"), matches(KEYWORD, "class Test"));
    assertEquals(List.of("return"), matches(KEYWORD, "int x; return y;"));
    assertEquals(List.of("new"), matches(KEYWORD, "x = new"));
  }

  @Test
  void annotationMatchesAtLineBeginningAndAfterWhitespace() {
    assertEquals(List.of("@Override"), matches(ANNOTATION, "@Override"));
    assertEquals(List.of("@Override"), matches(ANNOTATION, "  @Override"));
    assertEquals(List.of("@my-annotation"), matches(ANNOTATION, "@my-annotation"));
  }

  @Test
  void annotationDoesNotMatchWithoutName() {
    assertTrue(matches(ANNOTATION, "@").isEmpty());
    assertTrue(matches(ANNOTATION, "normalText").isEmpty());
  }

  @Test
  void lineCommentMatchesUntilLineEnd() {
    assertEquals(List.of("// comment"), matches(LINE_COMMENT, "// comment\nint x;"));
  }

  @Test
  void lineCommentMayContainKeywordLikeText() {
    assertEquals(
        List.of("// public class return null new"),
        matches(LINE_COMMENT, "// public class return null new"));
  }

  @Test
  void blockCommentMatchesAcrossLines() {
    String text = "/* first line\nsecond line */ int x;";

    assertEquals(List.of("/* first line\nsecond line */"), matches(BLOCK_COMMENT, text));
  }

  @Test
  void javadocCommentMatchesAcrossLines() {
    String text = "/**\n * documentation\n */ class Test {}";

    assertEquals(List.of("/**\n * documentation\n */"), matches(JAVADOC_COMMENT, text));
  }

  @Test
  void normalBlockCommentDoesNotMatchJavadocComment() {
    assertTrue(matches(BLOCK_COMMENT, "/** documentation */").isEmpty());
  }

  @Test
  void multipleLineCommentsMatchSeparately() {
    String text = "// first\nint x;\n// second";

    assertEquals(List.of("// first", "// second"), matches(LINE_COMMENT, text));
  }
}
