package org.skriptlang.skript.common.function;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.KeyProviderExpression;
import ch.njol.skript.lang.KeyedValue;
import ch.njol.skript.localization.Noun;
import ch.njol.skript.registrations.Classes;
import com.google.common.base.Preconditions;
import java.util.Set;
import java.util.StringJoiner;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.NonExtendable;
import org.skriptlang.skript.lang.converter.Converter;
import org.skriptlang.skript.lang.converter.Converters;

@NonExtendable
public interface Parameter<T> {
   @NotNull
   String name();

   @NotNull
   Class<T> type();

   @NotNull
   @Unmodifiable
   Set<Parameter.Modifier> modifiers();

   default boolean hasModifier(Parameter.Modifier modifier) {
      return this.modifiers().contains(modifier);
   }

   default <M extends Parameter.Modifier> M getModifier(Class<M> modifierClass) {
      return this.modifiers().stream().filter(modifierClass::isInstance).map(modifierClass::cast).findFirst().orElse(null);
   }

   default Object[] evaluate(@Nullable Expression<? extends T> argument, Event event) {
      if (argument == null) {
         return null;
      } else {
         Object[] values = argument.getArray(event);

         for (int i = 0; i < values.length; i++) {
            values[i] = Classes.clone(values[i]);
         }

         if (!this.hasModifier(Parameter.Modifier.KEYED)) {
            return values;
         } else {
            String[] keys = KeyProviderExpression.areKeysRecommended(argument) ? ((KeyProviderExpression)argument).getArrayKeys(event) : null;
            return KeyedValue.zip(values, keys);
         }
      }
   }

   default boolean isSingle() {
      return !this.type().isArray();
   }

   @Deprecated(
      forRemoval = true,
      since = "2.14"
   )
   default boolean single() {
      return this.isSingle();
   }

   default String toFormattedString() {
      StringJoiner joiner = new StringJoiner(" ");
      joiner.add("%s:".formatted(this.name()));
      if (this.hasModifier(Parameter.Modifier.OPTIONAL)) {
         joiner.add("optional");
      }

      Noun exact = Classes.getSuperClassInfo(this.type()).getName();
      if (this.type().isArray()) {
         joiner.add(exact.getPlural());
      } else {
         joiner.add(exact.getSingular());
      }

      if (this.hasModifier(Parameter.Modifier.RANGED)) {
         Parameter.Modifier.RangedModifier<?> range = this.getModifier(Parameter.Modifier.RangedModifier.class);
         joiner.add("between").add(range.getMin().toString()).add("and").add(range.getMax().toString());
      }

      return joiner.toString();
   }

   public interface Modifier {
      Parameter.Modifier OPTIONAL = of();
      Parameter.Modifier KEYED = of();
      Parameter.Modifier RANGED = new Parameter.Modifier.RangedModifier<>(0, 0);

      static Parameter.Modifier of() {
         return new Parameter.Modifier() {};
      }

      static <T extends Comparable<T>> Parameter.Modifier.RangedModifier<T> ranged(T min, T max) {
         return new Parameter.Modifier.RangedModifier(min, max);
      }

      public static class RangedModifier<T extends Comparable<T>> implements Parameter.Modifier {
         private final T min;
         private final T max;

         private RangedModifier(T min, T max) {
            Preconditions.checkState(min.compareTo(max) < 1, "Min value cannot be greater than max value!");
            this.min = min;
            this.max = max;
         }

         public T getMin() {
            return this.min;
         }

         public T getMax() {
            return this.max;
         }

         public boolean inRange(Object input) {
            if (!this.min.getClass().isInstance(input)) {
               Converter<Object, ?> converter = Converters.getConverter((Class<Object>)input.getClass(), this.min.getClass());
               if (converter == null) {
                  return false;
               }

               input = converter.convert(input);
               if (input == null) {
                  return false;
               }
            }

            return ((Comparable)input).compareTo(this.min) > -1 && ((Comparable)input).compareTo(this.max) < 1;
         }

         public boolean inRange(@NotNull Object[] inputs) {
            if (inputs.length == 0) {
               return false;
            } else {
               for (Object input : inputs) {
                  if (!this.inRange(input)) {
                     return false;
                  }
               }

               return true;
            }
         }

         @Override
         public boolean equals(Object obj) {
            return obj == Parameter.Modifier.RANGED
               || obj instanceof Parameter.Modifier.RangedModifier<?> range
                  && (this == Parameter.Modifier.RANGED || range.max == this.max && range.min == this.min);
         }

         @Override
         public int hashCode() {
            return 439824729;
         }

         @Override
         public String toString() {
            return "RangedModifier(min=" + this.min + ", max=" + this.max + ")";
         }
      }
   }
}
