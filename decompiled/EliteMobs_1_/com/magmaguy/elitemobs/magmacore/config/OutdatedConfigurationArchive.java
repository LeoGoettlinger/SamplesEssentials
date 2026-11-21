package com.magmaguy.elitemobs.magmacore.config;

import com.magmaguy.elitemobs.magmacore.scripting.ScriptDefinition;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptHook;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.stream.Stream;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

public final class OutdatedConfigurationArchive {
   public static final String RULES_RESOURCE = "outdated-config-keys.yml";
   public static final String DIRECTORY = "outdated files";
   private static final int MAX_SOURCE_BYTES = 4194304;
   private static final Map<JavaPlugin, OutdatedConfigurationArchive.OutdatedConfigurationArchiveOwner> owners = new IdentityHashMap<>();

   private OutdatedConfigurationArchive() {
   }

   public static void register(JavaPlugin plugin) {
      if (!owners.containsKey(plugin)) {
         OutdatedConfigurationArchive.OutdatedConfigurationArchiveOwner owner = new OutdatedConfigurationArchive.OutdatedConfigurationArchiveOwner(plugin);
         Bukkit.getServicesManager().register(Runnable.class, owner, plugin, ServicePriority.Normal);
         owners.put(plugin, owner);
      }
   }

   public static void unregister(JavaPlugin plugin) {
      OutdatedConfigurationArchive.OutdatedConfigurationArchiveOwner owner = owners.remove(plugin);
      if (owner != null) {
         owner.active = false;
         Bukkit.getServicesManager().unregister(Runnable.class, owner);
      }
   }

   public static void archiveFor(JavaPlugin plugin) {
      Runnable selected = null;

      for (RegisteredServiceProvider<Runnable> service : Bukkit.getServicesManager().getRegistrations(Runnable.class)) {
         if (service.getPlugin() == plugin && ((Runnable)service.getProvider()).getClass().getSimpleName().equals("OutdatedConfigurationArchiveOwner")) {
            if (selected != null) {
               throw new IllegalStateException("Multiple configuration archive owners for " + plugin.getName());
            }

            selected = (Runnable)service.getProvider();
         }
      }

      if (selected != null) {
         selected.run();
      } else {
         try {
            try (InputStream resource = plugin.getResource("outdated-config-keys.yml")) {
               if (resource != null) {
                  throw new IOException(
                     "Configuration archive owner is unavailable for "
                        + plugin.getName()
                        + ". Update that plugin before importing content; its policy was not read."
                  );
               }
            }
         } catch (IOException var7) {
            throw new UncheckedIOException("Cannot safely archive outdated " + plugin.getName() + " configuration", var7);
         }
      }
   }

   static void archive(JavaPlugin plugin) {
      try {
         try (InputStream resource = plugin.getResource("outdated-config-keys.yml")) {
            if (resource == null) {
               return;
            }

            Map<String, Set<OutdatedConfigurationArchive.Rule>> rules = readRules(readBounded(resource));
            Path data = plugin.getDataFolder().toPath().toAbsolutePath().normalize();
            Path storage = data.getParent().resolve("MagmaCore").resolve("outdated files");
            List<Path> archived = archive(data, storage, rules);
            if (!archived.isEmpty()) {
               plugin.getLogger()
                  .warning(
                     "Archived "
                        + archived.size()
                        + " outdated configuration file(s) to "
                        + storage.resolve(data.getFileName())
                        + ". Their original contents were preserved. Download updated content to replace retired DLC files."
                  );
            }
         }
      } catch (IOException var8) {
         throw new UncheckedIOException("Cannot safely archive outdated " + plugin.getName() + " configuration", var8);
      }
   }

