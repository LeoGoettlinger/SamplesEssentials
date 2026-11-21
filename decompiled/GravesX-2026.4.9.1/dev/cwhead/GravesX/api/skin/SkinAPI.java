package dev.cwhead.GravesX.api.skin;

import com.mojang.authlib.GameProfile;
import dev.cwhead.GravesX.util.SkinTextureUtil_post_1_21_9;
import me.jay.GravesX.util.SkinSignatureUtil;
import me.jay.GravesX.util.SkinTextureUtil;
import org.bukkit.block.Skull;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SkinAPI {
   public static String getSkinSignature(@NotNull Entity entity) {
      return SkinSignatureUtil.getSignature(entity);
   }

   public static void setSkullTexture(@NotNull Skull skull, @NotNull String name, @NotNull String base64) {
      try {
         SkinTextureUtil.setSkullBlockTexture(skull, name, base64);
      } catch (Exception var4) {
         SkinTextureUtil_post_1_21_9.setSkullBlockTexture(skull, name, base64);
      }
   }

   public static void setSkullTexture(@NotNull SkullMeta skullMeta, @NotNull String name, @NotNull String base64) {
      try {
         SkinTextureUtil.setSkullBlockTexture(skullMeta, name, base64);
      } catch (Exception var4) {
         SkinTextureUtil_post_1_21_9.setSkullBlockTexture(skullMeta, name, base64);
      }
   }

   @Nullable
   public static String getTexture(@NotNull Entity entity) {
      try {
         return SkinTextureUtil.getTexture(entity);
      } catch (Exception var2) {
         return SkinTextureUtil_post_1_21_9.getTexture(entity);
      }
   }

   @Nullable
   public static GameProfile getPlayerGameProfile(@NotNull Player player) {
      return SkinTextureUtil.getPlayerGameProfile(player);
   }

   @Nullable
   public static PlayerProfile getPlayerProfile(@NotNull Player player) {
      return SkinTextureUtil_post_1_21_9.getPlayerProfile(player);
   }
}
