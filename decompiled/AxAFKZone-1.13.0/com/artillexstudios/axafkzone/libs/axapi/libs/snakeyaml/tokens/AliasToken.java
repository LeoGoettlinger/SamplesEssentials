package com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.tokens;

import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.error.Mark;

public final class AliasToken extends Token {
   private final String value;

   public AliasToken(String value, Mark startMark, Mark endMark) {
      super(startMark, endMark);
      if (value == null) {
         throw new NullPointerException("alias is expected");
      } else {
         this.value = value;
      }
   }

   public String getValue() {
      return this.value;
   }

   @Override
   public Token.ID getTokenId() {
      return Token.ID.Alias;
   }
}
