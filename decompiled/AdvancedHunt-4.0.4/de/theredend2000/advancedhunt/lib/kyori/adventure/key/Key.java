package de.theredend2000.advancedhunt.lib.kyori.adventure.key;

import java.util.Comparator;
import java.util.Objects;
import java.util.OptionalInt;
import org.jspecify.annotations.Nullable;

public interface Key extends Comparable<Key>, Namespaced, Keyed {
   String MINECRAFT_NAMESPACE = "minecraft";
   char DEFAULT_SEPARATOR = ':';

   static Key key(@KeyPattern final String string) {
      return key(string, ':');
   }

   static Key key(final String string, final char character) {
      Objects.requireNonNull(string, "string");
      int index = string.indexOf(character);
      String namespace = index >= 1 ? string.substring(0, index) : "minecraft";
      String value = index >= 0 ? string.substring(index + 1) : string;
      return key(namespace, value);
   }

   static Key key(final Namespaced namespaced, @KeyPattern.Value final String value) {
      return key(Objects.requireNonNull(namespaced, "namespaced").namespace(), value);
   }

   static Key key(@KeyPattern.Namespace final String namespace, @KeyPattern.Value final String value) {
      return new KeyImpl(namespace, value);
   }

   static Comparator<? super Key> comparator() {
      return KeyImpl.COMPARATOR;
   }

   static boolean parseable(@Nullable final String string) {
      if (string == null) {
         return false;
      } else {
         int index = string.indexOf(58);
         String namespace = index >= 1 ? string.substring(0, index) : "minecraft";
         String value = index >= 0 ? string.substring(index + 1) : string;
         return parseableNamespace(namespace) && parseableValue(value);
      }
   }

   static boolean parseableNamespace(final String namespace) {
      return checkNamespace(namespace).isEmpty();
   }

   static OptionalInt checkNamespace(final String namespace) {
      Objects.requireNonNull(namespace, "namespace");
      if (namespace.equals("..")) {
         return OptionalInt.of(-1);
      } else {
         int i = 0;

         for (int length = namespace.length(); i < length; i++) {
            if (!allowedInNamespace(namespace.charAt(i))) {
               return OptionalInt.of(i);
            }
         }

         return OptionalInt.empty();
      }
   }

   static boolean parseableValue(final String value) {
      return checkValue(value).isEmpty();
   }

   static OptionalInt checkValue(final String value) {
      Objects.requireNonNull(value, "value");
      int i = 0;

      for (int length = value.length(); i < length; i++) {
         if (!allowedInValue(value.charAt(i))) {
            return OptionalInt.of(i);
         }
      }

      return OptionalInt.empty();
   }

   static boolean allowedInNamespace(final char character) {
      return KeyImpl.allowedInNamespace(character);
   }

   static boolean allowedInValue(final char character) {
      return KeyImpl.allowedInValue(character);
   }

   @KeyPattern.Namespace
   @Override
   String namespace();

   @KeyPattern.Value
   String value();

   String asString();

   default String asMinimalString() {
      return this.namespace().equals("minecraft") ? this.value() : this.asString();
   }

   default int compareTo(final Key that) {
      return comparator().compare(this, that);
   }

   @Override
   default Key key() {
      return this;
   }
}