   static Map<String, Set<OutdatedConfigurationArchive.Rule>> readRules(byte[] bytes) throws IOException {
      Map<?, ?> yaml = readMapping(bytes);
      Map<String, Set<OutdatedConfigurationArchive.Rule>> result = new LinkedHashMap<>();

      for (Entry<?, ?> entry : yaml.entrySet()) {
         if (entry.getKey() instanceof String category && category.matches("[A-Za-z0-9_-]+(?:/[A-Za-z0-9_-]+)*|[A-Za-z0-9_-]+\\.ya?ml")) {
            if (entry.getValue() instanceof List<?> rules && !rules.isEmpty()) {
               Set<OutdatedConfigurationArchive.Rule> selected = new LinkedHashSet<>();

               for (Object rule : rules) {
                  if (rule instanceof String key && !key.isBlank()) {
                     selected.add(new OutdatedConfigurationArchive.KeyRule(key));
                  } else if (rule instanceof Map<?, ?> fields
                     && fields.keySet().equals(Set.of("files", "key", "listEntryNames"))
                     && fields.get("key") instanceof String key
                     && !key.isBlank()) {
                     Set<String> files = readNames(fields.get("files"), "files", "[A-Za-z0-9_-]+\\.ya?ml");
                     Set<String> names = readNames(fields.get("listEntryNames"), "listEntryNames", "[a-z0-9_.:-]+");
                     selected.add(new OutdatedConfigurationArchive.ListEntryRule(files, key, names));
                  } else if (rule instanceof Map<?, ?> fields
                     && fields.keySet().equals(Set.of("files", "key", "value"))
                     && fields.get("key") instanceof String key
                     && key.matches("[A-Za-z0-9_-]+(?:\\.[A-Za-z0-9_-]+)*")) {
                     Set<String> files = readNames(fields.get("files"), "files", "[A-Za-z0-9_-]+\\.ya?ml");
                     selected.add(new OutdatedConfigurationArchive.ValueRule(files, List.of(key.split("\\.")), readValue(fields.get("value"))));
                  } else if (rule instanceof Map<?, ?> fields
                     && fields.keySet().equals(Set.of("lua"))
                     && fields.get("lua") instanceof Map<?, ?> lua
                     && lua.keySet().equals(Set.of("supportedHooks", "retiredHooks"))) {
                     if (category.endsWith(".yml") || category.endsWith(".yaml")) {
                        throw new IOException("A Lua retirement rule requires a script directory: " + category);
                     }

                     Set<String> supported = readNames(lua.get("supportedHooks"), "supportedHooks", "on_[a-z_]+");
                     Set<String> retired = readNames(lua.get("retiredHooks"), "retiredHooks", "on_[a-z_]+");
                     if (supported.stream().anyMatch(retired::contains)) {
                        throw new IOException("Supported and retired Lua hooks must not overlap: " + category);
                     }

                     selected.add(new OutdatedConfigurationArchive.LuaScriptRule(supported, retired));
                  } else {
                     if (rule instanceof Map<?, ?> fields && fields.keySet().equals(Set.of("files", "sha256"))) {
                        if (!category.endsWith(".yml") && !category.endsWith(".yaml")) {
                           selected.add(
                              new OutdatedConfigurationArchive.LuaSourceRule(
                                 readNames(fields.get("files"), "files", "[a-z0-9_-]+\\.lua"), readNames(fields.get("sha256"), "sha256", "[0-9a-f]{64}")
                              )
                           );
                           continue;
                        }

                        throw new IOException("A Lua source retirement rule requires a script directory: " + category);
                     }

                     throw new IOException("Invalid outdated configuration rule in " + category);
                  }
               }

               result.put(category, Set.copyOf(selected));
               continue;
            }

            throw new IOException("An outdated configuration category requires nonempty rules: " + category);
         }

         throw new IOException("An outdated configuration category must be a relative content directory or a root YAML filename");
      }

      return Map.copyOf(result);
   }

   private static Object readValue(Object value) throws IOException {
      if (value instanceof String text && !text.isBlank()) {
         return text;
      } else if (!(value instanceof Integer) && !(value instanceof Long)) {
         if (value instanceof List<?> entries && !entries.isEmpty()) {
            List<String> values = new ArrayList<>(entries.size());
            Iterator var3 = entries.iterator();

            while (var3.hasNext()) {
               if (!(var3.next() instanceof String text) || text.isBlank()) {
                  throw new IOException("An outdated configuration value list requires nonblank strings");
               }

               values.add(text);
            }

            return List.copyOf(values);
         } else {
            throw new IOException("An outdated configuration value must be a nonblank string, a whole number or a nonempty string list");
         }
      } else {
         return value;
      }
   }

   private static Set<String> readNames(Object value, String field, String pattern) throws IOException {
      if (value instanceof List<?> entries && !entries.isEmpty()) {
         Set<String> names = new LinkedHashSet<>();

         for (Object entry : entries) {
            if (!(entry instanceof String name) || !name.matches(pattern)) {
               throw new IOException("Invalid outdated configuration " + field + " entry: " + entry);
            }

            names.add(name);
         }

         return Set.copyOf(names);
      } else {
         throw new IOException("An outdated configuration rule requires nonempty " + field);
      }
   }

