package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.configurate.util.CheckedConsumer;

public interface BasicConfigurationNode extends ScopedConfigurationNode<BasicConfigurationNode> {
   static ConfigurationNodeFactory<BasicConfigurationNode> factory() {
      return BasicConfigurationNode::root;
   }

   static BasicConfigurationNode root() {
      return root(ConfigurationOptions.defaults());
   }

   static <E extends Exception> BasicConfigurationNode root(final CheckedConsumer<? super BasicConfigurationNode, E> maker) throws E {
      return root().act(maker);
   }

   static BasicConfigurationNode root(final ConfigurationOptions options) {
      return new BasicConfigurationNodeImpl(null, null, options);
   }

   static <E extends Exception> BasicConfigurationNode root(final ConfigurationOptions options, final CheckedConsumer<? super BasicConfigurationNode, E> maker) throws E {
      return root(options).act(maker);
   }
}
