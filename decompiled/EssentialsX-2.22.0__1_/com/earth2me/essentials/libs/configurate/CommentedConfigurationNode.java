package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.configurate.util.CheckedConsumer;

public interface CommentedConfigurationNode extends CommentedConfigurationNodeIntermediary<CommentedConfigurationNode> {
   static ConfigurationNodeFactory<CommentedConfigurationNode> factory() {
      return CommentedConfigurationNode::root;
   }

   static CommentedConfigurationNode root() {
      return root(ConfigurationOptions.defaults());
   }

   static <E extends Exception> CommentedConfigurationNode root(final CheckedConsumer<? super CommentedConfigurationNode, E> action) throws E {
      return root().act(action);
   }

   static CommentedConfigurationNode root(final ConfigurationOptions options) {
      return new CommentedConfigurationNodeImpl(null, null, options);
   }

   static <E extends Exception> CommentedConfigurationNode root(
      final ConfigurationOptions options, final CheckedConsumer<? super CommentedConfigurationNode, E> action
   ) throws E {
      return root(options).act(action);
   }
}
