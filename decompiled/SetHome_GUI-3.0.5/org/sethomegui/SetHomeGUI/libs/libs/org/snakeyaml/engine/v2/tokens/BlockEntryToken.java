package org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.tokens;

import java.util.Optional;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.exceptions.Mark;

public final class BlockEntryToken extends Token {
   public BlockEntryToken(Optional<Mark> startMark, Optional<Mark> endMark) {
      super(startMark, endMark);
   }

   @Override
   public Token.ID getTokenId() {
      return Token.ID.BlockEntry;
   }
}
