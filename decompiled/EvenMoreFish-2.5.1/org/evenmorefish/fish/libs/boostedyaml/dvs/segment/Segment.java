package org.evenmorefish.fish.libs.boostedyaml.dvs.segment;

public interface Segment {
   static Segment range(int start, int end, int step, int fill) {
      return new RangeSegment(start, end, step, fill);
   }

   static Segment range(int start, int end, int step) {
      return new RangeSegment(start, end, step, 0);
   }

   static Segment range(int start, int end) {
      return new RangeSegment(start, end, start < end ? 1 : -1, 0);
   }

   static Segment literal(String... elements) {
      return new LiteralSegment(elements);
   }

   int parse(String var1, int var2);

   String getElement(int var1);

   int getElementLength(int var1);

   int length();
}
