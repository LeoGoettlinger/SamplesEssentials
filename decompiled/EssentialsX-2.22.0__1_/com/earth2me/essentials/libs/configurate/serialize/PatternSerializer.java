package com.earth2me.essentials.libs.configurate.serialize;

import java.lang.reflect.AnnotatedType;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

final class PatternSerializer extends ScalarSerializer.Annotated<Pattern> {
   PatternSerializer() {
      super(Pattern.class);
   }

   public Pattern deserialize(final AnnotatedType type, final Object obj) throws SerializationException {
      try {
         PatternFlags flags = type.getAnnotation(PatternFlags.class);
         return flags != null ? Pattern.compile(obj.toString(), flags.value()) : Pattern.compile(obj.toString());
      } catch (PatternSyntaxException var4) {
         throw new SerializationException(var4);
      }
   }

   public Object serialize(final AnnotatedType type, final Pattern item, final Predicate<Class<?>> typeSupported) {
      return item.pattern();
   }
}
