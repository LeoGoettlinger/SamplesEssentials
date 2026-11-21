package com.artillexstudios.axrankmenu.libs.axapi.libs.snakeyaml.tokens;

import com.artillexstudios.axrankmenu.libs.axapi.libs.snakeyaml.error.Mark;

public final class FlowSequenceStartToken extends Token {
   public FlowSequenceStartToken(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }

   @Override
   public Token.ID getTokenId() {
      return Token.ID.FlowSequenceStart;
   }
}
