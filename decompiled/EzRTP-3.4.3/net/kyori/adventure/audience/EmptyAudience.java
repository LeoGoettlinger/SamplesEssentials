package net.kyori.adventure.audience;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.pointer.Pointer;
import net.kyori.adventure.resource.ResourcePackInfoLike;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

final class EmptyAudience implements Audience {
   static final EmptyAudience INSTANCE = new EmptyAudience();

   @Override
   public <T> Optional<T> get(final Pointer<T> pointer) {
      return Optional.empty();
   }

   @Contract("_, null -> null; _, !null -> !null")
   @Nullable
   @Override
   public <T> T getOrDefault(final Pointer<T> pointer, @Nullable final T defaultValue) {
      return defaultValue;
   }

   @UnknownNullability
   @Override
   public <T> T getOrDefaultFrom(final Pointer<T> pointer, final Supplier<? extends T> defaultValue) {
      return (T)defaultValue.get();
   }

   @Override
   public Audience filterAudience(final Predicate<? super Audience> filter) {
      return this;
   }

   @Override
   public void forEachAudience(final Consumer<? super Audience> action) {
   }

   @Override
   public void sendMessage(final ComponentLike message) {
   }

   @Override
   public void sendMessage(final Component message) {
   }

   @Override
   public void sendMessage(final Component message, final ChatType.Bound boundChatType) {
   }

   @Override
   public void sendMessage(final SignedMessage signedMessage, final ChatType.Bound boundChatType) {
   }

   @Override
   public void deleteMessage(final SignedMessage.Signature signature) {
   }

   @Override
   public void sendActionBar(final ComponentLike message) {
   }

   @Override
   public void sendPlayerListHeader(final ComponentLike header) {
   }

   @Override
   public void sendPlayerListFooter(final ComponentLike footer) {
   }

   @Override
   public void sendPlayerListHeaderAndFooter(final ComponentLike header, final ComponentLike footer) {
   }

   @Override
   public void openBook(final Book.Builder book) {
   }

   @Override
   public void sendResourcePacks(final ResourcePackInfoLike request, final ResourcePackInfoLike... others) {
   }

   @Override
   public void removeResourcePacks(final ResourcePackRequest request) {
   }

   @Override
   public void removeResourcePacks(final ResourcePackInfoLike request, final ResourcePackInfoLike... others) {
   }

   @Override
   public boolean equals(final Object that) {
      return this == that;
   }

   @Override
   public int hashCode() {
      return 0;
   }

   @Override
   public String toString() {
      return "EmptyAudience";
   }
}
