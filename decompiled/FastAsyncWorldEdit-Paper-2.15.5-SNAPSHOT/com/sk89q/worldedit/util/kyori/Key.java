package com.sk89q.worldedit.util.kyori;

import org.checkerframework.checker.nullness.qual.NonNull;

public interface Key extends Comparable<Key> {
   String MINECRAFT_NAMESPACE = "minecraft";

   @NonNull
   static Key of(@NonNull final String string) {
      return of(string, ':');
   }

   @NonNull
   static Key of(@NonNull final String string, final char character) {
      int index = string.indexOf(character);
      String namespace = index >= 1 ? string.substring(0, index) : "minecraft";
      String value = index >= 0 ? string.substring(index + 1) : string;
      return of(namespace, value);
   }

   @NonNull
   static Key of(@NonNull final String namespace, @NonNull final String value) {
      return new KeyImpl(namespace, value);
   }

   @NonNull
   String namespace();

   @NonNull
   String value();

   @NonNull
   String asString();

   public static final class ParseException extends RuntimeException {
      ParseException(final String message) {
         super(message);
      }
   }
}
