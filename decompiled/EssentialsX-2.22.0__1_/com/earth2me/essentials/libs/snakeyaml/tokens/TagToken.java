package com.earth2me.essentials.libs.snakeyaml.tokens;

import com.earth2me.essentials.libs.snakeyaml.error.Mark;

public final class TagToken extends Token {
   private final TagTuple value;

   public TagToken(TagTuple value, Mark startMark, Mark endMark) {
      super(startMark, endMark);
      this.value = value;
   }

   public TagTuple getValue() {
      return this.value;
   }

   @Override
   public Token.ID getTokenId() {
      return Token.ID.Tag;
   }
}
