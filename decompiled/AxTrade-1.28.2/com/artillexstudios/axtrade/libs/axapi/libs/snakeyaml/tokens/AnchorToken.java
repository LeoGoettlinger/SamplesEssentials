package com.artillexstudios.axtrade.libs.axapi.libs.snakeyaml.tokens;

import com.artillexstudios.axtrade.libs.axapi.libs.snakeyaml.error.Mark;

public final class AnchorToken extends Token {
   private final String value;

   public AnchorToken(String value, Mark startMark, Mark endMark) {
      super(startMark, endMark);
      this.value = value;
   }

   public String getValue() {
      return this.value;
   }

   @Override
   public Token.ID getTokenId() {
      return Token.ID.Anchor;
   }
}
