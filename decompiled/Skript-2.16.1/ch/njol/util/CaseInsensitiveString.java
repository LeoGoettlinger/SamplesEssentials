package ch.njol.util;

import java.io.Serializable;
import java.lang.invoke.StringConcatFactory;
import java.util.Locale;
import javax.annotation.Nullable;

@Deprecated(
   since = "2.10.0",
   forRemoval = true
)
public class CaseInsensitiveString implements Serializable, Comparable<CharSequence>, CharSequence {
   private static final long serialVersionUID = 1205018864604639962L;
   private final String s;
   private final String lc;
   private final Locale locale;

   public CaseInsensitiveString(String s) {
      this.s = s;
      this.locale = Locale.getDefault();
      this.lc = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(s.toLowerCase(this.locale));
   }

   public CaseInsensitiveString(String s, Locale locale) {
      this.s = s;
      this.locale = locale;
      this.lc = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(s.toLowerCase(locale));
   }

   @Override
   public int hashCode() {
      return this.lc.hashCode();
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (o == this) {
         return true;
      } else {
         return o instanceof CharSequence ? ((CharSequence)o).toString().toLowerCase(this.locale).equals(this.lc) : false;
      }
   }

   @Override
   public String toString() {
      return this.s;
   }

   @Override
   public char charAt(int i) {
      return this.s.charAt(i);
   }

   @Override
   public int length() {
      return this.s.length();
   }

   public CaseInsensitiveString subSequence(int start, int end) {
      return new CaseInsensitiveString(
         StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(this.s.substring(start, end)), this.locale
      );
   }

   public int compareTo(CharSequence s) {
      return this.lc.compareTo(s.toString().toLowerCase(this.locale));
   }
}
