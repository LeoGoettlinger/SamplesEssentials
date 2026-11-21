package com.sk89q.worldedit.util.kyori;

import java.util.Objects;
import java.util.function.IntPredicate;
import org.checkerframework.checker.nullness.qual.NonNull;

final class KeyImpl implements Key {
   private static final IntPredicate NAMESPACE_PREDICATE = value -> value == 95
      || value == 45
      || value >= 97 && value <= 122
      || value >= 48 && value <= 57
      || value == 46;
   private static final IntPredicate VALUE_PREDICATE = value -> value == 95
      || value == 45
      || value >= 97 && value <= 122
      || value >= 48 && value <= 57
      || value == 47
      || value == 46;
   private final String namespace;
   private final String value;

   KeyImpl(@NonNull final String namespace, @NonNull final String value) {
      if (!namespaceValid(namespace)) {
         throw new Key.ParseException("Non [a-z0-9_.-] character in namespace of location: " + asString(namespace, value));
      } else if (!valueValid(value)) {
         throw new Key.ParseException("Non [a-z0-9/._-] character in path of location: " + asString(namespace, value));
      } else {
         this.namespace = namespace;
         this.value = value;
      }
   }

   static boolean namespaceValid(@NonNull final String namespace) {
      return namespace.chars().allMatch(NAMESPACE_PREDICATE);
   }

   static boolean valueValid(@NonNull final String value) {
      return value.chars().allMatch(VALUE_PREDICATE);
   }

   @NonNull
   @Override
   public String namespace() {
      return this.namespace;
   }

   @NonNull
   @Override
   public String value() {
      return this.value;
   }

   @NonNull
   @Override
   public String asString() {
      return asString(this.namespace, this.value);
   }

   @NonNull
   private static String asString(@NonNull final String namespace, @NonNull final String value) {
      return namespace + ':' + value;
   }

   @NonNull
   @Override
   public String toString() {
      return this.asString();
   }

   @Override
   public boolean equals(final Object other) {
      if (this == other) {
         return true;
      } else if (!(other instanceof Key)) {
         return false;
      } else {
         Key that = (Key)other;
         return Objects.equals(this.namespace, that.namespace()) && Objects.equals(this.value, that.value());
      }
   }

   @Override
   public int hashCode() {
      int result = this.namespace.hashCode();
      return 31 * result + this.value.hashCode();
   }

   public int compareTo(@NonNull final Key that) {
      int value = this.value.compareTo(that.value());
      return value != 0 ? clampCompare(value) : clampCompare(this.namespace.compareTo(that.namespace()));
   }

   private static int clampCompare(final int value) {
      if (value < 0) {
         return -1;
      } else {
         return value > 0 ? 1 : value;
      }
   }
}
