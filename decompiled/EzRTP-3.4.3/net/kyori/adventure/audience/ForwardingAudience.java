package net.kyori.adventure.audience;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.chat.SignedMessage;
import net.kyori.adventure.dialog.DialogLike;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.pointer.Pointer;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.TitlePart;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.UnknownNullability;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface ForwardingAudience extends Audience {
   @OverrideOnly
   Iterable<? extends Audience> audiences();

   @Override
   default Pointers pointers() {
      return Pointers.empty();
   }

   @Override
   default Audience filterAudience(final Predicate<? super Audience> filter) {
      List<Audience> audiences = null;

      for (Audience audience : this.audiences()) {
         if (filter.test(audience)) {
            Audience filtered = audience.filterAudience(filter);
            if (filtered != Audience.empty()) {
               if (audiences == null) {
                  audiences = new ArrayList<>();
               }

               audiences.add(filtered);
            }
         }
      }

      return (Audience)(audiences != null ? Audience.audience(audiences) : Audience.empty());
   }

   @Override
   default void forEachAudience(final Consumer<? super Audience> action) {
      for (Audience audience : this.audiences()) {
         audience.forEachAudience(action);
      }
   }

   @Override
   default void sendMessage(final Component message) {
      for (Audience audience : this.audiences()) {
         audience.sendMessage(message);
      }
   }

   @Override
   default void sendMessage(final Component message, final ChatType.Bound boundChatType) {
      for (Audience audience : this.audiences()) {
         audience.sendMessage(message, boundChatType);
      }
   }

   @Override
   default void sendMessage(final SignedMessage signedMessage, final ChatType.Bound boundChatType) {
      for (Audience audience : this.audiences()) {
         audience.sendMessage(signedMessage, boundChatType);
      }
   }

   @Override
   default void deleteMessage(final SignedMessage.Signature signature) {
      for (Audience audience : this.audiences()) {
         audience.deleteMessage(signature);
      }
   }

   @Override
   default void sendActionBar(final Component message) {
      for (Audience audience : this.audiences()) {
         audience.sendActionBar(message);
      }
   }

   @Override
   default void sendPlayerListHeader(final Component header) {
      for (Audience audience : this.audiences()) {
         audience.sendPlayerListHeader(header);
      }
   }

   @Override
   default void sendPlayerListFooter(final Component footer) {
      for (Audience audience : this.audiences()) {
         audience.sendPlayerListFooter(footer);
      }
   }

   @Override
   default void sendPlayerListHeaderAndFooter(final Component header, final Component footer) {
      for (Audience audience : this.audiences()) {
         audience.sendPlayerListHeaderAndFooter(header, footer);
      }
   }

   @Override
   default <T> void sendTitlePart(final TitlePart<T> part, final T value) {
      for (Audience audience : this.audiences()) {
         audience.sendTitlePart(part, value);
      }
   }

   @Override
   default void clearTitle() {
      for (Audience audience : this.audiences()) {
         audience.clearTitle();
      }
   }

   @Override
   default void resetTitle() {
      for (Audience audience : this.audiences()) {
         audience.resetTitle();
      }
   }

   @Override
   default void showBossBar(final BossBar bar) {
      for (Audience audience : this.audiences()) {
         audience.showBossBar(bar);
      }
   }

   @Override
   default void hideBossBar(final BossBar bar) {
      for (Audience audience : this.audiences()) {
         audience.hideBossBar(bar);
      }
   }

   @Override
   default void playSound(final Sound sound) {
      for (Audience audience : this.audiences()) {
         audience.playSound(sound);
      }
   }

   @Override
   default void playSound(final Sound sound, final double x, final double y, final double z) {
      for (Audience audience : this.audiences()) {
         audience.playSound(sound, x, y, z);
      }
   }

   @Override
   default void playSound(final Sound sound, final Sound.Emitter emitter) {
      for (Audience audience : this.audiences()) {
         audience.playSound(sound, emitter);
      }
   }

   @Override
   default void stopSound(final SoundStop stop) {
      for (Audience audience : this.audiences()) {
         audience.stopSound(stop);
      }
   }

   @Override
   default void openBook(final Book book) {
      for (Audience audience : this.audiences()) {
         audience.openBook(book);
      }
   }

   @Override
   default void sendResourcePacks(final ResourcePackRequest request) {
      for (Audience audience : this.audiences()) {
         audience.sendResourcePacks(request);
      }
   }

   @Override
   default void removeResourcePacks(final Iterable<UUID> ids) {
      for (Audience audience : this.audiences()) {
         audience.removeResourcePacks(ids);
      }
   }

   @Override
   default void removeResourcePacks(final UUID id, final UUID... others) {
      for (Audience audience : this.audiences()) {
         audience.removeResourcePacks(id, others);
      }
   }

   @Override
   default void clearResourcePacks() {
      for (Audience audience : this.audiences()) {
         audience.clearResourcePacks();
      }
   }

   @Override
   default void showDialog(final DialogLike dialog) {
      for (Audience audience : this.audiences()) {
         audience.showDialog(dialog);
      }
   }

   @Override
   default void closeDialog() {
      for (Audience audience : this.audiences()) {
         audience.closeDialog();
      }
   }

   public interface Single extends ForwardingAudience {
      Audience audience();

      @Override
      default Iterable<? extends Audience> audiences() {
         return Collections.singleton(this.audience());
      }

      @Override
      default <T> Optional<T> get(final Pointer<T> pointer) {
         return this.audience().get(pointer);
      }

      @Contract("_, null -> null; _, !null -> !null")
      @Nullable
      @Override
      default <T> T getOrDefault(final Pointer<T> pointer, @Nullable final T defaultValue) {
         return this.audience().getOrDefault(pointer, defaultValue);
      }

      @UnknownNullability
      @Override
      default <T> T getOrDefaultFrom(final Pointer<T> pointer, final Supplier<? extends T> defaultValue) {
         return this.audience().getOrDefaultFrom(pointer, defaultValue);
      }

      @Override
      default Audience filterAudience(final Predicate<? super Audience> filter) {
         Audience audience = this.audience();
         return (Audience)(filter.test(audience) ? this : Audience.empty());
      }

      @Override
      default void forEachAudience(final Consumer<? super Audience> action) {
         this.audience().forEachAudience(action);
      }

      @Override
      default Pointers pointers() {
         return this.audience().pointers();
      }

      @Override
      default void sendMessage(final Component message) {
         this.audience().sendMessage(message);
      }

      @Override
      default void sendMessage(final Component message, final ChatType.Bound boundChatType) {
         this.audience().sendMessage(message, boundChatType);
      }

      @Override
      default void sendMessage(final SignedMessage signedMessage, final ChatType.Bound boundChatType) {
         this.audience().sendMessage(signedMessage, boundChatType);
      }

      @Override
      default void deleteMessage(final SignedMessage.Signature signature) {
         this.audience().deleteMessage(signature);
      }

      @Override
      default void sendActionBar(final Component message) {
         this.audience().sendActionBar(message);
      }

      @Override
      default void sendPlayerListHeader(final Component header) {
         this.audience().sendPlayerListHeader(header);
      }

      @Override
      default void sendPlayerListFooter(final Component footer) {
         this.audience().sendPlayerListFooter(footer);
      }

      @Override
      default void sendPlayerListHeaderAndFooter(final Component header, final Component footer) {
         this.audience().sendPlayerListHeaderAndFooter(header, footer);
      }

      @Override
      default <T> void sendTitlePart(final TitlePart<T> part, final T value) {
         this.audience().sendTitlePart(part, value);
      }

      @Override
      default void clearTitle() {
         this.audience().clearTitle();
      }

      @Override
      default void resetTitle() {
         this.audience().resetTitle();
      }

      @Override
      default void showBossBar(final BossBar bar) {
         this.audience().showBossBar(bar);
      }

      @Override
      default void hideBossBar(final BossBar bar) {
         this.audience().hideBossBar(bar);
      }

      @Override
      default void playSound(final Sound sound) {
         this.audience().playSound(sound);
      }

      @Override
      default void playSound(final Sound sound, final double x, final double y, final double z) {
         this.audience().playSound(sound, x, y, z);
      }

      @Override
      default void playSound(final Sound sound, final Sound.Emitter emitter) {
         this.audience().playSound(sound, emitter);
      }

      @Override
      default void stopSound(final SoundStop stop) {
         this.audience().stopSound(stop);
      }

      @Override
      default void openBook(final Book book) {
         this.audience().openBook(book);
      }

      @Override
      default void sendResourcePacks(final ResourcePackRequest request) {
         this.audience().sendResourcePacks(request.callback(Audiences.unwrapCallback(this, this.audience(), request.callback())));
      }

      @Override
      default void removeResourcePacks(final Iterable<UUID> ids) {
         this.audience().removeResourcePacks(ids);
      }

      @Override
      default void removeResourcePacks(final UUID id, final UUID... others) {
         this.audience().removeResourcePacks(id, others);
      }

      @Override
      default void clearResourcePacks() {
         this.audience().clearResourcePacks();
      }

      @Override
      default void showDialog(final DialogLike dialog) {
         this.audience().showDialog(dialog);
      }

      @Override
      default void closeDialog() {
         this.audience().closeDialog();
      }
   }
}
