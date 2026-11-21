package libs.com.ryderbelserion.vital.paper.api.builders;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public record PlayerBuilder(String name) {
   @Nullable
   public OfflinePlayer getOfflinePlayer() {
      if (this.name.isEmpty()) {
         return null;
      } else {
         CompletableFuture<UUID> future = CompletableFuture.<OfflinePlayer>supplyAsync(() -> Bukkit.getServer().getOfflinePlayer(this.name))
            .thenApply(OfflinePlayer::getUniqueId);
         return Bukkit.getServer().getOfflinePlayer(future.join());
      }
   }

   @Nullable
   public Player getPlayer() {
      return this.name.isEmpty() ? null : Bukkit.getServer().getPlayer(this.name);
   }
}
