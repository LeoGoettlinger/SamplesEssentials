package com.earth2me.essentials.libs.configurate.loader;

import com.earth2me.essentials.libs.configurate.BasicConfigurationNode;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import java.net.URL;
import java.nio.file.Path;
import java.util.Set;

public interface ConfigurationFormat {
   static ConfigurationFormat forExtension(final String extension) {
      ConfigurationFormats.Holder holder = ConfigurationFormats.BY_EXTENSION.get(extension);
      return holder == null ? null : holder.get();
   }

   static Set<ConfigurationFormat> supportedFormats() {
      return ConfigurationFormats.unwrappedFormats();
   }

   String id();

   Set<String> supportedExtensions();

   default ConfigurationLoader<? extends Object> create(Path file) {
      return this.create(file, BasicConfigurationNode.factory().createNode());
   }

   ConfigurationLoader<? extends Object> create(Path file, ConfigurationNode options);

   default ConfigurationLoader<? extends Object> create(URL url) {
      return this.create(url, BasicConfigurationNode.factory().createNode());
   }

   ConfigurationLoader<? extends Object> create(URL url, ConfigurationNode options);
}
