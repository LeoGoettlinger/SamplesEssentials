package de.theredend2000.advancedhunt.platform.impl;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.nbt.NBTByte;
import com.github.retrooper.packetevents.protocol.nbt.NBTCompound;
import com.github.retrooper.packetevents.protocol.nbt.NBTList;
import com.github.retrooper.packetevents.protocol.nbt.NBTString;
import com.github.retrooper.packetevents.protocol.world.blockentity.BlockEntityTypes;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockEntityData;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnLivingEntity;
import de.theredend2000.advancedhunt.platform.PlatformAdapter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class Spigot19PlatformAdapter implements PlatformAdapter {
   private final ModernParticleSpawner particleSpawner = new ModernParticleSpawner();

   @Override
   public boolean isAir(Material material) {
      return material == null ? true : "AIR".equals(material.name());
   }

   @Override
   public void spawnParticle(Location location, String particleName, int count, double offsetX, double offsetY, double offsetZ, double speed) {
      this.particleSpawner.spawn(location, particleName, count, offsetX, offsetY, offsetZ, speed);
   }

   @Override
   public void spawnParticleForPlayer(
      Player player, Location location, String particleName, int count, double offsetX, double offsetY, double offsetZ, double speed
   ) {
      this.particleSpawner.spawnForPlayer(player, location, particleName, count, offsetX, offsetY, offsetZ, speed);
   }

   @Override
   public void applySkullOwner(ItemMeta meta, String ownerName) {
      if (meta instanceof SkullMeta) {
         if (ownerName != null && !ownerName.trim().isEmpty()) {
            try {
               ((SkullMeta)meta).setOwner(ownerName);
            } catch (Throwable var4) {
            }
         }
      }
   }

   @Override
   public void applyHideTooltip(ItemMeta meta, boolean hide) {
   }

   @Override
   public void applyUnbreakable(ItemMeta meta, boolean unbreakable) {
   }

   @Override
   public void applyCustomModelData(ItemMeta meta, Integer customModelData) {
   }

   @Override
   public void applySkullOwner(ItemMeta meta, UUID ownerUuid) {
   }

   @Override
   public ItemStack ensurePlayerHeadItem(ItemStack item) {
      if (item == null) {
         return null;
      } else {
         try {
            Material type = item.getType();
            if (type == null) {
               return item;
            }

            if ("SKULL".equals(type.name())) {
               Material skullItem = Material.getMaterial("SKULL_ITEM");
               if (skullItem != null) {
                  ItemMeta meta = item.getItemMeta();
                  ItemStack converted = new ItemStack(skullItem, item.getAmount());
                  if (meta != null) {
                     converted.setItemMeta(meta);
                  }

                  item = converted;
                  type = converted.getType();
               }
            }

            if (type != null && "SKULL_ITEM".equals(type.name()) && item.getDurability() != 3) {
               item.setDurability((short)3);
            }
         } catch (Throwable var6) {
         }

         return item;
      }
   }

   @Override
   public boolean isMainHandInteract(PlayerInteractEvent event) {
      if (event == null) {
         return true;
      } else {
         try {
            return event.getHand() == EquipmentSlot.HAND;
         } catch (Throwable var3) {
            return true;
         }
      }
   }

   @Override
   public String getBlockStateString(Block block) {
      if (block == null) {
         return "0";
      } else {
         try {
            return String.valueOf(block.getData());
         } catch (Throwable var3) {
            return "0";
         }
      }
   }

   @Override
   public void setFireworkSilent(Firework firework, boolean silent) {
      if (firework != null) {
         try {
            firework.setSilent(silent);
         } catch (Throwable var4) {
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
               meta.add(new EntityData(2, EntityDataTypes.STRING, customName == null ? "" : customName));
               meta.add(new EntityData(3, EntityDataTypes.BOOLEAN, true));
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

   @Override
   public boolean destroyEntitiesForPlayer(Player player, int... entityIds) {
      if (player == null) {
         return false;
      } else if (entityIds != null && entityIds.length != 0) {
         try {
            if (!Bukkit.getPluginManager().isPluginEnabled("packetevents") && !Bukkit.getPluginManager().isPluginEnabled("PacketEvents")) {
               return false;
            }
         } catch (Throwable var5) {
            return false;
         }

         try {
            if (!PacketEvents.getAPI().isInitialized()) {
               return false;
            } else {
               WrapperPlayServerDestroyEntities packet = new WrapperPlayServerDestroyEntities(entityIds);
               PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
               return true;
            }
         } catch (Throwable var4) {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public boolean spawnGlowingBlockMarkerForPlayer(Player player, int entityId, UUID entityUuid, Location blockLocation) {
      if (player == null || blockLocation == null) {
         return false;
      } else if (blockLocation.getWorld() == null) {
         return false;
      } else {
         try {
            if (!Bukkit.getPluginManager().isPluginEnabled("packetevents") && !Bukkit.getPluginManager().isPluginEnabled("PacketEvents")) {
               return false;
            }
         } catch (Throwable var10) {
            return false;
         }

         try {
            if (!PacketEvents.getAPI().isInitialized()) {
               return false;
            } else {
               byte flags = 96;
               List<EntityData<?>> meta = new ArrayList<>();
               meta.add(new EntityData(0, EntityDataTypes.BYTE, (byte)96));
               Location spawnLoc = blockLocation.clone().add(0.5, 0.0, 0.5);
               WrapperPlayServerSpawnLivingEntity packet = new WrapperPlayServerSpawnLivingEntity(
                  entityId,
                  entityUuid,
                  EntityTypes.SHULKER,
                  new Vector3d(spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ()),
                  0.0F,
                  0.0F,
                  0.0F,
                  new Vector3d(0.0, 0.0, 0.0),
                  meta
               );
               PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
               return true;
            }
         } catch (Throwable var9) {
            return false;
         }
      }
   }

   @Override
   public void sendClickableCopyText(Player player, String displayText, String copyText, String hoverText) {
      TextComponent component = new TextComponent(displayText);
      component.setClickEvent(new ClickEvent(Action.SUGGEST_COMMAND, copyText));
      if (hoverText != null && !hoverText.isEmpty()) {
         component.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
      }

      player.spigot().sendMessage(component);
   }

   @Override
   public void sendSkullUpdatePacket(Player player, Location loc, String texture, String ownerName) {
      if (player != null && loc != null) {
         String normalizedTexture = this.normalizeProfileValue(texture);
         String normalizedOwnerName = this.normalizeProfileValue(ownerName);
         if (normalizedTexture != null || normalizedOwnerName != null) {
            try {
               if (!Bukkit.getPluginManager().isPluginEnabled("packetevents") && !Bukkit.getPluginManager().isPluginEnabled("PacketEvents")) {
                  return;
               }

               if (!PacketEvents.getAPI().isInitialized()) {
                  return;
               }

               Vector3i pos = new Vector3i(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
               NBTCompound root = new NBTCompound();
               root.setTag("id", new NBTString("minecraft:skull"));
               root.setTag("SkullType", new NBTByte((byte)3));
               NBTCompound skullOwner = new NBTCompound();
               if (normalizedOwnerName != null) {
                  skullOwner.setTag("Name", new NBTString(normalizedOwnerName));
               }

               if (normalizedTexture != null) {
                  skullOwner.setTag("Id", new NBTString(UUID.randomUUID().toString()));
                  NBTCompound properties = new NBTCompound();
                  NBTList<NBTCompound> textures = NBTList.createCompoundList();
                  NBTCompound textureTag = new NBTCompound();
                  textureTag.setTag("Value", new NBTString(normalizedTexture));
                  textures.addTag(textureTag);
                  properties.setTag("textures", textures);
                  skullOwner.setTag("Properties", properties);
               }

               root.setTag("SkullOwner", skullOwner);
               WrapperPlayServerBlockEntityData packet = new WrapperPlayServerBlockEntityData(pos, BlockEntityTypes.SKULL, root);
               PacketEvents.getAPI().getPlayerManager().sendPacket(player, packet);
            } catch (Throwable var13) {
            }
         }
      }
   }

   protected String normalizeProfileValue(String value) {
      if (value == null) {
         return null;
      } else {
         String trimmed = value.trim();
         return trimmed.isEmpty() ? null : trimmed;
      }
   }
}
