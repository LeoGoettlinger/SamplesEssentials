package com.earth2me.essentials.libs.configurate.loader;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.ConfigurationNode;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import com.earth2me.essentials.libs.kyori.option.Option;
import com.earth2me.essentials.libs.kyori.option.value.ValueSource;
import java.util.Objects;
import java.util.regex.Pattern;

final class ValueSources {
   private ValueSources() {
   }

   public static ValueSource node(final ConfigurationNode node) {
      return new ValueSources.Node(Objects.requireNonNull(node, "node"));
   }

   static final class Node implements ValueSource {
      private static final Pattern PATH_SPLIT = Pattern.compile("[:/]");
      private final ConfigurationNode source;

      Node(final ConfigurationNode source) {
         this.source = source;
      }

      @Nullable
      @Override
      public <T> T value(final Option<T> option) {
         String[] path = PATH_SPLIT.split(option.id(), -1);

         try {
            return this.source.node(path).get(option.valueType().type());
         } catch (SerializationException var4) {
            throw new IllegalArgumentException(var4);
         }
      }
   }
}
