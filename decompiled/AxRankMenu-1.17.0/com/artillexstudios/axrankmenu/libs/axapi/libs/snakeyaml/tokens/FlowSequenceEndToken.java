package com.artillexstudios.axrankmenu.libs.axapi.libs.snakeyaml.tokens;

import com.artillexstudios.axrankmenu.libs.axapi.libs.snakeyaml.error.Mark;

public final class FlowSequenceEndToken extends Token {
   public FlowSequenceEndToken(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }

   @Override
   public Token.ID getTokenId() {
      return Token.ID.FlowSequenceEnd;
   }
}
