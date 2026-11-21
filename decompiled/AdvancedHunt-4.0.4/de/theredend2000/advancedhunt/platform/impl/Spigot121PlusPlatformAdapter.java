package de.theredend2000.advancedhunt.platform.impl;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public final class Spigot121PlusPlatformAdapter extends Spigot1205PlusPlatformAdapter {
   @Override
   public boolean spawnHologramArmorStandForPlayer(Player player, int entityId, UUID entityUuid, Location location, String customName) {
      if (player == null || location == null) {
         return false;
      } else if (location.getWorld() == null) {
         return false;
      } else if (!this.isPacketEventsReady()) {
         return false;
      } else {
         try {
            List<EntityData<?>> meta = this.buildHologramArmorStandMetadata(player, customName);
            WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
               entityId,
               Optional.of(entityUuid),
               EntityTypes.ARMOR_STAND,
               new Vector3d(location.getX(), location.getY(), location.getZ()),
               0.0F,
               0.0F,
               0.0F,
               0,
               Optional.of(new Vector3d(0.0, 0.0, 0.0))
            );
            WrapperPlayServerEntityMetadata metaPacket = new WrapperPlayServerEntityMetadata(entityId, meta);
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, spawnPacket);
            PacketEvents.getAPI().getPlayerManager().sendPacket(player, metaPacket);
            return true;
         } catch (Throwable var9) {
            return false;
         }
      }
   }
}
