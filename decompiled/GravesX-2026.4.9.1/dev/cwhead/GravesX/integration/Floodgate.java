package dev.cwhead.GravesX.integration;

import com.ranull.graves.Graves;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

public class Floodgate {
   private final Graves plugin;
   private final FloodgateApi floodgateApi = FloodgateApi.getInstance();

   public Floodgate(Graves plugin) {
      this.plugin = plugin;
   }

   public boolean isBedrockPlayer(Player player) {
      return this.floodgateApi.isFloodgatePlayer(player.getUniqueId());
   }

   public boolean isFloodgateId(Player player) {
      return this.isFloodgateId(player.getUniqueId());
   }

   public boolean isFloodgateId(UUID uuid) {
      return this.floodgateApi.isFloodgateId(uuid);
   }

   public FloodgatePlayer getPlayer(Player player) {
      return this.getPlayer(player.getUniqueId());
   }

   public FloodgatePlayer getPlayer(UUID uuid) {
      return this.floodgateApi.getPlayer(uuid);
   }

   public UUID getNormalizedUUID(Player player) {
      if (this.isBedrockPlayer(player)) {
         this.plugin.debugMessage(player + " detected as bedrock player", 3);
         return Optional.ofNullable(this.getPlayer(player.getUniqueId())).<UUID>map(FloodgatePlayer::getCorrectUniqueId).orElse(player.getUniqueId());
      } else {
         this.plugin.debugMessage(player + " detected as java player", 3);
         return player.getUniqueId();
      }
   }

   @Deprecated
   public String getCorrectUsername(Player player) {
      return this.isBedrockPlayer(player)
         ? Optional.ofNullable(this.getPlayer(player.getUniqueId())).<String>map(FloodgatePlayer::getJavaUsername).orElse(player.getName())
         : player.getName();
   }

   public UUID getCorrectUniqueId(Player player) {
      return this.getCorrectUniqueId(player.getUniqueId());
   }

   public UUID getCorrectUniqueId(UUID uuid) {
      if (this.isFloodgateId(uuid)) {
         FloodgatePlayer fgPlayer = this.getPlayer(uuid);
         if (fgPlayer != null) {
            return fgPlayer.getCorrectUniqueId();
         }
      }

      return uuid;
   }
}
