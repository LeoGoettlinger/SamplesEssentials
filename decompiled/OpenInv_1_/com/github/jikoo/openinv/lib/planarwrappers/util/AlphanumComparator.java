package com.github.jikoo.openinv.lib.planarwrappers.util;

import java.util.Comparator;

public class AlphanumComparator implements Comparator<String> {
   protected final boolean isDigit(char ch) {
      return '0' <= ch && ch <= '9';
   }

   private StringBuilder getChunk(AlphanumComparator.CompareData data) {
      StringBuilder chunk = new StringBuilder();
      char c = data.string.charAt(data.marker);
      chunk.append(c);
      data.marker++;

      for (boolean expectingDigit = this.isDigit(c); data.marker < data.length; data.marker++) {
         c = data.string.charAt(data.marker);
         if (expectingDigit != this.isDigit(c)) {
            break;
         }

         chunk.append(c);
      }

      return chunk;
   }

   protected int compareNumeric(StringBuilder thisChunk, StringBuilder thatChunk) {
      int result = thisChunk.length() - thatChunk.length();
      int thisIndex = 0;
      int thatIndex = 0;
      if (result != 0) {
         thisIndex = this.getUnpaddedStart(thisChunk);
         thatIndex = this.getUnpaddedStart(thatChunk);
         result += thatIndex - thisIndex;
         if (result != 0) {
            return result;
         }
      }

      while (thisIndex < thisChunk.length()) {
         result = thisChunk.charAt(thisIndex) - thatChunk.charAt(thatIndex);
         if (result != 0) {
            return result;
         }

         thisIndex++;
         thatIndex++;
      }

      return result;
   }

   private int getUnpaddedStart(CharSequence chunk) {
      for (int index = 0; index < chunk.length(); index++) {
         if (chunk.charAt(index) != '0') {
            return index;
         }
      }

      return chunk.length() - 1;
   }

   protected int compareAlphabetic(StringBuilder thisChunk, StringBuilder thatChunk) {
      return thisChunk.compareTo(thatChunk);
   }

   protected int compareTail(AlphanumComparator.CompareData data1, AlphanumComparator.CompareData data2) {
      return data1.length - data2.length;
   }

   public int compare(String s1, String s2) {
      AlphanumComparator.CompareData data1 = new AlphanumComparator.CompareData(s1);
      AlphanumComparator.CompareData data2 = new AlphanumComparator.CompareData(s2);

      while (data1.marker < data1.length && data2.marker < data2.length) {
         StringBuilder thisChunk = this.getChunk(data1);
         StringBuilder thatChunk = this.getChunk(data2);
         int result;
         if (this.isDigit(thisChunk.charAt(0)) && this.isDigit(thatChunk.charAt(0))) {
            result = this.compareNumeric(thisChunk, thatChunk);
         } else {
            result = this.compareAlphabetic(thisChunk, thatChunk);
         }

         if (result != 0) {
            return result;
         }
      }

      return this.compareTail(data1, data2);
   }

   protected static final class CompareData {
      private int marker = 0;
      private final String string;
      private final int length;

      private CompareData(String string) {
         this.string = string;
         this.length = string.length();
      }

      public int getMarker() {
         return this.marker;
      }

      public String getString() {
         return this.string;
      }

      public int getLength() {
         return this.length;
      }
   }
}
