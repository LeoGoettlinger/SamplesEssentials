package ch.njol.skript.lang.parser;

import ch.njol.skript.lang.ParseContext;
import java.util.HashSet;
import java.util.Set;

public final class LiteralParseCache {
   private final Set<LiteralParseCache.Failure> failures = new HashSet<>();

   public boolean contains(LiteralParseCache.Failure failure) {
      return this.failures.contains(failure);
   }

   public void add(LiteralParseCache.Failure failure) {
      this.failures.add(failure);
   }

   public void clear() {
      this.failures.clear();
   }

   public record Failure(String data, ParseContext context) {
   }
}
