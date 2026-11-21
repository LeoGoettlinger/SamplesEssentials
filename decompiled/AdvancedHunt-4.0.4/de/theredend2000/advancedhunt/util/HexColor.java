package de.theredend2000.advancedhunt.util;

public class HexColor {
   private HexColor() {
      throw new UnsupportedOperationException("Utility class");
   }

   public static String color(String textToTranslate, char altColorChar) {
      if (textToTranslate == null) {
         return "";
      } else {
         StringBuilder stringBuilder = new StringBuilder();
         char[] textToTranslateCharArray = textToTranslate.toCharArray();
         boolean color = false;
         boolean hashtag = false;
         boolean doubleTag = false;
         int i = 0;

         while (i < textToTranslateCharArray.length) {
            char c = textToTranslateCharArray[i];
            if (doubleTag) {
               doubleTag = false;
               int max = i + 3;
               if (max <= textToTranslateCharArray.length) {
                  boolean match = true;

                  for (int n = i; n < max; n++) {
                     char tmp = textToTranslateCharArray[n];
                     if ((tmp < '0' || tmp > '9') && (tmp < 'a' || tmp > 'f') && (tmp < 'A' || tmp > 'F')) {
                        match = false;
                        break;
                     }
                  }

                  if (match) {
                     stringBuilder.append('§');
                     stringBuilder.append('x');

                     while (i < max) {
                        char tmp = textToTranslateCharArray[i];
                        stringBuilder.append('§');
                        stringBuilder.append(tmp);
                        stringBuilder.append('§');
                        stringBuilder.append(tmp);
                        i++;
                     }
                     continue;
                  }
               }

               stringBuilder.append(altColorChar);
               stringBuilder.append("##");
            }

            if (hashtag) {
               hashtag = false;
               if (c == '#') {
                  doubleTag = true;
                  i++;
                  continue;
               }

               int max = i + 6;
               if (max <= textToTranslateCharArray.length) {
                  boolean match = true;

                  for (int nx = i; nx < max; nx++) {
                     char tmp = textToTranslateCharArray[nx];
                     if ((tmp < '0' || tmp > '9') && (tmp < 'a' || tmp > 'f') && (tmp < 'A' || tmp > 'F')) {
                        match = false;
                        break;
                     }
                  }

                  if (match) {
                     stringBuilder.append('§');
                     stringBuilder.append('x');

                     while (i < max) {
                        stringBuilder.append('§');
                        stringBuilder.append(textToTranslateCharArray[i]);
                        i++;
                     }
                     continue;
                  }
               }

               stringBuilder.append(altColorChar);
               stringBuilder.append('#');
            }

            if (color) {
               color = false;
               if (c == '#') {
                  hashtag = true;
                  i++;
                  continue;
               }

               if (c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c == 'r' || c >= 'k' && c <= 'o' || c >= 'A' && c <= 'F' || c == 'R' || c >= 'K' && c <= 'O'
                  )
                {
                  stringBuilder.append('§');
                  stringBuilder.append(c);
                  i++;
                  continue;
               }

               stringBuilder.append(altColorChar);
            }

            if (c == altColorChar) {
               color = true;
               i++;
            } else {
               stringBuilder.append(c);
               i++;
            }
         }

         if (color) {
            stringBuilder.append(altColorChar);
         } else if (hashtag) {
            stringBuilder.append(altColorChar);
            stringBuilder.append('#');
         } else if (doubleTag) {
            stringBuilder.append(altColorChar);
            stringBuilder.append("##");
         }

         return stringBuilder.toString();
      }
   }
}
