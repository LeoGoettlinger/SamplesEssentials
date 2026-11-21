package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;

class CommentedConfigurationNodeImpl
   extends AbstractCommentedConfigurationNode<CommentedConfigurationNode, CommentedConfigurationNodeImpl>
   implements CommentedConfigurationNode {
   protected CommentedConfigurationNodeImpl(
      @Nullable final Object path, @Nullable final CommentedConfigurationNodeImpl parent, final ConfigurationOptions options
   ) {
      super(path, parent, options);
   }

   protected CommentedConfigurationNodeImpl(@Nullable final CommentedConfigurationNodeImpl parent, final CommentedConfigurationNodeImpl copyOf) {
      super(parent, copyOf);
   }

   protected CommentedConfigurationNodeImpl createNode(final Object path) {
      return new CommentedConfigurationNodeImpl(path, this, this.options());
   }

   protected CommentedConfigurationNodeImpl copy(@Nullable final CommentedConfigurationNodeImpl parent) {
      CommentedConfigurationNodeImpl copy = new CommentedConfigurationNodeImpl(parent, this);
      COMMENT_UPDATER.set(copy, this.comment);
      return copy;
   }

   public CommentedConfigurationNodeImpl self() {
      return this;
   }

   protected CommentedConfigurationNodeImpl implSelf() {
      return this;
   }

   @Override
   public String toString() {
      return "CommentedConfigurationNodeImpl{super=" + super.toString() + ", comment=" + this.comment + '}';
   }
}
