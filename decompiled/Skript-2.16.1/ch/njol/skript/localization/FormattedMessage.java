package ch.njol.skript.localization;

import ch.njol.skript.Skript;
import java.lang.invoke.StringConcatFactory;
import java.util.IllegalFormatException;

public final class FormattedMessage extends Message {
   private final Object[] args;

   public FormattedMessage(String key, Object... args) {
      super(key);

      assert args.length > 0;

      this.args = args;
   }

   @Override
   public String toString() {
      try {
         String val = this.getValue();
         return val == null ? this.key : StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(String.format(val, this.args));
      } catch (IllegalFormatException var3) {
         String m = "The formatted message '" + this.key + "' uses an illegal format: " + var3.getLocalizedMessage();
         Skript.adminBroadcast("<red>" + m);
         System.err.println("[Skript] " + m);
         var3.printStackTrace();
         return "[ERROR]";
      }
   }
}
