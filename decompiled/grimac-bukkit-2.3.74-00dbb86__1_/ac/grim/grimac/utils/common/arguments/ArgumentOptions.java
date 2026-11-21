package ac.grim.grimac.utils.common.arguments;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import lombok.Generated;

public class ArgumentOptions<T> {
   private final Class<T> clazz;
   private String key;
   private Supplier<T> defaultSupplier;
   private Predicate<T> verifier = t -> true;
   private Function<T, T> modifier = t -> t;
   private SystemArgument.Visibility visibility = SystemArgument.Visibility.VISIBLE;
   private boolean nullable = false;

   private ArgumentOptions(Class<T> clazz, String key, Supplier<T> defaultSupplier) {
      this.clazz = clazz;
      this.key = key;
      this.defaultSupplier = defaultSupplier;
   }

   public static <T> ArgumentOptions.Builder<T> from(Class<T> clazz, String key, @NotNull Supplier<T> defaultValue) {
      return new ArgumentOptions.Builder<>(new ArgumentOptions<>(clazz, key, defaultValue));
   }

   public static <T> ArgumentOptions.Builder<T> from(Class<T> clazz, String key, T defaultValue) {
      return new ArgumentOptions.Builder<>(new ArgumentOptions<>(clazz, key, () -> defaultValue)).nullable(defaultValue == null);
   }

   public static <T> ArgumentOptions.Builder<T> from(Class<T> clazz, String key) {
      return new ArgumentOptions.Builder<>(new ArgumentOptions<>(clazz, key, () -> null)).nullable(true);
   }

   @Generated
   public Class<T> getClazz() {
      return this.clazz;
   }

   @Generated
   public String getKey() {
      return this.key;
   }

   @Generated
   public Supplier<T> getDefaultSupplier() {
      return this.defaultSupplier;
   }

   @Generated
   public Predicate<T> getVerifier() {
      return this.verifier;
   }

   @Generated
   public Function<T, T> getModifier() {
      return this.modifier;
   }

   @Generated
   public SystemArgument.Visibility getVisibility() {
      return this.visibility;
   }

   @Generated
   public boolean isNullable() {
      return this.nullable;
   }

   public record Builder<T>(ArgumentOptions<T> options) {
      public ArgumentOptions.Builder<T> key(String key) {
         this.options.key = key;
         return this;
      }

      public ArgumentOptions.Builder<T> verifier(Predicate<T> predicate) {
         this.options.verifier = predicate;
         return this;
      }

      public ArgumentOptions.Builder<T> modifier(Function<T, T> modifier) {
         this.options.modifier = modifier;
         return this;
      }

      public ArgumentOptions.Builder<T> defaultSupplier(Supplier<T> supplier) {
         this.options.defaultSupplier = supplier;
         return this;
      }

      public ArgumentOptions.Builder<T> visibility(SystemArgument.Visibility visibility) {
         this.options.visibility = visibility;
         return this;
      }

      private ArgumentOptions.Builder<T> nullable(boolean nullable) {
         this.options.nullable = nullable;
         return this;
      }

      public ArgumentOptions<T> build() {
         return this.options;
      }
   }
}
