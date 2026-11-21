package de.oliver.fancynpcs.libs.chatcolorhandler.common.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParserRegistry {
   private final Set<Parser> parsers = new HashSet<>();

   public void register(Parser parser) {
      this.parsers.add(parser);
   }

   public String parseString(@NotNull String string, @Nullable Player player, @NotNull Parsers parsers) {
      for (Parser parser : parsers) {
         try {
            string = parser.parseString(string, player);
         } catch (Throwable var7) {
            Bukkit.getLogger()
               .log(
                  Level.WARNING,
                  "[ChatColorHandler] Failed to parse string '%s' through parser '%s: ".formatted(string, parser.getClass().getSimpleName()),
                  var7
               );
         }
      }

      return string;
   }

   public List<Parser> values() {
      return this.parsers.stream().sorted(Parsers.comparingPriority()).toList();
   }

   public List<Parser> unsorted() {
      return new ArrayList<>(this.parsers);
   }

   public List<Parser> sorted() {
      return this.values();
   }

   public List<Parser> ofTypes(@NotNull String... types) {
      return this.values().stream().filter(parser -> {
         ParserType parserType = parser.getType();
         if (parserType != null) {
            for (String type : types) {
               if (parserType.equals(type)) {
                  return true;
               }
            }
         }

         return false;
      }).sorted(Parsers.comparingPriority()).toList();
   }

   public List<Parser> ofType(@NotNull String type) {
      return this.ofTypes(type);
   }
}
