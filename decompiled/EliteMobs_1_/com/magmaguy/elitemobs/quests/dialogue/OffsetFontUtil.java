package com.magmaguy.elitemobs.quests.dialogue;

public final class OffsetFontUtil {
   public static final String BOX_GLYPH = new String(Character.toChars(986880));
   public static final String BOX_GLYPH_RIGHT = new String(Character.toChars(986891));
   private static final int[] MAGNITUDES = new int[]{512, 256, 128, 64, 32, 16, 8, 4, 2, 1};
   private static final int[] POSITIVE = new int[]{986890, 986889, 986888, 986887, 986886, 986885, 986884, 986883, 986882, 986881};
   private static final int[] NEGATIVE = new int[]{986906, 986905, 986904, 986903, 986902, 986901, 986900, 986899, 986898, 986897};

   public static String boxGlyph() {
      return BOX_GLYPH + pixels(-1) + BOX_GLYPH_RIGHT + pixels(2);
   }

   private OffsetFontUtil() {
   }

   public static String pixels(int advance) {
      if (advance == 0) {
         return "";
      } else {
         int[] glyphs = advance > 0 ? POSITIVE : NEGATIVE;
         int remaining = Math.abs(advance);
         StringBuilder builder = new StringBuilder();

         for (int i = 0; i < MAGNITUDES.length && remaining > 0; i++) {
            while (remaining >= MAGNITUDES[i]) {
               builder.appendCodePoint(glyphs[i]);
               remaining -= MAGNITUDES[i];
            }
         }

         return builder.toString();
      }
   }

   public static String shift(String content, int pixelsRight) {
      if (pixelsRight == 0) {
         return content;
      } else {
         return pixelsRight > 0 ? pixels(2 * pixelsRight) + content : content + pixels(-2 * pixelsRight);
      }
   }

   public static int totalAdvance(String string) {
      int total = 0;
      int i = 0;

      while (i < string.length()) {
         int codepoint = string.codePointAt(i);
         i += Character.charCount(codepoint);

         for (int m = 0; m < MAGNITUDES.length; m++) {
            if (codepoint == POSITIVE[m]) {
               total += MAGNITUDES[m];
               break;
            }

            if (codepoint == NEGATIVE[m]) {
               total -= MAGNITUDES[m];
               break;
            }
         }
      }

      return total;
   }
}
