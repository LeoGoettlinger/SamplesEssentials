package de.theredend2000.advancedhunt.lib.kyori.adventure.chat;

import de.theredend2000.advancedhunt.lib.kyori.adventure.identity.Identified;
import de.theredend2000.advancedhunt.lib.kyori.adventure.identity.Identity;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.ComponentLike;
import java.time.Instant;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public interface SignedMessage extends Identified {
   @Contract(
      value = "_ -> new",
      pure = true
   )
   static SignedMessage.Signature signature(final byte[] signature) {
      return new SignedMessageImpl.SignatureImpl(signature);
   }

   @Contract(
      value = "_, _ -> new",
      pure = true
   )
   static SignedMessage system(final String message, @Nullable final ComponentLike unsignedContent) {
      return new SignedMessageImpl(message, ComponentLike.unbox(unsignedContent));
   }

   @Contract(
      pure = true
   )
   Instant timestamp();

   @Contract(
      pure = true
   )
   long salt();

   @Contract(
      pure = true
   )
   @Nullable
   SignedMessage.Signature signature();

   @Contract(
      pure = true
   )
   @Nullable
   Component unsignedContent();

   @Contract(
      pure = true
   )
   String message();

   @Contract(
      pure = true
   )
   default boolean isSystem() {
      return this.identity() == Identity.nil();
   }

   @Contract(
      pure = true
   )
   default boolean canDelete() {
      return this.signature() != null;
   }

   public sealed interface Signature permits SignedMessageImpl.SignatureImpl {
      @Contract(
         pure = true
      )
      byte[] bytes();
   }
}
