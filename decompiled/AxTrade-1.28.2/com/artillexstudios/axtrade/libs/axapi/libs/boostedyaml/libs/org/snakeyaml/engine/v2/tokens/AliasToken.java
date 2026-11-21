package com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens;

import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.Anchor;
import com.artillexstudios.axtrade.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;
import java.util.Objects;
import java.util.Optional;

public final class AliasToken extends Token {
   private final Anchor value;

   public AliasToken(Anchor value, Optional<Mark> startMark, Optional<Mark> endMark) {
      super(startMark, endMark);
      Objects.requireNonNull(value);
      this.value = value;
   }

   public Anchor getValue() {
      return this.value;
   }

   @Override
   public Token.ID getTokenId() {
      return Token.ID.Alias;
   }
}
