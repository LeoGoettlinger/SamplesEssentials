package ch.njol.skript.classes;

import ch.njol.util.StringUtils;

public abstract class PatternedParser<T> extends Parser<T> {
   public abstract String[] getPatterns();

   public String getCombinedPatterns() {
      String[] patterns = this.getPatterns();
      return StringUtils.join(patterns, ", ");
   }
}
