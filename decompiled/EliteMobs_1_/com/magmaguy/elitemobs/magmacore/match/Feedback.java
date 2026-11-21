package com.magmaguy.elitemobs.magmacore.match;

import com.magmaguy.elitemobs.magmacore.util.ChatColorConverter;
import org.bukkit.entity.Player;

final class Feedback {
   private Feedback() {
   }

   static void message(Player player, String template, Object... replacements) {
      if (template != null && player != null && player.isOnline()) {
         player.sendMessage(ChatColorConverter.convert(replace(template, replacements)));
      }
   }

   static void title(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut, Object... replacements) {
      if ((title != null || subtitle != null) && player != null && player.isOnline()) {
         player.sendTitle(
            title == null ? "" : ChatColorConverter.convert(replace(title, replacements)),
            subtitle == null ? "" : ChatColorConverter.convert(replace(subtitle, replacements)),
            fadeIn,
            stay,
            fadeOut
         );
      }
   }

   private static String replace(String template, Object... replacements) {
      String result = template;

      for (int index = 0; index + 1 < replacements.length; index += 2) {
         result = result.replace(String.valueOf(replacements[index]), String.valueOf(replacements[index + 1]));
      }

      return result;
   }
}
