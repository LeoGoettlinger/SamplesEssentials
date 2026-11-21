package org.enginehub.linbus.tree;

import java.util.Objects;
import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.token.LinToken;

public final class LinStringTag extends LinTag<String> {
   private final String value;

   public static LinStringTag of(String value) {
      return new LinStringTag(value);
   }

   private LinStringTag(String value) {
      this.value = Objects.requireNonNull(value, "value is null");
   }

   @Override
   public LinTagType<LinStringTag> type() {
      return LinTagType.stringTag();
   }

   public String value() {
      return this.value;
   }

   @Override
   public LinStream linStream() {
      return LinStream.of(new LinToken.String(this.value));
   }
}
