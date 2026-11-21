package com.magmaguy.elitemobs.magmacore.config;

import com.magmaguy.elitemobs.magmacore.MagmaCore;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.shaded.reflections.Reflections;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class CustomConfig {
   private final HashMap<String, CustomConfigFields> customConfigFieldsHashMap = new HashMap<>();
   private final List<CustomConfigFields> customConfigFieldsArrayList = new ArrayList<>();
   private final String folderName;
   private final Class<? extends CustomConfigFields> customConfigFields;
   private final CustomConfigInheritancePolicy inheritancePolicy;

   public CustomConfig(String folderName, Class<? extends CustomConfigFields> customConfigFields, CustomConfigFields schematicConfigField) {
      this.folderName = folderName;
      this.customConfigFields = customConfigFields;
      this.inheritancePolicy = null;
      this.initialize(schematicConfigField);
   }

   public CustomConfig(String folderName, Class<? extends CustomConfigFields> customConfigFields) {
      this.folderName = folderName;
      this.customConfigFields = customConfigFields;
      this.inheritancePolicy = null;
   }

   public CustomConfig(String folderName, String packageName, Class<? extends CustomConfigFields> customConfigFields) {
      this(folderName, packageName, customConfigFields, null);
   }

   public CustomConfig(
      String folderName, String packageName, Class<? extends CustomConfigFields> customConfigFields, CustomConfigInheritancePolicy inheritancePolicy
   ) {
      this.folderName = folderName;
      this.customConfigFields = customConfigFields;
      this.inheritancePolicy = inheritancePolicy;
      String directory = MagmaCore.getInstance().getRequestingPlugin().getDataFolder().getAbsolutePath() + File.separatorChar + folderName;
      File file = Path.of(directory).toFile();
      if (!file.exists()) {
         file.mkdir();
      }

      if (!packageName.isEmpty()) {
         Reflections reflections = new Reflections(packageName);

         try {
            Set<Class> classSet = new HashSet<>(reflections.getSubTypesOf(customConfigFields));
            classSet.forEach(aClass -> {
               if (!Modifier.isAbstract(aClass.getModifiers())) {
                  try {
                     this.customConfigFieldsArrayList.add((CustomConfigFields)aClass.getDeclaredConstructor().newInstance());
                  } catch (Exception var4x) {
                     Logger.warn("Failed to generate plugin default classes for " + folderName + " ! This is very bad, warn the developer!");
                     var4x.printStackTrace();
                  }
               }
            });
         } catch (Exception var13) {
         }

         this.customConfigFieldsArrayList.removeIf(fields -> !this.ownsFilename(fields.getFilename()));
         Map<String, CustomConfigFields> defaultOwners = new HashMap<>();

         for (CustomConfigFields fields : this.customConfigFieldsArrayList) {
            CustomConfigFields previous = defaultOwners.putIfAbsent(normalizeFilename(fields.getFilename()), fields);
            if (previous != null) {
               throw new IllegalStateException(
                  "Conflicting defaults for "
                     + folderName
                     + "/"
                     + fields.getFilename()
                     + ": "
                     + previous.getClass().getName()
                     + " and "
                     + fields.getClass().getName()
               );
            }
         }

         try {
            if (!Files.isDirectory(Paths.get(MagmaCore.getInstance().getRequestingPlugin().getDataFolder().getPath() + File.separatorChar + folderName))) {
               this.generateFreshConfigurations();
               return;
            }
         } catch (Exception var14) {
            Logger.warn("Failed to generate plugin default files for " + folderName + " ! This is very bad, warn the developer!");
            var14.printStackTrace();
            return;
         }

         if (inheritancePolicy != null) {
            this.initializeInheritanceAware(file, defaultOwners);
         } else {
            for (File selected : ContentFileSelector.select(this.collectYamlFiles(file))) {
               this.fileInitializer(selected);
            }

            try {
               if (!this.customConfigFieldsArrayList.isEmpty()) {
                  this.generateFreshConfigurations();
               }
            } catch (Exception var12) {
               Logger.warn("Failed to finish generating default plugin files for " + folderName + " ! This is very bad, warn the developer!");
               var12.printStackTrace();
            }
         }
      }
   }

   public synchronized CustomConfigFields registerFile(File file) {
      Objects.requireNonNull(file, "file");
      if (!this.ownsFilename(file.getName())) {
         throw new IllegalArgumentException("Configuration belongs to another owner: " + file);
      } else if (!file.getName().toLowerCase(Locale.ROOT).endsWith(".yml")) {
         throw new IllegalArgumentException("Custom configuration files must end in .yml");
      } else {
         Path root = this.configurationDirectory().toPath().toAbsolutePath().normalize();
         Path candidate = file.toPath().toAbsolutePath().normalize();
         if (!candidate.startsWith(root)) {
            throw new IllegalArgumentException("Configuration file is outside " + root);
         } else {
            List<File> candidates = new ArrayList<>(this.collectYamlFiles(this.configurationDirectory()));
            if (!candidates.contains(file)) {
               candidates.add(file);
            }

            if (this.inheritancePolicy == null) {
               File selected = ContentFileSelector.select(candidates)
                  .stream()
                  .filter(source -> source.getName().equals(file.getName()))
                  .findFirst()
                  .orElseThrow();
               if (candidate.equals(selected.toPath().toAbsolutePath().normalize()) || !this.isLoadedFrom(selected)) {
                  this.initialize(selected);
               }

               return this.customConfigFieldsHashMap.get(selected.getName());
            } else {
               CustomConfig.InheritanceResolver resolver = new CustomConfig.InheritanceResolver(candidates);
               File selected = resolver.fileFor(normalizeFilename(file.getName()));
               this.customConfigFieldsHashMap
                  .keySet()
                  .removeIf(name -> !name.equals(selected.getName()) && normalizeFilename(name).equals(normalizeFilename(selected.getName())));
               if (candidate.equals(selected.toPath().toAbsolutePath().normalize()) || !this.isLoadedFrom(selected)) {
                  this.initializeResolved(selected, null, resolver);
               }

               return this.customConfigFieldsHashMap.get(selected.getName());
            }
         }
      }
   }

   private boolean isLoadedFrom(File source) {
      CustomConfigFields loaded = this.customConfigFieldsHashMap.get(source.getName());
      return loaded != null
         && loaded.getFile() != null
         && loaded.getFile().toPath().toAbsolutePath().normalize().equals(source.toPath().toAbsolutePath().normalize());
   }

   private void initializeInheritanceAware(File directory, Map<String, CustomConfigFields> premades) {
      List<File> existingFiles = this.collectYamlFiles(directory);
      Set<String> existingNames = new HashSet<>();

      for (File existingFile : existingFiles) {
         existingNames.add(normalizeFilename(existingFile.getName()));
      }

      Set<String> alreadyInitialized = new HashSet<>();

      for (Entry<String, CustomConfigFields> entry : premades.entrySet()) {
         if (!existingNames.contains(entry.getKey())) {
            this.initialize(entry.getValue());
            alreadyInitialized.add(entry.getKey());
         }
      }

      List<File> files = this.collectYamlFiles(directory);
      CustomConfig.InheritanceResolver resolver = new CustomConfig.InheritanceResolver(files);

      for (File file : files) {
         String key = normalizeFilename(file.getName());
         if (!alreadyInitialized.contains(key) && resolver.fileFor(key) == file) {
            this.initializeResolved(file, premades.get(key), resolver);
         }
      }

      this.customConfigFieldsArrayList.clear();
   }

   private void initializeResolved(File file, CustomConfigFields premade, CustomConfig.InheritanceResolver resolver) {
      CustomConfigFields fields = premade;

      try {
         if (fields == null) {
            Constructor<?> constructor = this.customConfigFields.getConstructor(String.class, boolean.class);
            fields = (CustomConfigFields)constructor.newInstance(file.getName(), true);
         }

         CustomConfig.InheritanceResolver.ResolvedConfiguration resolved = resolver.resolve(file);
         CustomConfig.LoadedState before = resolved.inherited() ? null : CustomConfig.LoadedState.capture(resolved.rawConfiguration());
         fields.setFile(file);
         if (resolved.inherited()) {
            fields.beginInheritedRead(resolved.readConfiguration(), resolved.rawConfiguration());
         } else {
            fields.setFileConfiguration(resolved.rawConfiguration());
         }

         fields.processConfigFields();
         if (!resolved.inherited()) {
            saveChanges(resolved.rawConfiguration(), file, before);
         }

         this.addCustomConfigFields(file.getName(), fields);
      } catch (Exception var8) {
         Logger.warn("Disabled inherited configuration " + file.getName() + ": " + rootMessage(var8));

         try {
            if (fields == null) {
               Constructor<?> constructor = this.customConfigFields.getConstructor(String.class, boolean.class);
               fields = (CustomConfigFields)constructor.newInstance(file.getName(), false);
            }

            fields.setFile(file);
            fields.setFileConfiguration(YamlConfiguration.loadConfiguration(file));
            fields.setEnabled(false);
            this.addCustomConfigFields(file.getName(), fields);
         } catch (Exception var7) {
            Logger.warn("Could not retain disabled configuration " + file.getName() + ": " + rootMessage(var7));
         }
      }
   }

   private File configurationDirectory() {
      return Path.of(MagmaCore.getInstance().getRequestingPlugin().getDataFolder().getPath(), this.folderName).toFile();
   }

   private List<File> collectYamlFiles(File directory) {
      if (directory != null && directory.isDirectory()) {
         try {
            List var3;
            try (Stream<Path> paths = Files.walk(directory.toPath())) {
               var3 = paths.filter(x$0 -> Files.isRegularFile(x$0))
                  .map(Path::toFile)
                  .filter(file -> this.ownsFilename(file.getName()))
                  .filter(file -> file.getName().toLowerCase(Locale.ROOT).endsWith(".yml"))
                  .sorted(Comparator.comparing(File::getAbsolutePath, String.CASE_INSENSITIVE_ORDER))
                  .toList();
            }

            return var3;
         } catch (Exception var7) {
            Logger.warn("Failed to enumerate inherited configurations in " + directory + ": " + rootMessage(var7));
            return List.of();
         }
      } else {
         return List.of();
      }
   }

   private static String normalizeFilename(String filename) {
      String normalized = filename.trim().toLowerCase(Locale.ROOT);
      return normalized.endsWith(".yml") ? normalized : normalized + ".yml";
   }

   private static String rootMessage(Throwable throwable) {
      Throwable current = throwable;

      while (current.getCause() != null) {
         current = current.getCause();
      }

      return current.getMessage() == null ? current.getClass().getSimpleName() : current.getMessage();
   }

   private void fileInitializer(File file) {
      if (this.ownsFilename(file.getName())) {
         Iterator<CustomConfigFields> iterator = this.customConfigFieldsArrayList.iterator();

         while (iterator.hasNext()) {
            CustomConfigFields premade = iterator.next();
            if (file.getName().equalsIgnoreCase(premade.getFilename())) {
               iterator.remove();

               try {
                  this.initialize(premade, file);
               } catch (Exception var5) {
                  Logger.warn("Failed to read plugin files for " + this.folderName + " ! This is very bad, warn the developer!");
                  var5.printStackTrace();
               }

               return;
            }
         }

         this.initialize(file);
      }
   }

   public HashMap<String, ? extends CustomConfigFields> getCustomConfigFieldsHashMap() {
      return this.customConfigFieldsHashMap;
   }

   public <V extends CustomConfigFields> void addCustomConfigFields(String filename, CustomConfigFields customConfigFields) {
      this.customConfigFieldsHashMap.put(filename, customConfigFields);
   }

   private void generateFreshConfigurations() {
      for (Object customConfigFields : this.customConfigFieldsArrayList) {
         this.initialize((CustomConfigFields)customConfigFields);
      }
   }

   private void initialize(CustomConfigFields customConfigFields) {
      File file = ConfigurationEngine.fileCreator(this.folderName, customConfigFields.getFilename());
      this.initialize(customConfigFields, file);
   }

   protected boolean ownsFilename(String filename) {
      return true;
   }

   private void initialize(CustomConfigFields customConfigFields, File file) {
      FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
      if (fileConfiguration == null) {
         throw new UncheckedIOException(new IOException("Could not load configuration file " + file));
      } else {
         CustomConfig.LoadedState before = CustomConfig.LoadedState.capture(fileConfiguration);
         customConfigFields.setFile(file);
         customConfigFields.setFileConfiguration(fileConfiguration);
         customConfigFields.processConfigFields();
         saveChanges(fileConfiguration, file, before);
         this.addCustomConfigFields(file.getName(), customConfigFields);
      }
   }

   private void initialize(File file) {
      try {
         if (!file.getName().endsWith(".yml")) {
            return;
         }

         FileConfiguration fileConfiguration = ConfigurationEngine.fileConfigurationCreator(file);
         CustomConfig.LoadedState before = CustomConfig.LoadedState.capture(fileConfiguration);
         Constructor<?> constructor = this.customConfigFields.getConstructor(String.class, boolean.class);
         CustomConfigFields instancedCustomConfigFields = (CustomConfigFields)constructor.newInstance(file.getName(), true);
         instancedCustomConfigFields.setFileConfiguration(fileConfiguration);
         instancedCustomConfigFields.setFile(file);
         instancedCustomConfigFields.processConfigFields();
         saveChanges(fileConfiguration, file, before);
         this.addCustomConfigFields(file.getName(), instancedCustomConfigFields);
      } catch (Exception var6) {
         Logger.warn("Bad constructor for file " + file.getName() + " ! You should probably delete that file.");
         var6.printStackTrace();
      }
   }

   private static void saveChanges(FileConfiguration configuration, File file, CustomConfig.LoadedState before) {
      configuration.options().copyDefaults(true);
      if (!before.equals(CustomConfig.LoadedState.capture(configuration))) {
         ConfigurationEngine.fileSaverCustomValues(configuration, file);
      }
   }

   private final class InheritanceResolver {
      private final Map<String, File> files = new HashMap<>();
      private final Map<String, CustomConfig.InheritanceResolver.ResolvedConfiguration> resolved = new HashMap<>();
      private final LinkedHashSet<String> resolving = new LinkedHashSet<>();

      private InheritanceResolver(List<File> sourceFiles) {
         for (File sourceFile : ContentFileSelector.select(sourceFiles, CustomConfig::normalizeFilename)) {
            this.files.put(CustomConfig.normalizeFilename(sourceFile.getName()), sourceFile);
         }
      }

      private File fileFor(String normalizedFilename) {
         return this.files.get(normalizedFilename);
      }

      private CustomConfig.InheritanceResolver.ResolvedConfiguration resolve(File file) {
         String key = CustomConfig.normalizeFilename(file.getName());
         CustomConfig.InheritanceResolver.ResolvedConfiguration cached = this.resolved.get(key);
         if (cached != null) {
            return cached;
         } else if (!this.resolving.add(key)) {
            List<String> cycle = new ArrayList<>(this.resolving);
            cycle.add(key);
            throw new IllegalArgumentException("extends cycle: " + String.join(" -> ", cycle));
         } else {
            CustomConfig.InheritanceResolver.ResolvedConfiguration var11;
            try {
               FileConfiguration raw = Objects.requireNonNull(ConfigurationEngine.fileConfigurationCreator(file), "Failed to read configuration " + file);
               String parent = raw.getString(CustomConfig.this.inheritancePolicy.parentKey());
               if (parent == null || parent.isBlank()) {
                  CustomConfig.InheritanceResolver.ResolvedConfiguration result = new CustomConfig.InheritanceResolver.ResolvedConfiguration(raw, raw, false);
                  this.resolved.put(key, result);
                  return result;
               }

               if (parent.contains("/") || parent.contains("\\")) {
                  throw new IllegalArgumentException("extends must name a file in the same configuration catalog");
               }

               String parentKey = CustomConfig.normalizeFilename(parent);
               File parentFile = this.files.get(parentKey);
               if (parentFile == null) {
                  throw new IllegalArgumentException("missing extends base " + parentKey);
               }

               CustomConfig.InheritanceResolver.ResolvedConfiguration base = this.resolve(parentFile);
               YamlConfiguration merged = new YamlConfiguration();
               this.copyLeaves(base.readConfiguration(), merged, true);
               this.copyLeaves(raw, merged, false);
               CustomConfig.InheritanceResolver.ResolvedConfiguration result = new CustomConfig.InheritanceResolver.ResolvedConfiguration(merged, raw, true);
               this.resolved.put(key, result);
               var11 = result;
            } finally {
               this.resolving.remove(key);
            }

            return var11;
         }
      }

      private void copyLeaves(FileConfiguration source, YamlConfiguration target, boolean inheritedValues) {
         for (Entry<String, Object> entry : source.getValues(true).entrySet()) {
            String path = entry.getKey();
            Object value = entry.getValue();
            if (!(value instanceof ConfigurationSection)) {
               if (path.equalsIgnoreCase(CustomConfig.this.inheritancePolicy.parentKey())) {
                  if (!inheritedValues) {
                     target.set(path, value);
                  }
               } else if (!inheritedValues || CustomConfig.this.inheritancePolicy.mayInherit(path)) {
                  target.set(path, value);
               }
            }
         }
      }

      private record ResolvedConfiguration(FileConfiguration readConfiguration, FileConfiguration rawConfiguration, boolean inherited) {
      }
   }

   private record LoadedState(
      Object values, Map<String, List<String>> comments, Map<String, List<String>> inlineComments, List<String> header, List<String> footer
   ) {
      private static CustomConfig.LoadedState capture(FileConfiguration configuration) {
         Map<String, List<String>> comments = new HashMap<>();
         Map<String, List<String>> inlineComments = new HashMap<>();

         for (String key : configuration.getKeys(true)) {
            List<String> block = configuration.getComments(key);
            List<String> inline = configuration.getInlineComments(key);
            if (!block.isEmpty()) {
               comments.put(key, new ArrayList<>(block));
            }

            if (!inline.isEmpty()) {
               inlineComments.put(key, new ArrayList<>(inline));
            }
         }

         return new CustomConfig.LoadedState(
            copyValue(configuration),
            comments,
            inlineComments,
            new ArrayList<>(configuration.options().getHeader()),
            new ArrayList<>(configuration.options().getFooter())
         );
      }

      private static Object copyValue(Object value) {
         if (value instanceof ConfigurationSection section) {
            return copyValue(section.getValues(false));
         } else if (value instanceof Map<?, ?> map) {
            Map<Object, Object> copy = new LinkedHashMap<>();
            map.forEach((key, entry) -> copy.put(key, copyValue(entry)));
            return copy;
         } else if (value instanceof List<?> list) {
            List<Object> copy = new ArrayList<>(list.size());
            list.forEach(entry -> copy.add(copyValue(entry)));
            return copy;
         } else {
            return value instanceof ConfigurationSerializable serializable
               ? new CustomConfig.LoadedState.SerializedValue(value.getClass(), copyValue(serializable.serialize()))
               : value;
         }
      }

      private record SerializedValue(Class<?> type, Object values) {
      }
   }
}
