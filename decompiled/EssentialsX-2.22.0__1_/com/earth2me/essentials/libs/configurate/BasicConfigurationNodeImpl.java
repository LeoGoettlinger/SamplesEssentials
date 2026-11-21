package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;

class BasicConfigurationNodeImpl extends AbstractConfigurationNode<BasicConfigurationNode, BasicConfigurationNodeImpl> implements BasicConfigurationNode {
   protected BasicConfigurationNodeImpl(@Nullable final Object key, @Nullable final BasicConfigurationNodeImpl parent, final ConfigurationOptions options) {
      super(key, parent, options);
   }

   protected BasicConfigurationNodeImpl(@Nullable final BasicConfigurationNodeImpl parent, final BasicConfigurationNodeImpl copyOf) {
      super(parent, copyOf);
   }

   protected BasicConfigurationNodeImpl copy(@Nullable final BasicConfigurationNodeImpl parent) {
      return new BasicConfigurationNodeImpl(parent, this);
   }

   public BasicConfigurationNodeImpl self() {
      return this;
   }

   protected BasicConfigurationNodeImpl implSelf() {
      return this;
   }

   protected BasicConfigurationNodeImpl createNode(final Object path) {
      return new BasicConfigurationNodeImpl(path, this, this.options());
   }
}
