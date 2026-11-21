package org.lushplugins.lushrewards.libraries.lushlib.utils.skullcreator;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.UUID;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushrewards.libraries.lushlib.LushLogger;
import org.lushplugins.lushrewards.libraries.lushlib.utils.SkullCreator;

public class LegacySkullCreator implements SkullCreator.Interface {
   private Method skullMetaSetProfileMethod;
   private Field skullMetaProfileField;

   @Override
   public ItemStack getCustomSkull(String texture) {
      ItemStack item = new ItemStack(Material.PLAYER_HEAD);
      if (item.getItemMeta() instanceof SkullMeta meta) {
         this.mutateItemMeta(meta, texture);
         item.setItemMeta(meta);
         return item;
      } else {
         return null;
      }
   }

   @Override
   public void mutateItemMeta(SkullMeta meta, String b64) {
      try {
         if (this.skullMetaSetProfileMethod == null) {
            this.skullMetaSetProfileMethod = meta.getClass().getDeclaredMethod("setProfile", GameProfile.class);
            this.skullMetaSetProfileMethod.setAccessible(true);
         }

         this.skullMetaSetProfileMethod.invoke(meta, this.makeProfile(b64));
      } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException var6) {
         try {
            if (this.skullMetaProfileField == null) {
               this.skullMetaProfileField = meta.getClass().getDeclaredField("profile");
               this.skullMetaProfileField.setAccessible(true);
            }

            this.skullMetaProfileField.set(meta, this.makeProfile(b64));
         } catch (NoSuchFieldException | IllegalAccessException var5) {
            var5.printStackTrace();
         }
      }
   }

   @Nullable
   @Override
   public String getB64(ItemStack itemStack) {
      try {
         if (itemStack.hasItemMeta() && itemStack.getItemMeta() instanceof SkullMeta skullMeta) {
            if (this.skullMetaProfileField == null) {
               this.skullMetaProfileField = skullMeta.getClass().getDeclaredField("profile");
               this.skullMetaProfileField.setAccessible(true);
            }

            GameProfile gameProfile = (GameProfile)this.skullMetaProfileField.get(skullMeta);
            Iterator<Property> iterator = gameProfile.getProperties().get("textures").iterator();
            if (iterator.hasNext()) {
               Property property = iterator.next();
               return property.getValue();
            }
         }

         return null;
      } catch (Exception var6) {
         return null;
      }
   }

   @Override
   public String getTexture(OfflinePlayer player) {
      GameProfile profile;
      try {
         Method getProfileMethod = player.getClass().getDeclaredMethod("getProfile");
         profile = (GameProfile)getProfileMethod.invoke(player);
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException var6) {
         var6.printStackTrace();
         return "";
      }

      Property property;
      try {
         property = (Property)profile.getProperties().get("textures").iterator().next();
      } catch (Exception var5) {
         return "";
      }

      return property.getValue();
   }

   private GameProfile makeProfile(String b64) {
      UUID id = null;

      try {
         id = new UUID(b64.substring(b64.length() - 20).hashCode(), b64.substring(b64.length() - 10).hashCode());
      } catch (StringIndexOutOfBoundsException var4) {
         if (b64.length() == 0) {
            LushLogger.getLogger().warning("Missing base64 texture found - check your config");
         } else {
            LushLogger.getLogger().warning("Invalid base64 texture (" + b64 + ") found - check your config");
         }
      }

      GameProfile profile = new GameProfile(id, "Player");
      profile.getProperties().put("textures", new Property("textures", b64));
      return profile;
   }
}
