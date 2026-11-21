package com.magmaguy.elitemobs.magmacore.enchantments;

import com.magmaguy.elitemobs.magmacore.config.ContentFileSelector;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptDefinition;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptHook;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptProvider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Predicate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class EnchantmentCatalog {
   private static final Set<String> FIELDS = Set.of(
      "isEnabled",
      "name",
      "description",
      "maxLevel",
      "curse",
      "validSlots",
      "itemTypes",
      "attackKinds",
      "requires",
      "conflicts",
      "stacking",
      "script",
      "parameters"
   );
   private static final long MAX_SOURCE_BYTES = 262144L;
   private final String namespace;
   private final Map<String, EnchantmentDefinition> definitions;
   private final Map<String, ScriptDefinition> scripts;

   private EnchantmentCatalog(String namespace, Map<String, EnchantmentDefinition> definitions, Map<String, ScriptDefinition> scripts) {
      this.namespace = namespace;
      this.definitions = Map.copyOf(definitions);
      this.scripts = Map.copyOf(scripts);
   }

   public Map<String, EnchantmentDefinition> definitions() {
      return this.definitions;
   }

   public String namespace() {
      return this.namespace;
   }

   public Optional<ScriptDefinition> script(String id) {
      return Optional.ofNullable(this.scripts.get(id));
   }

   public static EnchantmentCatalog load(String namespace, Path directory, Set<ScriptHook> supportedHooks) throws IOException {
      return load(namespace, directory, supportedHooks, path -> true);
   }

   public static EnchantmentCatalog load(final String namespace, Path directory, Set<ScriptHook> supportedHooks, Predicate<Path> ownsDefinition) throws IOException {
      EnchantmentDefinition.requireId(namespace + ":catalog");
      if (namespace.equals("minecraft")) {
         throw new IllegalArgumentException("Custom catalogs cannot own minecraft");
      } else {
         final Path root = directory.toRealPath();
         if (!Files.isDirectory(root)) {
            throw new IOException("Enchantment root is not a directory: " + root);
         } else {
            final Map<String, ScriptHook> hooks = new LinkedHashMap<>();

            for (ScriptHook hook : supportedHooks) {
               if (hooks.putIfAbsent(hook.getKey(), hook) != null) {
                  throw new IllegalArgumentException("Duplicate hook");
               }
            }

            ScriptProvider provider = new ScriptProvider() {
               @Override
               public String getNamespace() {
                  return namespace;
               }

               @Override
               public Path getScriptDirectory() {
                  return root;
               }

               @Override
               public ScriptHook resolveHook(String key) {
                  return hooks.get(key);
               }
            };
            Map<String, Path> selected = selectSources(root);
            Map<String, EnchantmentDefinition> definitions = new LinkedHashMap<>();
            Map<String, ScriptDefinition> scripts = new LinkedHashMap<>();
            Map<String, ScriptDefinition> validatedScripts = new LinkedHashMap<>();

            for (Entry<String, Path> entry : selected.entrySet()) {
               if (!entry.getKey().endsWith(".lua") && ownsDefinition.test(entry.getValue())) {
                  String filename = entry.getKey();
                  String id = namespace + ":" + filename.substring(0, filename.lastIndexOf(46));

                  try {
                     YamlConfiguration yaml = new YamlConfiguration();
                     yaml.loadFromString(readSource(root, entry.getValue()));
                     Set<String> unknown = new LinkedHashSet<>(yaml.getKeys(false));
                     unknown.removeAll(FIELDS);
                     if (!unknown.isEmpty()) {
                        throw new IllegalArgumentException("Unknown fields: " + unknown);
                     }

                     if (bool(yaml, "isEnabled", true)) {
                        EnchantmentDefinition definition = new EnchantmentDefinition(
                           id,
                           string(yaml, "name", null),
                           string(yaml, "description", ""),
                           integer(yaml, "maxLevel"),
                           bool(yaml, "curse", false),
                           enums(yaml, "validSlots", EnchantmentDefinition.Slot.class),
                           enums(yaml, "itemTypes", EnchantmentDefinition.ItemType.class),
                           strings(yaml, "attackKinds"),
                           strings(yaml, "requires"),
                           strings(yaml, "conflicts"),
                           stacking(yaml),
                           string(yaml, "script", null),
                           parameters(yaml)
                        );
                        if (definitions.putIfAbsent(id, definition) != null) {
                           throw new IllegalArgumentException("Both .yml and .yaml define the same identity: " + id);
                        }

                        ScriptDefinition script = validatedScripts.get(definition.script());
                        if (script == null) {
                           Path source = selected.get(definition.script());
                           if (source == null) {
                              throw new IllegalArgumentException("Missing selected script: " + definition.script());
                           }

                           script = ScriptDefinition.validate(definition.script(), source.toFile(), readSource(root, source), provider);
                           validatedScripts.put(definition.script(), script);
                        }

                        scripts.put(id, script);
                     }
                  } catch (RuntimeException | InvalidConfigurationException var20) {
                     throw new IOException("Invalid enchantment definition " + entry.getValue() + ": " + var20.getMessage(), var20);
                  }
               }
            }

            return new EnchantmentCatalog(namespace, definitions, scripts);
         }
      }
   }

   private static void requireContained(Path root, Path path) throws IOException {
      if (!path.toRealPath().startsWith(root)) {
         throw new IOException("Enchantment content escapes its root: " + path);
      }
   }

   public static void initializeDefaults(JavaPlugin plugin, Path directory, Collection<String> names) throws IOException {
      Files.createDirectories(directory);
      Path root = directory.toRealPath();
      Map<String, Path> selected = selectSources(root);

      for (String name : names) {
         String yamlName = name + ".yml";
         Path yamlPath = selected.getOrDefault(yamlName, selected.get(name + ".yaml"));
         if (yamlPath == null) {
            plugin.saveResource("enchantments/" + yamlName, false);
         } else {
            YamlConfiguration current = new YamlConfiguration();
            YamlConfiguration defaults = new YamlConfiguration();

            try (InputStream input = plugin.getResource("enchantments/" + yamlName)) {
               if (input == null) {
                  throw new IOException("Missing bundled enchantment " + yamlName);
               }

               current.loadFromString(readSource(root, yamlPath));
               defaults.load(new InputStreamReader(input, StandardCharsets.UTF_8));
               boolean missing = defaults.getKeys(true).stream().anyMatch(key -> !defaults.isConfigurationSection(key) && !current.contains(key));
               if (missing) {
                  current.setDefaults(defaults);
                  current.options().copyDefaults(true);
                  current.save(yamlPath.toFile());
               }
            } catch (InvalidConfigurationException var16) {
               throw new IOException("Invalid enchantment configuration " + yamlPath, var16);
            }
         }

         if (!selected.containsKey(name + ".lua")) {
            plugin.saveResource("enchantments/" + name + ".lua", false);
         }
      }
   }

   private static Map<String, Path> selectSources(final Path root) throws IOException {
      final List<File> candidates = new ArrayList<>();
      Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
         public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            EnchantmentCatalog.requireContained(root, dir);
            return FileVisitResult.CONTINUE;
         }

         public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            String name = file.getFileName().toString().toLowerCase(Locale.ROOT);
            if (name.endsWith(".yml") || name.endsWith(".yaml") || name.endsWith(".lua")) {
               EnchantmentCatalog.requireContained(root, file);
               if (!Files.isRegularFile(file)) {
                  throw new IOException("Not a regular content file: " + file);
               }

               candidates.add(file.toFile());
            }

            return FileVisitResult.CONTINUE;
         }
      });
      Map<String, Path> selected = new LinkedHashMap<>();

      for (File file : ContentFileSelector.select(candidates, name -> name.toLowerCase(Locale.ROOT))) {
         selected.put(file.getName().toLowerCase(Locale.ROOT), file.toPath());
      }

      return selected;
   }

   private static String readSource(Path root, Path path) throws IOException {
      requireContained(root, path);
      if (Files.size(path) > 262144L) {
         throw new IOException("Enchantment source exceeds 256 KiB: " + path);
      } else {
         String var4;
         try (InputStream stream = Files.newInputStream(path)) {
            byte[] bytes = stream.readNBytes(262145);
            if (bytes.length > 262144L) {
               throw new IOException("Enchantment source exceeds 256 KiB: " + path);
            }

            var4 = new String(bytes, StandardCharsets.UTF_8);
         }

         return var4;
      }
   }

   private static String string(ConfigurationSection section, String key, String fallback) {
      Object value = section.get(key);
      if (value == null && fallback != null) {
         return fallback;
      } else if (value instanceof String text) {
         return text;
      } else {
         throw new IllegalArgumentException(key + " must be a string");
      }
   }

   private static boolean bool(ConfigurationSection section, String key, boolean fallback) {
      Object value = section.get(key);
      if (value == null) {
         return fallback;
      } else if (value instanceof Boolean result) {
         return result;
      } else {
         throw new IllegalArgumentException(key + " must be a boolean");
      }
   }

   private static int integer(ConfigurationSection section, String key) {
      if (section.get(key) instanceof Integer value) {
         return value;
      } else {
         throw new IllegalArgumentException(key + " must be an integer");
      }
   }

   private static Set<String> strings(ConfigurationSection section, String key) {
      Object value = section.get(key);
      if (value == null) {
         return Set.of();
      } else if (value instanceof List<?> values && values.size() <= 128) {
         Set<String> result = new LinkedHashSet<>();
         Iterator var5 = values.iterator();

         while (var5.hasNext()) {
            if (!(var5.next() instanceof String text) || text.isBlank() || !result.add(text)) {
               throw new IllegalArgumentException(key + " must contain unique nonempty strings");
            }
         }

         return Set.copyOf(result);
      } else {
         throw new IllegalArgumentException(key + " must be a bounded list");
      }
   }

   private static <E extends Enum<E>> Set<E> enums(ConfigurationSection section, String key, Class<E> type) {
      Set<E> result = new LinkedHashSet<>();

      for (String name : strings(section, key)) {
         result.add(Enum.valueOf(type, name));
      }

      return Set.copyOf(result);
   }

   private static EnchantmentDefinition.Stacking stacking(ConfigurationSection section) {
      String var1 = string(section, "stacking", "source_item");

      return switch (var1) {
         case "source_item" -> EnchantmentDefinition.Stacking.SOURCE_ITEM;
         case "sum_equipped_levels" -> EnchantmentDefinition.Stacking.SUM_EQUIPPED_LEVELS;
         default -> throw new IllegalArgumentException("Unknown stacking policy");
      };
   }

   private static Map<String, Object> parameters(ConfigurationSection section) {
      Object raw = section.get("parameters");
      if (raw == null) {
         return Map.of();
      } else if (raw instanceof ConfigurationSection parameters) {
         return parameters.getValues(false);
      } else {
         throw new IllegalArgumentException("parameters must be a mapping");
      }
   }
}
