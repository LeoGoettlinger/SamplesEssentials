package net.kyori.adventure.audience;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.pointer.Pointer;
import net.kyori.adventure.resource.ResourcePackInfoLike;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

final class EmptyAudience implements Audience {
   static final EmptyAudience INSTANCE = new EmptyAudience();

   @NotNull
   @Override
   public <T> Optional<T> get(@NotNull final Pointer<T> pointer) {
      return Optional.empty();
   }

   @Contract("_, null -> null; _, !null -> !null")
   @Nullable
   @Override
   public <T> T getOrDefault(@NotNull final Pointer<T> pointer, @Nullable final T defaultValue) {
      return defaultValue;
   }

   @UnknownNullability
   @Override
   public <T> T getOrDefaultFrom(@NotNull final Pointer<T> pointer, @NotNull final Supplier<? extends T> defaultValue) {
      return (T)defaultValue.get();
   }

   @NotNull
   @Override
   public Audience filterAudience(@NotNull final Predicate<? super Audience> filter) {
      return this;
   }

   @Override
   public void forEachAudience(@NotNull final Consumer<? super Audience> action) {
   }

   @Override
   public void sendMessage(@NotNull final ComponentLike message) {
   }

   @Override
   public void sendMessage(@NotNull final Component message) {
   }

   @Deprecated
   @Override
   public void sendMessage(@NotNull final Identified source, @NotNull final Component message, @NotNull final MessageType type) {
   }

   @Deprecated
   @Override
   public void sendMessage(@NotNull final Identity source, @NotNull final Component message, @NotNull final MessageType type) {
   }

   @Override
   public void sendMessage(@NotNull final Component message, @NotNull final ChatType.Bound boundChatType) {
   }

   @Override
   public void sendMessage(@NotNull final SignedMessage signedMessage, @NotNull final ChatType.Bound boundChatType) {
   }

   @Override
   public void deleteMessage(@NotNull final SignedMessage.Signature signature) {
   }

   @Override
   public void sendActionBar(@NotNull final ComponentLike message) {
   }

   @Override
   public void sendPlayerListHeader(@NotNull final ComponentLike header) {
   }

   @Override
   public void sendPlayerListFooter(@NotNull final ComponentLike footer) {
   }

   @Override
   public void sendPlayerListHeaderAndFooter(@NotNull final ComponentLike header, @NotNull final ComponentLike footer) {
   }

   @Override
   public void openBook(@NotNull final Book.Builder book) {
   }

   @Override
   public void sendResourcePacks(@NotNull final ResourcePackInfoLike request, @NotNull final ResourcePackInfoLike... others) {
   }

   @Override
   public void removeResourcePacks(@NotNull final ResourcePackRequest request) {
   }

   @Override
   public void removeResourcePacks(@NotNull final ResourcePackInfoLike request, @NotNull final ResourcePackInfoLike... others) {
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
