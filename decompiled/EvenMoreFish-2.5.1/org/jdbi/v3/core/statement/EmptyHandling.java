package org.jdbi.v3.core.statement;

import java.util.function.BiConsumer;

public enum EmptyHandling implements BiConsumer<SqlStatement, String> {
   BLANK {
      public void accept(SqlStatement stmt, String key) {
         stmt.define(key, "");
      }
   },
   NULL_KEYWORD {
      public void accept(SqlStatement stmt, String key) {
         stmt.define(key, "null");
      }
   },
   DEFINE_NULL {
      public void accept(SqlStatement stmt, String key) {
         stmt.define(key, null);
      }
   },
   THROW {
      public void accept(SqlStatement stmt, String key) {
         throw new IllegalArgumentException("argument is null or empty; this is forbidden on this call to `bindList`");
      }
   };
}
