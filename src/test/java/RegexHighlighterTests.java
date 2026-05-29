import static org.junit.jupiter.api.Assertions.*;

import highlighting.core.HighlightRegion;
import highlighting.regex.RegexHighlighter;
import java.awt.Color;
import java.util.List;
import org.junit.jupiter.api.Test;

public class RegexHighlighterTests {

  private static final RegexHighlighter REGEX_HIGHLIGHTER = new RegexHighlighter();

  private static List<String> highlightedTexts(String text) {
    return REGEX_HIGHLIGHTER.computeRegions(text).stream()
        .map(region -> text.substring(region.start(), region.end()))
        .toList();
  }

  @Test
  void regexHighlighterCollectsSimpleMatchesWithoutOverlaps() {
    String text = "public class Test";

    List<String> matches = highlightedTexts(text);

    assertEquals(List.of("public", "class"), matches);
  }

  @Test
  void regexHighlighterCollectMatchesKeepsOverlappingRawMatches() {
    String text = "// return new class";

    List<String> rawMatches =
        REGEX_HIGHLIGHTER.collectMatches(text).stream()
            .map(region -> text.substring(region.start(), region.end()))
            .toList();

    assertTrue(rawMatches.contains("// return new class"));
    assertTrue(rawMatches.contains("return"));
    assertTrue(rawMatches.contains("new"));
    assertTrue(rawMatches.contains("class"));
  }

  @Test
  void regexHighlighterRemovesKeywordInsideLineCommentAfterConflictResolution() {
    String text = "// return new class";

    List<HighlightRegion> regions = REGEX_HIGHLIGHTER.computeRegions(text);

    assertEquals(1, regions.size());
    assertEquals(
        "// return new class", text.substring(regions.get(0).start(), regions.get(0).end()));
  }

  @Test
  void regexHighlighterKeepsJavadocAsSingleRegion() {
    String text = "/** public class Test */";

    List<HighlightRegion> regions = REGEX_HIGHLIGHTER.computeRegions(text);

    assertEquals(1, regions.size());
    assertEquals(
        "/** public class Test */", text.substring(regions.get(0).start(), regions.get(0).end()));
  }

  @Test
  void regexHighlighterKeepsAdjacentRegions() {
    String text = "return\"abc\"";

    List<HighlightRegion> regions = REGEX_HIGHLIGHTER.computeRegions(text);

    assertEquals(2, regions.size());

    assertEquals("return", text.substring(regions.get(0).start(), regions.get(0).end()));
    assertEquals("\"abc\"", text.substring(regions.get(1).start(), regions.get(1).end()));

    assertEquals(0, regions.get(0).start());
    assertEquals(6, regions.get(0).end());

    assertEquals(6, regions.get(1).start());
    assertEquals(11, regions.get(1).end());
  }

  @Test
  void regexHighlighterReturnsEmptyListForEmptyString() {
    assertTrue(REGEX_HIGHLIGHTER.computeRegions("").isEmpty());
  }

  @Test
  void regexHighlighterReturnsEmptyListForTextWithoutMatches() {
    assertTrue(REGEX_HIGHLIGHTER.computeRegions("normalIdentifier anotherIdentifier").isEmpty());
  }

  @Test
  void resolveConflictsKeepsNonOverlappingAdjacentRegions() {
    HighlightRegion first = new HighlightRegion(0, 5, Color.RED);
    HighlightRegion second = new HighlightRegion(5, 10, Color.BLUE);

    List<HighlightRegion> result = REGEX_HIGHLIGHTER.resolveConflicts(List.of(first, second));

    assertEquals(List.of(first, second), result);
  }

  @Test
  void resolveConflictsRemovesLaterOverlappingRegion() {
    HighlightRegion first = new HighlightRegion(0, 10, Color.RED);
    HighlightRegion second = new HighlightRegion(3, 8, Color.BLUE);

    List<HighlightRegion> result = REGEX_HIGHLIGHTER.resolveConflicts(List.of(first, second));

    assertEquals(List.of(first), result);
  }

  @Test
  void resolveConflictsKeepsRegionStartingExactlyAtPreviousEnd() {
    HighlightRegion first = new HighlightRegion(0, 5, Color.RED);
    HighlightRegion second = new HighlightRegion(5, 8, Color.BLUE);

    List<HighlightRegion> result = REGEX_HIGHLIGHTER.resolveConflicts(List.of(first, second));

    assertEquals(2, result.size());
    assertEquals(first, result.get(0));
    assertEquals(second, result.get(1));
  }
}
