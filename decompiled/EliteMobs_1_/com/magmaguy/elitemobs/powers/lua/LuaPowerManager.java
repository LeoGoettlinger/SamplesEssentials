package com.magmaguy.elitemobs.powers.lua;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfig;
import com.magmaguy.elitemobs.config.powers.LuaPowerConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.magmacore.config.ContentFileSelector;
import com.magmaguy.elitemobs.magmacore.scripting.ScriptDefinition;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LuaPowerManager {
   private static final Map<String, ScriptDefinition> definitions = new LinkedHashMap<>();
   private static final Pattern INCLUDE = Pattern.compile("^\\s*--\\s*@include\\s+(\\S+)\\s*$");

   private LuaPowerManager() {
   }

   public static Map<String, PowersConfigFields> discoverLuaPowers(Collection<PowersConfigFields> loadedYamlPowers) {
      LinkedHashMap<String, PowersConfigFields> discoveredPowers = new LinkedHashMap<>();
      LinkedHashSet<File> powerDirectories = new LinkedHashSet<>();
      Map<Path, PowersConfigFields> metadata = new HashMap<>();
      Map<Path, LuaPowerConfigFields> premades = new HashMap<>();

      for (PowersConfigFields loadedYamlPower : loadedYamlPowers) {
         if (loadedYamlPower.getFile() != null && loadedYamlPower.getFile().getParentFile() != null) {
            powerDirectories.add(loadedYamlPower.getFile().getParentFile());
            Path source = loadedYamlPower.getFile().toPath().toAbsolutePath().normalize();
            if (loadedYamlPower instanceof LuaPowerConfigFields lua) {
               premades.put(source, lua);
            } else {
               metadata.put(source, loadedYamlPower);
            }
         }
      }

      List<File> candidates = new ArrayList<>();
      Set<Path> visitedDirectories = new HashSet<>();

      for (File powerDirectory : powerDirectories) {
         collectSources(powerDirectory, candidates, visitedDirectories);
      }

      for (File file : ContentFileSelector.select(candidates, LuaPowersConfig::filenameKey)) {
         discoverFile(file, discoveredPowers, metadata, premades);
      }

      return discoveredPowers;
   }

   public static void shutdown() {
      definitions.clear();
   }

   private static void collectSources(File directory, List<File> candidates, Set<Path> visitedDirectories) {
      if (visitedDirectories.add(directory.toPath().toAbsolutePath().normalize())) {
         File[] files = directory.listFiles();
         if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName));

            for (File file : files) {
               if (file.isDirectory()) {
                  collectSources(file, candidates, visitedDirectories);
               } else if (file.getName().toLowerCase(Locale.ROOT).endsWith(".lua")) {
                  candidates.add(file);
               }
            }
         }
      }
   }

   private static void discoverFile(
      File file, Map<String, PowersConfigFields> discoveredPowers, Map<Path, PowersConfigFields> metadata, Map<Path, LuaPowerConfigFields> premades
   ) {
      try {
         String stem = file.getName().substring(0, file.getName().length() - 4);
         PowersConfigFields settings = metadata.get(file.toPath().resolveSibling(stem + ".yml").toAbsolutePath().normalize());
         if (settings == null) {
            settings = metadata.get(file.toPath().resolveSibling(stem + ".yaml").toAbsolutePath().normalize());
         }

         LuaPowerConfigFields power = premades.get(file.toPath().toAbsolutePath().normalize());
         if (power == null) {
            if (LuaPowersConfig.isPremadeFilename(file.getName())) {
               return;
            }

            if (settings != null && !settings.isEnabled()) {
               return;
            }

            power = loadLuaPower(
               file.getName(),
               file,
               settings == null ? null : settings.getEffect(),
               settings != null && settings.getPowerType() != null ? settings.getPowerType() : PowersConfigFields.PowerType.MISCELLANEOUS
            );
         }

         if (settings != null) {
            power.setEnabled(settings.isEnabled());
            power.setEffect(settings.getEffect());
            if (settings.getPowerType() != null) {
               power.setPowerType(settings.getPowerType());
            }

            power.setPowerCooldown(settings.getPowerCooldown());
            power.setGlobalCooldown(settings.getGlobalCooldown());
         }

         discoveredPowers.put(power.getFilename(), power);
      } catch (IOException var7) {
         Logger.warn("Failed to read Lua power file " + file.getName() + ".");
      } catch (Exception var8) {
         Logger.warn("Failed to load Lua power file " + file.getName() + ".");
         var8.printStackTrace();
      }
   }

   public static LuaPowerConfigFields loadLuaPower(String registryKey, File file, String effect, PowersConfigFields.PowerType powerType) throws IOException {
      Path path = file.toPath().toRealPath();
      String source = readSource(path, path.getParent(), new HashSet<>(), 0);
      return registerLuaPower(registryKey, file, source, effect, powerType).configFields;
   }

   private static String readSource(Path file, Path root, Set<Path> active, int depth) throws IOException {
      file = file.toRealPath();
      if (file.startsWith(root) && depth <= 16 && active.add(file)) {
         String var12;
         try {
            if (Files.size(file) > 4194304L) {
               throw new IOException("Lua source is too large: " + file);
            }

            StringBuilder source = new StringBuilder();

            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
               Matcher include = INCLUDE.matcher(line);
               if (include.matches()) {
                  source.append(readSource(file.getParent().resolve(include.group(1)), root, active, depth + 1));
               } else {
                  source.append(line);
               }

               source.append('\n');
               if (source.length() > 4194304) {
                  throw new IOException("Expanded Lua source is too large: " + file);
               }
            }

            var12 = source.toString();
         } finally {
            active.remove(file);
         }

         return var12;
      } else {
         throw new IOException("Invalid or circular Lua include: " + file);
      }
   }

   public static LuaPowerManager.Registration registerLuaPower(
      String registryKey, File sourceFile, String source, String effect, PowersConfigFields.PowerType powerType
   ) {
      Objects.requireNonNull(registryKey, "registryKey");
      Objects.requireNonNull(sourceFile, "sourceFile");
      Objects.requireNonNull(source, "source");
      Objects.requireNonNull(powerType, "powerType");
      source = source.replace("\r", "");
      ScriptDefinition definition = ScriptDefinition.validate(registryKey, sourceFile, source, new EliteMobsScriptProvider(sourceFile.getParentFile().toPath()));
      definitions.put(registryKey, definition);
      return new LuaPowerManager.Registration(registryKey, definition, new LuaPowerConfigFields(registryKey, sourceFile, definition, effect, powerType));
   }

   public static final class Registration implements AutoCloseable {
      private final String registryKey;
      private final ScriptDefinition definition;
      private final LuaPowerConfigFields configFields;
      private boolean closed;

      private Registration(String registryKey, ScriptDefinition definition, LuaPowerConfigFields configFields) {
         this.registryKey = registryKey;
         this.definition = definition;
         this.configFields = configFields;
      }

      public int executionPriority() {
         return this.definition.getPriority();
      }

      public Set<String> hookKeys() {
         LinkedHashSet<String> hooks = new LinkedHashSet<>();
         this.definition.getHooks().stream().map(hook -> hook.getKey()).sorted().forEach(hooks::add);
         return Collections.unmodifiableSet(hooks);
      }

      public LuaElitePower newPower() {
         if (this.closed) {
            throw new IllegalStateException("Lua power registration is closed");
         } else {
            return new LuaElitePower(this.configFields);
         }
      }

      @Override
      public void close() {
         if (!this.closed) {
            this.closed = true;
            LuaPowerManager.definitions.remove(this.registryKey, this.definition);
         }
      }
   }
}
