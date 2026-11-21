package net.kyori.adventure.chat;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.Nullable;

record SignedMessageImpl(String message, Component unsignedContent, Instant timestamp, long salt) implements SignedMessage {
   static final SecureRandom RANDOM = new SecureRandom();

   SignedMessageImpl(final String message, final Component unsignedContent) {
      this(message, unsignedContent, Instant.now(), RANDOM.nextLong());
   }

   @Nullable
   @Override
   public SignedMessage.Signature signature() {
      return null;
   }

   @Override
   public Identity identity() {
      return Identity.nil();
   }

   record SignatureImpl(byte[] bytes) implements SignedMessage.Signature {
      @Override
      public boolean equals(final Object o) {
         return o instanceof SignedMessageImpl.SignatureImpl(byte[] var4) ? Objects.deepEquals(this.bytes, var4) : false;
      }

      @Override
      public int hashCode() {
         return Arrays.hashCode(this.bytes);
      }

      @Override
      public String toString() {
         return "SignatureImpl{bytes=" + Arrays.toString(this.bytes) + "}";
      }
   }
}
