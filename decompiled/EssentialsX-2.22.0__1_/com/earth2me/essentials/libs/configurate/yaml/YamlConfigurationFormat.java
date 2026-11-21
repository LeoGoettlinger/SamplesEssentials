package com.earth2me.essentials.libs.configurate.yaml;

import com.earth2me.essentials.libs.configurate.CommentedConfigurationNode;
import com.earth2me.essentials.libs.configurate.loader.AbstractConfigurationFormat;
import com.earth2me.essentials.libs.configurate.util.UnmodifiableCollections;
import java.util.Set;

public class YamlConfigurationFormat extends AbstractConfigurationFormat<CommentedConfigurationNode, YamlConfigurationLoader, YamlConfigurationLoader.Builder> {
   private static final Set<String> SUPPORTED_EXTENSIONS = UnmodifiableCollections.toSet("yaml", "yml");

   public YamlConfigurationFormat() {
      super("yaml", YamlConfigurationLoader::builder, SUPPORTED_EXTENSIONS);
   }
}
