package de.oliver.fancylib.duration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;

public record FancyDuration(long millis) {
   public static FancyDuration parse(String input) {
      if (input != null && !input.isEmpty()) {
         if (!input.equalsIgnoreCase("never") && !input.equalsIgnoreCase("off") && !input.equalsIgnoreCase("none")) {
            long totalMillis = 0L;
            Matcher matcher = Pattern.compile("(\\d+)(ms|s|m|h|d)").matcher(input);

            int pos;
            for (pos = 0; matcher.find(); pos = matcher.end()) {
               if (matcher.start() != pos) {
                  throw new IllegalArgumentException("Invalid format: " + input);
               }

               long value = Long.parseLong(matcher.group(1));
               if (value < 0L) {
                  throw new IllegalArgumentException("Value cannot be negative: " + value);
               }

               String unit = matcher.group(2);
               switch (unit) {
                  case "ms":
                     totalMillis += value;
                     break;
                  case "s":
                     totalMillis += value * 1000L;
                     break;
                  case "m":
                     totalMillis += value * 60000L;
                     break;
                  case "h":
                     totalMillis += value * 3600000L;
                     break;
                  case "d":
                     totalMillis += value * 86400000L;
                     break;
                  default:
                     throw new IllegalArgumentException("Unknown unit: " + unit);
               }
            }

            if (pos != input.length()) {
               throw new IllegalArgumentException("Invalid format: " + input);
            } else {
               return new FancyDuration(totalMillis);
            }
         } else {
            return new FancyDuration(-1L);
         }
      } else {
         throw new IllegalArgumentException("Input cannot be null or empty");
      }
   }

   public boolean isNever() {
      return this.millis <= 0L;
   }

   public long seconds() {
      return this.millis / 1000L;
   }

   public long minutes() {
      return this.millis / 60000L;
   }

   public long hours() {
      return this.millis / 3600000L;
   }

   public long days() {
      return this.millis / 86400000L;
   }

   @NotNull
   @Override
   public String toString() {
      long remainingMillis = this.millis;
      long days = remainingMillis / 86400000L;
      remainingMillis %= 86400000L;
      long hours = remainingMillis / 3600000L;
      remainingMillis %= 3600000L;
      long minutes = remainingMillis / 60000L;
      remainingMillis %= 60000L;
      long seconds = remainingMillis / 1000L;
      remainingMillis %= 1000L;
      StringBuilder sb = new StringBuilder();
      if (days > 0L) {
         sb.append(days).append("d");
      }

      if (hours > 0L) {
         sb.append(hours).append("h");
      }

      if (minutes > 0L) {
         sb.append(minutes).append("m");
      }

      if (seconds > 0L) {
         sb.append(seconds).append("s");
      }

      if (remainingMillis > 0L) {
         sb.append(remainingMillis).append("ms");
      }

      return sb.toString();
   }
}
