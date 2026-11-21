package com.magmaguy.elitemobs.magmacore.config;

import com.magmaguy.elitemobs.magmacore.util.ConfigurationLocation;
import com.magmaguy.elitemobs.magmacore.util.ItemStackGenerator;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.magmacore.util.VersionChecker;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.Generated;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.Vector;

public abstract class CustomConfigFields {
   protected String filename;
   protected boolean isEnabled;
   protected FileConfiguration fileConfiguration;
   private FileConfiguration writableFileConfiguration;
   protected File file;

   public CustomConfigFields(String filename, boolean isEnabled) {
      this.filename = filename.contains(".yml") ? filename : filename + ".yml";
      this.isEnabled = isEnabled;
   }

   public void setFileConfiguration(FileConfiguration fileConfiguration) {
      this.fileConfiguration = fileConfiguration;
      this.writableFileConfiguration = fileConfiguration;
   }

   void beginInheritedRead(FileConfiguration readConfiguration, FileConfiguration writableConfiguration) {
      this.fileConfiguration = readConfiguration;
      this.writableFileConfiguration = writableConfiguration;
   }

   public FileConfiguration getWritableFileConfiguration() {
      return this.writableFileConfiguration == null ? this.fileConfiguration : this.writableFileConfiguration;
   }

   public CompletableFuture<Void> setEnabledAndSave(boolean enabled) {
      this.isEnabled = enabled;
      FileConfiguration writable = this.getWritableFileConfiguration();
      writable.set("isEnabled", enabled);
      return CompletableFuture.runAsync(() -> {
         try {
            writable.save(this.file);
         } catch (IOException var3) {
            throw new RuntimeException(var3);
         }
      });
   }

   public abstract void processConfigFields();

   protected boolean configHas(String configKey) {
      return this.fileConfiguration.contains(configKey);
   }

   protected String processString(String path, String value, String pluginDefault, boolean forceWriteDefault) {
      if (this.configHas(path)) {
         try {
            return this.fileConfiguration.getString(path);
         } catch (Exception var6) {
            Logger.warn("File " + this.filename + " has an incorrect entry for " + path);
            Logger.warn("Entry: " + value);
            return value;
         }
      } else {
         if (forceWriteDefault || !Objects.equals(value, pluginDefault)) {
            this.fileConfiguration.addDefault(path, value);
         }

         return value;
      }
   }

   public List<Object> processList(String path, List<Object> value, List<Object> pluginDefault, boolean forceWriteDefault) {
      if (this.configHas(path)) {
         try {
            return new ArrayList<>(Objects.requireNonNull(this.fileConfiguration.getList(path)));
         } catch (Exception var6) {
            Logger.warn("File " + this.filename + " has an incorrect entry for " + path);
            Logger.warn("Entry: " + value);
            return value;
         }
      } else {
         if (forceWriteDefault || value != pluginDefault) {
            this.fileConfiguration.addDefault(path, value);
         }

         return value;
      }
   }

   public List<String> processStringList(String path, List<String> value, List<String> pluginDefault, boolean forceWriteDefault) {
      if (this.configHas(path)) {
         try {
            return new ArrayList<>(this.fileConfiguration.getStringList(path));
         } catch (Exception var6) {
            Logger.warn("File " + this.filename + " has an incorrect entry for " + path);
            Logger.warn("Entry: " + value);
            return value;
         }
      } else {
         if (forceWriteDefault || value != pluginDefault) {
            this.fileConfiguration.addDefault(path, value);
         }

         return value;
      }
   }

   protected List<World> processWorldList(String path, List<World> value, List<World> pluginDefault, boolean forceWriteDefault) {
      if (!this.configHas(path)) {
         if (value != null && (forceWriteDefault || value != pluginDefault)) {
            this.processStringList(path, this.worldListToStringListConverter(value), this.worldListToStringListConverter(pluginDefault), forceWriteDefault);
         }

         return value;
      } else {
         try {
            List<String> validWorldStrings = this.processStringList(
               path, this.worldListToStringListConverter(pluginDefault), this.worldListToStringListConverter(value), forceWriteDefault
            );
            List<World> validWorlds = new ArrayList<>();
            if (!validWorldStrings.isEmpty()) {
               for (String string : validWorldStrings) {
                  World world = Bukkit.getWorld(string);
                  if (world != null) {
                     validWorlds.add(world);
                  }
               }
            }

            return validWorlds;
         } catch (Exception var10) {
            Logger.warn("File " + this.filename + " has an incorrect entry for " + path);
            Logger.warn("Entry: " + value);
            return value;
         }
      }
   }

