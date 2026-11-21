package net.essentialsx.api.v2;

import java.util.Locale;

public enum ChatType {
   SHOUT,
   QUESTION,
   LOCAL,
   SPY,
   UNKNOWN("normal");

   private final String key;

   private ChatType() {
      this.key = this.name().toLowerCase(Locale.ENGLISH);
   }

   private ChatType(final String key) {
      this.key = key;
   }

   public String key() {
      return this.key;
   }
}
