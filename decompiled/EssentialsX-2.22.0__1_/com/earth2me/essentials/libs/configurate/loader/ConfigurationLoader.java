package com.earth2me.essentials.libs.configurate.loader;

import com.earth2me.essentials.libs.configurate.ConfigurateException;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.ConfigurationNodeFactory;
import com.earth2me.essentials.libs.configurate.ConfigurationOptions;
import com.earth2me.essentials.libs.configurate.reference.ConfigurationReference;

public interface ConfigurationLoader<N extends ConfigurationNode> extends ConfigurationNodeFactory<N> {
   default N load() throws ConfigurateException {
      return this.load(this.defaultOptions());
   }

   N load(ConfigurationOptions options) throws ConfigurateException;

   ConfigurationReference<N> loadToReference() throws ConfigurateException;

   void save(ConfigurationNode node) throws ConfigurateException;

   default boolean canLoad() {
      return true;
   }

   default boolean canSave() {
      return true;
   }
}
