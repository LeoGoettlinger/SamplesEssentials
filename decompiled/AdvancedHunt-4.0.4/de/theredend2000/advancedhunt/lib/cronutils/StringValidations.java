package de.theredend2000.advancedhunt.lib.cronutils;

import de.theredend2000.advancedhunt.lib.cronutils.model.field.constraint.FieldConstraints;
import de.theredend2000.advancedhunt.lib.cronutils.model.field.value.SpecialChar;
import de.theredend2000.advancedhunt.lib.cronutils.utils.VisibleForTesting;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringValidations {
   private static final String ESCAPED_END = ")\\b";
   private static final String ESCAPED_START = "\\b(";
   private static final SpecialChar[] SPECIAL_CHARS = new SpecialChar[]{SpecialChar.L, SpecialChar.LW, SpecialChar.W};
   private static final Pattern NUMS_AND_CHARS_PATTERN = Pattern.compile("[#\\?/\\*0-9]");
   private final Pattern stringToIntKeysPattern;
   private final Pattern lwPattern;

   public StringValidations(FieldConstraints constraints) {
      this.lwPattern = this.buildLWPattern(constraints.getSpecialChars());
      this.stringToIntKeysPattern = this.buildStringToIntPattern(constraints.getStringMappingKeySet());
   }

   @VisibleForTesting
   Pattern buildStringToIntPattern(Set<String> strings) {
      return this.buildWordsPattern(strings);
   }

   @VisibleForTesting
   public String removeValidChars(String exp) {
      Matcher numsAndCharsMatcher = NUMS_AND_CHARS_PATTERN.matcher(exp.toUpperCase());
      Matcher stringToIntKeysMatcher = this.stringToIntKeysPattern.matcher(numsAndCharsMatcher.replaceAll(""));
      Matcher specialWordsMatcher = this.lwPattern.matcher(stringToIntKeysMatcher.replaceAll(""));
      return specialWordsMatcher.replaceAll("").replaceAll("\\s+", "").replaceAll(",", "").replaceAll("-", "");
   }

   @VisibleForTesting
   Pattern buildLWPattern(Set<SpecialChar> specialChars) {
      Set<String> scs = new HashSet<>();

      for (SpecialChar sc : SPECIAL_CHARS) {
         if (specialChars.contains(sc)) {
            scs.add(sc.name());
         }
      }

      return this.buildWordsPattern(scs);
   }

   @VisibleForTesting
   Pattern buildWordsPattern(Set<String> words) {
      StringBuilder builder = new StringBuilder("\\b(");
      Iterator<String> iterator = words.iterator();
      if (!iterator.hasNext()) {
         builder.append(")\\b");
         return Pattern.compile(builder.toString());
      } else {
         String next = iterator.next();
         builder.append(next);

         while (iterator.hasNext()) {
            builder.append("|");
            builder.append(iterator.next());
         }

         builder.append(")\\b");
         return Pattern.compile(builder.toString());
      }
   }
}
