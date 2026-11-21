package org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.tokens;

import java.util.Objects;
import java.util.Optional;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.comments.CommentType;
import org.sethomegui.SetHomeGUI.libs.libs.org.snakeyaml.engine.v2.exceptions.Mark;

public final class CommentToken extends Token {
   private final CommentType type;
   private final String value;

   public CommentToken(CommentType type, String value, Optional<Mark> startMark, Optional<Mark> endMark) {
      super(startMark, endMark);
      Objects.requireNonNull(type);
      this.type = type;
      Objects.requireNonNull(value);
      this.value = value;
   }

   public CommentType getCommentType() {
      return this.type;
   }

   public String getValue() {
      return this.value;
   }

   @Override
   public Token.ID getTokenId() {
      return Token.ID.Comment;
   }
}
