package ch.njol.skript.config;

import java.lang.invoke.StringConcatFactory;
import java.lang.reflect.Field;
import java.util.Locale;
import org.jetbrains.annotations.Nullable;

public class OptionSection {
   public final String key;

   public OptionSection(String key) {
      this.key = key;
   }

   @Nullable
   public final <T> T get(String key) {
      if (this.getClass() == OptionSection.class) {
         return null;
      } else {
         key = StringConcatFactory.makeConcatWithConstants<"makeConcatWithConstants","\u0001">(key.toLowerCase(Locale.ENGLISH));

         for (Field f : this.getClass().getDeclaredFields()) {
            f.setAccessible(true);
            if (Option.class.isAssignableFrom(f.getType())) {
               try {
                  Option<?> o = (Option<?>)f.get(this);
                  if (o.key.equals(key)) {
                     return (T)o.value();
                  }
               } catch (IllegalArgumentException var7) {
                  assert false;
               } catch (IllegalAccessException var8) {
                  assert false;
               }
            }
         }

         return null;
      }
   }
}
