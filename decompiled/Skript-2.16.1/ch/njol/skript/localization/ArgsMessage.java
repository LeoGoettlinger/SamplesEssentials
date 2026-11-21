package ch.njol.skript.localization;

import ch.njol.skript.Skript;
import java.lang.invoke.StringConcatFactory;
import java.util.IllegalFormatException;

public final class ArgsMessage extends Message {
   public ArgsMessage(String key) {
      super(key);
   }

   @Override
   public String toString() {
      throw new UnsupportedOperationException();
   }

   public String toString(Object... args) {
      try {
         String val = this.getValue();
         return val == null ? this.key : StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(String.format(val, args));
      } catch (IllegalFormatException var4) {
         String m = "The formatted message '" + this.key + "' uses an illegal format: " + var4.getLocalizedMessage();
         Skript.adminBroadcast("<red>" + m);
         System.err.println("[Skript] " + m);
         var4.printStackTrace();
         return "[ERROR]";
      }
   }
}
