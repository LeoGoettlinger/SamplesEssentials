package org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.nodes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.comments.CommentLine;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.common.Anchor;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.exceptions.Mark;

public abstract class Node {
   private final Optional<Mark> startMark;
   protected Optional<Mark> endMark;
   protected boolean resolved;
   private Tag tag;
   private boolean recursive;
   private Optional<Anchor> anchor;
   private List<CommentLine> inLineComments;
   private List<CommentLine> blockComments;
   private List<CommentLine> endComments;
   private Map<String, Object> properties;

   public Node(Tag tag, Optional<Mark> startMark, Optional<Mark> endMark) {
      this.setTag(tag);
      this.startMark = startMark;
      this.endMark = endMark;
      this.recursive = false;
      this.resolved = true;
      this.anchor = Optional.empty();
      this.inLineComments = null;
      this.blockComments = null;
      this.endComments = null;
      this.properties = null;
   }

   public Tag getTag() {
      return this.tag;
   }

   public void setTag(Tag tag) {
      Objects.requireNonNull(tag, "tag in a Node is required.");
      this.tag = tag;
   }

   public Optional<Mark> getEndMark() {
      return this.endMark;
   }

   public abstract NodeType getNodeType();

   public Optional<Mark> getStartMark() {
      return this.startMark;
   }

   @Override
   public final boolean equals(Object obj) {
      return super.equals(obj);
   }

   public boolean isRecursive() {
      return this.recursive;
   }

   public void setRecursive(boolean recursive) {
      this.recursive = recursive;
   }

   @Override
   public final int hashCode() {
      return super.hashCode();
   }

   public Optional<Anchor> getAnchor() {
      return this.anchor;
   }

   public void setAnchor(Optional<Anchor> anchor) {
      this.anchor = anchor;
   }

   public Object setProperty(String key, Object value) {
      if (this.properties == null) {
         this.properties = new HashMap<>();
      }

      return this.properties.put(key, value);
   }

   public Object getProperty(String key) {
      return this.properties == null ? null : this.properties.get(key);
   }

   public List<CommentLine> getInLineComments() {
      return this.inLineComments;
   }

   public void setInLineComments(List<CommentLine> inLineComments) {
      this.inLineComments = inLineComments;
   }

   public List<CommentLine> getBlockComments() {
      return this.blockComments;
   }

   public void setBlockComments(List<CommentLine> blockComments) {
      this.blockComments = blockComments;
   }

   public List<CommentLine> getEndComments() {
      return this.endComments;
   }

   public void setEndComments(List<CommentLine> endComments) {
      this.endComments = endComments;
   }
}
