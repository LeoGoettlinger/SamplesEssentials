package ch.njol.skript.bukkitutil;

import ch.njol.util.StringUtils;
import java.util.UUID;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UUIDUtils {
   @Nullable
   public static UUID asUUID(@NotNull Object object) {
      if (object instanceof OfflinePlayer offlinePlayer) {
         return offlinePlayer.getUniqueId();
      } else if (object instanceof Entity entity) {
         return entity.getUniqueId();
      } else {
         if (object instanceof String string && StringUtils.containsAny(string, "-")) {
            try {
               return UUID.fromString(string);
            } catch (Exception var6) {
            }
         } else if (object instanceof UUID uuid) {
            return uuid;
         }

         return null;
      }
   }
}
