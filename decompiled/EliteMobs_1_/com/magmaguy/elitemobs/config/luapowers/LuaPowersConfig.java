package com.magmaguy.elitemobs.config.luapowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.magmacore.config.ContentFileSelector;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.powers.lua.LuaPowerManager;
import com.magmaguy.elitemobs.shaded.reflections.Reflections;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Generated;

public class LuaPowersConfig {
   private static LinkedHashMap<String, PowersConfigFields> luaPowers = new LinkedHashMap<>();
   private static Set<String> premadeFilenames = Set.of();
   private final LinkedHashMap<String, LuaPowersConfigFields> pendingPremades = new LinkedHashMap<>();
   private final File powersDirectory;

   public LuaPowersConfig() {
      luaPowers = new LinkedHashMap<>();
      premadeFilenames = Set.of();
      this.powersDirectory = new File(MetadataHandler.PLUGIN.getDataFolder(), "powers");
      if (!this.powersDirectory.exists() && !this.powersDirectory.mkdirs()) {
         Logger.warn("Failed to create powers directory for premade Lua powers.");
      } else {
         for (LuaPowersConfigFields luaPowersConfigFields : this.discoverPremades()) {
            this.pendingPremades.put(luaPowersConfigFields.getFilename().toLowerCase(Locale.ROOT), luaPowersConfigFields);
         }

         premadeFilenames = Set.copyOf(this.pendingPremades.keySet());
         List<File> candidates = new ArrayList<>();
         this.collectSources(this.powersDirectory, candidates);

         for (File selected : ContentFileSelector.select(candidates, LuaPowersConfig::filenameKey)) {
            this.fileInitializer(selected);
         }

         for (LuaPowersConfigFields luaPowersConfigFields : new ArrayList<>(this.pendingPremades.values())) {
            this.initialize(luaPowersConfigFields);
         }
      }
   }

   public static boolean isPremadeFilename(String filename) {
      return premadeFilenames.contains(filename.toLowerCase(Locale.ROOT));
   }

   public static String filenameKey(String filename) {
      return isPremadeFilename(filename) ? filename.toLowerCase(Locale.ROOT) : filename;
   }

   private List<LuaPowersConfigFields> discoverPremades() {
      Reflections reflections = new Reflections("com.magmaguy.elitemobs.config.luapowers.premade");
      return reflections.getSubTypesOf(LuaPowersConfigFields.class)
         .stream()
         .filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
         .sorted(Comparator.comparing(Class::getSimpleName))
         .map(clazz -> {
            try {
               return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception var2) {
               throw new IllegalStateException("Failed to initialize premade Lua power config " + clazz.getName(), var2);
            }
         })
         .collect(Collectors.toList());
   }

   private void collectSources(File directory, List<File> candidates) {
      File[] files = directory.listFiles();
      if (files != null) {
         Arrays.sort(files, Comparator.comparing(File::getName));

         for (File file : files) {
            if (file.isDirectory()) {
               this.collectSources(file, candidates);
            } else if (file.getName().toLowerCase(Locale.ROOT).endsWith(".lua")) {
               candidates.add(file);
            }
         }
      }
   }

   private void fileInitializer(File file) {
      if (file.getName().toLowerCase(Locale.ROOT).endsWith(".lua")) {
         LuaPowersConfigFields luaPowersConfigFields = this.pendingPremades.remove(file.getName().toLowerCase(Locale.ROOT));
         if (luaPowersConfigFields != null) {
            this.initialize(luaPowersConfigFields, file);
         }
      }
   }

   private void initialize(LuaPowersConfigFields luaPowersConfigFields) {
      File file = new File(this.powersDirectory, luaPowersConfigFields.getFilename());

      try {
         if (!file.exists()) {
            File parentFile = file.getParentFile();
            if (parentFile != null && !parentFile.exists() && !parentFile.mkdirs()) {
               Logger.warn("Failed to create directories for premade Lua power " + luaPowersConfigFields.getFilename() + ".");
               return;
            }

            Files.writeString(file.toPath(), luaPowersConfigFields.getSource(), StandardCharsets.UTF_8);
         }

         this.initialize(luaPowersConfigFields, file);
      } catch (IOException var4) {
         Logger.warn("Failed to create premade Lua power file " + luaPowersConfigFields.getFilename() + ".");
         var4.printStackTrace();
      }
   }

   private void initialize(LuaPowersConfigFields luaPowersConfigFields, File file) {
      try {
         PowersConfigFields configFields = LuaPowerManager.loadLuaPower(
            luaPowersConfigFields.getFilename(), file, luaPowersConfigFields.getEffect(), luaPowersConfigFields.getPowerType()
         );
         luaPowers.put(luaPowersConfigFields.getFilename(), configFields);
      } catch (IOException var4) {
         Logger.warn("Failed to read premade Lua power file " + luaPowersConfigFields.getFilename() + ".");
         var4.printStackTrace();
      } catch (Exception var5) {
         Logger.warn("Failed to load premade Lua power " + luaPowersConfigFields.getFilename() + ".");
         var5.printStackTrace();
      }
   }

   @Generated
   public static LinkedHashMap<String, PowersConfigFields> getLuaPowers() {
      return luaPowers;
   }
}
