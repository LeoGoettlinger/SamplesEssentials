package com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.tokens;

import com.earth2me.essentials.libs.configurate.yaml.internal.snakeyaml.error.Mark;

public final class FlowMappingEndToken extends Token {
   public FlowMappingEndToken(Mark startMark, Mark endMark) {
      super(startMark, endMark);
   }

   @Override
   public Token.ID getTokenId() {
      return Token.ID.FlowMappingEnd;
   }
}
