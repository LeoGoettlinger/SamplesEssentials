package com.earth2me.essentials.libs.configurate.objectmapping.meta;

import com.earth2me.essentials.libs.checkerframework.checker.nullness.qual.Nullable;
import com.earth2me.essentials.libs.configurate.serialize.SerializationException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Constraint<V> {
   void validate(@Nullable V value) throws SerializationException;

   static <T extends Annotation> Constraint.Factory<T, Object> required() {
      return (data, type) -> value -> {
         if (value == null) {
            throw new SerializationException("A value is required for this field");
         }
      };
   }

   static Constraint.Factory<Matches, String> pattern() {
      return (data, type) -> {
         Pattern test = Pattern.compile(data.value(), data.flags());
         MessageFormat format = new MessageFormat(data.failureMessage(), Locale.getDefault());
         return value -> {
            if (value != null) {
               Matcher match = test.matcher(value);
               if (!match.matches()) {
                  throw new SerializationException(format.format(new Object[]{value, data.value()}));
               }
            }
         };
      };
   }

   static Constraint.Factory<Matches, String> localizedPattern(final ResourceBundle bundle) {
      return (data, type) -> {
         Pattern test = Pattern.compile(data.value(), data.flags());
         MessageFormat format = new MessageFormat(Localization.key(bundle, data.failureMessage()), bundle.getLocale());
         return value -> {
            if (value != null) {
               Matcher match = test.matcher(value);
               if (!match.matches()) {
                  throw new SerializationException(format.format(new Object[]{value, data.value()}));
               }
            }
         };
      };
   }

   public interface Factory<A extends Annotation, V> {
      Constraint<V> make(A data, Type type);
   }
}
