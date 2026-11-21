package org.snakeyaml.engine.v2.common;

import java.util.Optional;

public enum ScalarStyle {
   DOUBLE_QUOTED(Optional.of('"')),
   SINGLE_QUOTED(Optional.of('\'')),
   LITERAL(Optional.of('|')),
   FOLDED(Optional.of('>')),
   PLAIN(Optional.empty());

   private final Optional<Character> styleOpt;

   private ScalarStyle(Optional<Character> style) {
      this.styleOpt = style;
   }

   @Override
   public String toString() {
      return String.valueOf(this.styleOpt.orElse(':'));
   }
}
