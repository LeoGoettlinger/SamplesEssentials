package com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.tokens;

import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.common.ScalarStyle;
import com.artillexstudios.axafkzone.libs.axapi.libs.boostedyaml.libs.org.snakeyaml.engine.v2.exceptions.Mark;
import java.util.Objects;
import java.util.Optional;

public final class ScalarToken extends Token {
   private final String value;
   private final boolean plain;
   private final ScalarStyle style;

   public ScalarToken(String value, boolean plain, Optional<Mark> startMark, Optional<Mark> endMark) {
      this(value, plain, ScalarStyle.PLAIN, startMark, endMark);
   }

   public ScalarToken(String value, boolean plain, ScalarStyle style, Optional<Mark> startMark, Optional<Mark> endMark) {
      super(startMark, endMark);
      Objects.requireNonNull(value);
      this.value = value;
      this.plain = plain;
      Objects.requireNonNull(style);
      this.style = style;
   }

   public boolean isPlain() {
      return this.plain;
   }

   public String getValue() {
      return this.value;
   }

   public ScalarStyle getStyle() {
      return this.style;
   }

   @Override
   public Token.ID getTokenId() {
      return Token.ID.Scalar;
   }

   @Override
   public String toString() {
      return this.getTokenId().toString() + " plain=" + this.plain + " style=" + this.style + " value=" + this.value;
   }
}
