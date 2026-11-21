package com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.events;

import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;
import java.util.Optional;

public abstract class Event {
   private final Optional<Mark> startMark;
   private final Optional<Mark> endMark;

   public Event(Optional<Mark> startMark, Optional<Mark> endMark) {
      if ((!startMark.isPresent() || endMark.isPresent()) && (startMark.isPresent() || !endMark.isPresent())) {
         this.startMark = startMark;
         this.endMark = endMark;
      } else {
         throw new NullPointerException("Both marks must be either present or absent.");
      }
   }

   public Event() {
      this(Optional.empty(), Optional.empty());
   }

   public Optional<Mark> getStartMark() {
      return this.startMark;
   }

   public Optional<Mark> getEndMark() {
      return this.endMark;
   }

   public abstract Event.ID getEventId();

   public static enum ID {
      Alias,
      Comment,
      DocumentEnd,
      DocumentStart,
      MappingEnd,
      MappingStart,
      Scalar,
      SequenceEnd,
      SequenceStart,
      StreamEnd,
      StreamStart;
   }
}
