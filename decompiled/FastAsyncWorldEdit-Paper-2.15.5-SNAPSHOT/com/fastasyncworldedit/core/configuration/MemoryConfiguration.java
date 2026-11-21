package com.fastasyncworldedit.core.configuration;

import java.util.Map;
import java.util.Map.Entry;

public class MemoryConfiguration extends MemorySection implements Configuration {
   protected Configuration defaults;
   protected MemoryConfigurationOptions options;

   public MemoryConfiguration() {
   }

   public MemoryConfiguration(final Configuration defaults) {
      this.defaults = defaults;
   }

   @Override
   public void addDefault(final String path, final Object value) {
      if (path == null) {
         throw new NullPointerException("Path may not be null");
      } else {
         if (this.defaults == null) {
            this.defaults = new MemoryConfiguration();
         }

         this.defaults.set(path, value);
      }
   }

   @Override
   public void addDefaults(final Map<String, Object> defaults) {
      if (defaults == null) {
         throw new NullPointerException("Defaults may not be null");
      } else {
         for (Entry<String, Object> entry : defaults.entrySet()) {
            this.addDefault(entry.getKey(), entry.getValue());
         }
      }
   }

   @Override
   public void addDefaults(final Configuration defaults) {
      if (defaults == null) {
         throw new NullPointerException("Defaults may not be null");
      } else {
         this.addDefaults(defaults.getValues(true));
      }
   }

   @Override
   public void setDefaults(final Configuration defaults) {
      if (defaults == null) {
         throw new NullPointerException("Defaults may not be null");
      } else {
         this.defaults = defaults;
      }
   }

   @Override
   public Configuration getDefaults() {
      return this.defaults;
   }

   @Override
   public ConfigurationSection getParent() {
      return null;
   }

   public MemoryConfigurationOptions options() {
      if (this.options == null) {
         this.options = new MemoryConfigurationOptions(this);
      }

      return this.options;
   }
}
