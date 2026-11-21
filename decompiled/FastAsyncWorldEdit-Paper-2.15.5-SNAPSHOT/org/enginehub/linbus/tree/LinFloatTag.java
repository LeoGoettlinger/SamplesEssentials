package org.enginehub.linbus.tree;

import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.token.LinToken;

public final class LinFloatTag extends LinNumberTag<Float> {
   private final float value;

   public static LinFloatTag of(float value) {
      return new LinFloatTag(value);
   }

   private LinFloatTag(float value) {
      this.value = value;
   }

   @Override
   public LinTagType<LinFloatTag> type() {
      return LinTagType.floatTag();
   }

   public Float value() {
      return this.value;
   }

   public float valueAsFloat() {
      return this.value;
   }

   @Override
   public LinStream linStream() {
      return LinStream.of(new LinToken.Float(this.value));
   }
}
