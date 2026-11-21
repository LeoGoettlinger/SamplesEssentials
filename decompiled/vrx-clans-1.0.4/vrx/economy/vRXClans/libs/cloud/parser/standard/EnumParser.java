package vrx.economy.vRXClans.libs.cloud.parser.standard;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import vrx.economy.vRXClans.libs.cloud.caption.CaptionVariable;
import vrx.economy.vRXClans.libs.cloud.caption.StandardCaptionKeys;
import vrx.economy.vRXClans.libs.cloud.component.CommandComponent;
import vrx.economy.vRXClans.libs.cloud.context.CommandContext;
import vrx.economy.vRXClans.libs.cloud.context.CommandInput;
import vrx.economy.vRXClans.libs.cloud.exception.parsing.ParserException;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParseResult;
import vrx.economy.vRXClans.libs.cloud.parser.ArgumentParser;
import vrx.economy.vRXClans.libs.cloud.parser.ParserDescriptor;
import vrx.economy.vRXClans.libs.cloud.suggestion.BlockingSuggestionProvider;

@API(
   status = API.Status.STABLE
)
public final class EnumParser<C, E extends Enum<E>> implements ArgumentParser<C, E>, BlockingSuggestionProvider.Strings<C> {
   private final Class<E> enumClass;
   private final EnumSet<E> acceptedValues;

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C, E extends Enum<E>> ParserDescriptor<C, E> enumParser(@NonNull final Class<E> enumClass) {
      return ParserDescriptor.of(new EnumParser<>(enumClass), enumClass);
   }

   @API(
      status = API.Status.STABLE
   )
   @NonNull
   public static <C, E extends Enum<E>> CommandComponent.Builder<C, E> enumComponent(@NonNull final Class<E> enumClass) {
      return CommandComponent.<C, E>builder().parser(enumParser(enumClass));
   }

   public EnumParser(@NonNull final Class<E> enumClass) {
      this.enumClass = enumClass;
      this.acceptedValues = EnumSet.allOf(enumClass);
   }

   @NonNull
   public Class<E> enumClass() {
      return this.enumClass;
   }

   @NonNull
   public Collection<E> acceptedValues() {
      return Collections.unmodifiableSet(this.acceptedValues);
   }

   @NonNull
   @Override
   public ArgumentParseResult<E> parse(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput commandInput) {
      String input = commandInput.readString();

      for (E value : this.acceptedValues) {
         if (value.name().equalsIgnoreCase(input)) {
            return ArgumentParseResult.success(value);
         }
      }

      return ArgumentParseResult.failure(new EnumParser.EnumParseException(input, this.enumClass, commandContext));
   }

   @NonNull
   @Override
   public Iterable<String> stringSuggestions(@NonNull final CommandContext<C> commandContext, @NonNull final CommandInput input) {
      return EnumSet.allOf(this.enumClass).stream().map(e -> e.name().toLowerCase(Locale.ROOT)).collect(Collectors.toList());
   }

   @API(
      status = API.Status.STABLE
   )
   public static final class EnumParseException extends ParserException {
      private final String input;
      private final Class<? extends Enum<?>> enumClass;

      public EnumParseException(@NonNull final String input, @NonNull final Class<? extends Enum<?>> enumClass, @NonNull final CommandContext<?> context) {
         super(
            EnumParser.class,
            context,
            StandardCaptionKeys.ARGUMENT_PARSE_FAILURE_ENUM,
            CaptionVariable.of("input", input),
            CaptionVariable.of("acceptableValues", join(enumClass))
         );
         this.input = input;
         this.enumClass = enumClass;
      }

      @NonNull
      private static String join(@NonNull final Class<? extends Enum> clazz) {
         EnumSet<?> enumSet = EnumSet.allOf((Class<E>)clazz);
         return enumSet.stream().map(e -> e.toString().toLowerCase(Locale.ROOT)).collect(Collectors.joining(", "));
      }

      @NonNull
      public String input() {
         return this.input;
      }

      @NonNull
      public Class<? extends Enum<?>> enumClass() {
         return this.enumClass;
      }

      @Override
      public boolean equals(final Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            EnumParser.EnumParseException that = (EnumParser.EnumParseException)o;
            return this.input.equals(that.input) && this.enumClass.equals(that.enumClass);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Objects.hash(this.input, this.enumClass);
      }
   }
}
