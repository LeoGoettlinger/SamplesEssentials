package com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.events;

import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.comments.CommentType;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;
import java.util.Objects;
import java.util.Optional;

public final class CommentEvent extends Event {
   private final CommentType type;
   private final String value;

   public CommentEvent(CommentType type, String value, Optional<Mark> startMark, Optional<Mark> endMark) {
      super(startMark, endMark);
      Objects.requireNonNull(type);
      this.type = type;
      Objects.requireNonNull(value);
      this.value = value;
   }

   public String getValue() {
      return this.value;
   }

   public CommentType getCommentType() {
      return this.type;
   }

   @Override
   public Event.ID getEventId() {
      return Event.ID.Comment;
   }

   @Override
   public String toString() {
      return "=COM " + this.type + " " + this.value;
   }
}
