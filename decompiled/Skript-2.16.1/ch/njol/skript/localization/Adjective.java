package ch.njol.skript.localization;

import ch.njol.skript.Skript;
import java.lang.invoke.StringConcatFactory;
import java.util.HashMap;
import org.jetbrains.annotations.Nullable;

public class Adjective extends Message {
   private static final int DEFINITE_ARTICLE = -100;
   private static final String DEFINITE_ARTICLE_TOKEN = "+";
   private final HashMap<Integer, String> genders = new HashMap<>();
   @Nullable
   String def;

   public Adjective(String key) {
      super(key);
   }

   @Override
   protected void onValueChange() {
      this.genders.clear();
      String v = this.getValue();
      this.def = v;
      if (v != null) {
         int s = v.indexOf(64);
         int e = v.lastIndexOf(64);
         if (s != -1) {
            if (s == e) {
               Skript.error("Invalid use of '@' in the adjective '" + this.key + "' in the language file: " + v);
            } else {
               this.def = v.substring(0, s) + v.substring(e + 1);
               int c = s;

               int c2;
               do {
                  c2 = v.indexOf(64, c + 1);
                  int d = v.indexOf(58, c + 1);
                  if (d == -1 || d > c2) {
                     Skript.error("Missing colon (:) to separate the gender in the adjective '" + this.key + "' in the language file at index " + c + ": " + v);
                     return;
                  }

                  String gender = v.substring(c + 1, d);
                  int g = gender.equals("+") ? -100 : Noun.getGender(gender, this.key);
                  if (!this.genders.containsKey(g)) {
                     this.genders.put(g, v.substring(0, s) + v.substring(d + 1, c2) + v.substring(e + 1));
                  }

                  c = c2;
               } while (c2 < e);
            }
         }
      }
   }

   @Override
   public String toString() {
      this.validate();
      return this.def;
   }

   public String toString(int gender, int flags) {
      this.validate();
      if ((flags & 2) != 0 && this.genders.containsKey(-100)) {
         gender = -100;
      } else if ((flags & 1) != 0) {
         gender = -2;
      }

      String a = this.genders.get(gender);
      return a != null ? a : StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(this.def);
   }

   public static String toString(Adjective[] adjectives, int gender, int flags, boolean and) {
      StringBuilder b = new StringBuilder();

      for (int i = 0; i < adjectives.length; i++) {
         if (i != 0) {
            if (i == adjectives.length - 1) {
               b.append(" ").append(and ? GeneralWords.and : GeneralWords.or).append(" ");
            } else {
               b.append(", ");
            }
         }

         b.append(adjectives[i].toString(gender, flags));
      }

      return StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(b.toString());
   }

   public String toString(Noun n, int flags) {
      return n.toString(this, flags);
   }
}
