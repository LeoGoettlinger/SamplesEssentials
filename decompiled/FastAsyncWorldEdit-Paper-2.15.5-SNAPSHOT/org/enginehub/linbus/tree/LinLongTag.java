package org.enginehub.linbus.tree;

import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.token.LinToken;

public final class LinLongTag extends LinNumberTag<Long> {
   private final long value;

   public static LinLongTag of(long value) {
      return new LinLongTag(value);
   }

   private LinLongTag(long value) {
      this.value = value;
   }

   @Override
   public LinTagType<LinLongTag> type() {
      return LinTagType.longTag();
   }

   public Long value() {
      return this.value;
   }

   public long valueAsLong() {
      return this.value;
   }

   @Override
   public LinStream linStream() {
      return LinStream.of(new LinToken.Long(this.value));
   }
}
