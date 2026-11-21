package org.enginehub.piston.util;

import com.google.common.base.Splitter;
import com.google.common.collect.Streams;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CaseHelper {
   private static final Splitter CAPITAL_SPLITTER = Splitter.onPattern("(?<!^|\\s)(?=\\p{Lu})").omitEmptyStrings();

   public static String titleToSpacedLower(String titleCase) {
      return Streams.stream(CAPITAL_SPLITTER.split(titleCase))
         .map(
            x -> {
               int firstCp = Character.toLowerCase(x.codePointAt(0));
               IntStream restCp = x.codePoints().skip(1L);
               return Streams.concat(new IntStream[]{IntStream.of(firstCp), restCp})
                  .collect(() -> new StringBuilder(x.length()), StringBuilder::appendCodePoint, StringBuilder::append);
            }
         )
         .collect(Collectors.joining(" "));
   }

   public static String titleToCamel(String titleCase) {
      int firstCp = Character.toLowerCase(titleCase.codePointAt(0));
      IntStream restCp = titleCase.codePoints().skip(1L);
      return Streams.concat(new IntStream[]{IntStream.of(firstCp), restCp})
         .collect(() -> new StringBuilder(titleCase.length()), StringBuilder::appendCodePoint, StringBuilder::append)
         .toString();
   }

   public static String camelToTitle(String camelCase) {
      StringBuilder out = new StringBuilder(camelCase.length());
      int capital = Character.toUpperCase(camelCase.codePointAt(0));
      out.appendCodePoint(capital).append(camelCase, camelCase.offsetByCodePoints(0, 1), camelCase.length());
      return out.toString();
   }
}
