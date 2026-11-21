package com.ranull.graves.compatibility;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.ranull.graves.Graves;
import com.ranull.graves.data.BlockData;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.BlockFaceUtil;
import com.ranull.graves.util.MaterialUtil;
import dev.cwhead.GravesX.util.PlayerHeadUtil;
import dev.cwhead.GravesX.util.SkinTextureUtil_post_1_21_9;
import java.lang.reflect.Field;
import java.util.Collection;
import me.jay.GravesX.util.SkinTextureUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.block.TileState;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

public class CompatibilityBlockData implements Compatibility {
   @Override
   public BlockData setBlockData(Location location, Material material, Grave grave, Graves plugin) {
      return material != null ? this.handleBlockPlacement(location, material, grave, plugin) : new BlockData(location, grave.getUUID(), null, null);
   }

   private BlockData handleBlockPlacement(Location location, Material material, Grave grave, Graves plugin) {
      Block block = location.getBlock();
      String originalMaterial = block.getType().name();
      String replaceMaterial = block.getType().name();
      String replaceData = block.getBlockData().clone().getAsString(true);
      if (this.isLevelledBlock(block) || this.isSpecialBlock(block)) {
         replaceMaterial = null;
         replaceData = null;
      }

      block.setType(material);
      if (block.getBlockData() instanceof Waterlogged waterlogged) {
         this.setWaterlogged(block, originalMaterial, waterlogged);
      }

      if (material == Material.PLAYER_HEAD && block.getState() instanceof Skull) {
         replaceData = PlayerHeadUtil.appendFromBlock(block, replaceData);
         this.updateSkullBlock(block, grave, plugin);
      }

      return new BlockData(location, grave.getUUID(), replaceMaterial, replaceData);
   }

   private boolean isLevelledBlock(Block block) {
      return block.getBlockData() instanceof Levelled l && l.getLevel() != 0;
   }

   private boolean isSpecialBlock(Block block) {
      return block.getType() == Material.NETHER_PORTAL || block.getBlockData() instanceof Openable;
   }

   private void setWaterlogged(Block block, String originalMaterial, Waterlogged waterlogged) {
      waterlogged.setWaterlogged(MaterialUtil.isWater(originalMaterial));
      block.setBlockData(waterlogged);
   }

   @Override
   public boolean canBuild(Player player, Location location, Graves plugin) {
      Plugin landProtectionAddonPlugin = plugin.getServer().getPluginManager().getPlugin("GravesXAddon-LandProtection");
      if (landProtectionAddonPlugin != null && landProtectionAddonPlugin.isEnabled()) {
         return true;
      } else {
         BlockPlaceEvent blockPlaceEvent = new BlockPlaceEvent(
            location.getBlock(),
            location.getBlock().getState(),
            location.getBlock(),
            player.getInventory().getItemInMainHand(),
            player,
            true,
            EquipmentSlot.HAND
         );
         plugin.getServer().getPluginManager().callEvent(blockPlaceEvent);
         return blockPlaceEvent.canBuild() && !blockPlaceEvent.isCancelled();
      }
   }

   @Override
   public boolean hasTitleData(Block block) {
      return block.getState() instanceof TileState;
   }

   private void updateSkullBlock(Block block, Grave grave, Graves plugin) {
      int headType = plugin.getConfigManager().getConfigSection("block.head.type", grave).getInt("block.head.type");
      String headBase64 = plugin.getConfigManager().getConfigSection("block.head.base64", grave).getString("block.head.base64");
      String headName = plugin.getConfigManager().getConfigSection("block.head.name", grave).getString("block.head.name");
      Skull skull = (Skull)block.getState();
      Rotatable skullRotate = (Rotatable)block.getBlockData();
      skullRotate.setRotation(BlockFaceUtil.getYawBlockFace(grave.getYaw()).getOppositeFace());
      skull.setBlockData(skullRotate);
      this.applySkullData(skull, grave, plugin, headType, headBase64, headName);
   }

