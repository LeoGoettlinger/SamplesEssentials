package com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.tokens;

import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.comments.CommentType;
import com.artillexstudios.axafkzone.libs.axapi.libs.snakeyaml.error.Mark;
import java.util.Objects;

public final class CommentToken extends Token {
   private final CommentType type;
   private final String value;

   public CommentToken(CommentType type, String value, Mark startMark, Mark endMark) {
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
