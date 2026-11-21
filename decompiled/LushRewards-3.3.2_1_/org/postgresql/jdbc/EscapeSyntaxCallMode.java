package org.postgresql.jdbc;

public enum EscapeSyntaxCallMode {
   SELECT("select"),
   CALL_IF_NO_RETURN("callIfNoReturn"),
   CALL("call");

   private final String value;

   private EscapeSyntaxCallMode(String value) {
      this.value = value;
   }

   public static EscapeSyntaxCallMode of(String mode) {
      for (EscapeSyntaxCallMode escapeSyntaxCallMode : values()) {
         if (escapeSyntaxCallMode.value.equals(mode)) {
            return escapeSyntaxCallMode;
         }
      }

      return SELECT;
   }

   public String value() {
      return this.value;
   }
}
