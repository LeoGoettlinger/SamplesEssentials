package org.lushplugins.lushrewards.libraries.lushlib.libraries.chatcolor.parsers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Parsers {
   private Map<Parser, Integer> parsers = new HashMap<>();

   public void register(Parser parser, int priority) {
      this.parsers.put(parser, priority);
      this.parsers = sortByValue(this.parsers);
   }

   public String parseString(@NotNull String string, @NotNull Parser.OutputType outputType, @Nullable Player player, @NotNull List<Parser> parsers) {
      for (Parser parser : parsers) {
         try {
            string = parser.parseString(string, outputType, player);
         } catch (Throwable var8) {
            Bukkit.getLogger()
               .log(
                  Level.WARNING,
                  "[ChatColorHandler] Failed to parse string '%s' through parser '%s: ".formatted(string, parser.getClass().getSimpleName()),
                  var8
               );
         }
      }

      return string;
   }

   public List<Parser> getRegisteredParsers() {
      return this.parsers.keySet().stream().toList();
   }

   public List<Resolver> getRegisteredResolvers() {
      return this.parsers.keySet().stream().map(parser -> parser instanceof Resolver resolver ? resolver : null).filter(Objects::nonNull).toList();
   }

   public List<Parser> ofType(@NotNull String type) {
      return this.ofTypes(type);
   }

   public List<Parser> ofTypes(@NotNull String... types) {
      return this.parsers.keySet().stream().filter(parser -> {
         for (String type : types) {
            if (parser.getType().equals(type)) {
               return true;
            }
         }

         return false;
      }).toList();
   }

   private static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
      List<Entry<K, V>> list = new ArrayList<>(map.entrySet());
      list.sort(Entry.comparingByValue());
      Collections.reverse(list);
      Map<K, V> result = new LinkedHashMap<>();

      for (Entry<K, V> entry : list) {
         result.put(entry.getKey(), entry.getValue());
      }

      return result;
   }
}
