package com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens;

import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;
import java.util.Objects;
import java.util.Optional;

public abstract class Token {
   private final Optional<Mark> startMark;
   private final Optional<Mark> endMark;

   public Token(Optional<Mark> startMark, Optional<Mark> endMark) {
      Objects.requireNonNull(startMark);
      Objects.requireNonNull(endMark);
      this.startMark = startMark;
      this.endMark = endMark;
   }

   public Optional<Mark> getStartMark() {
      return this.startMark;
   }

   public Optional<Mark> getEndMark() {
      return this.endMark;
   }

   public abstract Token.ID getTokenId();

   @Override
   public String toString() {
      return this.getTokenId().toString();
   }

   public static enum ID {
      Alias("<alias>"),
      Anchor("<anchor>"),
      BlockEnd("<block end>"),
      BlockEntry("-"),
      BlockMappingStart("<block mapping start>"),
      BlockSequenceStart("<block sequence start>"),
      Directive("<directive>"),
      DocumentEnd("<document end>"),
      DocumentStart("<document start>"),
      FlowEntry(","),
      FlowMappingEnd("}"),
      FlowMappingStart("{"),
      FlowSequenceEnd("]"),
      FlowSequenceStart("["),
      Key("?"),
      Scalar("<scalar>"),
      StreamEnd("<stream end>"),
      StreamStart("<stream start>"),
      Tag("<tag>"),
      Comment("#"),
      Value(":");

      private final String description;

      private ID(String s) {
         this.description = s;
      }

      @Override
      public String toString() {
         return this.description;
      }
   }
}
