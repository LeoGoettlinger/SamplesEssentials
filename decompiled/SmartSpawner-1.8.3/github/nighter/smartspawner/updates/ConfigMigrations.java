package github.nighter.smartspawner.updates;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class ConfigMigrations {
   public static final List<YamlMigrator.Rename> CONFIG = List.of(
      new YamlMigrator.Rename("database.standalone.host", "database.sql.host"),
      new YamlMigrator.Rename("database.standalone.port", "database.sql.port"),
      new YamlMigrator.Rename("database.standalone.username", "database.sql.username"),
      new YamlMigrator.Rename("database.standalone.password", "database.sql.password"),
      new YamlMigrator.Rename("database.standalone.pool.maximum-size", "database.sql.pool.maximum-size"),
      new YamlMigrator.Rename("database.standalone.pool.minimum-idle", "database.sql.pool.minimum-idle"),
      new YamlMigrator.Rename("database.standalone.pool.connection-timeout", "database.sql.pool.connection-timeout"),
      new YamlMigrator.Rename("database.standalone.pool.max-lifetime", "database.sql.pool.max-lifetime"),
      new YamlMigrator.Rename("database.standalone.pool.idle-timeout", "database.sql.pool.idle-timeout"),
      new YamlMigrator.Rename("database.standalone.pool.keepalive-time", "database.sql.pool.keepalive-time"),
      new YamlMigrator.Rename("database.standalone.pool.leak-detection-threshold", "database.sql.pool.leak-detection-threshold"),
      new YamlMigrator.Rename("database.mode", "database.type"),
      new YamlMigrator.Rename("database.table_prefix", "database.table-prefix"),
      new YamlMigrator.Rename("database.server_name", "database.server-name"),
      new YamlMigrator.Rename("database.sync_across_servers", "database.sync-across-servers"),
      new YamlMigrator.Rename("database.migrate_from_local", "database.migrate-from-local"),
      new YamlMigrator.Rename("database.sqlite.file", "database.sqlite-file"),
      new YamlMigrator.Rename("database.sql.host", "database.host"),
      new YamlMigrator.Rename("database.sql.port", "database.port"),
      new YamlMigrator.Rename("database.sql.username", "database.username"),
      new YamlMigrator.Rename("database.sql.password", "database.password"),
      new YamlMigrator.Rename("database.sql.pool.maximum-size", "database.pool-size"),
      new YamlMigrator.Rename("custom_economy.enabled", "sell_integration.enabled"),
      new YamlMigrator.Rename("custom_economy.currency", "sell_integration.currency"),
      new YamlMigrator.Rename("custom_economy.coinsengine_currency", "sell_integration.excellenteconomy_currency"),
      new YamlMigrator.Rename("custom_economy.price_source_mode", "sell_integration.price_source_mode"),
      new YamlMigrator.Rename("custom_economy.shop_integration.enabled", "sell_integration.shop_integration.enabled"),
      new YamlMigrator.Rename("custom_economy.shop_integration.preferred_plugin", "sell_integration.shop_integration.preferred_plugin"),
      new YamlMigrator.Rename("custom_economy.custom_prices.enabled", "sell_integration.custom_prices.enabled"),
      new YamlMigrator.Rename("custom_economy.custom_prices.default_price", "sell_integration.custom_prices.default_price"),
      new YamlMigrator.Rename("sell_integration.coinsengine_currency", "sell_integration.excellenteconomy_currency"),
      new YamlMigrator.Rename("spawner_break.auto_sell_and_claim_exp_on_break", "spawner_break.sell_and_xp_break")
   );
   public static final List<YamlMigrator.Rename> ACTIVITY_LOG = List.of(
      new YamlMigrator.Rename("enabled", "discord.enabled"),
      new YamlMigrator.Rename("webhook_url", "discord.webhook_url"),
      new YamlMigrator.Rename("show_player_head", "discord.show_player_head"),
      new YamlMigrator.Rename("log_all_events", "discord.log_all_events"),
      new YamlMigrator.Rename("logged_events", "discord.logged_events")
   );
   public static final YamlMigrator.CustomMigration ACTIVITY_LOG_LAYOUT = (user, defaults) -> {
      boolean changed = false;

      for (String key : new ArrayList(user.getKeys(false))) {
         ConfigurationSection legacy = user.getConfigurationSection(key + ".embed");
         if (legacy != null) {
            String target = "embeds." + key;
            if (!user.contains(target)) {
               for (String path : legacy.getKeys(true)) {
                  if (!legacy.isConfigurationSection(path)) {
                     user.set(target + "." + path, legacy.get(path));
                  }
               }
            }

            user.set(key, null);
            changed = true;
         }
      }

      return changed;
   };
   public static final List<YamlMigrator.Rename> SELL_INTEGRATION = List.of();
   public static final YamlMigrator.OwnedSection SELL_INTEGRATION_PRICES = YamlMigrator.OwnedSection.curated(
      (defaults, path) -> path.equals("custom_prices.prices")
   );
   public static final List<YamlMigrator.Rename> MESSAGES = List.of();
   public static final List<YamlMigrator.Rename> COMMAND_MESSAGES = List.of();
   public static final List<YamlMigrator.Rename> GUI = List.of();
   public static final List<YamlMigrator.Rename> COMMAND_GUI = List.of();
   public static final List<YamlMigrator.Rename> FORMATTING = List.of();
   public static final List<YamlMigrator.Rename> ITEMS = List.of();
   private static final String[] LEGACY_DATABASE_SECTIONS = new String[]{"database.sql", "database.sqlite"};
   private static final String DATABASE_SECTION = "database";
   private static final String AUTOSAVE_KEY = "autosave-interval";
   public static final YamlMigrator.CustomMigration CONFIG_VALUES = (user, defaults) -> {
      boolean changed = false;
      if ("COINSENGINE".equals(user.getString("sell_integration.currency"))) {
         user.set("sell_integration.currency", "EXCELLENTECONOMY");
         changed = true;
      }

      if ("DATABASE".equals(user.getString("database.type"))) {
         user.set("database.type", "MYSQL");
         changed = true;
      }

      if ("YAML".equals(user.getString("database.type"))) {
         user.set("database.type", "SQLITE");
         changed = true;
      }

      for (String section : LEGACY_DATABASE_SECTIONS) {
         if (user.contains(section)) {
            user.set(section, null);
            changed = true;
         }
      }

      return rebuildDatabaseSection(user, defaults) || changed;
   };
   private static final Pattern LEGACY_CLICK_PATH = Pattern.compile(
      "^(slot_[^.]+(?:\\.if\\.[^.]+)?)\\.(click|left_click|right_click|shift_left_click|shift_right_click)$"
   );
   private static final Pattern LEGACY_SOUND_PATH = Pattern.compile("^(slot_[^.]+(?:\\.if\\.[^.]+)?)\\.sound(?:\\.(success|fail))?(.*)$");
   public static final YamlMigrator.CustomMigration GUI_LAYOUT = (user, defaults) -> {
      List<ConfigMigrations$1Move> moves = new ArrayList<>();

      for (String path : user.getKeys(true)) {
         if (!user.isConfigurationSection(path)) {
            String newPath = null;
            Matcher clickMatcher = LEGACY_CLICK_PATH.matcher(path);
            if (clickMatcher.matches()) {
               newPath = clickMatcher.group(1) + "." + clickMatcher.group(2) + ".action";
            } else {
               Matcher soundMatcher = LEGACY_SOUND_PATH.matcher(path);
               if (soundMatcher.matches()) {
                  String soundKey = switch (soundMatcher.group(2)) {
                     case "success" -> "sound_success";
                     case "fail" -> "sound_fail";
                     case null, default -> "sound";
                  };
                  newPath = soundMatcher.group(1) + ".click." + soundKey + soundMatcher.group(3);
               }
            }

            if (newPath != null && !newPath.equals(path)) {
               moves.add(new ConfigMigrations$1Move(path, newPath, user.get(path), user.contains(newPath)));
            }
         }
      }

      if (moves.isEmpty()) {
         return false;
      } else {
         for (ConfigMigrations$1Move m : moves) {
            user.set(m.oldPath(), null);
         }

         for (ConfigMigrations$1Move m : moves) {
            if (!m.newExists() && !user.contains(m.newPath())) {
               user.set(m.newPath(), m.value());
            }
         }

         return true;
      }
   };
   private static final String[] ITEM_DEFAULT_SECTIONS = new String[]{"smart_spawner", "item_spawner", "vanilla_spawner"};
   public static final YamlMigrator.CustomMigration ITEM_DEFAULTS = (user, defaults) -> {
      if (defaults == null) {
         return false;
      } else {
         boolean changed = false;

         for (String section : ITEM_DEFAULT_SECTIONS) {
            String base = section + ".default";
            ConfigurationSection defSection = defaults.getConfigurationSection(base);
            if (defSection != null) {
               for (String key : defSection.getKeys(true)) {
                  if (!defSection.isConfigurationSection(key)) {
                     String path = base + "." + key;
                     if (!user.contains(path)) {
                        user.set(path, defaults.get(path));
                        user.setComments(path, defaults.getComments(path));
                        changed = true;
                     }
                  }
               }
            }
         }

         return changed;
      }
   };

   private ConfigMigrations() {
   }

   public static List<YamlMigrator.Rename> forLanguageFile(LanguageUpdater.LanguageFileType type) {
      return switch (type) {
         case MESSAGES -> MESSAGES;
         case COMMAND_MESSAGES -> COMMAND_MESSAGES;
         case GUI -> GUI;
         case COMMAND_GUI -> COMMAND_GUI;
         case FORMATTING -> FORMATTING;
         case ITEMS -> ITEMS;
      };
   }

   private static boolean rebuildDatabaseSection(YamlConfiguration user, YamlConfiguration defaults) {
      if (defaults == null) {
         return false;
      } else {
         ConfigurationSection userDb = user.getConfigurationSection("database");
         ConfigurationSection defaultDb = defaults.getConfigurationSection("database");
         if (userDb != null && defaultDb != null && !userDb.contains("autosave-interval")) {
            Map<String, Object> existing = new LinkedHashMap<>();

            for (String key : userDb.getKeys(true)) {
               if (!userDb.isConfigurationSection(key)) {
                  existing.put(key, userDb.get(key));
               }
            }

            for (String keyx : new ArrayList(userDb.getKeys(false))) {
               userDb.set(keyx, null);
            }

            for (String keyx : defaultDb.getKeys(true)) {
               if (!defaultDb.isConfigurationSection(keyx)) {
                  String path = "database." + keyx;
                  user.set(path, existing.containsKey(keyx) ? existing.remove(keyx) : defaults.get(path));
                  user.setComments(path, defaults.getComments(path));
                  user.setInlineComments(path, defaults.getInlineComments(path));
               }
            }

            existing.forEach((keyxx, value) -> user.set("database." + keyxx, value));
            return true;
         } else {
            return false;
         }
      }
   }
}
