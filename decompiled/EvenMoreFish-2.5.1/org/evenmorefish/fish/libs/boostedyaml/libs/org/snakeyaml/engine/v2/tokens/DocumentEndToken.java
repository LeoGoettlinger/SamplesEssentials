package org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens;

import java.util.Optional;
import org.evenmorefish.fish.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;

public final class DocumentEndToken extends Token {
   public DocumentEndToken(Optional<Mark> startMark, Optional<Mark> endMark) {
      super(startMark, endMark);
   }

   @Override
   public Token.ID getTokenId() {
      return Token.ID.DocumentEnd;
   }
}
