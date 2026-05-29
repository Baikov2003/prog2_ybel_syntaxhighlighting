package highlighting.regex;

import highlighting.core.HighlightRegion;
import highlighting.core.SyntaxHighlighter;
import highlighting.presets.MiniJavaTokens;
import java.util.ArrayList;
import java.util.List;

public class RegexHighlighter extends SyntaxHighlighter {

  @Override
  public List<HighlightRegion> collectMatches(String text) {
    List<HighlightRegion> result = new ArrayList<>();

    for (Token token : MiniJavaTokens.defaultTokens()) {
      result.addAll(token.test(text));
    }

    return result;
  }

  @Override
  public List<HighlightRegion> resolveConflicts(List<HighlightRegion> regions) {
    List<HighlightRegion> result = new ArrayList<>();

    for (HighlightRegion region : regions) {
      boolean hasOverlap = false;

      for (HighlightRegion selected : result) {
        if (overlaps(region, selected)) {
          hasOverlap = true;
          break;
        }
      }

      if (!hasOverlap) {
        result.add(region);
      }
    }

    return result;
  }

  private boolean overlaps(HighlightRegion a, HighlightRegion b) {
    return a.start() < b.end() && b.start() < a.end();
  }
}
