package com.fastasyncworldedit.core.configuration;

import java.util.Map;

public interface Configuration extends ConfigurationSection {
   @Override
   void addDefault(final String path, final Object value);

   void addDefaults(final Map<String, Object> defaults);

   void addDefaults(final Configuration defaults);

   void setDefaults(final Configuration defaults);

   Configuration getDefaults();

   ConfigurationOptions options();
}
