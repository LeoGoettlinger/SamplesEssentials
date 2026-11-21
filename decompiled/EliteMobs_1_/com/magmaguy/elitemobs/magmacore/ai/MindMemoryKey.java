package com.magmaguy.elitemobs.magmacore.ai;

import java.util.Objects;
import java.util.regex.Pattern;

public final class MindMemoryKey<T> {
   private static final Pattern IDENTIFIER = Pattern.compile("[a-z0-9._-]+:[a-z0-9/._-]+");
   private final String identifier;
   private final Class<T> type;
   private final MindValueCodec<T> codec;

   private MindMemoryKey(String identifier, Class<T> type, MindValueCodec<T> codec) {
      if (identifier != null && IDENTIFIER.matcher(identifier).matches()) {
         this.identifier = identifier;
         this.type = Objects.requireNonNull(type, "type");
         this.codec = codec;
      } else {
         throw new IllegalArgumentException("Mind memory identifiers must be namespaced lower-case keys");
      }
   }

   public static <T> MindMemoryKey<T> transientKey(String identifier, Class<T> type) {
      return new MindMemoryKey<>(identifier, type, null);
   }

   public static <T> MindMemoryKey<T> persistentKey(String identifier, Class<T> type, MindValueCodec<T> codec) {
      return new MindMemoryKey<>(identifier, type, Objects.requireNonNull(codec, "codec"));
   }

   public String identifier() {
      return this.identifier;
   }

   public Class<T> type() {
      return this.type;
   }

   public boolean persistent() {
      return this.codec != null;
   }

   MindValueCodec<T> codec() {
      if (this.codec == null) {
         throw new IllegalStateException(this.identifier + " is transient");
      } else {
         return this.codec;
      }
   }

   T requireType(Object value) {
      if (!this.type.isInstance(value)) {
         throw new IllegalArgumentException("Memory " + this.identifier + " requires " + this.type.getSimpleName());
      } else {
         return this.type.cast(value);
      }
   }

   @Override
   public boolean equals(Object other) {
      return other instanceof MindMemoryKey<?> key && this.identifier.equals(key.identifier);
   }

   @Override
   public int hashCode() {
      return this.identifier.hashCode();
   }

   @Override
   public String toString() {
      return this.identifier;
   }
}