   private List<String> worldListToStringListConverter(List<World> pluginDefault) {
      if (pluginDefault == null) {
         return null;
      } else {
         List<String> newList = new ArrayList<>();
         pluginDefault.forEach(element -> newList.add(element.getName()));
         return newList;
      }
   }

   protected <T extends Enum<T>> List<T> processEnumList(String path, List<T> value, List<T> pluginDefault, Class<T> enumClass, boolean forceWriteDefault) {
      if (this.configHas(path)) {
         try {
            List<T> newList = new ArrayList<>();
            List<String> stringList = this.processStringList(
               path, this.enumListToStringListConverter(value), this.enumListToStringListConverter(pluginDefault), forceWriteDefault
            );
            stringList.forEach(
               string -> {
                  try {
                     newList.add(Enum.valueOf(enumClass, string.toUpperCase(Locale.ROOT)));
                  } catch (Exception var6x) {
                     if (string != null && string.toUpperCase(Locale.ROOT).endsWith("_SPEAR") && VersionChecker.serverVersionOlderThan(21, 11)) {
                        return;
                     }

                     Logger.warn(
                        this.filename
                           + " : Value "
                           + string
                           + " is not a valid for "
                           + path
                           + " ! This may be due to your server version, or due to an invalid value!"
                     );
                  }
               }
            );
            return newList;
         } catch (Exception var8) {
            var8.printStackTrace();
            Logger.warn("File " + this.filename + " has an incorrect entry for " + path);
            Logger.warn("Entry: " + value);
            return value;
         }
      } else {
         if (forceWriteDefault || value != pluginDefault) {
            this.processStringList(path, this.enumListToStringListConverter(value), this.enumListToStringListConverter(pluginDefault), forceWriteDefault);
         }

         return value;
      }
   }

   private <T extends Enum<T>> List<String> enumListToStringListConverter(List<T> list) {
      if (list == null) {
         return Collections.emptyList();
      } else {
         List<String> newList = new ArrayList<>();
         list.forEach(element -> newList.add(element.toString()));
         return newList;
      }
   }

   protected int processInt(String path, int value, int pluginDefault, boolean forceWriteDefault) {
      if (this.configHas(path)) {
         try {
            return this.fileConfiguration.getInt(path);
         } catch (Exception var6) {
            Logger.warn("File " + this.filename + " has an incorrect entry for " + path);
            Logger.warn("Entry: " + value);
            return value;
         }
      } else {
         if (forceWriteDefault || value != pluginDefault) {
            this.fileConfiguration.addDefault(path, value);
         }

         return value;
      }
   }

   protected long processLong(String path, long value, long pluginDefault, boolean forceWriteDefault) {
      if (this.configHas(path)) {
         try {
            return this.fileConfiguration.getLong(path);
         } catch (Exception var8) {
            Logger.warn("File " + this.filename + " has an incorrect entry for " + path);
            Logger.warn("Entry: " + value);
            return value;
         }
      } else {
         if (forceWriteDefault || value != pluginDefault) {
            this.fileConfiguration.addDefault(path, value);
         }

         return value;
      }
   }

   protected double processDouble(String path, double value, double pluginDefault, boolean forceWriteDefault) {
      if (this.configHas(path)) {
         try {
            return this.fileConfiguration.getDouble(path);
         } catch (Exception var8) {
            Logger.warn("File " + this.filename + " has an incorrect entry for " + path);
            Logger.warn("Entry: " + value);
            return value;
         }
      } else {
         if (forceWriteDefault || value != pluginDefault) {
            this.fileConfiguration.addDefault(path, value);
         }

         return value;
      }
   }

