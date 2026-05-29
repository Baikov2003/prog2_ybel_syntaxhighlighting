package highlighting.regex;

import highlighting.core.HighlightRegion;
import highlighting.core.SyntaxHighlighter;
import highlighting.presets.MiniJavaTokens;
import java.util.ArrayList;
import java.util.List;

public class ScanningHighlighter extends SyntaxHighlighter {

  @Override
  public List<HighlightRegion> collectMatches(String text) {
    List<HighlightRegion> result = new ArrayList<>();
    List<Token> tokens = MiniJavaTokens.defaultTokens();

    int position = 0;

    while (position < text.length()) {
      HighlightRegion bestMatch = null;

      for (Token token : tokens) {
        for (HighlightRegion region : token.test(text)) {
          if (region.start() == position && region.end() > region.start()) {
            if (bestMatch == null || length(region) > length(bestMatch)) {
              bestMatch = region;
            }
          }
        }
      }

      if (bestMatch != null) {
        result.add(bestMatch);
        position = bestMatch.end();
      } else {
        position++;
      }
    }

    return result;
  }

  @Override
  public List<HighlightRegion> normalize(List<HighlightRegion> candidates) {
    return candidates;
  }

  private int length(HighlightRegion region) {
    return region.end() - region.start();
  }
}
