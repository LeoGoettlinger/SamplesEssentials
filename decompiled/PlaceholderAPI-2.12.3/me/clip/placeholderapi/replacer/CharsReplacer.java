package me.clip.placeholderapi.replacer;

import java.util.Locale;
import java.util.function.Function;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CharsReplacer implements Replacer {
   @NotNull
   private final Replacer.Closure closure;

   public CharsReplacer(@NotNull Replacer.Closure closure) {
      this.closure = closure;
   }

   @NotNull
   @Override
   public String apply(@NotNull String text, @Nullable OfflinePlayer player, @NotNull Function<String, PlaceholderExpansion> lookup) {
      char head = this.closure.head;
      int startPlaceholder = text.indexOf(head);
      if (startPlaceholder == -1) {
         return text;
      } else {
         int length = text.length();
         StringBuilder builder = new StringBuilder(length + (length >> 3));
         int cursor = 0;
         char tail = this.closure.tail;

         label65:
         while (true) {
            if (startPlaceholder > cursor) {
               builder.append(text, cursor, startPlaceholder);
            }

            int endPlaceholder = text.indexOf(tail, startPlaceholder + 1);
            if (endPlaceholder == -1) {
               builder.append(text, startPlaceholder, length);
               return builder.toString();
            }

            int underscoreIndex = -1;
            int i = startPlaceholder + 1;

            while (true) {
               if (i < endPlaceholder) {
                  char current = text.charAt(i);
                  if (current != ' ' || underscoreIndex != -1) {
                     if (current == '_' && underscoreIndex == -1) {
                        underscoreIndex = i;
                     }

                     i++;
                     continue;
                  }

                  builder.append(head);
                  cursor = startPlaceholder + 1;
                  startPlaceholder = text.indexOf(head, cursor);
                  if (startPlaceholder == -1) {
                     break label65;
                  }
               } else if (underscoreIndex == -1) {
                  builder.append(text, startPlaceholder, endPlaceholder + 1);
                  cursor = endPlaceholder + 1;
                  startPlaceholder = text.indexOf(head, cursor);
               } else {
                  String identifier = text.substring(startPlaceholder + 1, underscoreIndex);
                  String parameters = "";
                  if (underscoreIndex + 1 < endPlaceholder) {
                     parameters = text.substring(underscoreIndex + 1, endPlaceholder);
                  }

                  PlaceholderExpansion expansion = lookup.apply(identifier.toLowerCase(Locale.ROOT));
                  String replacement = null;
                  if (expansion != null) {
                     replacement = expansion.onRequest(player, parameters);
                  }

                  if (replacement != null) {
                     builder.append(replacement);
                  } else {
                     builder.append(head).append(identifier);
                     builder.append('_').append(parameters);
                     builder.append(tail);
                  }

                  cursor = endPlaceholder + 1;
                  startPlaceholder = text.indexOf(head, cursor);
               }

               if (startPlaceholder == -1) {
                  break label65;
               }
               break;
            }
         }

         if (cursor < length) {
            builder.append(text, cursor, length);
         }

         return builder.toString();
      }
   }
}
