package ac.grim.grimac.manager.config.update;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.shaded.snakeyaml.Yaml;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ConfigUpdater {
   private final Class<?> resourceAnchor;
   private final Logger logger;

   public ConfigUpdater(@NotNull Class<?> resourceAnchor, @NotNull Logger logger) {
      this.resourceAnchor = Objects.requireNonNull(resourceAnchor, "resourceAnchor");
      this.logger = Objects.requireNonNull(logger, "logger");
   }

   @NotNull
   public ConfigUpdater.Result update(@NotNull File configFile, @NotNull ConfigUpdater.Spec spec) throws IOException {
      Map<File, ConfigUpdater.Spec> single = new LinkedHashMap<>();
      single.put(configFile, spec);
      Map<File, ConfigUpdater.Result> results = this.updateAll(single, "en");
      return results.get(configFile);
   }

   @NotNull
   public Map<File, ConfigUpdater.Result> updateAll(@NotNull Map<File, ConfigUpdater.Spec> specs, @NotNull String langCode) throws IOException {
      Map<String, List<WriteLog.Entry>> pendingCrossFile = new LinkedHashMap<>();
      Map<String, Map<String, Object>> siblingReadViews = new LinkedHashMap<>();
      Map<File, ConfigUpdater.Result> results = new LinkedHashMap<>();
      Map<String, File> filesByName = new LinkedHashMap<>();

      for (File f : specs.keySet()) {
         filesByName.put(f.getName(), f);
      }

      for (Entry<File, ConfigUpdater.Spec> entry : specs.entrySet()) {
         File configFile = entry.getKey();
         ConfigUpdater.Spec spec = entry.getValue();
         results.put(configFile, this.updateSingle(configFile, spec, langCode, filesByName, siblingReadViews, pendingCrossFile));
      }

      for (Entry<String, List<WriteLog.Entry>> q : pendingCrossFile.entrySet()) {
         File sibling = filesByName.get(q.getKey());
         if (sibling != null && sibling.exists()) {
            try {
               this.applyEntries(sibling, q.getValue());
            } catch (IOException var11) {
               this.logger.log(Level.WARNING, "[grim-config-updater] failed to flush cross-file writes into " + sibling.getName(), (Throwable)var11);
            }
         } else {
            this.logger
               .warning(
                  "[grim-config-updater] queued cross-file write to '"
                     + q.getKey()
                     + "' has no resolvable file in this batch — dropping "
                     + q.getValue().size()
                     + " op(s)"
               );
         }
      }

      return results;
   }

   @NotNull
   private ConfigUpdater.Result updateSingle(
      @NotNull File configFile,
      @NotNull ConfigUpdater.Spec spec,
      @NotNull String langCode,
      @NotNull Map<String, File> filesByName,
      @NotNull Map<String, Map<String, Object>> siblingReadViews,
      @NotNull Map<String, List<WriteLog.Entry>> pendingCrossFile
   ) throws IOException {
      if (!configFile.exists()) {
         return new ConfigUpdater.Result(false, -1, spec.latestVersion, null);
      } else {
         Map<String, Object> oldData = readYaml(configFile.toPath());
         if (oldData == null) {
            return new ConfigUpdater.Result(false, -1, spec.latestVersion, "couldn't parse " + configFile.getName());
         } else {
            ConfigUpdater.ConfigFlavor onDiskFlavor = ConfigUpdater.ConfigFlavor.parse(oldData.get("config-flavor"));
            if (onDiskFlavor != null && onDiskFlavor != spec.flavor) {
               String warning = configFile.getName()
                  + " has config-flavor="
                  + onDiskFlavor
                  + " but this build is "
                  + spec.flavor
                  + "; refusing to migrate. Move/rename the file and let the bundled default be written fresh.";
               this.logger.log(Level.SEVERE, "[grim-config-updater] " + warning);
               return new ConfigUpdater.Result(false, -1, spec.latestVersion, warning);
            } else {
               int oldVersion = parseVersion(oldData.get("config-version"));
               if (oldVersion >= spec.latestVersion && onDiskFlavor != null) {
                  return new ConfigUpdater.Result(false, oldVersion, spec.latestVersion, null);
               } else {
                  Path backup = configFile.toPath().resolveSibling(configFile.getName() + ".v" + oldVersion + ".bak");
                  if (!Files.exists(backup)) {
                     Files.copy(configFile.toPath(), backup);
                  }

                  String resolvedResource = this.resolveBundledDefaultPath(spec, langCode);

                  try (InputStream defaultStream = this.openResource(resolvedResource)) {
                     Files.copy(defaultStream, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                  }

                  Map<String, Object> newData = readYaml(configFile.toPath());
                  if (newData == null) {
                     String warning = "bundled default " + resolvedResource + " is unparseable; backup at " + backup;
                     this.logger.log(Level.SEVERE, "[grim-config-updater] " + warning);
                     return new ConfigUpdater.Result(false, oldVersion, spec.latestVersion, warning);
                  } else {
                     WriteLog ownLog = WriteLog.active();
                     YamlMap inputView = new YamlMapImpl(oldData, WriteLog.noop());
                     YamlMap outputView = new YamlMapImpl(newData, ownLog);
                     autoLift("", oldData, newData, outputView);

                     for (int v = oldVersion + 1; v <= spec.latestVersion; v++) {
                        ConfigUpdater.Migration m = spec.migrations.get(v);
                        if (m != null) {
                           MigrationContext ctx = new ConfigUpdater.ContextImpl(
                              inputView, outputView, filesByName, siblingReadViews, pendingCrossFile, this.logger
                           );

                           try {
                              m.apply(ctx);
                           } catch (RuntimeException var21) {
                              this.logger
                                 .log(
                                    Level.WARNING,
                                    "[grim-config-updater] migration to v"
                                       + v
                                       + " failed for "
                                       + configFile.getName()
                                       + " — partial state may have been applied",
                                    (Throwable)var21
                                 );
                           }
                        }
                     }

                     outputView.put("config-version", spec.latestVersion);
                     outputView.put("config-flavor", spec.flavor.name());
                     this.applyEntries(
                        configFile,
                        ownLog.finalState()
                           .entrySet()
                           .stream()
                           .map(e -> new WriteLog.Entry(e.getValue() == null ? WriteLog.Op.REMOVE : WriteLog.Op.PUT, e.getKey(), e.getValue()))
                           .toList()
                     );
                     this.logger
                        .info(
                           "[grim-config-updater] "
                              + configFile.getName()
                              + " migrated v"
                              + oldVersion
                              + " → v"
                              + spec.latestVersion
                              + " (flavor "
                              + spec.flavor
                              + ", backup at "
                              + backup.getFileName()
                              + ")"
                        );
                     return new ConfigUpdater.Result(true, oldVersion, spec.latestVersion, null);
                  }
               }
            }
         }
      }
   }

   private static void autoLift(String pathPrefix, Map<String, Object> oldData, Map<String, Object> newData, YamlMap output) {
      for (Entry<String, Object> entry : oldData.entrySet()) {
         String key = entry.getKey();
         if (newData.containsKey(key)) {
            String currentPath = pathPrefix.isEmpty() ? key : pathPrefix + "." + key;
            Object oldValue = entry.getValue();
            Object newValue = newData.get(key);
            if (oldValue instanceof Map && newValue instanceof Map) {
               autoLift(currentPath, (Map<String, Object>)oldValue, (Map<String, Object>)newValue, output);
            } else if (!Objects.equals(oldValue, newValue)) {
               output.put(currentPath, oldValue);
            }
         }
      }
   }

   private void applyEntries(File configFile, List<WriteLog.Entry> entries) throws IOException {
      if (!entries.isEmpty()) {
         ConfigPatcher patcher = new ConfigPatcher(configFile);
         List<ConfigPatcher.PendingChange> pending = new ArrayList<>();

         for (WriteLog.Entry e : entries) {
            if (e.op() == WriteLog.Op.REMOVE) {
               this.logger
                  .log(
                     Level.FINE,
                     "[grim-config-updater] REMOVE op for '"
                        + e.path()
                        + "' on "
                        + configFile.getName()
                        + " is not yet supported; expected the bundled default to drop the key"
                  );
            } else {
               ConfigPatcher.NodePosition pos = patcher.getNodePosition(e.path());
               if (pos == null) {
                  this.logger.log(Level.FINE, "[grim-config-updater] no slot for migrated path '" + e.path() + "' in " + configFile.getName() + "; dropping");
               } else {
                  pending.add(new ConfigPatcher.PendingChange(pos, e.value()));
               }
            }
         }

         Collections.sort(pending);

         for (ConfigPatcher.PendingChange c : pending) {
            patcher.applyChange(c);
         }

         patcher.save();
      }
   }

   @Nullable
   private static Map<String, Object> readYaml(Path file) {
      try {
         byte[] bytes = Files.readAllBytes(file);
         if (bytes.length == 0) {
            return null;
         } else {
            Object loaded = new Yaml().load(new String(bytes, StandardCharsets.UTF_8));
            return loaded instanceof Map ? (Map)loaded : null;
         }
      } catch (Exception var3) {
         return null;
      }
   }

   private InputStream openResource(String resourcePath) throws IOException {
      InputStream in = this.resourceAnchor.getResourceAsStream(resourcePath);
      if (in == null) {
         throw new IOException("bundled default not found at " + resourcePath);
      } else {
         return in;
      }
   }

   @NotNull
   private String resolveBundledDefaultPath(@NotNull ConfigUpdater.Spec spec, @NotNull String langCode) {
      String lang = langCode.toLowerCase();
      if (!lang.isEmpty() && !lang.equals("en")) {
         String langPath = spec.resourceDirectory + lang + ".yml";
         if (this.resourceAnchor.getResource(langPath) != null) {
            return langPath;
         }
      }

      return spec.resourceDirectory + "en.yml";
   }

   private static int parseVersion(@Nullable Object raw) {
      if (raw == null) {
         return 0;
      } else if (raw instanceof Number n) {
         return n.intValue();
      } else {
         try {
            return Integer.parseInt(raw.toString().trim());
         } catch (NumberFormatException var2) {
            return 0;
         }
      }
   }

   public static enum ConfigFlavor {
      V2,
      V3;

      @Nullable
      public static ConfigUpdater.ConfigFlavor parse(@Nullable Object raw) {
         if (raw == null) {
            return null;
         } else {
            String s = raw.toString().trim().toUpperCase();

            try {
               return valueOf(s);
            } catch (IllegalArgumentException var3) {
               return null;
            }
         }
      }
   }

   private static final class ContextImpl implements MigrationContext {
      private final YamlMap input;
      private final YamlMap output;
      private final Map<String, File> filesByName;
      private final Map<String, Map<String, Object>> siblingReadViews;
      private final Map<String, List<WriteLog.Entry>> pendingCrossFile;
      private final Logger logger;

      ContextImpl(
         YamlMap input,
         YamlMap output,
         Map<String, File> filesByName,
         Map<String, Map<String, Object>> siblingReadViews,
         Map<String, List<WriteLog.Entry>> pendingCrossFile,
         Logger logger
      ) {
         this.input = input;
         this.output = output;
         this.filesByName = filesByName;
         this.siblingReadViews = siblingReadViews;
         this.pendingCrossFile = pendingCrossFile;
         this.logger = logger;
      }

      @NotNull
      @Override
      public YamlMap input() {
         return this.input;
      }

      @NotNull
      @Override
      public YamlMap output() {
         return this.output;
      }

      @NotNull
      @Override
      public YamlMap otherFile(@NotNull String fileName) {
         File f = this.filesByName.get(fileName);
         Map<String, Object> view = this.siblingReadViews.computeIfAbsent(fileName, n -> {
            if (f != null && f.exists()) {
               Map<String, Object> parsed = ConfigUpdater.readYaml(f.toPath());
               return (Map<String, Object>)(parsed == null ? new LinkedHashMap<>() : parsed);
            } else {
               return new LinkedHashMap<>();
            }
         });
         WriteLog log = WriteLog.sibling(fileName, this.pendingCrossFile, this.logger);
         return new YamlMapImpl(view, log);
      }
   }

   @FunctionalInterface
   public interface Migration {
      void apply(@NotNull MigrationContext var1);
   }

   public static final class Result {
      public final boolean migrated;
      public final int oldVersion;
      public final int newVersion;
      @Nullable
      public final String warning;

      Result(boolean migrated, int oldVersion, int newVersion, @Nullable String warning) {
         this.migrated = migrated;
         this.oldVersion = oldVersion;
         this.newVersion = newVersion;
         this.warning = warning;
      }
   }

   public static final class Spec {
      public final String resourceDirectory;
      public final int latestVersion;
      @NotNull
      public final ConfigUpdater.ConfigFlavor flavor;
      @NotNull
      public final Map<Integer, ConfigUpdater.Migration> migrations;

      Spec(
         @NotNull String resourceDirectory,
         int latestVersion,
         @NotNull ConfigUpdater.ConfigFlavor flavor,
         @NotNull Map<Integer, ConfigUpdater.Migration> migrations
      ) {
         Objects.requireNonNull(resourceDirectory, "resourceDirectory");
         this.resourceDirectory = resourceDirectory.endsWith("/") ? resourceDirectory : resourceDirectory + "/";
         this.latestVersion = latestVersion;
         this.flavor = Objects.requireNonNull(flavor, "flavor");
         this.migrations = Map.copyOf(migrations);
      }

      @NotNull
      public static ConfigUpdater.Spec.Builder builder(@NotNull String resourceDirectory, int latestVersion, @NotNull ConfigUpdater.ConfigFlavor flavor) {
         return new ConfigUpdater.Spec.Builder(resourceDirectory, latestVersion, flavor);
      }

      public static final class Builder {
         private final String resourceDirectory;
         private final int latestVersion;
         private final ConfigUpdater.ConfigFlavor flavor;
         private final Map<Integer, ConfigUpdater.Migration> migrations = new LinkedHashMap<>();

         private Builder(String resourceDirectory, int latestVersion, ConfigUpdater.ConfigFlavor flavor) {
            this.resourceDirectory = resourceDirectory;
            this.latestVersion = latestVersion;
            this.flavor = flavor;
         }

         @NotNull
         public ConfigUpdater.Spec.Builder migration(int toVersion, @NotNull ConfigUpdater.Migration m) {
            this.migrations.put(toVersion, m);
            return this;
         }

         @NotNull
         public ConfigUpdater.Spec build() {
            return new ConfigUpdater.Spec(this.resourceDirectory, this.latestVersion, this.flavor, this.migrations);
         }
      }
   }
}
