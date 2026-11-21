package com.lishid.openinv.util;

public class StringMetric {
   public static float compareJaroWinkler(String a, String b) {
      float jaroScore = compareJaro(a, b);
      if (jaroScore < 0.7F) {
         return jaroScore;
      } else {
         String prefix = commonPrefix(a, b);
         int prefixLength = Math.min(prefix.codePointCount(0, prefix.length()), 4);
         return jaroScore + prefixLength * 0.1F * (1.0F - jaroScore);
      }
   }

   private static float compareJaro(String a, String b) {
      if (a.isEmpty() && b.isEmpty()) {
         return 1.0F;
      } else if (!a.isEmpty() && !b.isEmpty()) {
         int[] charsA = a.codePoints().toArray();
         int[] charsB = b.codePoints().toArray();
         int halfLength = Math.max(0, Math.max(charsA.length, charsB.length) / 2 - 1);
         int[] commonA = getCommonCodePoints(charsA, charsB, halfLength);
         int[] commonB = getCommonCodePoints(charsB, charsA, halfLength);
         float transpositions = 0.0F;
         int commonCharacters = 0;

         for (int length = commonA.length; commonCharacters < length && commonA[commonCharacters] > -1; commonCharacters++) {
            if (commonA[commonCharacters] != commonB[commonCharacters]) {
               transpositions++;
            }
         }

         if (commonCharacters == 0) {
            return 0.0F;
         } else {
            float aCommonRatio = (float)commonCharacters / charsA.length;
            float bCommonRatio = (float)commonCharacters / charsB.length;
            float transpositionRatio = (commonCharacters - transpositions / 2.0F) / commonCharacters;
            return (aCommonRatio + bCommonRatio + transpositionRatio) / 3.0F;
         }
      } else {
         return 0.0F;
      }
   }

   private static int[] getCommonCodePoints(int[] charsA, int[] charsB, int separation) {
      int[] common = new int[Math.min(charsA.length, charsB.length)];
      boolean[] matched = new boolean[charsB.length];
      int commonIndex = 0;
      int i = 0;

      for (int length = charsA.length; i < length; i++) {
         int character = charsA[i];
         int index = indexOf(character, charsB, i - separation, i + separation + 1, matched);
         if (index > -1) {
            common[commonIndex++] = character;
            matched[index] = true;
         }
      }

      if (commonIndex < common.length) {
         common[commonIndex] = -1;
      }

      return common;
   }

   private static int indexOf(int character, int[] buffer, int fromIndex, int toIndex, boolean[] matched) {
      int j = Math.max(0, fromIndex);

      for (int length = Math.min(toIndex, buffer.length); j < length; j++) {
         if (buffer[j] == character && !matched[j]) {
            return j;
         }
      }

      return -1;
   }

   private static String commonPrefix(CharSequence a, CharSequence b) {
      int maxPrefixLength = Math.min(a.length(), b.length());
      int p = 0;

      while (p < maxPrefixLength && a.charAt(p) == b.charAt(p)) {
         p++;
      }

      if (validSurrogatePairAt(a, p - 1) || validSurrogatePairAt(b, p - 1)) {
         p--;
      }

      return a.subSequence(0, p).toString();
   }

   private static boolean validSurrogatePairAt(CharSequence string, int index) {
      return index >= 0
         && index <= string.length() - 2
         && Character.isHighSurrogate(string.charAt(index))
         && Character.isLowSurrogate(string.charAt(index + 1));
   }

   private StringMetric() {
   }
}
