package github.nighter.smartspawner.api.gui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GuiDurationParser {
   private static final long TICKS_PER_SECOND = 20L;
   private static final long TICKS_PER_MINUTE = 1200L;
   private static final long TICKS_PER_HOUR = 72000L;
   private static final long TICKS_PER_DAY = 1728000L;
   private static final long TICKS_PER_WEEK = 12096000L;
   private static final long TICKS_PER_MONTH = 51840000L;
   private static final long TICKS_PER_YEAR = 630720000L;
   private static final Pattern DURATION_PATTERN = Pattern.compile(
      "(?:(\\d+)y)?_?(?:(\\d+)mo)?_?(?:(\\d+)w)?_?(?:(\\d+)d)?_?(?:(\\d+)h)?_?(?:(\\d+)m)?_?(?:(\\d+)s)?", 2
   );

   private GuiDurationParser() {
   }

   public static long parseToTicks(String value) {
      if (value != null && !value.isBlank()) {
         String trimmed = value.trim();

         try {
            long ticks = Long.parseLong(trimmed);
            if (ticks < 0L) {
               throw new IllegalArgumentException("Duration cannot be negative: " + value);
            } else {
               return ticks;
            }
         } catch (NumberFormatException var7) {
            Matcher matcher = DURATION_PATTERN.matcher(trimmed);
            if (!matcher.matches()) {
               throw new IllegalArgumentException("Invalid duration: " + value);
            } else {
               long ticksx = 0L;
               boolean hasUnit = false;

               for (int group = 1; group <= 7; group++) {
                  if (matcher.group(group) != null) {
                     hasUnit = true;
                     break;
                  }
               }

               ticksx = add(ticksx, matcher.group(1), 630720000L);
               ticksx = add(ticksx, matcher.group(2), 51840000L);
               ticksx = add(ticksx, matcher.group(3), 12096000L);
               ticksx = add(ticksx, matcher.group(4), 1728000L);
               ticksx = add(ticksx, matcher.group(5), 72000L);
               ticksx = add(ticksx, matcher.group(6), 1200L);
               ticksx = add(ticksx, matcher.group(7), 20L);
               if (!hasUnit) {
                  throw new IllegalArgumentException("Invalid duration: " + value);
               } else {
                  return ticksx;
               }
            }
         }
      } else {
         throw new IllegalArgumentException("Duration cannot be blank");
      }
   }

   private static long add(long total, String amount, long multiplier) {
      if (amount == null) {
         return total;
      } else {
         try {
            return Math.addExact(total, Math.multiplyExact(Long.parseLong(amount), multiplier));
         } catch (ArithmeticException var6) {
            throw new IllegalArgumentException("Duration is too large", var6);
         }
      }
   }
}
