package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.util.CheckedConsumer;
import java.util.Map;

public interface AttributedConfigurationNode extends CommentedConfigurationNodeIntermediary<AttributedConfigurationNode> {
   String TAG_ROOT = "root";

   static ConfigurationNodeFactory<AttributedConfigurationNode> factory() {
      return options -> root("root", options);
   }

   static AttributedConfigurationNode root() {
      return root("root", ConfigurationOptions.defaults());
   }

   static <E extends Exception> AttributedConfigurationNode root(final CheckedConsumer<? super AttributedConfigurationNode, E> action) throws E {
      return root().act(action);
   }

   static AttributedConfigurationNode root(final String tagName) {
      return root(tagName, ConfigurationOptions.defaults());
   }

   static <E extends Exception> AttributedConfigurationNode root(final String tagName, final CheckedConsumer<? super AttributedConfigurationNode, E> action) throws E {
      return root(tagName).act(action);
   }

   static AttributedConfigurationNode root(final String tagName, final ConfigurationOptions options) {
      return new AttributedConfigurationNodeImpl(tagName, null, null, options);
   }

   static <E extends Exception> AttributedConfigurationNode root(
      final String tagName, final ConfigurationOptions options, CheckedConsumer<? super AttributedConfigurationNode, E> action
   ) throws E {
      return root(tagName, options).act(action);
   }

   String tagName();

   AttributedConfigurationNode tagName(String name);

   AttributedConfigurationNode addAttribute(String name, String value);

   AttributedConfigurationNode removeAttribute(String name);

   AttributedConfigurationNode attributes(Map<String, String> attributes);

   Map<String, String> attributes();

   boolean hasAttributes();

   @Nullable
   String attribute(String name);
}
