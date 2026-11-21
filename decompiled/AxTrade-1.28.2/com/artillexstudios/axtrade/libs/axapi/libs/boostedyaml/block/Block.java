package com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.block;

import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.comments.CommentLine;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.comments.CommentType;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.MappingNode;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.Node;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.NodeTuple;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes.SequenceNode;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.utils.format.NodeRole;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class Block<T> {
   @Nullable
   List<CommentLine> beforeKeyComments = null;
   @Nullable
   List<CommentLine> inlineKeyComments = null;
   @Nullable
   List<CommentLine> afterKeyComments = null;
   @Nullable
   List<CommentLine> beforeValueComments = null;
   @Nullable
   List<CommentLine> inlineValueComments = null;
   @Nullable
   List<CommentLine> afterValueComments = null;
   private T value;
   private boolean ignored;

   public Block(@Nullable Node keyNode, @Nullable Node valueNode, @Nullable T value) {
      this.value = value;
      this.init(keyNode, valueNode);
   }

   public Block(@Nullable T value) {
      this(null, null, value);
   }

   public Block(@Nullable Block<?> previous, @Nullable T value) {
      this.value = value;
      if (previous != null) {
         this.beforeKeyComments = previous.beforeKeyComments;
         this.inlineKeyComments = previous.inlineKeyComments;
         this.afterKeyComments = previous.afterKeyComments;
         this.beforeValueComments = previous.beforeValueComments;
         this.inlineValueComments = previous.inlineValueComments;
         this.afterValueComments = previous.afterValueComments;
      }
   }

   protected void init(@Nullable Node key, @Nullable Node value) {
      if (key != null) {
         this.beforeKeyComments = (List<CommentLine>)(key.getBlockComments() == null ? new ArrayList<>(0) : key.getBlockComments());
         this.inlineKeyComments = key.getInLineComments();
         this.afterKeyComments = key.getEndComments();
         this.collectComments(key, this.beforeKeyComments, true);
      }

      if (value != null) {
         this.beforeValueComments = (List<CommentLine>)(value.getBlockComments() == null ? new ArrayList<>(0) : value.getBlockComments());
         this.inlineValueComments = value.getInLineComments();
         this.afterValueComments = value.getEndComments();
         this.collectComments(value, this.beforeValueComments, true);
      }
   }

   private void collectComments(@NotNull Node node, @NotNull List<CommentLine> destination, boolean initial) {
      if (!initial) {
         if (node.getBlockComments() != null) {
            destination.addAll(this.toBlockComments(node.getBlockComments()));
         }

         if (node.getInLineComments() != null) {
            destination.addAll(this.toBlockComments(node.getInLineComments()));
         }

         if (node.getEndComments() != null) {
            destination.addAll(this.toBlockComments(node.getEndComments()));
         }
      }

      if (node instanceof SequenceNode) {
         SequenceNode sequenceNode = (SequenceNode)node;

         for (Node sub : sequenceNode.getValue()) {
            this.collectComments(sub, destination, false);
         }
      } else if (!initial && node instanceof MappingNode) {
         MappingNode mappingNode = (MappingNode)node;

         for (NodeTuple sub : mappingNode.getValue()) {
            this.collectComments(sub.getKeyNode(), destination, false);
            this.collectComments(sub.getValueNode(), destination, false);
         }
      }
   }

   private List<CommentLine> toBlockComments(@NotNull List<CommentLine> commentLines) {
      int i = -1;

      for (CommentLine commentLine : commentLines) {
         commentLines.set(
            ++i,
            commentLine.getCommentType() != CommentType.IN_LINE
               ? commentLine
               : new CommentLine(commentLine.getStartMark(), commentLine.getEndMark(), commentLine.getValue(), CommentType.BLOCK)
         );
      }

      return commentLines;
   }

   public void setValue(T value) {
      this.value = value;
   }

   @Nullable
   public List<String> getComments() {
      List<CommentLine> comments = Comments.get(this, NodeRole.KEY, Comments.Position.BEFORE);
      return comments == null ? null : comments.stream().map(CommentLine::getValue).collect(Collectors.toList());
   }

   public void setComments(@Nullable List<String> comments) {
      Comments.set(
         this,
         NodeRole.KEY,
         Comments.Position.BEFORE,
         comments == null ? null : comments.stream().map(comment -> Comments.create(comment, Comments.Position.BEFORE)).collect(Collectors.toList())
      );
   }

   public void removeComments() {
      Comments.remove(this, NodeRole.KEY, Comments.Position.BEFORE);
   }

   public void addComments(@NotNull List<String> comments) {
      Comments.add(
         this,
         NodeRole.KEY,
         Comments.Position.BEFORE,
         comments.stream().map(comment -> Comments.create(comment, Comments.Position.BEFORE)).collect(Collectors.toList())
      );
   }

   public void addComment(@NotNull String comment) {
      Comments.add(this, NodeRole.KEY, Comments.Position.BEFORE, Comments.create(comment, Comments.Position.BEFORE));
   }

   public void setIgnored(boolean ignored) {
      this.ignored = ignored;
   }

   public boolean isIgnored() {
      return this.ignored;
   }

   public abstract boolean isSection();

   public T getStoredValue() {
      return this.value;
   }
}
