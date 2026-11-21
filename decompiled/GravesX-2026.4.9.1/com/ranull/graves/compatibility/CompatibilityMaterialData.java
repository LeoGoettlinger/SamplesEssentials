package com.ranull.graves.compatibility;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.ranull.graves.Graves;
import com.ranull.graves.data.BlockData;
import com.ranull.graves.type.Grave;
import com.ranull.graves.util.BlockFaceUtil;
import dev.cwhead.GravesX.util.SkinTextureUtil_post_1_21_9;
import java.lang.reflect.Field;
import java.util.Collection;
import me.jay.GravesX.util.SkinTextureUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.Openable;
import org.bukkit.plugin.Plugin;

public class CompatibilityMaterialData implements Compatibility {
   @Override
   public BlockData setBlockData(Location location, Material material, Grave grave, Graves plugin) {
      if (material == null) {
         return new BlockData(location, grave.getUUID(), null, null);
      } else {
         Block block = location.getBlock();
         String replaceMaterial = block.getType().name();
         if (block.getType().name().equals("NETHER_PORTAL") || block.getState().getData() instanceof Openable) {
            replaceMaterial = null;
         }

         block.setType(material);
         if ((material.name().equals("SKULL") || material.name().equals("PLAYER_HEAD")) && block.getState() instanceof Skull) {
            this.updateSkullBlock(block, grave, plugin);
         }

         return new BlockData(location, grave.getUUID(), replaceMaterial, null);
      }
   }

