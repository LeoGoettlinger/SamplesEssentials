package com.artillexstudios.axtrade.libs.axapi.libs.snakeyaml.tokens;

import com.artillexstudios.axtrade.libs.axapi.libs.snakeyaml.error.Mark;

public final class FlowEntryToken extends Token {
   public FlowEntryToken(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }

   @Override
   public Token.ID getTokenId() {
      return Token.ID.FlowEntry;
   }
}
