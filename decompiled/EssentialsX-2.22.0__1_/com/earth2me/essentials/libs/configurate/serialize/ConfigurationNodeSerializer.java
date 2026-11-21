package com.earth2me.essentials.libs.configurate.serialize;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.BasicConfigurationNode;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.ConfigurationOptions;
import java.lang.reflect.Type;

class ConfigurationNodeSerializer implements TypeSerializer<ConfigurationNode> {
   static final Class<ConfigurationNode> TYPE = ConfigurationNode.class;

   public ConfigurationNode deserialize(final Type type, final ConfigurationNode node) {
      return node.copy();
   }

   public void serialize(final Type type, @Nullable final ConfigurationNode obj, final ConfigurationNode node) throws SerializationException {
      node.set(obj);
   }

   @Nullable
   public ConfigurationNode emptyValue(final Type specificType, final ConfigurationOptions options) {
      return BasicConfigurationNode.root(options);
   }
}
