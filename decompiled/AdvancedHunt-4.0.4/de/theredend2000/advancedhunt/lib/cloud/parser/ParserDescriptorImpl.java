package de.theredend2000.advancedhunt.lib.cloud.parser;

import io.leangen.geantyref.TypeToken;
import java.util.Objects;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.Immutable;
import org.apiguardian.api.API;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.immutables.value.Generated;

@ParametersAreNonnullByDefault
@CheckReturnValue
@API(
   status = API.Status.INTERNAL,
   consumers = {"de.theredend2000.advancedhunt.lib.cloud.*"}
)
@Generated(
   from = "ParserDescriptor",
   generator = "Immutables"
)
@Immutable
final class ParserDescriptorImpl<C, T> implements ParserDescriptor<C, T> {
   @NonNull
   private final ArgumentParser<C, T> parser;
   @NonNull
   private final TypeToken<T> valueType;

   private ParserDescriptorImpl(@NonNull ArgumentParser<C, T> parser, @NonNull TypeToken<T> valueType) {
      this.parser = Objects.requireNonNull(parser, "parser");
      this.valueType = Objects.requireNonNull(valueType, "valueType");
   }

   private ParserDescriptorImpl(ParserDescriptorImpl<C, T> original, @NonNull ArgumentParser<C, T> parser, @NonNull TypeToken<T> valueType) {
      this.parser = parser;
      this.valueType = valueType;
   }

   @NonNull
   @Override
   public ArgumentParser<C, T> parser() {
      return this.parser;
   }

   @NonNull
   @Override
   public TypeToken<T> valueType() {
      return this.valueType;
   }

   public final ParserDescriptorImpl<C, T> withParser(ArgumentParser<C, T> value) {
      if (this.parser == value) {
         return this;
      } else {
         ArgumentParser<C, T> newValue = Objects.requireNonNull(value, "parser");
         return new ParserDescriptorImpl<>(this, newValue, this.valueType);
      }
   }

   public final ParserDescriptorImpl<C, T> withValueType(TypeToken<T> value) {
      if (this.valueType == value) {
         return this;
      } else {
         TypeToken<T> newValue = Objects.requireNonNull(value, "valueType");
         return new ParserDescriptorImpl<>(this, this.parser, newValue);
      }
   }

   @Override
   public boolean equals(@Nullable Object another) {
      return this == another ? true : another instanceof ParserDescriptorImpl && this.equalTo(0, (ParserDescriptorImpl<?, ?>)another);
   }

   private boolean equalTo(int synthetic, ParserDescriptorImpl<?, ?> another) {
      return this.parser.equals(another.parser) && this.valueType.equals(another.valueType);
   }

   @Override
   public int hashCode() {
      int h = 5381;
      h += (h << 5) + this.parser.hashCode();
      return h + (h << 5) + this.valueType.hashCode();
   }

   @Override
   public String toString() {
      return "ParserDescriptor{parser=" + this.parser + ", valueType=" + this.valueType + "}";
   }

   public static <C, T> ParserDescriptorImpl<C, T> of(@NonNull ArgumentParser<C, T> parser, @NonNull TypeToken<T> valueType) {
      return new ParserDescriptorImpl<>(parser, valueType);
   }

   public static <C, T> ParserDescriptorImpl<C, T> copyOf(ParserDescriptor<C, T> instance) {
      return instance instanceof ParserDescriptorImpl ? (ParserDescriptorImpl)instance : of(instance.parser(), instance.valueType());
   }
}
