package com.fastasyncworldedit.core.configuration.file;

import com.fastasyncworldedit.core.configuration.MemoryConfiguration;
import com.fastasyncworldedit.core.configuration.MemoryConfigurationOptions;

public class FileConfigurationOptions extends MemoryConfigurationOptions {
   private String header = null;
   private boolean copyHeader = true;

   protected FileConfigurationOptions(final MemoryConfiguration configuration) {
      super(configuration);
   }

   public FileConfiguration configuration() {
      return (FileConfiguration)super.configuration();
   }

   public FileConfigurationOptions copyDefaults(final boolean value) {
      super.copyDefaults(value);
      return this;
   }

   public FileConfigurationOptions pathSeparator(final char value) {
      super.pathSeparator(value);
      return this;
   }

   public String header() {
      return this.header;
   }

   public FileConfigurationOptions header(final String value) {
      this.header = value;
      return this;
   }

   public boolean copyHeader() {
      return this.copyHeader;
   }

   public FileConfigurationOptions copyHeader(final boolean value) {
      this.copyHeader = value;
      return this;
   }
}
