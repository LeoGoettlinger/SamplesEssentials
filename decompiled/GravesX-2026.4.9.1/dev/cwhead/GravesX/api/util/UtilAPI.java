package dev.cwhead.GravesX.api.util;

import com.ranull.graves.Graves;
import com.ranull.graves.util.Base64Util;
import com.ranull.graves.util.ClassUtil;
import com.ranull.graves.util.ColorUtil;
import com.ranull.graves.util.EntityUtil;
import com.ranull.graves.util.ExperienceUtil;
import com.ranull.graves.util.FileUtil;
import com.ranull.graves.util.MaterialUtil;
import com.ranull.graves.util.PermissionUtil;
import com.ranull.graves.util.ReflectionUtil;
import com.ranull.graves.util.ResourceUtil;
import com.ranull.graves.util.UUIDUtil;
import com.ranull.graves.util.UpdateUtil;
import com.ranull.graves.util.YAMLUtil;
import dev.cwhead.GravesX.api.world.LocationAPI;
import dev.cwhead.GravesX.util.LibraryLoaderUtil;
import dev.cwhead.GravesX.util.MclogsUtil;
import dev.cwhead.GravesX.util.SkinTextureUtil_post_1_21_9;
import java.io.File;
import java.util.Objects;
import java.util.UUID;
import me.jay.GravesX.util.SkinSignatureUtil;
import me.jay.GravesX.util.SkinTextureUtil;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UtilAPI {
   private final Graves plugin;
   private final LocationAPI world;

   public UtilAPI(Graves plugin, LocationAPI world) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      this.world = Objects.requireNonNull(world, "world");
   }

   public String objectToBase64(@NotNull Object object) {
      return Base64Util.objectToBase64(object);
   }

   public Object base64ToObject(@NotNull String base64String) {
      return Base64Util.base64ToObject(base64String);
   }

   public void loadClass(@NotNull String className) {
      ClassUtil.loadClass(className);
   }

   public Color getColor(@NotNull String colorName) {
      return ColorUtil.getColor(colorName);
   }

   public Color getColorFromHex(@NotNull String hex) {
      return ColorUtil.getColorFromHex(hex);
   }

   public DustOptions dustFromHex(@NotNull String hexColor, float size) {
      return ColorUtil.createDustOptionsFromHex(hexColor, size);
   }

   public boolean hasPermission(@NotNull Entity entity, @NotNull String permission) {
      return EntityUtil.hasPermission(entity, permission);
   }

   public int playerTotalXp(@NotNull Player player) {
      return ExperienceUtil.getPlayerExperience(player);
   }

   public int xpAtLevel(int level) {
      return ExperienceUtil.getExperienceAtLevel(level);
   }

   public long levelFromXp(long experience) {
      return ExperienceUtil.getLevelFromExperience(experience);
   }

   public int dropPercent(int experience, float percent) {
      return ExperienceUtil.getDropPercent(experience, percent);
   }

   @Deprecated
   public int playerDropXp(@NotNull Player player, float expStorePercent) {
      return ExperienceUtil.getPlayerDropExperience(player, expStorePercent);
   }

   public void moveFile(@NotNull File file, @NotNull String name) {
      FileUtil.moveFile(file, name);
   }

   @Deprecated
   public void copyFile(@NotNull File file, @NotNull String name) {
      FileUtil.copyFile(file, name);
   }

   public boolean isAir(@NotNull Material material) {
      return MaterialUtil.isAir(material);
   }

   public boolean isLava(@NotNull Material material) {
      return MaterialUtil.isLava(material);
   }

   public boolean isSafeNotSolid(@NotNull Material material) {
      return MaterialUtil.isSafeNotSolid(material);
   }

   public boolean isSafeSolid(@NotNull Material material) {
      return MaterialUtil.isSafeSolid(material);
   }

   public boolean isWater(@NotNull Material material) {
      return MaterialUtil.isWater(material);
   }

   public boolean isPlayerHead(@NotNull Material material) {
      return MaterialUtil.isPlayerHead(material);
   }

   public boolean isPlayerHead(@NotNull String material) {
      return MaterialUtil.isPlayerHead(material);
   }

   public String postLog(@NotNull String content) {
      return MclogsUtil.postLogToMclogs(content);
   }

   public int highestInt(@NotNull Player player, @Nullable String permission) {
      return PermissionUtil.getHighestInt(player, permission);
   }

   public double highestDouble(@NotNull Player player, String permission) {
      return PermissionUtil.getHighestDouble(player, permission);
   }

   public void swingMainHand(@NotNull Player player) {
      ReflectionUtil.swingMainHand(player);
   }

   public void copyResources(@NotNull String inputPath, @NotNull String outputPath) {
      ResourceUtil.copyResources(inputPath, outputPath, this.plugin);
   }

   public void copyResources(@NotNull String inputPath, @NotNull String outputPath, boolean overwrite) {
      ResourceUtil.copyResources(inputPath, outputPath, overwrite, this.plugin);
   }

   public LibraryLoaderUtil libraryLoader() {
      return new LibraryLoaderUtil(this.plugin);
   }

   public String skinSignature(@NotNull Entity entity) {
      return SkinSignatureUtil.getSignature(entity);
   }

   public String skinTexture(@NotNull Entity entity) {
      try {
         return SkinTextureUtil.getTexture(entity);
      } catch (Throwable var3) {
         return SkinTextureUtil_post_1_21_9.getTexture(entity);
      }
   }

   public UUID uuidOf(@NotNull String string) {
      return UUIDUtil.getUUID(string);
   }

   public String latestSpigotVersion(int resourceId) {
      return UpdateUtil.getLatestVersion(resourceId);
   }

   public boolean isValidYaml(@NotNull File file) {
      return YAMLUtil.isValidYAML(file);
   }
}
