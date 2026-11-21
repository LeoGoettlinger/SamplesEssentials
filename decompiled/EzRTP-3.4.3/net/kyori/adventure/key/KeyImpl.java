package net.kyori.adventure.key;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;
import java.util.OptionalInt;

record KeyImpl(String namespace, String value) implements Key {
   static final Comparator<? super Key> COMPARATOR = Comparator.comparing(Key::value).thenComparing(Key::namespace);

   KeyImpl(String namespace, String value) {
      checkError("namespace", namespace, namespace, value, Key.checkNamespace(namespace), "^(?!\\.\\.$)[a-z0-9_.-]+$");
      checkError("value", value, namespace, value, Key.checkValue(value), "[a-z0-9_\\-./]+");
      this.namespace = namespace;
      this.value = value;
   }

   static void checkError(final String name, final String checkPart, final String namespace, final String value, final OptionalInt index, final String pattern) {
      Objects.requireNonNull(checkPart, name);
      if (index.isPresent()) {
         int indexValue = index.getAsInt();
         if (indexValue == -1) {
            throw new InvalidKeyException(namespace, value, String.format("'%s' is not a valid value for %s", checkPart, name));
         } else {
            char character = checkPart.charAt(indexValue);
            throw new InvalidKeyException(
               namespace,
               value,
               String.format(
                  "Non " + pattern + " character in %s of Key[%s] at index %d ('%s', bytes: %s)",
                  name,
                  asString(namespace, value),
                  indexValue,
                  character,
                  Arrays.toString(String.valueOf(character).getBytes(StandardCharsets.UTF_8))
               )
            );
         }
      }
   }

   static boolean allowedInNamespace(final char character) {
      return character == '_' || character == '-' || character >= 'a' && character <= 'z' || character >= '0' && character <= '9' || character == '.';
   }

   static boolean allowedInValue(final char character) {
      return character == '_'
         || character == '-'
         || character >= 'a' && character <= 'z'
         || character >= '0' && character <= '9'
         || character == '.'
         || character == '/';
   }

   @Override
   public String asString() {
      return asString(this.namespace, this.value);
   }

   private static String asString(final String namespace, final String value) {
      return namespace + ":" + value;
   }

   @Override
   public String toString() {
      return this.asString();
   }

   @Override
   public boolean equals(final Object other) {
      if (this == other) {
         return true;
      } else {
         return !(other instanceof Key that) ? false : Objects.equals(this.namespace, that.namespace()) && Objects.equals(this.value, that.value());
      }
   }

   @Override
   public int compareTo(final Key that) {
      return Key.super.compareTo(that);
   }
}