   protected Double processDouble(String path, Double value, Double pluginDefault, boolean forceWriteDefault) {
      if (this.configHas(path)) {
         try {
            return this.fileConfiguration.getDouble(path);
         } catch (Exception var6) {
            Logger.warn("File " + this.filename + " has an incorrect entry for " + path);
            Logger.warn("Entry: " + value);
            return value;
         }
      } else {
         if (forceWriteDefault || !Objects.equals(value, pluginDefault)) {
            this.fileConfiguration.addDefault(path, value);
         }

         return value;
      }
   }

   protected boolean processBoolean(String path, boolean value, boolean pluginDefault, boolean forceWriteDefault) {
      if (this.configHas(path)) {
         try {
            return this.fileConfiguration.getBoolean(path);
         } catch (Exception var6) {
            Logger.warn("File " + this.filename + " has an incorrect entry for " + path);
            Logger.warn("Entry: " + value);
            return value;
         }
      } else {
         if (forceWriteDefault || value != pluginDefault) {
            this.fileConfiguration.addDefault(path, value);
         }

         return value;
      }
   }

   public <T extends Enum<T>> T processEnum(String path, T value, T pluginDefault, Class<T> enumClass, boolean forceWriteDefault) {
      if (this.configHas(path)) {
         try {
            String rawValue = this.fileConfiguration.getString(path).toUpperCase(Locale.ROOT);
            return !VersionChecker.serverVersionOlderThan(21, 9) && rawValue.equals("CHAIN")
               ? Enum.valueOf(enumClass, "IRON_CHAIN")
               : Enum.valueOf(enumClass, rawValue);
         } catch (Exception var8) {
            String rawValuex = this.fileConfiguration.getString(path);
            if (rawValuex != null && rawValuex.toUpperCase(Locale.ROOT).endsWith("_SPEAR") && VersionChecker.serverVersionOlderThan(21, 11)) {
               return pluginDefault;
            } else {
               Logger.warn("File " + this.filename + " has an incorrect entry for " + path);
               Logger.warn("Entry: " + rawValuex);
               T var9 = null;
               return (T)(var9 == null ? pluginDefault : var9);
            }
         }
      } else {
         if (forceWriteDefault || value != pluginDefault) {
            String valueString = null;
            if (value != null) {
               valueString = value.toString().toUpperCase(Locale.ROOT);
            }

            String pluginDefaultString = null;
            if (pluginDefault != null) {
               pluginDefaultString = pluginDefault.toString().toUpperCase(Locale.ROOT);
            }

            this.processString(path, valueString, pluginDefaultString, forceWriteDefault);
         }

         return value;
      }
   }

