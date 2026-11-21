package org.sethomegui.SetHomeGUI.libs.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.comments.CommentLine;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.comments.CommentType;
import org.sethomegui.SetHomeGUI.libs.utils.format.NodeRole;

public class Comments {
   public static final CommentLine BLANK_LINE = new CommentLine(Optional.empty(), Optional.empty(), "", CommentType.BLANK_LINE);

   @Nullable
   public static List<CommentLine> get(@NotNull Block<?> block, @NotNull NodeRole node, @NotNull Comments.Position position) {
      switch (position) {
         case BEFORE:
            return node == NodeRole.KEY ? block.beforeKeyComments : block.beforeValueComments;
         case INLINE:
            return node == NodeRole.KEY ? block.inlineKeyComments : block.inlineValueComments;
         case AFTER:
            return node == NodeRole.KEY ? block.afterKeyComments : block.afterValueComments;
         default:
            return null;
      }
   }

   @Deprecated
   @Nullable
   public static List<CommentLine> get(@NotNull Block<?> block, @NotNull Comments.NodeType node, @NotNull Comments.Position position) {
      return get(block, node.toRole(), position);
   }

   public static void set(@NotNull Block<?> block, @NotNull NodeRole node, @NotNull Comments.Position position, @Nullable List<CommentLine> comments) {
      if (comments != null) {
         comments = new ArrayList<>(comments);
      }

      switch (position) {
         case BEFORE:
            if (node == NodeRole.KEY) {
               block.beforeKeyComments = comments;
            } else {
               block.beforeValueComments = comments;
            }
            break;
         case INLINE:
            if (node == NodeRole.KEY) {
               block.inlineKeyComments = comments;
            } else {
               block.inlineValueComments = comments;
            }
            break;
         case AFTER:
            if (node == NodeRole.KEY) {
               block.afterKeyComments = comments;
            } else {
               block.afterValueComments = comments;
            }
      }
   }

   @Deprecated
   public static void set(@NotNull Block<?> block, @NotNull Comments.NodeType node, @NotNull Comments.Position position, @Nullable List<CommentLine> comments) {
      set(block, node.toRole(), position, comments);
   }

   public static void remove(@NotNull Block<?> block, @NotNull NodeRole node, @NotNull Comments.Position position) {
      set(block, node, position, null);
   }

   @Deprecated
   public static void remove(@NotNull Block<?> block, @NotNull Comments.NodeType node, @NotNull Comments.Position position) {
      set(block, node.toRole(), position, null);
   }

   public static void add(@NotNull Block<?> block, @NotNull NodeRole node, @NotNull Comments.Position position, @NotNull List<CommentLine> comments) {
      comments.forEach(comment -> add(block, node, position, comment));
   }

   @Deprecated
   public static void add(@NotNull Block<?> block, @NotNull Comments.NodeType node, @NotNull Comments.Position position, @NotNull List<CommentLine> comments) {
      comments.forEach(comment -> add(block, node.toRole(), position, comment));
   }

   public static void add(@NotNull Block<?> block, @NotNull NodeRole node, @NotNull Comments.Position position, @NotNull CommentLine comment) {
      switch (position) {
         case BEFORE:
            if (node == NodeRole.KEY) {
               if (block.beforeKeyComments == null) {
                  block.beforeKeyComments = new ArrayList<>();
               }

               block.beforeKeyComments.add(comment);
            } else {
               if (block.beforeValueComments == null) {
                  block.beforeValueComments = new ArrayList<>();
               }

               block.beforeValueComments.add(comment);
            }
            break;
         case INLINE:
            if (node == NodeRole.KEY) {
               if (block.inlineKeyComments == null) {
                  block.inlineKeyComments = new ArrayList<>();
               }

               block.inlineKeyComments.add(comment);
            } else {
               if (block.inlineValueComments == null) {
                  block.inlineValueComments = new ArrayList<>();
               }

               block.inlineValueComments.add(comment);
            }
            break;
         case AFTER:
            if (node == NodeRole.KEY) {
               if (block.afterKeyComments == null) {
                  block.afterKeyComments = new ArrayList<>();
               }

               block.afterKeyComments.add(comment);
            } else {
               if (block.afterValueComments == null) {
                  block.afterValueComments = new ArrayList<>();
               }

               block.afterValueComments.add(comment);
            }
      }
   }

   @Deprecated
   public static void add(@NotNull Block<?> block, @NotNull Comments.NodeType node, @NotNull Comments.Position position, @NotNull CommentLine comment) {
      add(block, node.toRole(), position, comment);
   }

   @NotNull
   public static CommentLine create(@NotNull String comment, @NotNull Comments.Position position) {
      return new CommentLine(Optional.empty(), Optional.empty(), comment, position == Comments.Position.INLINE ? CommentType.IN_LINE : CommentType.BLOCK);
   }

   /** @deprecated */
   public static enum NodeType {
      KEY,
      VALUE;

      public NodeRole toRole() {
         return this == KEY ? NodeRole.KEY : NodeRole.VALUE;
      }
   }

   public static enum Position {
      BEFORE,
      INLINE,
      AFTER;
   }
}
