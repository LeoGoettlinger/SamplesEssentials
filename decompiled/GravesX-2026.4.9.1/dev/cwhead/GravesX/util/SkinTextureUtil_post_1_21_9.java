package dev.cwhead.GravesX.util;

import com.ranull.graves.util.Base64Util;
import com.ranull.skulltextureapi.SkullTextureAPI;
import java.net.URL;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

public class SkinTextureUtil_post_1_21_9 {
   private SkinTextureUtil_post_1_21_9() {
   }

   private static PlayerProfile createProfileWithTexture(String name, String base64) {
      PlayerProfile profile = Bukkit.createPlayerProfile(UUID.randomUUID(), name);
      PlayerTextures textures = profile.getTextures();
      textures.setSkin(Base64Util.extractSkinURL(base64));
      profile.setTextures(textures);
      return profile;
   }

   public static void setSkullBlockTexture(Skull skull, String name, String base64) {
      try {
         PlayerProfile profile = createProfileWithTexture(name, base64);
         skull.setOwnerProfile(profile);
      } catch (Throwable var4) {
         Bukkit.getLogger().warning("Failed to set Skull texture: " + var4);
         var4.printStackTrace();
      }
   }

   public static void setSkullBlockTexture(SkullMeta skullMeta, String name, String base64) {
      try {
         PlayerProfile profile = createProfileWithTexture(name, base64);
         skullMeta.setOwnerProfile(profile);
      } catch (Throwable var4) {
         Bukkit.getLogger().warning("Failed to set SkullMeta texture: " + var4);
         var4.printStackTrace();
      }
   }

   public static String getTexture(Entity entity) {
      if (entity instanceof Player player) {
         PlayerProfile profile = player.getPlayerProfile();
         if (profile.getTextures().getSkin() != null) {
            return profile.getTextures().getSkin().toString();
         }
      } else {
         Plugin api = Bukkit.getPluginManager().getPlugin("SkullTextureAPI");
         if (api instanceof SkullTextureAPI && api.isEnabled()) {
            try {
               String base64 = SkullTextureAPI.getTexture(entity);
               if (base64 != null && !base64.isEmpty()) {
                  return base64;
               }
            } catch (NoSuchMethodError var4) {
            }
         }
      }

      return null;
   }

   public static PlayerProfile getPlayerProfile(Player player) {
      return player.getPlayerProfile();
   }

   public static PlayerProfile createProfileFromTextureString(UUID uuid, String name, String textureOrBase64OrHash) {
      PlayerProfile profile = Bukkit.createPlayerProfile(uuid, name);

      try {
         if (textureOrBase64OrHash == null || textureOrBase64OrHash.isEmpty()) {
            return profile;
         }

         String s = textureOrBase64OrHash.trim();
         if (s.startsWith("http://") || s.startsWith("https://")) {
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(s));
            profile.setTextures(textures);
            return profile;
         }

         if (s.matches("^[0-9a-fA-F]{32,}$")) {
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL("https://textures.minecraft.net/texture/" + s));
            profile.setTextures(textures);
            return profile;
         }

         URL url = Base64Util.extractSkinURL(s);
         if (url != null) {
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(url);
            profile.setTextures(textures);
         }
      } catch (Throwable var7) {
         Bukkit.getLogger().warning("Failed to build PlayerProfile from texture: " + var7);
         var7.printStackTrace();
      }

      return profile;
   }
}