   public ItemStack processItemStack(String path, ItemStack value, ItemStack pluginDefault, boolean forceWriteDefault) {
      if (!this.configHas(path)) {
         if (forceWriteDefault || value != pluginDefault) {
            this.processString(path, this.itemStackDeserializer(value), this.itemStackDeserializer(pluginDefault), forceWriteDefault);
         }

         return value;
      } else {
         try {
            String materialString = this.processString(path, this.itemStackDeserializer(value), this.itemStackDeserializer(pluginDefault), forceWriteDefault);
            if (materialString == null) {
               return null;
            } else if (materialString.matches("[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}")) {
               ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
               SkullMeta skullMeta = (SkullMeta)playerHead.getItemMeta();
               skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(UUID.fromString(materialString)));
               playerHead.setItemMeta(skullMeta);
               return playerHead;
            } else {
               String baseMaterial = materialString.contains(":") ? materialString.split(":")[0] : materialString;
               baseMaterial = mapLegacyMaterialName(baseMaterial);
               Material material = Material.getMaterial(baseMaterial);
               if (material == null) {
                  if (baseMaterial.endsWith("_SPEAR") && VersionChecker.serverVersionOlderThan(21, 11)) {
                     return null;
                  } else {
                     throw new IllegalArgumentException("Unknown material: " + baseMaterial);
                  }
               } else if (!materialString.contains(":")) {
                  return ItemStackGenerator.generateItemStack(material);
               } else {
                  ItemStack itemStack = ItemStackGenerator.generateItemStack(material);
                  if (!baseMaterial.contains("leather_") && !baseMaterial.contains("LEATHER_")) {
                     ItemMeta itemMeta = itemStack.getItemMeta();
                     itemMeta.setCustomModelData(Integer.parseInt(materialString.split(":")[1]));
                     itemStack.setItemMeta(itemMeta);
                  } else {
                     LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta)itemStack.getItemMeta();
                     leatherArmorMeta.setColor(Color.fromRGB(Integer.parseInt(materialString.split(":")[1], 16)));
                     itemStack.setItemMeta(leatherArmorMeta);
                  }

                  return itemStack;
               }
            }
         } catch (Exception var10) {
            Logger.warn("File " + this.filename + " has an incorrect entry for " + path);
            Logger.warn("Entry: " + value);
            return value;
         }
      }
   }

   private static String mapLegacyMaterialName(String material) {
      if (material.startsWith("GOLD_")) {
         return "GOLDEN_" + material.substring(5);
      } else {
         return material.startsWith("CHAIN_") ? "CHAINMAIL_" + material.substring(6) : material;
      }
   }

   public Map<String, Object> processMap(String path, Map<String, Object> value) {
      if (!this.configHas(path) && value != null) {
         this.fileConfiguration.addDefaults(value);
      }

      return this.fileConfiguration.get(path) == null ? Collections.emptyMap() : this.fileConfiguration.getConfigurationSection(path).getValues(false);
   }

   public Map<String, Object> processMapWithKey(String path, Map<String, Object> value) {
      if (!this.configHas(path) && value != null) {
         this.fileConfiguration.addDefault(path, value);
         this.fileConfiguration.createSection(path, value);
      }

      return this.fileConfiguration.get(path) == null ? Collections.emptyMap() : this.fileConfiguration.getConfigurationSection(path).getValues(false);
   }

   public ConfigurationSection processConfigurationSection(String path, Map<String, Object> value) {
      if (!this.configHas(path) && value != null) {
         this.fileConfiguration.addDefaults(value);
      }

      return this.fileConfiguration.getConfigurationSection(path);
   }

   private String itemStackDeserializer(ItemStack itemStack) {
      return itemStack == null ? null : itemStack.getType().toString();
   }

   protected Location processLocation(String path, Location value, String pluginDefault, boolean forceWriteDefault) {
      if (this.configHas(path)) {
         try {
            return ConfigurationLocation.serialize(this.fileConfiguration.getString(path));
         } catch (Exception var6) {
            Logger.warn("File " + this.filename + " has an incorrect entry for " + path);
            Logger.warn("Entry: " + value);
            return null;
         }
      } else {
         if (forceWriteDefault || !Objects.equals(value, pluginDefault)) {
            this.fileConfiguration.addDefault(path, ConfigurationLocation.deserialize(value));
         }

         return value;
      }
   }

   protected Vector processVector(String path, Vector value, Vector pluginDefault, boolean forceWriteDefault) {
      if (this.configHas(path)) {
         try {
            String string = this.fileConfiguration.getString(path);
            if (string == null) {
               return null;
            } else {
               String[] strings = string.split(",");
               if (strings.length < 3) {
                  Logger.warn("File " + this.filename + " has an incorrect entry for " + path);
                  return null;
               } else {
                  return new Vector(Double.parseDouble(strings[0]), Double.parseDouble(strings[1]), Double.parseDouble(strings[2]));
               }
            }
         } catch (Exception var7) {
            Logger.warn("File " + this.filename + " has an incorrect entry for " + path);
            Logger.warn("Entry: " + value);
            return null;
         }
      } else {
         if ((forceWriteDefault || !Objects.equals(value, pluginDefault)) && pluginDefault != null) {
            String vectorString = value.getX() + "," + value.getY() + "," + value.getZ();
            this.fileConfiguration.addDefault(path, vectorString);
         }

         return value;
      }
   }

   @Generated
   public String getFilename() {
      return this.filename;
   }

   @Generated
   public boolean isEnabled() {
      return this.isEnabled;
   }

   @Generated
   public void setEnabled(boolean isEnabled) {
      this.isEnabled = isEnabled;
   }

   @Generated
   public FileConfiguration getFileConfiguration() {
      return this.fileConfiguration;
   }

   @Generated
   public File getFile() {
      return this.file;
   }

   @Generated
   public void setFile(File file) {
      this.file = file;
   }
}