   static List<Path> archive(Path dataDirectory, Path archiveDirectory, Map<String, Set<OutdatedConfigurationArchive.Rule>> rules) throws IOException {
      Path data = dataDirectory.toAbsolutePath().normalize();
      Path storage = archiveDirectory.toAbsolutePath().normalize();
      if (storage.startsWith(data)) {
         throw new IOException("Archive storage must be outside the content root");
      } else if (Files.exists(data, LinkOption.NOFOLLOW_LINKS) && !rules.isEmpty()) {
         Set<Path> inspectedPaths = new HashSet<>();
         requireNoLinks(data, inspectedPaths);
         Yaml parser = newParser();
         Map<Path, byte[]> matches = new LinkedHashMap<>();

         for (Entry<String, Set<OutdatedConfigurationArchive.Rule>> rule : rules.entrySet()) {
            Path category = data.resolve(rule.getKey()).normalize();
            if (!category.startsWith(data) || category.equals(data)) {
               throw new IOException("Invalid content category");
            }

            if (Files.exists(category, LinkOption.NOFOLLOW_LINKS)) {
               requireNoLinks(category, inspectedPaths);
               if ((rule.getKey().endsWith(".yml") || rule.getKey().endsWith(".yaml")) && !Files.isRegularFile(category, LinkOption.NOFOLLOW_LINKS)) {
                  throw new IOException("Not a regular YAML file: " + category);
               }

               List<OutdatedConfigurationArchive.LuaScriptRule> luaRules = rule.getValue()
                  .stream()
                  .filter(OutdatedConfigurationArchive.LuaScriptRule.class::isInstance)
                  .map(OutdatedConfigurationArchive.LuaScriptRule.class::cast)
                  .toList();
               List<OutdatedConfigurationArchive.LuaSourceRule> sourceRules = rule.getValue()
                  .stream()
                  .filter(OutdatedConfigurationArchive.LuaSourceRule.class::isInstance)
                  .map(OutdatedConfigurationArchive.LuaSourceRule.class::cast)
                  .toList();
               List<OutdatedConfigurationArchive.Rule> yamlRules = rule.getValue()
                  .stream()
                  .filter(
                     selected -> !(selected instanceof OutdatedConfigurationArchive.LuaScriptRule)
                        && !(selected instanceof OutdatedConfigurationArchive.LuaSourceRule)
                  )
                  .toList();

               try (Stream<Path> paths = Files.walk(category)) {
                  for (Path path : paths.sorted().toList()) {
                     requireNoLinks(path, inspectedPaths);
                     String name = path.getFileName().toString().toLowerCase(Locale.ROOT);
                     if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
                        boolean lua = name.endsWith(".lua") && (!luaRules.isEmpty() || sourceRules.stream().anyMatch(selected -> selected.appliesTo(name)));
                        boolean yaml = (name.endsWith(".yml") || name.endsWith(".yaml")) && yamlRules.stream().anyMatch(selected -> selected.appliesTo(name));
                        if (lua || yaml) {
                           if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) {
                              throw new IOException("Not a regular configuration file: " + path);
                           }

                           byte[] original;
                           try (InputStream input = Files.newInputStream(path)) {
                              original = readBounded(input);
                           } catch (IOException var29) {
                              if (!lua) {
                                 throw var29;
                              }
                              continue;
                           }

                           if (lua) {
                              if (sourceRules.stream().anyMatch(selected -> selected.matchesSource(name, original))
                                 || luaRules.stream().anyMatch(selected -> selected.matchesScript(path, original))) {
                                 matches.put(path, original);
                              }
                           } else if (matchesYaml(name, original, yamlRules, parser)) {
                              matches.put(path, original);
                           }
                        }
                     }
                  }
               }
            }
         }

