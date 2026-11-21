package org.enginehub.linbus.tree;

import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.token.LinToken;

public final class LinShortTag extends LinNumberTag<Short> {
   private final short value;

   public static LinShortTag of(short value) {
      return new LinShortTag(value);
   }

   private LinShortTag(short value) {
      this.value = value;
   }

   @Override
   public LinTagType<LinShortTag> type() {
      return LinTagType.shortTag();
   }

   public Short value() {
      return this.value;
   }

   public short valueAsShort() {
      return this.value;
   }

   @Override
   public LinStream linStream() {
      return LinStream.of(new LinToken.Short(this.value));
   }
}
