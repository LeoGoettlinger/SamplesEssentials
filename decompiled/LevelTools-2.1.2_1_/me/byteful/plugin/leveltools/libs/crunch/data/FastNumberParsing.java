package me.byteful.plugin.leveltools.libs.crunch.data;

public class FastNumberParsing {
   public static int parseInt(String input) {
      return parseInt(input, 0, input.length());
   }

   public static int parseInt(String input, int start, int end) {
      if (start == end) {
         throw new NumberFormatException("Zero-length input");
      } else {
         int i = start;
         boolean negative = false;
         if (input.charAt(start) == '-') {
            negative = true;
            i = start + 1;
         }

         int output;
         for (output = 0; i < end; i++) {
            char c = input.charAt(i);
            if (c > '9' || c < '0') {
               throw new NumberFormatException("Non-numeric character in input '" + input.substring(start, end) + "'");
            }

            output *= 10;
            output += c - '0';
         }

         return negative ? -output : output;
      }
   }

   public static double parseDouble(String input) {
      return parseDouble(input, 0, input.length());
   }

   public static double parseDouble(String input, int start, int end) {
      if (start == end) {
         throw new NumberFormatException("Zero-length input");
      } else {
         int i = start;
         boolean negative = false;
         if (input.charAt(start) == '-') {
            negative = true;
            i = start + 1;
         }

         double output = 0.0;
         double after = 0.0;

         int decimal;
         for (decimal = -1; i < end; i++) {
            char c = input.charAt(i);
            if (c == '.') {
               if (decimal != -1) {
                  throw new NumberFormatException("Second period in double for input '" + input + "'");
               }

               decimal = i;
            } else {
               if (c > '9' || c < '0') {
                  throw new NumberFormatException("Non-numeric character in input '" + input + "'");
               }

               if (decimal != -1) {
                  after *= 10.0;
                  after += c - '0';
               } else {
                  output *= 10.0;
                  output += c - '0';
               }
            }
         }

         after /= Math.pow(10.0, end - decimal - 1);
         return negative ? -output - after : output + after;
      }
   }
}
