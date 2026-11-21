package github.nighter.smartspawner.hooks.protections.api;

import com.iridium.iridiumskyblock.api.IridiumSkyblockAPI;
import com.iridium.iridiumskyblock.database.Island;
import com.iridium.iridiumskyblock.database.User;
import com.iridium.iridiumskyblock.dependencies.iridiumcore.Item;
import com.iridium.iridiumskyblock.dependencies.iridiumteams.Permission;
import com.iridium.iridiumskyblock.dependencies.xseries.XMaterial;
import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.hooks.protections.ProtectionHook;
import github.nighter.smartspawner.language.format.ColorUtil;
import github.nighter.smartspawner.updates.YamlMigrator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class IridiumSkyblock implements ProtectionHook {
   private static final String FILE_NAME = "iridium_skyblock.yml";
   private static final String DEFAULT_LOCALE = "en_US";
   private static YamlConfiguration language = new YamlConfiguration();

   public static void init(SmartSpawner plugin) {
      loadLanguage(plugin);
      Permission spawnerOpenMenuPermission = new Permission(
         new Item(
            XMaterial.SPAWNER,
            38,
            1,
            text(plugin, "iridium_skyblock.permissions.open_menu.name", "&bSpawner Menu Permission"),
            lore(
               plugin,
               "iridium_skyblock.permissions.open_menu.lore",
               List.of("&7Permission for permit users to open spawners menu.", "", "&b&lPermission", "%permission%")
            )
         ),
         1,
         1
      );
      Permission spawnerStackPermission = new Permission(
         new Item(
            XMaterial.SPAWNER,
            39,
            1,
            text(plugin, "iridium_skyblock.permissions.stack.name", "&7Spawner Stack Permission"),
            lore(
               plugin,
               "iridium_skyblock.permissions.stack.lore",
               List.of("&7Permission for permit users to stack spawners.", "", "&b&lPermission", "%permission%")
            )
         ),
         1,
         1
      );
      IridiumSkyblockAPI.getInstance().addPermission(spawnerOpenMenuPermission, "SpawnerOpenMenuPermission");
      IridiumSkyblockAPI.getInstance().addPermission(spawnerStackPermission, "SpawnerStackPermission");
   }

   private static void loadLanguage(SmartSpawner plugin) {
      String locale = plugin.getConfig().getString("language", "en_US");
      String localeResource = "language/" + locale + "/iridium_skyblock.yml";
      String resourcePath = resourceExists(plugin, localeResource) ? localeResource : "language/en_US/iridium_skyblock.yml";
      File languageFile = new File(plugin.getDataFolder(), "language/" + locale + "/iridium_skyblock.yml");
      YamlMigrator.migrate(languageFile, plugin.getResource(resourcePath), List.of(), plugin.getLogger());
      language = YamlConfiguration.loadConfiguration(languageFile);
   }

   private static boolean resourceExists(SmartSpawner plugin, String resourcePath) {
      try {
         boolean var3;
         try (InputStream input = plugin.getResource(resourcePath)) {
            var3 = input != null;
         }

         return var3;
      } catch (IOException var7) {
         return false;
      }
   }

   private static String text(SmartSpawner plugin, String path, String fallback) {
      String value = language.getString(path, fallback);
      return ColorUtil.translateHexColorCodes(value);
   }

   private static List<String> lore(SmartSpawner plugin, String path, List<String> fallback) {
      List<String> lines = language.getStringList(path);
      if (lines.isEmpty()) {
         lines = fallback;
      }

      return lines.stream().map(ColorUtil::translateHexColorCodes).toList();
   }

   @Override
   public boolean canStack(@NotNull Player player, @NotNull Location location) {
      return checkPermission(player, location, "SpawnerStackPermission");
   }

   @Override
   public boolean canOpenMenu(@NotNull Player player, @NotNull Location location) {
      return checkPermission(player, location, "SpawnerOpenMenuPermission");
   }

   private static boolean checkPermission(@NotNull Player player, @NotNull Location location, String permissionKey) {
      User user = IridiumSkyblockAPI.getInstance().getUser(player);
      Optional<Island> island = IridiumSkyblockAPI.getInstance().getIslandViaLocation(location);
      Optional<Permission> permission = IridiumSkyblockAPI.getInstance().getPermissions(permissionKey);
      return user != null && !island.isEmpty() && !permission.isEmpty()
         ? IridiumSkyblockAPI.getInstance().getIslandPermission(island.get(), user, permission.get(), permissionKey)
         : true;
   }
}
