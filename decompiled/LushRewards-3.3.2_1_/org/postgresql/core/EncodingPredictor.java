package org.postgresql.core;

import java.io.IOException;
import org.checkerframework.checker.nullness.qual.Nullable;

public class EncodingPredictor {
   private static final EncodingPredictor.Translation[] FATAL_TRANSLATIONS = new EncodingPredictor.Translation[]{
      new EncodingPredictor.Translation("ВАЖНО", null, "ru", "WIN", "ALT", "KOI8"),
      new EncodingPredictor.Translation("致命错误", null, "zh_CN", "EUC_CN", "GBK", "BIG5"),
      new EncodingPredictor.Translation("KATASTROFALNY", null, "pl", "LATIN2"),
      new EncodingPredictor.Translation("FATALE", null, "it", "LATIN1", "LATIN9"),
      new EncodingPredictor.Translation("FATAL", new String[]{"は存在しません", "ロール", "ユーザ"}, "ja", "EUC_JP", "SJIS"),
      new EncodingPredictor.Translation(null, null, "fr/de/es/pt_BR", "LATIN1", "LATIN3", "LATIN4", "LATIN5", "LATIN7", "LATIN9")
   };

   @Nullable
   public static EncodingPredictor.DecodeResult decode(byte[] bytes, int offset, int length) {
      Encoding defaultEncoding = Encoding.defaultEncoding();

      for (EncodingPredictor.Translation tr : FATAL_TRANSLATIONS) {
         for (String encoding : tr.encodings) {
            Encoding encoder = Encoding.getDatabaseEncoding(encoding);
            if (encoder != defaultEncoding) {
               if (tr.fatalText != null) {
                  byte[] encoded;
                  try {
                     byte[] tmp = encoder.encode(tr.fatalText);
                     encoded = new byte[tmp.length + 2];
                     encoded[0] = 83;
                     encoded[encoded.length - 1] = 0;
                     System.arraycopy(tmp, 0, encoded, 1, tmp.length);
                  } catch (IOException var20) {
                     continue;
                  }

                  if (!arrayContains(bytes, offset, length, encoded, 0, encoded.length)) {
                     continue;
                  }
               }

               if (tr.texts != null) {
                  boolean foundOne = false;

                  for (String text : tr.texts) {
                     try {
                        byte[] textBytes = encoder.encode(text);
                        if (arrayContains(bytes, offset, length, textBytes, 0, textBytes.length)) {
                           foundOne = true;
                           break;
                        }
                     } catch (IOException var21) {
                     }
                  }

                  if (!foundOne) {
                     continue;
                  }
               }

               try {
                  String decoded = encoder.decode(bytes, offset, length);
                  if (decoded.indexOf(65533) == -1) {
                     return new EncodingPredictor.DecodeResult(decoded, encoder.name());
                  }
               } catch (IOException var19) {
               }
            }
         }
      }

      return null;
   }

   private static boolean arrayContains(byte[] first, int firstOffset, int firstLength, byte[] second, int secondOffset, int secondLength) {
      if (firstLength < secondLength) {
         return false;
      } else {
         for (int i = 0; i < firstLength; i++) {
            while (i < firstLength && first[firstOffset + i] != second[secondOffset]) {
               i++;
            }

            int j = 1;

            while (j < secondLength && first[firstOffset + i + j] == second[secondOffset + j]) {
               j++;
            }

            if (j == secondLength) {
               return true;
            }
         }

         return false;
      }
   }

   public static class DecodeResult {
      public final String result;
      @Nullable
      public final String encoding;

      DecodeResult(String result, @Nullable String encoding) {
         this.result = result;
         this.encoding = encoding;
      }
   }

   static class Translation {
      @Nullable
      public final String fatalText;
      @Nullable
      private final String[] texts;
      public final String language;
      public final String[] encodings;

      Translation(@Nullable String fatalText, @Nullable String[] texts, String language, String... encodings) {
         this.fatalText = fatalText;
         this.texts = texts;
         this.language = language;
         this.encodings = encodings;
      }
   }
}
