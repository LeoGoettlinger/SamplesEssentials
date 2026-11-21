package org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.nodes;

import java.util.Objects;
import java.util.Optional;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.ScalarStyle;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;

public class ScalarNode extends Node {
   private final ScalarStyle style;
   private final String value;

   public ScalarNode(Tag tag, boolean resolved, String value, ScalarStyle style, Optional<Mark> startMark, Optional<Mark> endMark) {
      super(tag, startMark, endMark);
      Objects.requireNonNull(value, "value in a Node is required.");
      this.value = value;
      Objects.requireNonNull(style, "Scalar style must be provided.");
      this.style = style;
      this.resolved = resolved;
   }

   public ScalarNode(Tag tag, String value, ScalarStyle style) {
      this(tag, true, value, style, Optional.empty(), Optional.empty());
   }

   public ScalarStyle getScalarStyle() {
      return this.style;
   }

   @Override
   public NodeType getNodeType() {
      return NodeType.SCALAR;
   }

   public String getValue() {
      return this.value;
   }

   @Override
   public String toString() {
      return "<" + this.getClass().getName() + " (tag=" + this.getTag() + ", value=" + this.getValue() + ")>";
   }

   public boolean isPlain() {
      return this.style == ScalarStyle.PLAIN;
   }
}
