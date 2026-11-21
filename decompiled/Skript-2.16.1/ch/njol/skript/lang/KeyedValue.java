package ch.njol.skript.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record KeyedValue<T>(@NotNull String key, @NotNull T value) implements Entry<String, T> {
   public KeyedValue(@NotNull String key, @NotNull T value) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(value, "value");
      this.key = key;
      this.value = value;
   }

   public KeyedValue(Entry<String, T> entry) {
      this(entry.getKey(), entry.getValue());
   }

   public String getKey() {
      return this.key();
   }

   @Override
   public T getValue() {
      return this.value();
   }

   @Override
   public T setValue(T value) {
      throw new UnsupportedOperationException("KeyedValue is immutable, cannot set value");
   }

   public KeyedValue<T> withKey(@NotNull String newKey) {
      return new KeyedValue<>(newKey, this.value());
   }

   public <U> KeyedValue<U> withValue(@NotNull U newValue) {
      return new KeyedValue<>(this.key(), newValue);
   }

   public static <T, U> KeyedValue<U>[] map(KeyedValue<T>[] source, Function<T, U> mapper) {
      if (source == null) {
         return new KeyedValue[0];
      } else {
         KeyedValue<U>[] mapped = new KeyedValue[source.length];

         for (int i = 0; i < source.length; i++) {
            if (source[i] != null) {
               U mappedValue = mapper.apply(source[i].value());
               mapped[i] = mappedValue != null ? source[i].withValue(mappedValue) : null;
            }
         }

         return mapped;
      }
   }

   @NotNull
   public static <T> KeyedValue<T>[] zip(@NotNull T[] values, @NotNull @Nullable String[] keys) {
      if (keys == null) {
         KeyedValue<T>[] keyedValues = new KeyedValue[values.length];

         for (int i = 0; i < values.length; i++) {
            keyedValues[i] = new KeyedValue<>(String.valueOf(i + 1), values[i]);
         }

         return keyedValues;
      } else if (values.length != keys.length) {
         throw new IllegalArgumentException("Values and keys must have the same length");
      } else {
         KeyedValue<T>[] keyedValues = new KeyedValue[values.length];

         for (int i = 0; i < values.length; i++) {
            keyedValues[i] = new KeyedValue<>(keys[i], values[i]);
         }

         return keyedValues;
      }
   }

   public static <T> KeyedValue.UnzippedKeyValues<T> unzip(@NotNull KeyedValue<T>[] keyedValues) {
      List<String> keys = new ArrayList<>(keyedValues.length);
      List<T> values = new ArrayList<>(keyedValues.length);

      for (KeyedValue<T> keyedValue : keyedValues) {
         keys.add(keyedValue.key());
         values.add(keyedValue.value());
      }

      return new KeyedValue.UnzippedKeyValues<>(keys, values);
   }

   public static <T> KeyedValue.UnzippedKeyValues<T> unzip(Iterator<KeyedValue<T>> keyedValues) {
      List<String> keys = new ArrayList<>();
      List<T> values = new ArrayList<>();

      while (keyedValues.hasNext()) {
         KeyedValue<T> keyedValue = keyedValues.next();
         keys.add(keyedValue.key());
         values.add(keyedValue.value());
      }

      return new KeyedValue.UnzippedKeyValues<>(keys, values);
   }

   public record UnzippedKeyValues<T>(@NotNull @Nullable List<String> keys, @NotNull List<T> values) {
      public UnzippedKeyValues(@Nullable List<String> keys, @NotNull List<T> values) {
         this.values = Objects.requireNonNull(values, "values");
         this.keys = (List<String>)(keys != null ? keys : new ArrayList<>(values.size()));
         if (keys == null) {
            for (int i = 1; i <= values.size(); i++) {
               this.keys.add(String.valueOf(i));
            }
         } else if (keys.size() != values.size()) {
            throw new IllegalArgumentException("Keys and values must have the same length");
         }
      }

      public UnzippedKeyValues(@NotNull @Nullable String[] keys, @NotNull T[] values) {
         this(keys != null ? Arrays.asList(keys) : null, Arrays.asList(values));
      }
   }
}
