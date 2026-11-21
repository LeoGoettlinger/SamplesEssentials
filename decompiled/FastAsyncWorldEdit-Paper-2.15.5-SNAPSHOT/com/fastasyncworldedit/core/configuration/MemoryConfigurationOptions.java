package com.fastasyncworldedit.core.configuration;

public class MemoryConfigurationOptions extends ConfigurationOptions {
   protected MemoryConfigurationOptions(final MemoryConfiguration configuration) {
      super(configuration);
   }

   public MemoryConfiguration configuration() {
      return (MemoryConfiguration)super.configuration();
   }

   public MemoryConfigurationOptions copyDefaults(final boolean value) {
      super.copyDefaults(value);
      return this;
   }

   public MemoryConfigurationOptions pathSeparator(final char value) {
      super.pathSeparator(value);
      return this;
   }
}