         if (matches.isEmpty()) {
            return List.of();
         } else {
            requireNoLinks(storage);
            Files.createDirectories(storage);
            requireNoLinks(storage);
            Path owner = storage.resolve(data.getFileName());
            requireNoLinks(owner);
            Files.createDirectories(owner);
            Path batch = owner.resolve(Instant.now().toString().replace(':', '-') + "-" + UUID.randomUUID());
            Files.createDirectory(batch);
            List<Path> archived = new ArrayList<>();

            for (Entry<Path, byte[]> entry : matches.entrySet()) {
               Path source = entry.getKey();
               requireNoLinks(source);

               byte[] current;
               try (InputStream inputx = Files.newInputStream(source)) {
                  current = readBounded(inputx);
               }

               if (!Arrays.equals(entry.getValue(), current)) {
                  throw new IOException("Configuration changed during archival: " + source);
               }

               Path destination = batch.resolve(data.relativize(source));
               requireNoLinks(destination);
               Files.createDirectories(destination.getParent());
               requireNoLinks(destination);
               Files.move(source, destination);
               archived.add(destination);
            }

            return List.copyOf(archived);
         }
      } else {
         return List.of();
      }
   }

   private static boolean matchesYaml(String name, byte[] source, List<OutdatedConfigurationArchive.Rule> rules, Yaml parser) throws IOException {
      Map<?, ?> yaml = readMapping(source, parser);
      return rules.stream().anyMatch(rule -> rule.matches(name, yaml));
   }

   public static boolean isArchivePath(Path path) {
      Path absolute = path.toAbsolutePath().normalize();

      for (int index = 1; index < absolute.getNameCount(); index++) {
         if (absolute.getName(index).toString().equalsIgnoreCase("outdated files") && absolute.getName(index - 1).toString().equalsIgnoreCase("MagmaCore")) {
            return true;
         }
      }

      return false;
   }

   private static void requireNoLinks(Path path) throws IOException {
      requireNoLinks(path, new HashSet<>());
   }

   private static void requireNoLinks(Path path, Set<Path> inspectedPaths) throws IOException {
      Path absolute = path.toAbsolutePath().normalize();

      for (Path parent = absolute; parent != null && !inspectedPaths.contains(parent); parent = parent.getParent()) {
         if (Files.isSymbolicLink(parent)) {
            throw new IOException("Refusing linked archive/content path: " + parent);
         }

         if (Files.exists(parent, LinkOption.NOFOLLOW_LINKS) && !parent.toRealPath().equals(parent)) {
            throw new IOException("Refusing redirected archive/content path: " + parent);
         }

         inspectedPaths.add(parent);
      }
   }

   private static byte[] readBounded(InputStream input) throws IOException {
      byte[] bytes = input.readNBytes(4194305);
      if (bytes.length > 4194304) {
         throw new IOException("Configuration exceeds the archival inspection limit");
      } else {
         return bytes;
      }
   }

   private static Map<?, ?> readMapping(byte[] bytes) throws IOException {
      return readMapping(bytes, newParser());
   }

   private static Yaml newParser() {
      LoaderOptions options = new LoaderOptions();
      options.setAllowDuplicateKeys(false);
      options.setCodePointLimit(4194304);
      return new Yaml(new SafeConstructor(options));
   }

   private static Map<?, ?> readMapping(byte[] bytes, Yaml parser) throws IOException {
      try {
         Object value = parser.load(new String(bytes, StandardCharsets.UTF_8));
         if (value == null) {
            return Map.of();
         } else if (value instanceof Map<?, ?> mapping) {
            return mapping;
         } else {
            throw new IOException("Configuration must be a YAML mapping");
         }
      } catch (RuntimeException var4) {
         throw new IOException("Cannot inspect YAML for retired keys", var4);
      }
   }

   record KeyRule(String key) implements OutdatedConfigurationArchive.Rule {
      @Override
      public boolean matches(String filename, Map<?, ?> yaml) {
         return yaml.containsKey(this.key);
      }
   }

   record ListEntryRule(Set<String> files, String key, Set<String> names) implements OutdatedConfigurationArchive.Rule {
      ListEntryRule(Set<String> files, String key, Set<String> names) {
         files = Set.copyOf(files);
         names = Set.copyOf(names);
         this.files = files;
         this.key = key;
         this.names = names;
      }

      @Override
      public boolean appliesTo(String filename) {
         return this.files.contains(filename);
      }

      @Override
      public boolean matches(String filename, Map<?, ?> yaml) {
         if (this.files.contains(filename)) {
            Object var4 = yaml.get(this.key);
            if (var4 instanceof List) {
               for (Object entry : (List)var4) {
                  if (entry instanceof String text) {
                     int comma = text.indexOf(44);
                     if (comma >= 0 && this.names.contains(text.substring(0, comma).toLowerCase(Locale.ROOT))) {
                        return true;
                     }
                  }
               }

               return false;
            }
         }

         return false;
      }
   }

   record LuaScriptRule(Set<String> supportedHooks, Set<String> retiredHooks) implements OutdatedConfigurationArchive.Rule {
      LuaScriptRule(Set<String> supportedHooks, Set<String> retiredHooks) {
         supportedHooks = Set.copyOf(supportedHooks);
         retiredHooks = Set.copyOf(retiredHooks);
         this.supportedHooks = supportedHooks;
         this.retiredHooks = retiredHooks;
      }

      @Override
      public boolean matches(String filename, Map<?, ?> yaml) {
         return false;
      }

      boolean matchesScript(final Path path, byte[] source) {
         ScriptProvider contract = new ScriptProvider() {
            @Override
            public String getNamespace() {
               return "archive";
            }

            @Override
            public Path getScriptDirectory() {
               return path.getParent();
            }

            @Override
            public ScriptHook resolveHook(String key) {
               return !LuaScriptRule.this.supportedHooks.contains(key) && !LuaScriptRule.this.retiredHooks.contains(key) ? null : new ScriptHook(key);
            }
         };

         try {
            ScriptDefinition definition = ScriptDefinition.validate(
               path.getFileName().toString(),
               path.toFile(),
               StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(source)).toString().replace("\r", ""),
               contract
            );
            return definition.getHooks().stream().anyMatch(hook -> this.retiredHooks.contains(hook.getKey()));
         } catch (CharacterCodingException | RuntimeException var5) {
            return false;
         }
      }
   }

   record LuaSourceRule(Set<String> files, Set<String> sha256) implements OutdatedConfigurationArchive.Rule {
      LuaSourceRule(Set<String> files, Set<String> sha256) {
         files = Set.copyOf(files);
         sha256 = Set.copyOf(sha256);
         this.files = files;
         this.sha256 = sha256;
      }

      @Override
      public boolean matches(String filename, Map<?, ?> yaml) {
         return false;
      }

      @Override
      public boolean appliesTo(String filename) {
         return this.files.contains(filename);
      }

      boolean matchesSource(String filename, byte[] source) {
         if (!this.files.contains(filename)) {
            return false;
         } else {
            byte[] normalized = new String(source, StandardCharsets.UTF_8).replace("\r", "").getBytes(StandardCharsets.UTF_8);

            try {
               return this.sha256.contains(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(normalized)));
            } catch (NoSuchAlgorithmException var5) {
               throw new IllegalStateException("SHA-256 is unavailable", var5);
            }
         }
      }
   }

   private static final class OutdatedConfigurationArchiveOwner implements Runnable {
      private final JavaPlugin plugin;
      private volatile boolean active = true;

      private OutdatedConfigurationArchiveOwner(JavaPlugin plugin) {
         this.plugin = plugin;
      }

      @Override
      public synchronized void run() {
         if (this.active && this.plugin.isEnabled()) {
            OutdatedConfigurationArchive.archive(this.plugin);
         } else {
            throw new IllegalStateException("Configuration archive owner is disabled: " + this.plugin.getName());
         }
      }
   }

   sealed interface Rule
      permits OutdatedConfigurationArchive.KeyRule,
      OutdatedConfigurationArchive.ListEntryRule,
      OutdatedConfigurationArchive.ValueRule,
      OutdatedConfigurationArchive.LuaScriptRule,
      OutdatedConfigurationArchive.LuaSourceRule {
      boolean matches(String var1, Map<?, ?> var2);

      default boolean appliesTo(String filename) {
         return true;
      }
   }

   record ValueRule(Set<String> files, List<String> path, Object value) implements OutdatedConfigurationArchive.Rule {
      ValueRule(Set<String> files, List<String> path, Object value) {
         files = Set.copyOf(files);
         path = List.copyOf(path);
         this.files = files;
         this.path = path;
         this.value = value;
      }

      @Override
      public boolean appliesTo(String filename) {
         return this.files.contains(filename);
      }

      @Override
      public boolean matches(String filename, Map<?, ?> yaml) {
         if (!this.files.contains(filename)) {
            return false;
         } else {
            Object current = yaml;

            for (String key : this.path) {
               if (!(current instanceof Map<?, ?> mapping)) {
                  return false;
               }

               current = mapping.get(key);
            }

            return !(this.value instanceof Number expected)
               ? this.value.equals(current)
               : current instanceof Number actual && new BigDecimal(expected.toString()).compareTo(new BigDecimal(actual.toString())) == 0;
         }
      }
   }
}
