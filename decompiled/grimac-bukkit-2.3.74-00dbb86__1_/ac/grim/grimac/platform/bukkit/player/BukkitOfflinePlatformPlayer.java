package ac.grim.grimac.platform.bukkit.player;

import ac.grim.grimac.platform.api.player.OfflinePlatformPlayer;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.Objects;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.OfflinePlayer;

public class BukkitOfflinePlatformPlayer implements OfflinePlatformPlayer {
   @NotNull
   private final OfflinePlayer offlinePlayer;

   @Override
   public boolean isOnline() {
      return this.offlinePlayer.isOnline();
   }

   @NotNull
   @Override
   public String getName() {
      return Objects.requireNonNull(this.offlinePlayer.getName());
   }

   @NotNull
   @Override
   public UUID getUniqueId() {
      return this.offlinePlayer.getUniqueId();
   }

   @Override
   public boolean equals(Object o) {
      return o instanceof OfflinePlatformPlayer player && this.getUniqueId().equals(player.getUniqueId());
   }

   @Generated
   public BukkitOfflinePlatformPlayer(@NotNull OfflinePlayer offlinePlayer) {
      if (offlinePlayer == null) {
         throw new NullPointerException("offlinePlayer is marked non-null but is null");
      } else {
         this.offlinePlayer = offlinePlayer;
      }
   }
}
