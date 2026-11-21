package me.clip.placeholderapi.replacer;

import java.util.function.Function;
import java.util.regex.Pattern;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class RelationalExactReplacer {
   private static final Pattern DELIMITER = Pattern.compile("_");

   @NotNull
   public String apply(@NotNull String text, @Nullable Player player1, @Nullable Player player2, @NotNull Function<String, PlaceholderExpansion> lookup) {
      String[] parts = DELIMITER.split(text);
      PlaceholderExpansion expansion;
      if (parts.length == 0) {
         expansion = lookup.apply(text);
      } else {
         expansion = lookup.apply(parts[0]);
      }

      if (expansion == null) {
         return "%rel_" + text + '%';
      } else if (!(expansion instanceof Relational)) {
         return "%rel_" + text + '%';
      } else {
         String params;
         if (text.endsWith("_")) {
            params = "";
         } else {
            params = text.substring(text.indexOf(95) + 1);
         }

         String result = ((Relational)expansion).onPlaceholderRequest(player1, player2, params);
         return result == null ? "%rel_" + text + '%' : result;
      }
   }
}
