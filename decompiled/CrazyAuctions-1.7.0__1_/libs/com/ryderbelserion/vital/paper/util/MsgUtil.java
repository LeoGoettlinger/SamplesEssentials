package libs.com.ryderbelserion.vital.paper.util;

import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import libs.com.ryderbelserion.vital.common.util.AdvUtil;
import libs.com.ryderbelserion.vital.paper.api.enums.Support;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MsgUtil {
   private MsgUtil() {
      throw new AssertionError();
   }

   @NotNull
   public static Component parse(@NotNull String value, @Nullable UUID uuid) {
      if (value.isEmpty()) {
         return Component.empty();
      } else {
         return uuid != null ? parse(value, null, Bukkit.getPlayer(uuid)) : parse(value, null, null);
      }
   }

   @NotNull
   public static Component parse(@NotNull String value, @Nullable Map<String, String> placeholders, @Nullable Player player) {
      return (Component)(value.isEmpty() ? Component.empty() : AdvUtil.parse(parsePlaceholders(value, placeholders, player)));
   }

   public static String parsePlaceholders(@NotNull String value, @Nullable Map<String, String> placeholders, @Nullable Player player) {
      String clonedMessage = Support.placeholder_api.isEnabled() && player != null ? PlaceholderAPI.setPlaceholders(player, value) : value;
      if (placeholders != null && !placeholders.isEmpty()) {
         for (Entry<String, String> entry : placeholders.entrySet()) {
            String key = entry.getKey().toLowerCase();
            String entryValue = entry.getValue();
            clonedMessage = clonedMessage.replace(key, entryValue);
         }
      }

      return clonedMessage;
   }
}
