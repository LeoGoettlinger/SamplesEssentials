package ac.grim.grimac.shaded.kyori.adventure.chat;

import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import ac.grim.grimac.shaded.kyori.adventure.identity.Identity;
import ac.grim.grimac.shaded.kyori.adventure.text.Component;
import java.security.SecureRandom;
import java.time.Instant;

final class SignedMessageImpl implements SignedMessage {
   static final SecureRandom RANDOM = new SecureRandom();
   private final Instant instant = Instant.now();
   private final long salt = RANDOM.nextLong();
   private final String message;
   private final Component unsignedContent;

   SignedMessageImpl(final String message, final Component unsignedContent) {
      this.message = message;
      this.unsignedContent = unsignedContent;
   }

   @NotNull
   @Override
   public Instant timestamp() {
      return this.instant;
   }

   @Override
   public long salt() {
      return this.salt;
   }

   @Override
   public SignedMessage.Signature signature() {
      return null;
   }

   @Nullable
   @Override
   public Component unsignedContent() {
      return this.unsignedContent;
   }

   @NotNull
   @Override
   public String message() {
      return this.message;
   }

   @NotNull
   @Override
   public Identity identity() {
      return Identity.nil();
   }

   static final class SignatureImpl implements SignedMessage.Signature {
      final byte[] signature;

      SignatureImpl(final byte[] signature) {
         this.signature = signature;
      }

      @Override
      public byte[] bytes() {
         return this.signature;
      }
   }
}
