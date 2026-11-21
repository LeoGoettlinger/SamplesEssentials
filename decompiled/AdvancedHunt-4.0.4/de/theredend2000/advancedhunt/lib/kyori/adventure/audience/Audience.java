package de.theredend2000.advancedhunt.lib.kyori.adventure.audience;

import de.theredend2000.advancedhunt.lib.kyori.adventure.bossbar.BossBar;
import de.theredend2000.advancedhunt.lib.kyori.adventure.chat.ChatType;
import de.theredend2000.advancedhunt.lib.kyori.adventure.chat.SignedMessage;
import de.theredend2000.advancedhunt.lib.kyori.adventure.dialog.DialogLike;
import de.theredend2000.advancedhunt.lib.kyori.adventure.inventory.Book;
import de.theredend2000.advancedhunt.lib.kyori.adventure.inventory.BookLike;
import de.theredend2000.advancedhunt.lib.kyori.adventure.pointer.Pointered;
import de.theredend2000.advancedhunt.lib.kyori.adventure.resource.ResourcePackInfo;
import de.theredend2000.advancedhunt.lib.kyori.adventure.resource.ResourcePackInfoLike;
import de.theredend2000.advancedhunt.lib.kyori.adventure.resource.ResourcePackRequest;
import de.theredend2000.advancedhunt.lib.kyori.adventure.resource.ResourcePackRequestLike;
import de.theredend2000.advancedhunt.lib.kyori.adventure.sound.Sound;
import de.theredend2000.advancedhunt.lib.kyori.adventure.sound.SoundStop;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.Component;
import de.theredend2000.advancedhunt.lib.kyori.adventure.text.ComponentLike;
import de.theredend2000.advancedhunt.lib.kyori.adventure.title.Title;
import de.theredend2000.advancedhunt.lib.kyori.adventure.title.TitlePart;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;

public interface Audience extends Pointered {
   static Audience empty() {
      return EmptyAudience.INSTANCE;
   }

   static Audience audience(final Audience... audiences) {
      int length = audiences.length;
      if (length == 0) {
         return empty();
      } else {
         return (Audience)(length == 1 ? audiences[0] : audience(List.of(audiences)));
      }
   }

   static ForwardingAudience audience(final Iterable<? extends Audience> audiences) {
      return () -> audiences;
   }

   static Collector<? super Audience, ?, ForwardingAudience> toAudience() {
      return Audiences.COLLECTOR;
   }

   default Audience filterAudience(final Predicate<? super Audience> filter) {
      return filter.test(this) ? this : empty();
   }

   default void forEachAudience(final Consumer<? super Audience> action) {
      action.accept(this);
   }

   @ForwardingAudienceOverrideNotRequired
   default void sendMessage(final ComponentLike message) {
      this.sendMessage(message.asComponent());
   }

   default void sendMessage(final Component message) {
   }

   default void sendMessage(final Component message, final ChatType.Bound boundChatType) {
   }

   @ForwardingAudienceOverrideNotRequired
   default void sendMessage(final ComponentLike message, final ChatType.Bound boundChatType) {
      this.sendMessage(message.asComponent(), boundChatType);
   }

   default void sendMessage(final SignedMessage signedMessage, final ChatType.Bound boundChatType) {
   }

   @ForwardingAudienceOverrideNotRequired
   default void deleteMessage(final SignedMessage signedMessage) {
      if (signedMessage.canDelete()) {
         this.deleteMessage(Objects.requireNonNull(signedMessage.signature()));
      }
   }

   default void deleteMessage(final SignedMessage.Signature signature) {
   }

   @ForwardingAudienceOverrideNotRequired
   default void sendActionBar(final ComponentLike message) {
      this.sendActionBar(message.asComponent());
   }

   default void sendActionBar(final Component message) {
   }

   @ForwardingAudienceOverrideNotRequired
   default void sendPlayerListHeader(final ComponentLike header) {
      this.sendPlayerListHeader(header.asComponent());
   }

   default void sendPlayerListHeader(final Component header) {
      this.sendPlayerListHeaderAndFooter(header, Component.empty());
   }

   @ForwardingAudienceOverrideNotRequired
   default void sendPlayerListFooter(final ComponentLike footer) {
      this.sendPlayerListFooter(footer.asComponent());
   }

   default void sendPlayerListFooter(final Component footer) {
      this.sendPlayerListHeaderAndFooter(Component.empty(), footer);
   }

