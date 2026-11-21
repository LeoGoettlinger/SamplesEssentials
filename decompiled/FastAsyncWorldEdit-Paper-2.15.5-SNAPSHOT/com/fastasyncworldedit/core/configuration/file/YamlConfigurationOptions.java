package com.fastasyncworldedit.core.configuration.file;

public class YamlConfigurationOptions extends FileConfigurationOptions {
   private int indent = 2;

   protected YamlConfigurationOptions(final YamlConfiguration configuration) {
      super(configuration);
   }

   public YamlConfiguration configuration() {
      return (YamlConfiguration)super.configuration();
   }

   public YamlConfigurationOptions copyDefaults(final boolean value) {
      super.copyDefaults(value);
      return this;
   }

   public YamlConfigurationOptions pathSeparator(final char value) {
      super.pathSeparator(value);
      return this;
   }

   public YamlConfigurationOptions header(final String value) {
      super.header(value);
      return this;
   }

   public YamlConfigurationOptions copyHeader(final boolean value) {
      super.copyHeader(value);
      return this;
   }

   public int indent() {
      return this.indent;
   }

   public YamlConfigurationOptions indent(final int value) {
      if (value < 2) {
         throw new IllegalArgumentException("Indent must be at least 2 characters");
      } else if (value > 9) {
         throw new IllegalArgumentException("Indent cannot be greater than 9 characters");
      } else {
         this.indent = value;
         return this;
      }
   }
}
