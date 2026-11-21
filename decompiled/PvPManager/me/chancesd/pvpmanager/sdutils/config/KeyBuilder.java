package me.chancesd.pvpmanager.sdutils.config;

import org.bukkit.configuration.file.FileConfiguration;

public class KeyBuilder implements Cloneable {
   private final FileConfiguration config;
   private final char separator;
   private final StringBuilder builder;

   public KeyBuilder(FileConfiguration config, char separator) {
      this.config = config;
      this.separator = separator;
      this.builder = new StringBuilder();
   }

   private KeyBuilder(KeyBuilder keyBuilder) {
      this.config = keyBuilder.config;
      this.separator = keyBuilder.separator;
      this.builder = new StringBuilder(keyBuilder.toString());
   }

   public void parseLine(String line, boolean checkIfExists) {
      line = line.trim();
      String[] currentSplitLine = line.split(":");
      if (currentSplitLine.length > 2) {
         currentSplitLine = line.split(": ");
      }

      String key = currentSplitLine[0].replace("'", "").replace("\"", "");
      if (checkIfExists) {
         while (this.builder.length() > 0 && !this.config.contains(this.builder.toString() + this.separator + key)) {
            this.removeLastKey();
         }
      }

      if (this.builder.length() > 0) {
         this.builder.append(this.separator);
      }

      this.builder.append(key);
   }

   public String getLastKey() {
      return this.builder.length() == 0 ? "" : this.builder.toString().split("[" + this.separator + "]")[0];
   }

   public boolean isEmpty() {
      return this.builder.length() == 0;
   }

   public void clear() {
      this.builder.setLength(0);
   }

   public boolean isSubKeyOf(String parentKey) {
      return KeyUtils.isSubKeyOf(parentKey, this.builder.toString(), this.separator);
   }

   public boolean isSubKey(String subKey) {
      return KeyUtils.isSubKeyOf(this.builder.toString(), subKey, this.separator);
   }

   public boolean isConfigSection() {
      String key = this.builder.toString();
      return this.config.isConfigurationSection(key);
   }

   public boolean isConfigSectionWithKeys() {
      String key = this.builder.toString();
      return this.config.isConfigurationSection(key) && !this.config.getConfigurationSection(key).getKeys(false).isEmpty();
   }

   public void removeLastKey() {
      if (this.builder.length() != 0) {
         String keyString = this.builder.toString();
         String[] split = keyString.split("[" + this.separator + "]");
         int minIndex = Math.max(0, this.builder.length() - split[split.length - 1].length() - 1);
         this.builder.replace(minIndex, this.builder.length(), "");
      }
   }

   @Override
   public String toString() {
      return this.builder.toString();
   }

   protected KeyBuilder clone() {
      return new KeyBuilder(this);
   }
}
