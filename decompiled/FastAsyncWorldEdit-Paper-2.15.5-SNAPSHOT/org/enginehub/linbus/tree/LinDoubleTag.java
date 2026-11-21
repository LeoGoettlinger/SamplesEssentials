package org.enginehub.linbus.tree;

import org.enginehub.linbus.stream.LinStream;
import org.enginehub.linbus.stream.token.LinToken;

public final class LinDoubleTag extends LinNumberTag<Double> {
   private final double value;

   public static LinDoubleTag of(double value) {
      return new LinDoubleTag(value);
   }

   private LinDoubleTag(double value) {
      this.value = value;
   }

   @Override
   public LinTagType<LinDoubleTag> type() {
      return LinTagType.doubleTag();
   }

   public Double value() {
      return this.value;
   }

   public double valueAsDouble() {
      return this.value;
   }

   @Override
   public LinStream linStream() {
      return LinStream.of(new LinToken.Double(this.value));
   }
}
