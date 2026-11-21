package net.kyori.adventure.key;

import org.jspecify.annotations.Nullable;

public final class InvalidKeyException extends RuntimeException {
   private static final long serialVersionUID = -5413304087321449434L;
   private final String keyNamespace;
   private final String keyValue;

   InvalidKeyException(final String keyNamespace, final String keyValue, @Nullable final String message) {
      super(message);
      this.keyNamespace = keyNamespace;
      this.keyValue = keyValue;
   }

   public String keyNamespace() {
      return this.keyNamespace;
   }

   public String keyValue() {
      return this.keyValue;
   }
}
