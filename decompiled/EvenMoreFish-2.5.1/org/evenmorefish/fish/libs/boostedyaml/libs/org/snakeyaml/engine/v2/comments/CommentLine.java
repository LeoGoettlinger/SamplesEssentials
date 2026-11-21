package org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.comments;

import java.util.Objects;
import java.util.Optional;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.events.CommentEvent;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;

public class CommentLine {
   private final Optional<Mark> startMark;
   private final Optional<Mark> endMark;
   private final String value;
   private final CommentType commentType;

   public CommentLine(CommentEvent event) {
      this(event.getStartMark(), event.getEndMark(), event.getValue(), event.getCommentType());
   }

   public CommentLine(Optional<Mark> startMark, Optional<Mark> endMark, String value, CommentType commentType) {
      Objects.requireNonNull(startMark);
      this.startMark = startMark;
      Objects.requireNonNull(endMark);
      this.endMark = endMark;
      Objects.requireNonNull(value);
      this.value = value;
      Objects.requireNonNull(commentType);
      this.commentType = commentType;
   }

   public Optional<Mark> getEndMark() {
      return this.endMark;
   }

   public Optional<Mark> getStartMark() {
      return this.startMark;
   }

   public CommentType getCommentType() {
      return this.commentType;
   }

   public String getValue() {
      return this.value;
   }

   @Override
   public String toString() {
      return "<" + this.getClass().getName() + " (type=" + this.getCommentType() + ", value=" + this.getValue() + ")>";
   }
}
