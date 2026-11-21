package me.jay.GravesX.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.ranull.skulltextureapi.SkullTextureAPI;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

public final class SkinTextureUtil {
   private static volatile String GAMEPROFILE_METHOD;

   private SkinTextureUtil() {
   }

   public static void setSkullBlockTexture(Skull skull, String name, String base64) {
      try {
         GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name);

         try {
            gameProfile.properties().put("textures", new Property("textures", base64));
            Field profileField = skull.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            if (profileField.getType().getSimpleName().contains("ResolvableProfile")) {
               Constructor<?> resolvableProfileConstructor = Class.forName("net.minecraft.world.item.component.ResolvableProfile")
                  .getDeclaredConstructor(GameProfile.class);
               resolvableProfileConstructor.setAccessible(true);
               Object resolvableProfileInstance = resolvableProfileConstructor.newInstance(gameProfile);
               profileField.set(skull, resolvableProfileInstance);
            } else {
               profileField.set(skull, gameProfile);
            }
         } catch (IllegalAccessException | NoSuchMethodException | ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodError var8) {
            gameProfile.getProperties().put("textures", new Property("textures", base64));
            Field profileFieldx = skull.getClass().getDeclaredField("profile");
            profileFieldx.setAccessible(true);
            if (profileFieldx.getType().getSimpleName().contains("ResolvableProfile")) {
               Constructor<?> resolvableProfileConstructor = Class.forName("net.minecraft.world.item.component.ResolvableProfile")
                  .getDeclaredConstructor(GameProfile.class);
               resolvableProfileConstructor.setAccessible(true);
               Object resolvableProfileInstance = resolvableProfileConstructor.newInstance(gameProfile);
               profileFieldx.set(skull, resolvableProfileInstance);
            } else {
               profileFieldx.set(skull, gameProfile);
            }
         }
      } catch (IllegalAccessException | NoSuchFieldException | InstantiationException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException | NoSuchMethodError var9) {
         Bukkit.getLogger().warning("Failed to set the Skull texture. Cause: " + (var9.getCause() != null ? var9.getCause() : var9).toString());
         var9.printStackTrace();
      }
   }

   public static void setSkullBlockTexture(SkullMeta skullMeta, String name, String base64) {
      try {
         GameProfile gameProfile = new GameProfile(UUID.randomUUID(), name);

         try {
            gameProfile.properties().put("textures", new Property("textures", base64));
            Field profileField = skullMeta.getClass().getDeclaredField("profile");
            profileField.setAccessible(true);
            if (profileField.getType().getSimpleName().contains("ResolvableProfile")) {
               Constructor<?> resolvableProfileConstructor = Class.forName("net.minecraft.world.item.component.ResolvableProfile")
                  .getDeclaredConstructor(GameProfile.class);
               resolvableProfileConstructor.setAccessible(true);
               Object resolvableProfileInstance = resolvableProfileConstructor.newInstance(gameProfile);
               profileField.set(skullMeta, resolvableProfileInstance);
            } else {
               profileField.set(skullMeta, gameProfile);
            }
         } catch (IllegalAccessException | NoSuchMethodException | ClassNotFoundException | InstantiationException | InvocationTargetException | NoSuchMethodError var8) {
            gameProfile.getProperties().put("textures", new Property("textures", base64));
            Field profileFieldx = skullMeta.getClass().getDeclaredField("profile");
            profileFieldx.setAccessible(true);
            if (profileFieldx.getType().getSimpleName().contains("ResolvableProfile")) {
               Constructor<?> resolvableProfileConstructor = Class.forName("net.minecraft.world.item.component.ResolvableProfile")
                  .getDeclaredConstructor(GameProfile.class);
               resolvableProfileConstructor.setAccessible(true);
               Object resolvableProfileInstance = resolvableProfileConstructor.newInstance(gameProfile);
               profileFieldx.set(skullMeta, resolvableProfileInstance);
            } else {
               profileFieldx.set(skullMeta, gameProfile);
            }
         }
      } catch (IllegalAccessException | NoSuchFieldException | InstantiationException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException | NoSuchMethodError var9) {
         Bukkit.getLogger().warning("Failed to set the SkullMeta texture. Cause: " + (var9.getCause() != null ? var9.getCause() : var9).toString());
         var9.printStackTrace();
      }
   }

   public static String getTexture(Entity entity) {
      if (entity instanceof Player) {
         GameProfile gameProfile = getPlayerGameProfile((Player)entity);
         if (gameProfile != null) {
            try {
               PropertyMap propertyMap = gameProfile.properties();
               if (propertyMap.containsKey("textures")) {
                  Collection<Property> propertyCollection = propertyMap.get("textures");

                  try {
                     return !propertyCollection.isEmpty() ? propertyCollection.stream().findFirst().get().value() : null;
                  } catch (NoSuchMethodError var8) {
                     return !propertyCollection.isEmpty() ? propertyCollection.stream().findFirst().get().getValue() : null;
                  }
               }
            } catch (NoSuchMethodError var9) {
               PropertyMap propertyMapx = gameProfile.getProperties();
               if (propertyMapx.containsKey("textures")) {
                  Collection<Property> propertyCollection = propertyMapx.get("textures");

                  try {
                     return !propertyCollection.isEmpty() ? propertyCollection.stream().findFirst().get().value() : null;
                  } catch (NoSuchMethodError var7) {
                     return !propertyCollection.isEmpty() ? propertyCollection.stream().findFirst().get().getValue() : null;
                  }
               }
            }
         }
      } else {
         Plugin skullTextureAPIPlugin = Bukkit.getServer().getPluginManager().getPlugin("SkullTextureAPI");
         if (skullTextureAPIPlugin != null && skullTextureAPIPlugin.isEnabled() && skullTextureAPIPlugin instanceof SkullTextureAPI) {
            try {
               String base64 = SkullTextureAPI.getTexture(entity);
               if (base64 != null && !base64.equals("")) {
                  return base64;
               }
            } catch (NoSuchMethodError var6) {
            }
         }
      }

      return null;
   }

   public static GameProfile getPlayerGameProfile(Player player) {
      try {
         Object playerObject = player.getClass().getMethod("getHandle").invoke(player);
         if (GAMEPROFILE_METHOD == null) {
            findGameProfileMethod(playerObject);
         }

         if (GAMEPROFILE_METHOD != null && !GAMEPROFILE_METHOD.equals("")) {
            Method gameProfile = playerObject.getClass().getMethod(GAMEPROFILE_METHOD);
            gameProfile.setAccessible(true);
            return (GameProfile)gameProfile.invoke(playerObject);
         }
      } catch (ReflectiveOperationException var3) {
      }

      return null;
   }

   private static void findGameProfileMethod(Object playerObject) {
      for (Method method : playerObject.getClass().getMethods()) {
         if (method.getReturnType().getName().endsWith("GameProfile")) {
            GAMEPROFILE_METHOD = method.getName();
            return;
         }
      }

      GAMEPROFILE_METHOD = "";
   }
}