   private void applySkullData(Skull skull, Grave grave, Graves plugin, int headType, String headBase64, String headName) {
      if (headType == 0) {
         if (grave.getOwnerType() == EntityType.PLAYER) {
            try {
               skull.setOwningPlayer(plugin.getServer().getOfflinePlayer(grave.getOwnerUUID()));
            } catch (Exception var9) {
               skull.setOwner(grave.getOwnerName());
            }
         } else if (grave.getOwnerTexture() != null) {
            if (plugin.getVersionManager().isPost1_21_9()) {
               SkinTextureUtil_post_1_21_9.setSkullBlockTexture(skull, grave.getOwnerName(), grave.getOwnerTexture());
            } else {
               SkinTextureUtil.setSkullBlockTexture(skull, grave.getOwnerName(), grave.getOwnerTexture());
            }
         } else if (headBase64 != null && !headBase64.isEmpty()) {
            if (plugin.getVersionManager().isPost1_21_9()) {
               SkinTextureUtil_post_1_21_9.setSkullBlockTexture(skull, grave.getOwnerName(), headBase64);
            } else {
               SkinTextureUtil.setSkullBlockTexture(skull, grave.getOwnerName(), headBase64);
            }
         }
      } else if (headType == 1 && headBase64 != null && !headBase64.isEmpty()) {
         if (plugin.getVersionManager().isPost1_21_9()) {
            SkinTextureUtil_post_1_21_9.setSkullBlockTexture(skull, grave.getOwnerName(), headBase64);
         } else {
            SkinTextureUtil.setSkullBlockTexture(skull, grave.getOwnerName(), headBase64);
         }
      } else if (headType == 2 && headName != null && headName.length() <= 16) {
         try {
            skull.setOwningPlayer(plugin.getServer().getOfflinePlayer(headName));
         } catch (Exception var8) {
            skull.setOwner(headName);
         }
      }

      skull.update();
   }

   @Override
   public ItemStack getSkullItemStack(Grave grave, Graves plugin) {
      ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
      SkullMeta skullMeta = (SkullMeta)itemStack.getItemMeta();
      if (skullMeta != null) {
         if (grave.getOwnerType() == EntityType.PLAYER) {
            OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(grave.getOwnerUUID());
            skullMeta.setOwningPlayer(offlinePlayer);
         } else if (grave.getOwnerType() != null) {
            String entityTexture = this.getEntityTexture(grave.getOwnerType());
            if (entityTexture != null) {
               if (plugin.getVersionManager().isPost1_21_9()) {
                  SkinTextureUtil_post_1_21_9.setSkullBlockTexture(skullMeta, grave.getOwnerName(), entityTexture);
               } else {
                  SkinTextureUtil.setSkullBlockTexture(skullMeta, grave.getOwnerName(), entityTexture);
               }
            }
         }

         itemStack.setItemMeta(skullMeta);
      }

      return itemStack;
   }

   private String getEntityTexture(EntityType entityType) {
      return switch (entityType) {
         case ZOMBIE -> "base64_texture_for_zombie";
         case SKELETON -> "base64_texture_for_skeleton";
         default -> null;
      };
   }

   @Override
   public String getSkullTexture(ItemStack itemStack) {
      if (itemStack.getType() == Material.PLAYER_HEAD && itemStack.getItemMeta() != null) {
         SkullMeta skullMeta = (SkullMeta)itemStack.getItemMeta();
         return this.extractSkullTexture(skullMeta);
      } else {
         return null;
      }
   }

   private String extractSkullTexture(SkullMeta skullMeta) {
      try {
         Field profileField = skullMeta.getClass().getDeclaredField("profile");
         profileField.setAccessible(true);
         GameProfile gameProfile = (GameProfile)profileField.get(skullMeta);
         if (gameProfile != null && this.getTexturesKey(gameProfile)) {
            try {
               Collection<Property> propertyCollection = gameProfile.properties().get("textures");
               if (!propertyCollection.isEmpty()) {
                  return propertyCollection.stream().findFirst().map(p -> {
                     try {
                        return p.value();
                     } catch (NoSuchMethodError var2x) {
                        return p.getValue();
                     }
                  }).orElse(null);
               }
            } catch (NoSuchMethodError var6) {
               Collection<Property> propertyCollectionx = gameProfile.getProperties().get("textures");
               if (!propertyCollectionx.isEmpty()) {
                  return propertyCollectionx.stream().findFirst().map(p -> {
                     try {
                        return p.value();
                     } catch (NoSuchMethodError var2x) {
                        return p.getValue();
                     }
                  }).orElse(null);
               }
            }
         }
      } catch (Exception var7) {
         Bukkit.getLogger().severe("Failed to extract Skull Texture. Cause: ");
         var7.printStackTrace();
      }

      return null;
   }

   private boolean getTexturesKey(GameProfile gameProfile) {
      try {
         return gameProfile.properties().containsKey("textures");
      } catch (NoSuchMethodError var5) {
         try {
            return gameProfile.getProperties().containsKey("textures");
         } catch (Exception var4) {
            Bukkit.getLogger().severe("Failed to get textures key. Cause: " + var4.getCause());
            var4.printStackTrace();
            return false;
         }
      }
   }
}
