package net.luckperms.api.node;

import java.util.Locale;
import net.luckperms.api.node.types.ChatMetaNode;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.SuffixNode;
import org.checkerframework.checker.nullness.qual.NonNull;

public enum ChatMetaType {
   PREFIX(NodeType.PREFIX) {
      @NonNull
      @Override
      public ChatMetaNode.Builder<?, ?> builder() {
         return PrefixNode.builder();
      }

      @NonNull
      @Override
      public ChatMetaNode.Builder<?, ?> builder(@NonNull String prefix, int priority) {
         return PrefixNode.builder(prefix, priority);
      }
   },
   SUFFIX(NodeType.SUFFIX) {
      @NonNull
      @Override
      public ChatMetaNode.Builder<?, ?> builder() {
         return SuffixNode.builder();
      }

      @NonNull
      @Override
      public ChatMetaNode.Builder<?, ?> builder(@NonNull String suffix, int priority) {
         return SuffixNode.builder(suffix, priority);
      }
   };

   private final String name;
   private final NodeType<? extends ChatMetaNode<?, ?>> nodeType;

   private ChatMetaType(NodeType<? extends ChatMetaNode<?, ?>> nodeType) {
      this.name = nodeType.name().toLowerCase(Locale.ROOT);
      this.nodeType = nodeType;
   }

   @NonNull
   public NodeType<? extends ChatMetaNode<?, ?>> nodeType() {
      return this.nodeType;
   }

   @NonNull
   public abstract ChatMetaNode.Builder<?, ?> builder();

   @NonNull
   public abstract ChatMetaNode.Builder<?, ?> builder(@NonNull String var1, int var2);

   @Override
   public String toString() {
      return this.name;
   }
}
