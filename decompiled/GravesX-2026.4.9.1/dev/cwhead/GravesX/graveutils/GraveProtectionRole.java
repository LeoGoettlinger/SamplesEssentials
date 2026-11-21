package dev.cwhead.GravesX.graveutils;

import com.ranull.graves.Graves;
import com.ranull.graves.type.Grave;
import dev.cwhead.GravesX.integration.Floodgate;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.entity.Player;

public class GraveProtectionRole {
   private final Graves plugin;

   public GraveProtectionRole(Graves plugin) {
      this.plugin = plugin;
   }

   public GraveProtectionRole.GraveRole getRole(Grave grave, UUID playerId) {
      UUID ownerId = grave.getOwnerUUID();
      UUID killerId = grave.getKillerUUID();
      if (this.plugin.getIntegrationManager().hasFloodgate()) {
         Floodgate floodgate = this.plugin.getIntegrationManager().getFloodgate();
         if (ownerId != null && floodgate.isFloodgateId(ownerId)) {
            ownerId = floodgate.getCorrectUniqueId(ownerId);
         }

         if (killerId != null && floodgate.isFloodgateId(killerId)) {
            killerId = floodgate.getCorrectUniqueId(killerId);
         }
      }

      boolean isOwner = Objects.equals(ownerId, playerId);
      boolean isKiller = killerId != null && Objects.equals(killerId, playerId);
      if (isOwner) {
         return killerId == null ? GraveProtectionRole.GraveRole.MISSING_OWNER : GraveProtectionRole.GraveRole.OWNER;
      } else if (isKiller) {
         return GraveProtectionRole.GraveRole.KILLER;
      } else {
         return killerId == null ? GraveProtectionRole.GraveRole.MISSING_OTHER : GraveProtectionRole.GraveRole.OTHER;
      }
   }

   public GraveProtectionRole.GraveRole getRole(Player player, Grave grave) {
      UUID playerId = this.plugin.getIntegrationManager().hasFloodgate()
         ? this.plugin.getIntegrationManager().getFloodgate().getNormalizedUUID(player)
         : player.getUniqueId();
      return this.getRole(grave, playerId);
   }

   public static enum GraveRole {
      OWNER,
      KILLER,
      OTHER,
      MISSING_OWNER,
      MISSING_OTHER;
   }
}
