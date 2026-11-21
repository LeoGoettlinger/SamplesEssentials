package de.theredend2000.advancedhunt.platform.impl;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnLivingEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

public class Spigot114PlatformAdapter extends Spigot113PlatformAdapter {
   @Override
   public void applyUnbreakable(ItemMeta meta, boolean unbreakable) {
      if (meta != null) {
         meta.setUnbreakable(unbreakable);
      }
   }

   @Override
   public void applyCustomModelData(ItemMeta meta, Integer customModelData) {
      if (meta != null) {
         meta.setCustomModelData(customModelData);
      }
   }

   @Override
   public boolean spawnHologramArmorStandForPlayer(Player player, int entityId, UUID entityUuid, Location location, String customName) {
      if (player == null || location == null) {
         return false;
      } else if (location.getWorld() == null) {
         return false;
      } else {
         try {
            if (!Bukkit.getPluginManager().isPluginEnabled("packetevents") && !Bukkit.getPluginManager().isPluginEnabled("PacketEvents")) {
               return false;
            }
         } catch (Throwable var11) {
            return false;
         }

         try {
            if (!PacketEvents.getAPI().isInitialized()) {
               return false;
            } else {
               byte invisibleFlag = 96;
               byte armorStandFlags = 9;
               List<EntityData<?>> meta = new ArrayList<>();
               meta.add(new EntityData(0, EntityDataTypes.BYTE, (byte)96));
               meta.add(new EntityData(2, EntityDataTypes.OPTIONAL_COMPONENT, Optional.of(this.toJsonTextComponent(customName))));
               meta.add(new EntityData(3, EntityDataTypes.BOOLEAN, true));
               meta.add(new EntityData(5, EntityDataTypes.BOOLEAN, true));
               meta.add(new EntityData(14, EntityDataTypes.BYTE, (byte)9));
               WrapperPlayServerSpawnLivingEntity packet = new WrapperPlayServerSpawnLivingEntity(
                  entityId,
                  entityUuid,
                  EntityTypes.ARMOR_STAND,
                  new Vector3d(location.getX(), location.getY(), location.getZ()),
                  0.0F,
                  0.0F,
                  0.0F,
                  new Vector3d(0.0, 0.0, 0.0),
                  meta
               );
               PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
               return true;
            }
         } catch (Throwable var10) {
            return false;
         }
      }
   }
}
