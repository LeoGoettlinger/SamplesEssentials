package org.jsoup.nodes;

public class Range {
   private static final Range.Position UntrackedPos = new Range.Position(-1, -1, -1);
   private final Range.Position start;
   private final Range.Position end;
   static final Range Untracked = new Range(UntrackedPos, UntrackedPos);

   public Range(Range.Position start, Range.Position end) {
      this.start = start;
      this.end = end;
   }

   public Range.Position start() {
      return this.start;
   }

   public int startPos() {
      return this.start.pos;
   }

   public Range.Position end() {
      return this.end;
   }

   public int endPos() {
      return this.end.pos;
   }

   public boolean isTracked() {
      return this != Untracked;
   }

   public boolean isImplicit() {
      return !this.isTracked() ? false : this.start.equals(this.end);
   }

   static Range of(Node node, boolean start) {
      String key = start ? "jsoup.start" : "jsoup.end";
      if (!node.hasAttributes()) {
         return Untracked;
      } else {
         Object range = node.attributes().userData(key);
         return range != null ? (Range)range : Untracked;
      }
   }

   @Deprecated
   public void track(Node node, boolean start) {
   }

   @Override
   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Range range = (Range)o;
         return !this.start.equals(range.start) ? false : this.end.equals(range.end);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.start.hashCode();
      return 31 * result + this.end.hashCode();
   }

   @Override
   public String toString() {
      return this.start + "-" + this.end;
   }

   public static class AttributeRange {
      static final Range.AttributeRange UntrackedAttr = new Range.AttributeRange(Range.Untracked, Range.Untracked);
      private final Range nameRange;
      private final Range valueRange;

      public AttributeRange(Range nameRange, Range valueRange) {
         this.nameRange = nameRange;
         this.valueRange = valueRange;
      }

      public Range nameRange() {
         return this.nameRange;
      }

      public Range valueRange() {
         return this.valueRange;
      }

      @Override
      public String toString() {
         return this.nameRange().toString() + "=" + this.valueRange().toString();
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            Range.AttributeRange that = (Range.AttributeRange)o;
            return !this.nameRange.equals(that.nameRange) ? false : this.valueRange.equals(that.valueRange);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = this.nameRange.hashCode();
         return 31 * result + this.valueRange.hashCode();
      }
   }

   public static class Position {
      private final int pos;
      private final int lineNumber;
      private final int columnNumber;

      public Position(int pos, int lineNumber, int columnNumber) {
         this.pos = pos;
         this.lineNumber = lineNumber;
         this.columnNumber = columnNumber;
      }

      public int pos() {
         return this.pos;
      }

      public int lineNumber() {
         return this.lineNumber;
      }

      public int columnNumber() {
         return this.columnNumber;
      }

      public boolean isTracked() {
         return this != Range.UntrackedPos;
      }

      @Override
      public String toString() {
         return this.lineNumber + "," + this.columnNumber + ":" + this.pos;
      }

      @Override
      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            Range.Position position = (Range.Position)o;
            if (this.pos != position.pos) {
               return false;
            } else {
               return this.lineNumber != position.lineNumber ? false : this.columnNumber == position.columnNumber;
            }
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = this.pos;
         result = 31 * result + this.lineNumber;
         return 31 * result + this.columnNumber;
      }
   }
}
