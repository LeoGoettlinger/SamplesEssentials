package de.theredend2000.advancedhunt.platform.impl;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTIntArray;
import com.github.retrooper.packetevents.protocol.nbt.NBTList;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.world.blockentity.BlockEntityTypes;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockEntityData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnLivingEntity;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

public class Spigot1205PlusPlatformAdapter extends Spigot115PlatformAdapter {
   @Override
   public void applyHideTooltip(ItemMeta meta, boolean hide) {
      if (meta != null) {
         meta.setHideTooltip(hide);
      }
   }

   protected int getArmorStandFlagsIndex(Player player) {
      return 15;
   }

   protected boolean isPacketEventsReady() {
      try {
         if (!Bukkit.getPluginManager().isPluginEnabled("packetevents") && !Bukkit.getPluginManager().isPluginEnabled("PacketEvents")) {
            return false;
         }
      } catch (Throwable var3) {
         return false;
      }

      try {
         return PacketEvents.getAPI().isInitialized();
      } catch (Throwable var2) {
         return false;
      }
   }

   protected List<EntityData<?>> buildHologramArmorStandMetadata(Player player, String customName) {
      byte invisibleFlag = 96;
      byte armorStandFlags = 9;
      int armorStandFlagsIndex = this.getArmorStandFlagsIndex(player);
      List<EntityData<?>> meta = new ArrayList<>();
      meta.add(new EntityData(0, EntityDataTypes.BYTE, (byte)96));
      meta.add(new EntityData(2, EntityDataTypes.OPTIONAL_COMPONENT, Optional.of(this.toJsonTextComponent(customName))));
      meta.add(new EntityData(3, EntityDataTypes.BOOLEAN, true));
      meta.add(new EntityData(5, EntityDataTypes.BOOLEAN, true));
      meta.add(new EntityData(armorStandFlagsIndex, EntityDataTypes.BYTE, (byte)9));
      return meta;
   }

