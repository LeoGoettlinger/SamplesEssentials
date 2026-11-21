package github.nighter.smartspawner.updates;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public final class YamlMigrator {
   private static final List<String> LEGACY_VERSION_KEYS = List.of("config_version", "language_version", "gui_layout_version");

   private YamlMigrator() {
   }

   public static boolean migrate(File file, InputStream defaultStream, List<YamlMigrator.Rename> renames, Logger log) {
      return migrate(file, defaultStream, renames, null, true, log);
   }

   public static boolean migrate(File file, InputStream defaultStream, List<YamlMigrator.Rename> renames, YamlMigrator.CustomMigration custom, Logger log) {
      return migrate(file, defaultStream, renames, custom, true, log);
   }

   public static boolean migrate(
      File file, InputStream defaultStream, List<YamlMigrator.Rename> renames, YamlMigrator.CustomMigration custom, boolean addMissing, Logger log
   ) {
      return migrate(file, defaultStream, renames, custom, addMissing, null, log);
   }

   public static boolean migrate(
      File file,
      InputStream defaultStream,
      List<YamlMigrator.Rename> renames,
      YamlMigrator.CustomMigration custom,
      boolean addMissing,
      YamlMigrator.OwnedSection ownedSections,
      Logger log
   ) {
      if (!file.exists()) {
         return extract(file, defaultStream, log);
      } else {
         YamlConfiguration user;
         try {
            user = YamlConfiguration.loadConfiguration(file);
         } catch (Exception var18) {
            log.log(Level.SEVERE, "Could not read " + file.getName() + " for migration", (Throwable)var18);
            return false;
         }

         YamlConfiguration defaults = null;
         if (defaultStream != null) {
            try (InputStreamReader reader = new InputStreamReader(defaultStream, StandardCharsets.UTF_8)) {
               defaults = YamlConfiguration.loadConfiguration(reader);
            } catch (IOException var17) {
               log.log(Level.SEVERE, "Could not read default resource for " + file.getName(), (Throwable)var17);
               return false;
            }
         }

         int stripped = stripLegacyKeys(user);
         int renamed = applyRenames(user, renames);
         boolean customChanged = custom != null && custom.apply(user, defaults);
         int added = addMissing && defaults != null ? addMissingKeys(user, defaults, ownedSections) : 0;
         if (stripped == 0 && renamed == 0 && !customChanged && added == 0) {
            return false;
         } else {
            try {
               user.save(file);
            } catch (IOException var15) {
               log.log(Level.SEVERE, "Failed to save migrated " + file.getName(), (Throwable)var15);
               return false;
            }

            StringBuilder summary = new StringBuilder("Updated ").append(file.getName()).append(':');
            if (added > 0) {
               summary.append(" added ").append(added).append(" key(s);");
            }

            if (renamed > 0) {
               summary.append(" renamed ").append(renamed).append(" key(s);");
            }

            if (stripped > 0) {
               summary.append(" removed ").append(stripped).append(" legacy key(s);");
            }

            if (customChanged) {
               summary.append(" applied value migrations;");
            }

            log.info(summary.substring(0, summary.length() - 1));
            return true;
         }
      }
   }

   private static boolean extract(File file, InputStream defaultStream, Logger log) {
      if (defaultStream == null) {
         log.warning("No bundled resource to create " + file.getName());
         return false;
      } else {
         try {
            File parent = file.getParentFile();
            if (parent != null) {
               parent.mkdirs();
            }

            byte[] bytes = defaultStream.readAllBytes();
            Files.write(file.toPath(), bytes);
            return true;
         } catch (IOException var5) {
            log.log(Level.SEVERE, "Failed to create " + file.getName(), (Throwable)var5);
            return false;
         }
      }
   }

   private static int stripLegacyKeys(YamlConfiguration user) {
      int count = 0;

      for (String key : LEGACY_VERSION_KEYS) {
         if (user.contains(key)) {
            user.set(key, null);
            count++;
         }
      }

      return count;
   }

   private static int applyRenames(YamlConfiguration user, List<YamlMigrator.Rename> renames) {
      int count = 0;

      for (YamlMigrator.Rename r : renames) {
         if (user.contains(r.oldPath())) {
            if (!user.contains(r.newPath())) {
               user.set(r.newPath(), user.get(r.oldPath()));
            }

            user.set(r.oldPath(), null);
            pruneEmptyParents(user, r.oldPath());
            count++;
         }
      }

      return count;
   }

   private static void pruneEmptyParents(YamlConfiguration user, String path) {
      for (int dot = path.lastIndexOf(46); dot > 0; dot = path.lastIndexOf(46, dot - 1)) {
         String parent = path.substring(0, dot);
         ConfigurationSection section = user.getConfigurationSection(parent);
         if (section == null || !section.getKeys(false).isEmpty()) {
            return;
         }

         user.set(parent, null);
      }
   }

   private static int addMissingKeys(YamlConfiguration user, YamlConfiguration defaults, YamlMigrator.OwnedSection owned) {
      Set<String> locked = owned == null ? Set.of() : lockedSections(user, defaults, owned);
      int count = 0;

      for (String path : defaults.getKeys(true)) {
         if (!defaults.isConfigurationSection(path) && !isInside(path, locked) && !user.contains(path)) {
            user.set(path, defaults.get(path));
            user.setComments(path, defaults.getComments(path));
            user.setInlineComments(path, defaults.getInlineComments(path));
            count++;
         }
      }

      return count;
   }

   private static Set<String> lockedSections(YamlConfiguration user, YamlConfiguration defaults, YamlMigrator.OwnedSection owned) {
      Set<String> locked = new HashSet<>();

      for (String path : defaults.getKeys(true)) {
         if (defaults.isConfigurationSection(path) && owned.matcher().matches(defaults, path)) {
            if (owned.ownership() != YamlMigrator.Ownership.USER_MANAGED && !user.isConfigurationSection(path)) {
               if (owned.ownership() == YamlMigrator.Ownership.CURATED) {
                  int dot = path.lastIndexOf(46);
                  if (dot > 0 && user.isConfigurationSection(path.substring(0, dot))) {
                     locked.add(path);
                  }
               }
            } else {
               locked.add(path);
            }
         }
      }

      return locked;
   }

   private static boolean isInside(String path, Set<String> locked) {
      if (locked.isEmpty()) {
         return false;
      } else {
         for (int dot = path.indexOf(46); dot >= 0; dot = path.indexOf(46, dot + 1)) {
            if (locked.contains(path.substring(0, dot))) {
               return true;
            }
         }

         return false;
      }
   }

   @FunctionalInterface
   public interface CustomMigration {
      boolean apply(YamlConfiguration var1, YamlConfiguration var2);
   }

   public record OwnedSection(YamlMigrator.SectionMatcher matcher, YamlMigrator.Ownership ownership) {
      public static YamlMigrator.OwnedSection curated(YamlMigrator.SectionMatcher matcher) {
         return new YamlMigrator.OwnedSection(matcher, YamlMigrator.Ownership.CURATED);
      }

      public static YamlMigrator.OwnedSection restoredWhenAbsent(YamlMigrator.SectionMatcher matcher) {
         return new YamlMigrator.OwnedSection(matcher, YamlMigrator.Ownership.RESTORED_WHEN_ABSENT);
      }

      public static YamlMigrator.OwnedSection userManaged(YamlMigrator.SectionMatcher matcher) {
         return new YamlMigrator.OwnedSection(matcher, YamlMigrator.Ownership.USER_MANAGED);
      }
   }

   public static enum Ownership {
      CURATED,
      RESTORED_WHEN_ABSENT,
      USER_MANAGED;
   }

   public record Rename(String oldPath, String newPath) {
   }

   @FunctionalInterface
   public interface SectionMatcher {
      boolean matches(YamlConfiguration var1, String var2);
   }
}
