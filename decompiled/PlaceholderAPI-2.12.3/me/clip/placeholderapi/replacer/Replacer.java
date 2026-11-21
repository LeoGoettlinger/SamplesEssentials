package me.clip.placeholderapi.replacer;

import java.util.function.Function;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Replacer {
   @NotNull
   String apply(@NotNull String var1, @Nullable OfflinePlayer var2, @NotNull Function<String, PlaceholderExpansion> var3);

   public static enum Closure {
      BRACKET('{', '}'),
      PERCENT('%', '%');

      public final char head;
      public final char tail;

      private Closure(final char head, final char tail) {
         this.head = head;
         this.tail = tail;
      }
   }
}
