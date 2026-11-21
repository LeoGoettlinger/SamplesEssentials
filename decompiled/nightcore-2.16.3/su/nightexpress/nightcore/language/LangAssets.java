package su.nightexpress.nightcore.language;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.NightCore;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.BukkitThing;
import su.nightexpress.nightcore.util.FileUtil;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.bridge.RegistryType;

@Deprecated
public class LangAssets {
   private static FileConfig config;

   public static void load(@NonNull NightCore core) {
      String langCode = core.getLanguage();
      String assetsCode = downloadAssets(core, langCode);
      config = FileConfig.loadOrExtract(core, "/lang/", getFileName(assetsCode));
      loadDamageTypes();
   }

   public static void shutdown() {
      config.saveChanges();
      config = null;
   }

   private static void loadDamageTypes() {
      BukkitThing.getAll(RegistryType.DAMAGE_TYPE).forEach(damageType -> getOrCreate("DamageType", damageType));
   }

   @NonNull
   private static String downloadAssets(@NonNull NightCore plugin, @NonNull String langCode) {
      File file = new File(plugin.getDataFolder().getAbsolutePath() + "/lang/", getFileName(langCode));
      if (file.exists()) {
         return langCode;
      } else {
         FileUtil.create(file);
         String url = "https://github.com/nulli0n/nightcore-spigot/raw/master/assets/" + langCode + ".yml";

         try {
            String var8;
            try (
               BufferedInputStream in = new BufferedInputStream(new URL(url).openStream());
               FileOutputStream fileOutputStream = new FileOutputStream(file);
            ) {
               plugin.info("Downloading '" + langCode + "' assets from github...");
               byte[] dataBuffer = new byte[1024];

               int bytesRead;
               while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                  fileOutputStream.write(dataBuffer, 0, bytesRead);
               }

               var8 = langCode;
            }

            return var8;
         } catch (IOException var13) {
            plugin.error("Could not download language assets for '" + langCode + "' (no such assets?).");
            return LangManager.isDefault(langCode) ? langCode : downloadAssets(plugin, "en");
         }
      }
   }

   @NonNull
   private static String getFileName(@NonNull String langCode) {
      return "assets_" + langCode + ".yml";
   }

   @NonNull
   public static FileConfig getConfig() {
      return config;
   }

   @Deprecated
   @NonNull
   public static String get(@NonNull PotionEffectType type) {
      return getAsset("PotionEffectType", type);
   }

   @Deprecated
   @NonNull
   public static String get(@NonNull EntityType type) {
      return getAsset("EntityType", type);
   }

   @Deprecated
   @NonNull
   public static String get(@NonNull Material type) {
      return getAsset("Material", type);
   }

   @NonNull
   public static String get(@NonNull World world) {
      return getOrCreate("World", world);
   }

   @Deprecated
   @NonNull
   public static String get(@NonNull Enchantment enchantment) {
      return getOrCreate("Enchantment", enchantment);
   }

   @NonNull
   public static String get(@NonNull DamageType damageType) {
      return getOrCreate("DamageType", damageType);
   }

   @NonNull
   public static String getAsset(@NonNull String path, @NonNull Keyed keyed) {
      return getAsset(path, BukkitThing.toString(keyed));
   }

   @NonNull
   public static String getAsset(@NonNull String path, @NonNull String nameRaw) {
      return getAsset(path + "." + nameRaw).orElse(nameRaw);
   }

   @NonNull
   public static Optional<String> getAsset(@NonNull String path) {
      return Optional.ofNullable(config.getString(path));
   }

   @NonNull
   public static String getOrCreate(@NonNull String path, @NonNull Keyed keyed) {
      return getOrCreate(path, BukkitThing.toString(keyed));
   }

   @NonNull
   public static String getOrCreate(@NonNull String path, @NonNull String nameRaw) {
      config.addMissing(path + "." + nameRaw, StringUtil.capitalizeUnderscored(nameRaw));
      return getAsset(path, nameRaw);
   }
}
