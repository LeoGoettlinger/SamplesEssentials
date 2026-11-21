package ch.jalu.configme.properties.inlinearray;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StandardInlineArrayConverters<T> implements InlineArrayConverter<T> {
   public static final InlineArrayConverter<Long> LONG = new StandardInlineArrayConverters<>(",", Long[]::new, Long::parseLong);
   public static final InlineArrayConverter<Integer> INTEGER = new StandardInlineArrayConverters<>(",", Integer[]::new, Integer::parseInt);
   public static final InlineArrayConverter<Float> FLOAT = new StandardInlineArrayConverters<>(",", Float[]::new, Float::parseFloat);
   public static final InlineArrayConverter<Double> DOUBLE = new StandardInlineArrayConverters<>(",", Double[]::new, Double::parseDouble);
   public static final InlineArrayConverter<Short> SHORT = new StandardInlineArrayConverters<>(",", Short[]::new, Short::parseShort);
   public static final InlineArrayConverter<Byte> BYTE = new StandardInlineArrayConverters<>(",", Byte[]::new, Byte::parseByte);
   public static final InlineArrayConverter<Boolean> BOOLEAN = new StandardInlineArrayConverters<>(
      ",", Boolean[]::new, s -> s.isEmpty() ? null : Boolean.parseBoolean(s)
   );
   public static final InlineArrayConverter<String> STRING = new StandardInlineArrayConverters<>("\n", String[]::new, s -> s, false);
   private final String separator;
   private final IntFunction<T[]> arrayProducer;
   private final Function<String, T> convertFunction;
   private final boolean useTrimAndSpaces;

   public StandardInlineArrayConverters(@NotNull String separator, @NotNull IntFunction<T[]> arrayProducer, @NotNull Function<String, T> convertFunction) {
      this(separator, arrayProducer, convertFunction, true);
   }

   public StandardInlineArrayConverters(
      @NotNull String separator, @NotNull IntFunction<T[]> arrayProducer, @NotNull Function<String, T> convertFunction, boolean useTrimAndSpaces
   ) {
      this.separator = separator;
      this.arrayProducer = arrayProducer;
      this.convertFunction = convertFunction;
      this.useTrimAndSpaces = useTrimAndSpaces;
   }

   @NotNull
   @Override
   public T[] fromString(@NotNull String input) {
      String[] inputArray = input.split(Pattern.quote(this.separator));
      return Arrays.stream(inputArray).map(this::convert).filter(Objects::nonNull).toArray(this.arrayProducer);
   }

   @NotNull
   @Override
   public String toExportValue(@NotNull T[] value) {
      String delimiter = this.useTrimAndSpaces ? this.separator + " " : this.separator;
      StringJoiner joiner = new StringJoiner(delimiter);

      for (T entry : value) {
         joiner.add(String.valueOf(entry));
      }

      return joiner.toString();
   }

   @Nullable
   protected T convert(@NotNull String input) {
      try {
         String argument = this.useTrimAndSpaces ? input.trim() : input;
         return this.convertFunction.apply(argument);
      } catch (Exception var3) {
         return null;
      }
   }
}
