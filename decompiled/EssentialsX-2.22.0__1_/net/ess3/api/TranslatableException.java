package net.ess3.api;

import com.earth2me.essentials.I18n;
import com.earth2me.essentials.adventure.AdventureUtil;

public class TranslatableException extends Exception {
   private final String tlKey;
   private final Object[] args;

   public TranslatableException(String tlKey, Object... args) {
      this(null, tlKey, args);
   }

   public TranslatableException(Throwable cause, String tlKey, Object... args) {
      this.tlKey = tlKey;
      this.args = args;
      if (cause != null) {
         this.initCause(cause);
      }
   }

   public TranslatableException setCause(Throwable cause) {
      this.initCause(cause);
      return this;
   }

   public String getTlKey() {
      return this.tlKey;
   }

   public Object[] getArgs() {
      return this.args;
   }

   @Override
   public String getMessage() {
      String literal = I18n.tlLiteral(this.tlKey, this.args);
      return AdventureUtil.getAdventureFacet().miniToLegacy(literal);
   }
}
