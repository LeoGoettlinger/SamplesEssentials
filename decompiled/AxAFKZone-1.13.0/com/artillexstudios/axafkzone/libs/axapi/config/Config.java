package com.artillexstudios.axafkzone.libs.axapi.config;

import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.YamlDocument;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.block.implementation.Section;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axafkzone.libs.axapi.utils.YamlUtils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Config {
   private YamlDocument configuration;

   public Config(File file) {
      this(file, null);
   }

   public Config(File file, InputStream defaults) {
      this(file, defaults, GeneralSettings.DEFAULT, LoaderSettings.DEFAULT, DumperSettings.DEFAULT, UpdaterSettings.DEFAULT);
   }

   public Config(
      File file,
      InputStream defaults,
      GeneralSettings generalSettings,
      LoaderSettings loaderSettings,
      DumperSettings dumperSettings,
      UpdaterSettings updaterSettings
   ) {
      try {
         this.configuration = YamlDocument.create(file, defaults, generalSettings, loaderSettings, dumperSettings, updaterSettings);
      } catch (Exception var8) {
         YamlUtils.suggest(file);
      }
   }

   public <T> T get(String route) {
      return (T)this.configuration.get(route);
   }

   public <T> T get(String route, T def) {
      return (T)this.configuration.get(route, def);
   }

   public void set(String key, Object value) {
      this.configuration.set(key, value);
   }

   public void remove(String key) {
      this.configuration.remove(key);
   }

   public boolean getBoolean(String key) {
      return this.configuration.getBoolean(key);
   }

   public String getString(String key) {
      return this.configuration.getString(key);
   }

   public int getInt(String key) {
      return this.configuration.getInt(key);
   }

   public int getInt(String key, int def) {
      return this.configuration.getInt(key, def);
   }

   public long getLong(String key) {
      return this.configuration.getLong(key);
   }

   public long getLong(String key, long def) {
      return this.configuration.getLong(key, def);
   }

   public List<String> getStringList(String key) {
      return this.getList(key);
   }

   public List<String> getStringList(String key, List<String> def) {
      return this.getList(key, def);
   }

   public <T> List<T> getList(String key) {
      return (List<T>)this.configuration.getList(key);
   }

   public <T> List<T> getList(String key, List<T> def) {
      return (List<T>)this.configuration.getList(key, def);
   }

   public double getDouble(String key) {
      return this.configuration.getDouble(key);
   }

   public double getDouble(String key, double def) {
      return this.configuration.getDouble(key, def);
   }

   public float getDouble(String key, float def) {
      return this.configuration.getFloat(key, def);
   }

   public String getString(String key, String def) {
      return this.configuration.getString(key, def);
   }

   public boolean getBoolean(String key, boolean def) {
      return this.configuration.getBoolean(key, def);
   }

   public float getFloat(String key) {
      return this.configuration.getFloat(key);
   }

   public float getFloat(String key, float def) {
      return this.configuration.getFloat(key, def);
   }

   public <T, U> List<Map<T, U>> getMapList(String key) {
      List<Map<T, U>> listMap = new ArrayList<>();

      for (Map<?, ?> map : this.configuration.getMapList(key)) {
         HashMap<T, U> hashMap = new HashMap<>();
         map.forEach((k, v) -> hashMap.put((T)k, (U)v));
         listMap.add(hashMap);
      }

      return listMap;
   }

   public Section getSection(String key) {
      return this.configuration.getSection(key);
   }

   public <T> Optional<T> getOptional(String key) {
      return (Optional<T>)this.configuration.getOptional(key);
   }

   public Set<String> getKeys(boolean deep) {
      return this.configuration.getRoutesAsStrings(deep);
   }

   public boolean reload() {
      try {
         return this.configuration.reload();
      } catch (Exception var2) {
         YamlUtils.suggest(this.configuration.getFile());
         return false;
      }
   }

   public void save() {
      try {
         this.configuration.save();
      } catch (IOException var2) {
         YamlUtils.suggest(this.configuration.getFile());
      }
   }

   public YamlDocument getBackingDocument() {
      return this.configuration;
   }
}