   @ForwardingAudienceOverrideNotRequired
   default void sendPlayerListHeaderAndFooter(final ComponentLike header, final ComponentLike footer) {
      this.sendPlayerListHeaderAndFooter(header.asComponent(), footer.asComponent());
   }

   default void sendPlayerListHeaderAndFooter(final Component header, final Component footer) {
   }

   @ForwardingAudienceOverrideNotRequired
   default void showTitle(final Title title) {
      Title.Times times = title.times();
      if (times != null) {
         this.sendTitlePart(TitlePart.TIMES, times);
      }

      this.sendTitlePart(TitlePart.SUBTITLE, title.subtitle());
      this.sendTitlePart(TitlePart.TITLE, title.title());
   }

   default <T> void sendTitlePart(final TitlePart<T> part, final T value) {
   }

   default void clearTitle() {
   }

   default void resetTitle() {
   }

   default void showBossBar(final BossBar bar) {
   }

   default void hideBossBar(final BossBar bar) {
   }

   default void playSound(final Sound sound) {
   }

   default void playSound(final Sound sound, final double x, final double y, final double z) {
   }

   default void playSound(final Sound sound, final Sound.Emitter emitter) {
   }

   @ForwardingAudienceOverrideNotRequired
   default void stopSound(final Sound sound) {
      this.stopSound(Objects.requireNonNull(sound, "sound").asStop());
   }

   default void stopSound(final SoundStop stop) {
   }

   @ForwardingAudienceOverrideNotRequired
   default void openBook(final Book.Builder book) {
      this.openBook(Objects.requireNonNull(book, "book").build());
   }

   @ForwardingAudienceOverrideNotRequired
   default void openBook(final BookLike book) {
      this.openBook(Objects.requireNonNull(book, "book").asBook());
   }

   default void openBook(final Book book) {
   }

   @ForwardingAudienceOverrideNotRequired
   default void sendResourcePacks(final ResourcePackInfoLike first, final ResourcePackInfoLike... others) {
      this.sendResourcePacks(ResourcePackRequest.addingRequest(first, others));
   }

   @ForwardingAudienceOverrideNotRequired
   default void sendResourcePacks(final ResourcePackRequestLike request) {
      this.sendResourcePacks(request.asResourcePackRequest());
   }

   default void sendResourcePacks(final ResourcePackRequest request) {
   }

   @ForwardingAudienceOverrideNotRequired
   default void removeResourcePacks(final ResourcePackRequestLike request) {
      this.removeResourcePacks(request.asResourcePackRequest());
   }

   @ForwardingAudienceOverrideNotRequired
   default void removeResourcePacks(final ResourcePackRequest request) {
      List<ResourcePackInfo> infos = request.packs();
      if (infos.size() == 1) {
         this.removeResourcePacks(infos.getFirst().id());
      } else if (infos.isEmpty()) {
         return;
      }

      UUID[] otherReqs = new UUID[infos.size() - 1];

      for (int i = 0; i < otherReqs.length; i++) {
         otherReqs[i] = infos.get(i + 1).id();
      }

      this.removeResourcePacks(infos.getFirst().id(), otherReqs);
   }

   @ForwardingAudienceOverrideNotRequired
   default void removeResourcePacks(final ResourcePackInfoLike request, final ResourcePackInfoLike... others) {
      UUID[] otherReqs = new UUID[others.length];

      for (int i = 0; i < others.length; i++) {
         otherReqs[i] = others[i].asResourcePackInfo().id();
      }

      this.removeResourcePacks(request.asResourcePackInfo().id(), otherReqs);
   }

   default void removeResourcePacks(final Iterable<UUID> ids) {
      Iterator<UUID> it = ids.iterator();
      if (it.hasNext()) {
         UUID id = it.next();
         UUID[] others;
         if (!it.hasNext()) {
            others = new UUID[0];
         } else if (ids instanceof Collection) {
            others = new UUID[((Collection)ids).size() - 1];

            for (int i = 0; i < others.length; i++) {
               others[i] = it.next();
            }
         } else {
            List<UUID> othersList = new ArrayList<>();

            while (it.hasNext()) {
               othersList.add(it.next());
            }

            others = othersList.toArray(new UUID[0]);
         }

         this.removeResourcePacks(id, others);
      }
   }

   default void removeResourcePacks(final UUID id, final UUID... others) {
   }

   default void clearResourcePacks() {
   }

   default void showDialog(final DialogLike dialog) {
   }

   default void closeDialog() {
   }
}
