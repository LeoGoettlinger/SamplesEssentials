package ch.njol.skript.classes;

import ch.njol.skript.util.Utils;
import org.jetbrains.annotations.Nullable;

@Deprecated(
   since = "2.10.0",
   forRemoval = true
)
public interface Converter<F, T> extends org.skriptlang.skript.lang.converter.Converter<F, T> {
   int $_WARNING = Utils.loadedRemovedClassWarning(Converter.class);
   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   int NO_LEFT_CHAINING = 1;
   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   int NO_RIGHT_CHAINING = 2;
   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   int NO_CHAINING = 3;
   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   int NO_COMMAND_ARGUMENTS = 8;

   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   @Nullable
   @Override
   T convert(F var1);

   @Deprecated(
      since = "2.10.0",
      forRemoval = true
   )
   public static final class ConverterUtils {
      @Deprecated(
         since = "2.10.0",
         forRemoval = true
      )
      public static <F, T> Converter<?, T> createInstanceofConverter(Class<F> from, Converter<F, T> conv) {
         throw new UnsupportedOperationException();
      }

      @Deprecated(
         since = "2.10.0",
         forRemoval = true
      )
      public static <F, T> Converter<F, T> createInstanceofConverter(Converter<F, ?> conv, Class<T> to) {
         throw new UnsupportedOperationException();
      }

      @Deprecated(
         since = "2.10.0",
         forRemoval = true
      )
      public static <F, T> Converter<?, T> createDoubleInstanceofConverter(Class<F> from, Converter<F, ?> conv, Class<T> to) {
         throw new UnsupportedOperationException();
      }
   }
}
