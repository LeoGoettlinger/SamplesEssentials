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
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class Spigot113PlatformAdapter extends Spigot19PlatformAdapter {
   protected String toJsonTextComponent(String text) {
      String safe = text == null ? "" : text;
      safe = safe.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
      return "{\"text\":\"" + safe + "\"}";
   }

   @Override
   public boolean isAir(Material material) {
      if (material == null) {
         return true;
      } else {
         String name = material.name();
         return "AIR".equals(name) || "CAVE_AIR".equals(name) || "VOID_AIR".equals(name);
      }
   }

   @Override
   public void applySkullOwner(ItemMeta meta, UUID ownerUuid) {
      if (meta instanceof SkullMeta) {
         if (ownerUuid != null) {
            ((SkullMeta)meta).setOwningPlayer(Bukkit.getOfflinePlayer(ownerUuid));
         }
      }
   }

   @Override
   public ItemStack ensurePlayerHeadItem(ItemStack item) {
      if (item == null) {
         return null;
      } else {
         try {
            Material type = item.getType();
            if (type != null && "PLAYER_WALL_HEAD".equals(type.name())) {
               ItemMeta meta = item.getItemMeta();
               item.setType(Material.PLAYER_HEAD);
               if (meta != null) {
                  item.setItemMeta(meta);
               }

               return item;
            }

            if (type != null && ("SKULL_ITEM".equals(type.name()) || "LEGACY_SKULL_ITEM".equals(type.name()))) {
               ItemMeta meta = item.getItemMeta();
               item.setType(Material.PLAYER_HEAD);
               if (meta != null) {
                  item.setItemMeta(meta);
               }
            }
         } catch (Throwable var4) {
         }

         return item;
      }
   }

   @Override
   public String getBlockStateString(Block block) {
      if (block == null) {
         return "";
      } else {
         try {
            BlockData data = block.getBlockData();
            return data != null ? data.getAsString() : "";
         } catch (Throwable var3) {
            return "";
         }
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
               meta.add(new EntityData(11, EntityDataTypes.BYTE, (byte)9));
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
