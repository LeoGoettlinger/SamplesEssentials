package com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.tokens;

import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.error.Mark;

public final class StreamEndToken extends Token {
   public StreamEndToken(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }

   @Override
   public Token.ID getTokenId() {
      return Token.ID.StreamEnd;
   }
}