   @Override
   public boolean canBuild(Player player, Location location, Graves plugin) {
      Plugin landProtectionAddonPlugin = plugin.getServer().getPluginManager().getPlugin("GravesXAddon-LandProtection");
      if (landProtectionAddonPlugin != null && landProtectionAddonPlugin.isEnabled()) {
         return true;
      } else {
         Block placedBlock = location.getBlock();
         BlockState replacedBlockState = placedBlock.getState();
         Block placedAgainst = null;

         for (BlockFace face : BlockFace.values()) {
            Block relative = placedBlock.getRelative(face);
            if (relative.getType().isSolid()) {
               placedAgainst = relative;
               break;
            }
         }

         if (placedAgainst == null) {
            placedAgainst = placedBlock.getRelative(BlockFace.DOWN);
         }

         if (!plugin.getVersionManager().is_v1_8() && !plugin.getVersionManager().is_v1_7()) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
               ItemStack item = player.getInventory().getItem(slot);
               if (item != null && !item.getType().isAir()) {
                  BlockPlaceEvent event = new BlockPlaceEvent(placedBlock, replacedBlockState, placedAgainst, item, player, true, slot);
                  plugin.getServer().getPluginManager().callEvent(event);
                  if (event.canBuild() && !event.isCancelled()) {
                     return true;
                  }
               }
            }

            return false;
         } else {
            BlockPlaceEvent legacyEvent = new BlockPlaceEvent(placedBlock, replacedBlockState, placedAgainst, player.getItemInHand(), player, true);
            plugin.getServer().getPluginManager().callEvent(legacyEvent);
            return legacyEvent.canBuild() && !legacyEvent.isCancelled();
         }
      }
   }

   private static <T extends Enum<T>> T getEnumConstant(Class<?> enumClass, String name) {
      return Enum.valueOf((Class<T>)enumClass, name);
   }

   @Override
   public boolean hasTitleData(Block block) {
      BlockState state = block.getState();
      if (state instanceof Sign sign) {
         try {
            Class<?> sideClass = Class.forName("org.bukkit.block.sign.Side");
            Object frontSide = sign.getClass().getMethod("getSide", sideClass).invoke(sign, getEnumConstant(sideClass, "FRONT"));
            Object backSide = sign.getClass().getMethod("getSide", sideClass).invoke(sign, getEnumConstant(sideClass, "BACK"));

            for (int i = 0; i < 4; i++) {
               String frontLine = (String)frontSide.getClass().getMethod("getLine", int.class).invoke(frontSide, i);
               if (frontLine != null && !frontLine.trim().isEmpty()) {
                  return true;
               }

               String backLine = (String)backSide.getClass().getMethod("getLine", int.class).invoke(backSide, i);
               if (backLine != null && !backLine.trim().isEmpty()) {
                  return true;
               }
            }
         } catch (ReflectiveOperationException var12) {
            String[] lines = sign.getLines();

            for (String line : lines) {
               if (line != null && !line.trim().isEmpty()) {
                  return true;
               }
            }
         }
      } else {
         if (state instanceof Skull skull) {
            return skull.hasOwner();
         }

         if (state instanceof Nameable nameable) {
            String name = nameable.getCustomName();
            return name != null && !name.trim().isEmpty();
         }
      }

      return false;
   }

   private void updateSkullBlock(Block block, Grave grave, Graves plugin) {
      int headType = plugin.getConfigManager().getConfigSection("block.head.type", grave).getInt("block.head.type");
      String headBase64 = plugin.getConfigManager().getConfigSection("block.head.base64", grave).getString("block.head.base64");
      String headName = plugin.getConfigManager().getConfigSection("block.head.name", grave).getString("block.head.name");
      Skull skull = (Skull)block.getState();
      skull.setSkullType(SkullType.PLAYER);
      skull.setRotation(BlockFaceUtil.getYawBlockFace(grave.getYaw()).getOppositeFace());
      boolean useFallback = plugin.getVersionManager().is_v1_7();

      try {
         if (headType == 0) {
            if (grave.getOwnerType() == EntityType.PLAYER) {
               if (useFallback || headBase64 == null || headBase64.isEmpty()) {
                  OfflinePlayer player = Bukkit.getOfflinePlayer(grave.getOwnerName());

                  try {
                     skull.setOwningPlayer(player);
                  } catch (NoClassDefFoundError | NoSuchMethodError var14) {
                     skull.setOwner(grave.getOwnerName());
                  }
               } else if (plugin.getVersionManager().isPost1_21_9()) {
                  SkinTextureUtil_post_1_21_9.setSkullBlockTexture(skull, grave.getOwnerName(), headBase64);
               } else {
                  SkinTextureUtil.setSkullBlockTexture(skull, grave.getOwnerName(), headBase64);
               }
            } else {
               try {
                  skull.setOwningPlayer(plugin.getServer().getOfflinePlayer(grave.getOwnerName()));
               } catch (NoClassDefFoundError | NoSuchMethodError var13) {
                  skull.setOwner(grave.getOwnerName());
               }
            }
         } else if (headType == 1 && headBase64 != null && !headBase64.isEmpty()) {
            if (!useFallback) {
               if (plugin.getVersionManager().isPost1_21_9()) {
                  SkinTextureUtil_post_1_21_9.setSkullBlockTexture(skull, grave.getOwnerName(), headBase64);
               } else {
                  SkinTextureUtil.setSkullBlockTexture(skull, grave.getOwnerName(), headBase64);
               }
            } else {
               try {
                  skull.setOwningPlayer(plugin.getServer().getOfflinePlayer(grave.getOwnerName()));
               } catch (NoClassDefFoundError | NoSuchMethodError var12) {
                  skull.setOwner(grave.getOwnerName());
               }
            }
         } else if (headType == 2 && headName != null && headName.length() <= 16) {
            try {
               skull.setOwningPlayer(plugin.getServer().getOfflinePlayer(headName));
            } catch (NoClassDefFoundError | NoSuchMethodError var11) {
               skull.setOwner(headName);
            }
         }
      } catch (Exception var15) {
         plugin.getLogger().warning("Failed to use new skull method; falling back. Reason: " + var15.getMessage());
         skull.setOwner(grave.getOwnerName());
      }

      skull.update();
   }

   @Override
   public ItemStack getSkullItemStack(Grave grave, Graves plugin) {
      Material material = Material.matchMaterial("PLAYER_HEAD");
      ItemStack itemStack;
      if (material != null) {
         itemStack = new ItemStack(material, 1);
      } else {
         material = Material.matchMaterial("SKULL_ITEM");
         if (material == null) {
            return null;
         }

         itemStack = new ItemStack(material, 1, (short)3);
      }

      if (itemStack.getItemMeta() instanceof SkullMeta skullMeta) {
         if (grave.getOwnerType() == EntityType.PLAYER) {
            try {
               OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(grave.getOwnerName());
               skullMeta.setOwningPlayer(offlinePlayer);
            } catch (NoClassDefFoundError | NoSuchMethodError var7) {
               skullMeta.setOwner(grave.getOwnerName());
            }
         } else {
            String texture = grave.getOwnerTexture();
            if (texture != null && !texture.isEmpty()) {
               if (plugin.getVersionManager().isPost1_21_9()) {
                  SkinTextureUtil_post_1_21_9.setSkullBlockTexture(skullMeta, grave.getOwnerName(), texture);
               } else {
                  SkinTextureUtil.setSkullBlockTexture(skullMeta, grave.getOwnerName(), texture);
               }
            }
         }

         itemStack.setItemMeta(skullMeta);
      }

      return itemStack;
   }

   @Override
   public String getSkullTexture(ItemStack itemStack) {
      Material material = Material.matchMaterial("SKULL_ITEM");
      if (material != null && itemStack.getType() == material && itemStack.getItemMeta() != null) {
         SkullMeta skullMeta = (SkullMeta)itemStack.getItemMeta();

         try {
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            GameProfile gameProfile = (GameProfile)profileField.get(skullMeta);
            if (gameProfile != null && this.getTexturesKey(gameProfile)) {
               try {
                  Collection<Property> propertyCollection = gameProfile.properties().get("textures");
                  if (!propertyCollection.isEmpty()) {
                     try {
                        return propertyCollection.stream().findFirst().get().value();
                     } catch (NoSuchMethodError var10) {
                        return propertyCollection.stream().findFirst().get().getValue();
                     }
                  }
               } catch (NoSuchMethodError var11) {
                  Collection<Property> propertyCollectionx = gameProfile.getProperties().get("textures");
                  if (!propertyCollectionx.isEmpty()) {
                     try {
                        return propertyCollectionx.stream().findFirst().get().value();
                     } catch (NoSuchMethodError var9) {
                        return propertyCollectionx.stream().findFirst().get().getValue();
                     }
                  }
               }
            }
         } catch (IllegalAccessException | NoSuchFieldException var12) {
            var12.printStackTrace();
         }
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