   @Override
   public boolean spawnHologramArmorStandForPlayer(Player player, int entityId, UUID entityUuid, Location location, String customName) {
      if (player == null || location == null) {
         return false;
      } else if (location.getWorld() == null) {
         return false;
      } else {
         try {
            if (!this.isPacketEventsReady()) {
               return false;
            } else {
               List<EntityData<?>> meta = this.buildHologramArmorStandMetadata(player, customName);
               WrapperPlayServerSpawnLivingEntity spawnPacket = new WrapperPlayServerSpawnLivingEntity(
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
               PacketEvents.getAPI().getPlayerManager().sendPacket(player, spawnPacket);
               return true;
            }
         } catch (Throwable var8) {
            return false;
         }
      }
   }

   @Override
   public boolean spawnGlowingBlockMarkerForPlayer(Player player, int entityId, UUID entityUuid, Location blockLocation) {
      if (player == null || blockLocation == null) {
         return false;
      } else if (blockLocation.getWorld() == null) {
         return false;
      } else if (!this.isPacketEventsReady()) {
         return super.spawnGlowingBlockMarkerForPlayer(player, entityId, entityUuid, blockLocation);
      } else {
         try {
            Block block = blockLocation.getBlock();
            if (block == null) {
               return super.spawnGlowingBlockMarkerForPlayer(player, entityId, entityUuid, blockLocation);
            } else {
               int globalBlockStateId = 0;

               try {
                  if (block.getBlockData() != null) {
                     WrappedBlockState wrapped = SpigotConversionUtil.fromBukkitBlockData(block.getBlockData());
                     globalBlockStateId = wrapped != null ? wrapped.getGlobalId() : 0;
                  }
               } catch (Throwable var21) {
               }

               byte entityFlags = 64;
               List<EntityData<?>> meta = new ArrayList<>();
               meta.add(new EntityData(0, EntityDataTypes.BYTE, (byte)64));
               int packedFullBright = 15728880;

               try {
                  ClientVersion clientVersion = PacketEvents.getAPI().getPlayerManager().getClientVersion(player);
                  if (clientVersion == null) {
                     clientVersion = PacketEvents.getAPI().getServerManager().getVersion().toClientVersion();
                  }

                  if (clientVersion != null && clientVersion.isNewerThanOrEquals(ClientVersion.V_1_21)) {
                     meta.add(new EntityData(16, EntityDataTypes.INT, 15728880));
                  } else {
                     meta.add(new EntityData(16, EntityDataTypes.OPTIONAL_INT, Optional.of(15728880)));
                  }
               } catch (Throwable var22) {
               }

               String typeName = block.getType().name();
               boolean isHeadOrSkull = typeName.endsWith("_HEAD") || typeName.endsWith("_SKULL");
               float scaleX = isHeadOrSkull ? 1.003F : 1.0025F;
               float scaleY = isHeadOrSkull ? 1.006F : 1.0025F;
               float scaleZ = isHeadOrSkull ? 1.003F : 1.0025F;
               float halfExpandX = (scaleX - 1.0F) / 2.0F;
               float halfExpandY = (scaleY - 1.0F) / 2.0F;
               float halfExpandZ = (scaleZ - 1.0F) / 2.0F;
               meta.add(new EntityData(11, EntityDataTypes.VECTOR3F, new Vector3f(-halfExpandX, -halfExpandY, -halfExpandZ)));
               meta.add(new EntityData(12, EntityDataTypes.VECTOR3F, new Vector3f(scaleX, scaleY, scaleZ)));
               meta.add(new EntityData(23, EntityDataTypes.BLOCK_STATE, globalBlockStateId));
               Location spawnLoc = blockLocation.clone();
               WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(
                  entityId,
                  Optional.of(entityUuid),
                  EntityTypes.BLOCK_DISPLAY,
                  new Vector3d(spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ()),
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
            }
         } catch (Throwable var23) {
            return super.spawnGlowingBlockMarkerForPlayer(player, entityId, entityUuid, blockLocation);
         }
      }
   }

   @Override
   public void sendSkullUpdatePacket(Player player, Location loc, String texture, String ownerName) {
      if (player != null && loc != null) {
         if (loc.getWorld() != null) {
            String normalizedTexture = this.normalizeProfileValue(texture);
            String normalizedOwnerName = this.normalizeProfileValue(ownerName);
            if (normalizedTexture != null || normalizedOwnerName != null) {
               if (!this.isPacketEventsReady()) {
                  super.sendSkullUpdatePacket(player, loc, normalizedTexture, normalizedOwnerName);
               } else {
                  try {
                     Vector3i pos = new Vector3i(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                     NBTCompound root = new NBTCompound();
                     root.setTag("id", new NBTString("minecraft:skull"));
                     NBTCompound profile = new NBTCompound();
                     if (normalizedOwnerName != null) {
                        profile.setTag("name", new NBTString(normalizedOwnerName));
                     }

                     if (normalizedTexture != null) {
                        UUID randomUUID = UUID.randomUUID();
                        int[] uuidInts = new int[]{
                           (int)(randomUUID.getMostSignificantBits() >> 32),
                           (int)randomUUID.getMostSignificantBits(),
                           (int)(randomUUID.getLeastSignificantBits() >> 32),
                           (int)randomUUID.getLeastSignificantBits()
                        };
                        profile.setTag("id", new NBTIntArray(uuidInts));
                        NBTList<NBTCompound> properties = NBTList.createCompoundList();
                        NBTCompound textureTag = new NBTCompound();
                        textureTag.setTag("name", new NBTString("textures"));
                        textureTag.setTag("value", new NBTString(normalizedTexture));
                        properties.addTag(textureTag);
                        profile.setTag("properties", properties);
                     }

                     root.setTag("profile", profile);
                     WrapperPlayServerBlockEntityData packet = new WrapperPlayServerBlockEntityData(pos, BlockEntityTypes.SKULL, root);
                     PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
                  } catch (Throwable var14) {
                     super.sendSkullUpdatePacket(player, loc, normalizedTexture, normalizedOwnerName);
                  }
               }
            }
         }
      }
   }
}
