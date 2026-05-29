import static org.junit.jupiter.api.Assertions.*;

import highlighting.core.HighlightRegion;
import highlighting.regex.ScanningHighlighter;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ScanningHighlighterTests {

  private static final ScanningHighlighter SCANNING_HIGHLIGHTER = new ScanningHighlighter();

  private static List<String> highlightedTexts(String text) {
    return SCANNING_HIGHLIGHTER.computeRegions(text).stream()
        .map(region -> text.substring(region.start(), region.end()))
        .toList();
  }

  @Test
  void scanningHighlighterFindsSimpleMatchesInOrder() {
    String text = "public class Test";

    List<String> matches = highlightedTexts(text);

    assertEquals(List.of("public", "class"), matches);
  }

  @Test
  void scanningHighlighterReturnsEmptyListForEmptyString() {
    assertTrue(SCANNING_HIGHLIGHTER.computeRegions("").isEmpty());
  }

  @Test
  void scanningHighlighterReturnsEmptyListForTextWithoutMatches() {
    assertTrue(SCANNING_HIGHLIGHTER.computeRegions("normalIdentifier anotherIdentifier").isEmpty());
  }

  @Test
  void scanningHighlighterKeepsAdjacentRegions() {
    String text = "return\"abc\"";

    List<HighlightRegion> regions = SCANNING_HIGHLIGHTER.computeRegions(text);

    assertEquals(2, regions.size());

    assertEquals("return", text.substring(regions.get(0).start(), regions.get(0).end()));
    assertEquals("\"abc\"", text.substring(regions.get(1).start(), regions.get(1).end()));

    assertEquals(0, regions.get(0).start());
    assertEquals(6, regions.get(0).end());

    assertEquals(6, regions.get(1).start());
    assertEquals(11, regions.get(1).end());
  }

  @Test
  void scanningHighlighterUsesLongestMatchAtCurrentPosition() {
    String text = "/** public class Test */";

    List<HighlightRegion> regions = SCANNING_HIGHLIGHTER.computeRegions(text);

    assertEquals(1, regions.size());
    assertEquals(
        "/** public class Test */", text.substring(regions.get(0).start(), regions.get(0).end()));
  }

  @Test
  void scanningHighlighterDoesNotHighlightKeywordInsideLineComment() {
    String text = "// return new class";

    List<HighlightRegion> regions = SCANNING_HIGHLIGHTER.computeRegions(text);

    assertEquals(1, regions.size());
    assertEquals(
        "// return new class", text.substring(regions.get(0).start(), regions.get(0).end()));
  }

  @Test
  void scanningHighlighterDoesNotHighlightCommentLikeTextInsideString() {
    String text = "\"this is not // a comment\"";

    List<HighlightRegion> regions = SCANNING_HIGHLIGHTER.computeRegions(text);

    assertEquals(1, regions.size());
    assertEquals(
        "\"this is not // a comment\"",
        text.substring(regions.get(0).start(), regions.get(0).end()));
  }

  @Test
  void scanningHighlighterFindsMultipleSeparateMatches() {
    String text = "@Override public class Test { return null; }";

    List<String> matches = highlightedTexts(text);

    assertEquals(List.of("@Override", "public", "class", "return", "null"), matches);
  }

  @Test
  void normalizeReturnsSameListInstance() {
    List<HighlightRegion> regions =
        List.of(
            new HighlightRegion(0, 5, java.awt.Color.RED),
            new HighlightRegion(5, 10, java.awt.Color.BLUE));

    assertSame(regions, SCANNING_HIGHLIGHTER.normalize(regions));
  }

  @Test
  void scanningHighlighterProducesSortedNonOverlappingRegions() {
    String text = "public \"hello\" // return class";

    List<HighlightRegion> regions = SCANNING_HIGHLIGHTER.computeRegions(text);

    for (int i = 1; i < regions.size(); i++) {
      HighlightRegion previous = regions.get(i - 1);
      HighlightRegion current = regions.get(i);

      assertTrue(previous.end() <= current.start());
    }
  }
}
