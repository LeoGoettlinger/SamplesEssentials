package com.earth2me.essentials.libs.configurate;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.NonNull;
import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

abstract class AbstractCommentedConfigurationNode<N extends CommentedConfigurationNodeIntermediary<N>, A extends AbstractCommentedConfigurationNode<N, A>>
   extends AbstractConfigurationNode<N, A>
   implements CommentedConfigurationNodeIntermediary<N> {
   protected static final AtomicReferenceFieldUpdater<AbstractCommentedConfigurationNode, String> COMMENT_UPDATER = AtomicReferenceFieldUpdater.newUpdater(
      AbstractCommentedConfigurationNode.class, String.class, "comment"
   );
   @Nullable
   protected volatile String comment;

   protected AbstractCommentedConfigurationNode(@Nullable final A parent, final A copyOf) {
      super(parent, copyOf);
   }

   protected AbstractCommentedConfigurationNode(@Nullable final Object key, @Nullable final A parent, @NonNull final ConfigurationOptions options) {
      super(key, parent, options);
   }

   @Nullable
   @Override
   public String comment() {
      return this.comment;
   }

   @Override
   public N comment(@Nullable final String comment) {
      if (!Objects.equals(COMMENT_UPDATER.getAndSet(this, comment), comment)) {
         this.attachIfNecessary();
      }

      return this.self();
   }

   @Override
   public N commentIfAbsent(final String comment) {
      if (COMMENT_UPDATER.compareAndSet(this, null, comment)) {
         this.attachIfNecessary();
      }

      return this.self();
   }

   public N from(final ConfigurationNode that) {
      if (that instanceof CommentedConfigurationNodeIntermediary) {
         String otherComment = ((CommentedConfigurationNodeIntermediary)that).comment();
         if (otherComment != null) {
            this.comment(otherComment);
         }
      }

      return super.from(that);
   }

   public N mergeFrom(final ConfigurationNode other) {
      if (other instanceof CommentedConfigurationNodeIntermediary) {
         String otherComment = ((CommentedConfigurationNodeIntermediary)other).comment();
         if (otherComment != null) {
            this.commentIfAbsent(otherComment);
         }
      }

      return super.mergeFrom(other);
   }

   @Override
   public boolean equals(final Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof AbstractCommentedConfigurationNode)) {
         return false;
      } else if (!super.equals(o)) {
         return false;
      } else {
         AbstractCommentedConfigurationNode<?, ?> that = (AbstractCommentedConfigurationNode<?, ?>)o;
         return Objects.equals(this.comment, that.comment);
      }
   }

   @Override
   public int hashCode() {
      int result = super.hashCode();
      return 31 * result + Objects.hashCode(this.comment);
   }
}
