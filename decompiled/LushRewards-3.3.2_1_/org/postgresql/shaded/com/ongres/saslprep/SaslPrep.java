package org.postgresql.shaded.com.ongres.saslprep;

import java.nio.CharBuffer;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;
import org.postgresql.shaded.com.ongres.stringprep.StringPrep;

public class SaslPrep {
   private static final int MAX_UTF = 65535;

   public static String saslPrep(String value, boolean storedString) {
      List<Integer> valueBuilder = new ArrayList<>();
      List<Integer> codePoints = new ArrayList<>();

      for (int i = 0; i < value.length(); i++) {
         int codePoint = value.codePointAt(i);
         codePoints.add(codePoint);
         if (codePoint > 65535) {
            i++;
         }

         if (!StringPrep.prohibitionNonAsciiSpace(codePoint)) {
            valueBuilder.add(codePoint);
         }
      }

      StringBuilder stringBuilder = new StringBuilder();

      for (int codePointx : codePoints) {
         if (!StringPrep.mapToNothing(codePointx)) {
            char[] characters = Character.toChars(codePointx);
            stringBuilder.append(characters);
         }
      }

      String normalized = Normalizer.normalize(CharBuffer.wrap(stringBuilder.toString().toCharArray()), Form.NFKC);
      valueBuilder = new ArrayList<>();

      for (int i = 0; i < normalized.length(); i++) {
         int codePointxx = normalized.codePointAt(i);
         codePoints.add(codePointxx);
         if (codePointxx > 65535) {
            i++;
         }

         if (!StringPrep.prohibitionNonAsciiSpace(codePointxx)) {
            valueBuilder.add(codePointxx);
         }
      }

      for (int character : valueBuilder) {
         if (StringPrep.prohibitionNonAsciiSpace(character)
            || StringPrep.prohibitionAsciiControl(character)
            || StringPrep.prohibitionNonAsciiControl(character)
            || StringPrep.prohibitionPrivateUse(character)
            || StringPrep.prohibitionNonCharacterCodePoints(character)
            || StringPrep.prohibitionSurrogateCodes(character)
            || StringPrep.prohibitionInappropriatePlainText(character)
            || StringPrep.prohibitionInappropriateCanonicalRepresentation(character)
            || StringPrep.prohibitionChangeDisplayProperties(character)
            || StringPrep.prohibitionTaggingCharacters(character)) {
            throw new IllegalArgumentException("Prohibited character " + String.valueOf(Character.toChars(character)));
         }

         if (storedString && StringPrep.unassignedCodePoints(character)) {
            throw new IllegalArgumentException("Prohibited character " + String.valueOf(Character.toChars(character)));
         }
      }

      StringPrep.bidirectional(valueBuilder);
      return normalized;
   }
}
