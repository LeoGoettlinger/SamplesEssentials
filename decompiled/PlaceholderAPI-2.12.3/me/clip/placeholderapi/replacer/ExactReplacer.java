package me.clip.placeholderapi.replacer;

import java.util.function.Function;
import java.util.regex.Pattern;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ExactReplacer implements Replacer {
   private static final Pattern DELIMITER = Pattern.compile("_");
   private final char start;
   private final char end;

   public ExactReplacer(char start, char end) {
      this.start = start;
      this.end = end;
   }

   @NotNull
   @Override
   public String apply(@NotNull String text, @Nullable OfflinePlayer player, @NotNull Function<String, PlaceholderExpansion> lookup) {
      text = text.substring(1, text.length() - 1);
      String[] parts = DELIMITER.split(text);
      PlaceholderExpansion expansion;
      if (parts.length == 0) {
         expansion = lookup.apply(text);
      } else {
         expansion = lookup.apply(parts[0]);
      }

      if (expansion == null) {
         return this.start + text + this.end;
      } else {
         String params;
         if (text.endsWith("_")) {
            params = "";
         } else {
            params = text.substring(text.indexOf(95) + 1);
         }

         String result = expansion.onRequest(player, params);
         return result == null ? this.start + text + this.end : result;
      }
   }
}
