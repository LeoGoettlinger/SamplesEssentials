package ch.njol.skript.localization;

import ch.njol.util.StringUtils;
import java.lang.invoke.StringConcatFactory;

public class PluralizingArgsMessage extends Message {
   public PluralizingArgsMessage(String key) {
      super(key);
   }

   public String toString(Object... args) {
      String val = this.getValue();
      return val == null ? this.key : format(StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(String.format(val, args)));
   }

   public static String format(String s) {
      StringBuilder b = new StringBuilder();
      int last = 0;
      boolean plural = false;

      for (int i = 0; i < s.length(); i++) {
         if ('0' <= s.charAt(i) && s.charAt(i) <= '9') {
            if (Math.abs(StringUtils.numberAfter(s, i)) != 1.0) {
               plural = true;
            }
         } else if (s.charAt(i) == 166) {
            int c1 = s.indexOf(166, i + 1);
            if (c1 == -1) {
               break;
            }

            int c2 = s.indexOf(166, c1 + 1);
            if (c2 == -1) {
               break;
            }

            b.append(s.substring(last, i));
            b.append(plural ? s.substring(c1 + 1, c2) : s.substring(i + 1, c1));
            i = c2;
            last = c2 + 1;
            plural = false;
         }
      }

      if (last == 0) {
         return s;
      } else {
         b.append(s.substring(last, s.length()));
         return b + "";
      }
   }
}
