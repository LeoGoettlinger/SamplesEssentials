package org.jdbi.v3.core.mapper;

import java.util.Locale;
import java.util.function.UnaryOperator;

public enum CaseStrategy implements UnaryOperator<String> {
   NOP {
      public String apply(String t) {
         return t;
      }
   },
   LOWER {
      public String apply(String t) {
         return t.toLowerCase(Locale.ROOT);
      }
   },
   UPPER {
      public String apply(String t) {
         return t.toUpperCase(Locale.ROOT);
      }
   },
   LOCALE_LOWER {
      public String apply(String t) {
         return t.toLowerCase(Locale.getDefault());
      }
   },
   LOCALE_UPPER {
      public String apply(String t) {
         return t.toUpperCase(Locale.getDefault());
      }
   };
}
