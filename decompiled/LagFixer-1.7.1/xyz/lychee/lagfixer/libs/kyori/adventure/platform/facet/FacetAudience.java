package xyz.lychee.lagfixer.libs.kyori.adventure.platform.facet;

import java.io.Closeable;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArraySet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lychee.lagfixer.libs.kyori.adventure.audience.Audience;
import xyz.lychee.lagfixer.libs.kyori.adventure.audience.MessageType;
import xyz.lychee.lagfixer.libs.kyori.adventure.bossbar.BossBar;
import xyz.lychee.lagfixer.libs.kyori.adventure.chat.ChatType;
import xyz.lychee.lagfixer.libs.kyori.adventure.chat.SignedMessage;
import xyz.lychee.lagfixer.libs.kyori.adventure.identity.Identity;
import xyz.lychee.lagfixer.libs.kyori.adventure.inventory.Book;
import xyz.lychee.lagfixer.libs.kyori.adventure.pointer.Pointers;
import xyz.lychee.lagfixer.libs.kyori.adventure.sound.Sound;
import xyz.lychee.lagfixer.libs.kyori.adventure.sound.SoundStop;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.Component;
import xyz.lychee.lagfixer.libs.kyori.adventure.text.flattener.ComponentFlattener;
import xyz.lychee.lagfixer.libs.kyori.adventure.title.Title;
import xyz.lychee.lagfixer.libs.kyori.adventure.title.TitlePart;

@ApiStatus.Internal
public class FacetAudience<V> implements Audience, Closeable {
   @NotNull
   protected final FacetAudienceProvider<V, FacetAudience<V>> provider;
   @NotNull
   private final Set<V> viewers;
   @Nullable
   private V viewer;
   private volatile Pointers pointers;
   @Nullable
   private final Facet.Chat<V, Object> chat;
   @Nullable
   private final Facet.ActionBar<V, Object> actionBar;
   @Nullable
   private final Facet.Title<V, Object, Object, Object> title;
   @Nullable
   private final Facet.Sound<V, Object> sound;
   @Nullable
   private final Facet.EntitySound<V, Object> entitySound;
   @Nullable
   private final Facet.Book<V, Object, Object> book;
   @Nullable
   private final Facet.BossBar.Builder<V, Facet.BossBar<V>> bossBar;
   @Nullable
   private final Map<BossBar, Facet.BossBar<V>> bossBars;
   @Nullable
   private final Facet.TabList<V, Object> tabList;
   @NotNull
   private final Collection<? extends Facet.Pointers<V>> pointerProviders;

   public FacetAudience(
      @NotNull final FacetAudienceProvider provider,
      @NotNull final Collection<? extends V> viewers,
      @Nullable final Collection<? extends Facet.Chat> chat,
      @Nullable final Collection<? extends Facet.ActionBar> actionBar,
      @Nullable final Collection<? extends Facet.Title> title,
      @Nullable final Collection<? extends Facet.Sound> sound,
      @Nullable final Collection<? extends Facet.EntitySound> entitySound,
      @Nullable final Collection<? extends Facet.Book> book,
      @Nullable final Collection<? extends Facet.BossBar.Builder> bossBar,
      @Nullable final Collection<? extends Facet.TabList> tabList,
      @Nullable final Collection<? extends Facet.Pointers> pointerProviders
   ) {
      this.provider = Objects.requireNonNull(provider, "audience provider");
      this.viewers = new CopyOnWriteArraySet<>();

      for (V viewer : Objects.requireNonNull(viewers, "viewers")) {
         this.addViewer(viewer);
      }

      this.refresh();
      this.chat = Facet.of((Collection<Facet.Chat<V, Object>>)chat, this.viewer);
      this.actionBar = Facet.of((Collection<Facet.ActionBar<V, Object>>)actionBar, this.viewer);
      this.title = Facet.of((Collection<Facet.Title<V, Object, Object, Object>>)title, this.viewer);
      this.sound = Facet.of((Collection<Facet.Sound<V, Object>>)sound, this.viewer);
      this.entitySound = Facet.of((Collection<Facet.EntitySound<V, Object>>)entitySound, this.viewer);
      this.book = Facet.of((Collection<Facet.Book<V, Object, Object>>)book, this.viewer);
      this.bossBar = Facet.of((Collection<Facet.BossBar.Builder<V, Facet.BossBar<V>>>)bossBar, this.viewer);
      this.bossBars = this.bossBar == null ? null : Collections.synchronizedMap(new IdentityHashMap<>(4));
      this.tabList = Facet.of((Collection<Facet.TabList<V, Object>>)tabList, this.viewer);
      this.pointerProviders = (Collection<? extends Facet.Pointers<V>>)(pointerProviders == null ? Collections.emptyList() : pointerProviders);
   }

   public void addViewer(@NotNull final V viewer) {
      if (this.viewers.add(viewer) && this.viewer == null) {
         this.viewer = viewer;
         this.refresh();
      }
   }

   public void removeViewer(@NotNull final V viewer) {
      if (this.viewers.remove(viewer) && this.viewer == viewer) {
         this.viewer = this.viewers.isEmpty() ? null : this.viewers.iterator().next();
         this.refresh();
      }

      if (this.bossBars != null) {
         for (Facet.BossBar<V> listener : this.bossBars.values()) {
            listener.removeViewer(viewer);
         }
      }
   }

   public void refresh() {
      synchronized (this) {
         this.pointers = null;
      }

      if (this.bossBars != null) {
         for (Entry<BossBar, Facet.BossBar<V>> entry : this.bossBars.entrySet()) {
            BossBar bar = entry.getKey();
            Facet.BossBar<V> listener = entry.getValue();
            listener.bossBarNameChanged(bar, bar.name(), bar.name());
         }
      }
   }

   @Override
   public void sendMessage(@NotNull final Identity source, @NotNull final Component original, @NotNull final MessageType type) {
      if (this.chat != null) {
         Object message = this.createMessage(original, this.chat);
         if (message != null) {
            for (V viewer : this.viewers) {
               this.chat.sendMessage(viewer, source, message, type);
            }
         }
      }
   }

   @Override
   public void sendMessage(@NotNull final Component original, @NotNull final ChatType.Bound boundChatType) {
      if (this.chat != null) {
         Object message = this.createMessage(original, this.chat);
         if (message != null) {
            Component name = this.provider.componentRenderer.render(boundChatType.name(), this);
            Component target = null;
            if (boundChatType.target() != null) {
               target = this.provider.componentRenderer.render(boundChatType.target(), this);
            }

            Object renderedType = boundChatType.type().bind(name, target);

            for (V viewer : this.viewers) {
               this.chat.sendMessage(viewer, Identity.nil(), message, renderedType);
            }
         }
      }
   }

   @Override
   public void sendMessage(@NotNull final SignedMessage signedMessage, @NotNull final ChatType.Bound boundChatType) {
      if (signedMessage.isSystem()) {
         Component content = (Component)(signedMessage.unsignedContent() != null ? signedMessage.unsignedContent() : Component.text(signedMessage.message()));
         this.sendMessage(content, boundChatType);
      } else {
         Audience.super.sendMessage(signedMessage, boundChatType);
      }
   }

   @Override
   public void sendActionBar(@NotNull final Component original) {
      if (this.actionBar != null) {
         Object message = this.createMessage(original, this.actionBar);
         if (message != null) {
            for (V viewer : this.viewers) {
               this.actionBar.sendMessage(viewer, message);
            }
         }
      }
   }

   @Override
   public void playSound(@NotNull final Sound original) {
      if (this.sound != null) {
         for (V viewer : this.viewers) {
            Object position = this.sound.createPosition(viewer);
            if (position != null) {
               this.sound.playSound(viewer, original, position);
            }
         }
      }
   }

   @Override
   public void playSound(@NotNull final Sound sound, @NotNull final Sound.Emitter emitter) {
      if (this.entitySound != null) {
         if (emitter == Sound.Emitter.self()) {
            for (V viewer : this.viewers) {
               Object message = this.entitySound.createForSelf(viewer, sound);
               if (message != null) {
                  this.entitySound.playSound(viewer, message);
               }
            }
         } else {
            Object message = this.entitySound.createForEmitter(sound, emitter);
            if (message == null) {
               return;
            }

            for (V viewerx : this.viewers) {
               this.entitySound.playSound(viewerx, message);
            }
         }
      }
   }

   @Override
   public void playSound(@NotNull final Sound original, final double x, final double y, final double z) {
      if (this.sound != null) {
         Object position = this.sound.createPosition(x, y, z);

         for (V viewer : this.viewers) {
            this.sound.playSound(viewer, original, position);
         }
      }
   }

   @Override
   public void stopSound(@NotNull final SoundStop original) {
      if (this.sound != null) {
         for (V viewer : this.viewers) {
            this.sound.stopSound(viewer, original);
         }
      }
   }

   @Override
   public void openBook(@NotNull final Book original) {
      if (this.book != null) {
         String title = this.toPlain(original.title());
         String author = this.toPlain(original.author());
         List<Object> pages = new LinkedList<>();

         for (Component originalPage : original.pages()) {
            Object page = this.createMessage(originalPage, this.book);
            if (page != null) {
               pages.add(page);
            }
         }

         if (title != null && author != null && !pages.isEmpty()) {
            Object book = this.book.createBook(title, author, pages);
            if (book != null) {
               for (V viewer : this.viewers) {
                  this.book.openBook(viewer, book);
               }
            }
         }
      }
   }

   private String toPlain(final Component comp) {
      if (comp == null) {
         return null;
      } else {
         StringBuilder builder = new StringBuilder();
         ComponentFlattener.basic().flatten(this.provider.componentRenderer.render(comp, this), builder::append);
         return builder.toString();
      }
   }

   @Override
   public void showTitle(final Title original) {
      if (this.title != null) {
         Object mainTitle = this.createMessage(original.title(), this.title);
         Object subTitle = this.createMessage(original.subtitle(), this.title);
         Title.Times times = original.times();
         int inTicks = times == null ? -1 : this.title.toTicks(times.fadeIn());
         int stayTicks = times == null ? -1 : this.title.toTicks(times.stay());
         int outTicks = times == null ? -1 : this.title.toTicks(times.fadeOut());
         Object collection = this.title.createTitleCollection();
         if (inTicks != -1 || stayTicks != -1 || outTicks != -1) {
            this.title.contributeTimes(collection, inTicks, stayTicks, outTicks);
         }

         this.title.contributeSubtitle(collection, subTitle);
         this.title.contributeTitle(collection, mainTitle);
         Object title = this.title.completeTitle(collection);
         if (title != null) {
            for (V viewer : this.viewers) {
               this.title.showTitle(viewer, title);
            }
         }
      }
   }

   @Override
   public <T> void sendTitlePart(@NotNull final TitlePart<T> part, @NotNull final T value) {
      if (this.title != null) {
         Objects.requireNonNull(value, "value");
         Object collection = this.title.createTitleCollection();
         if (part == TitlePart.TITLE) {
            Object message = this.createMessage((Component)value, this.title);
            if (message != null) {
               this.title.contributeTitle(collection, message);
            }
         } else if (part == TitlePart.SUBTITLE) {
            Object message = this.createMessage((Component)value, this.title);
            if (message != null) {
               this.title.contributeSubtitle(collection, message);
            }
         } else {
            if (part != TitlePart.TIMES) {
               throw new IllegalArgumentException("Unknown TitlePart '" + part + "'");
            }

            Title.Times times = (Title.Times)value;
            int inTicks = this.title.toTicks(times.fadeIn());
            int stayTicks = this.title.toTicks(times.stay());
            int outTicks = this.title.toTicks(times.fadeOut());
            if (inTicks != -1 || stayTicks != -1 || outTicks != -1) {
               this.title.contributeTimes(collection, inTicks, stayTicks, outTicks);
            }
         }

         Object title = this.title.completeTitle(collection);
         if (title != null) {
            for (V viewer : this.viewers) {
               this.title.showTitle(viewer, title);
            }
         }
      }
   }

   @Override
   public void clearTitle() {
      if (this.title != null) {
         for (V viewer : this.viewers) {
            this.title.clearTitle(viewer);
         }
      }
   }

   @Override
   public void resetTitle() {
      if (this.title != null) {
         for (V viewer : this.viewers) {
            this.title.resetTitle(viewer);
         }
      }
   }

   @Override
   public void showBossBar(@NotNull final BossBar bar) {
      if (this.bossBar != null && this.bossBars != null) {
         Facet.BossBar<V> listener;
         synchronized (this.bossBars) {
            listener = this.bossBars.get(bar);
            if (listener == null) {
               listener = new FacetBossBarListener<>(this.bossBar.createBossBar(this.viewers), message -> this.provider.componentRenderer.render(message, this));
               this.bossBars.put(bar, listener);
            }
         }

         if (listener.isEmpty()) {
            listener.bossBarInitialized(bar);
            bar.addListener(listener);
         }

         for (V viewer : this.viewers) {
            listener.addViewer(viewer);
         }
      }
   }

   @Override
   public void hideBossBar(@NotNull final BossBar bar) {
      if (this.bossBars != null) {
         Facet.BossBar<V> listener = this.bossBars.get(bar);
         if (listener != null) {
            for (V viewer : this.viewers) {
               listener.removeViewer(viewer);
            }

            if (listener.isEmpty() && this.bossBars.remove(bar) != null) {
               bar.removeListener(listener);
               listener.close();
            }
         }
      }
   }

   @Override
   public void sendPlayerListHeader(@NotNull final Component header) {
      if (this.tabList != null) {
         Object headerFormatted = this.createMessage(header, this.tabList);
         if (headerFormatted == null) {
            return;
         }

         for (V viewer : this.viewers) {
            this.tabList.send(viewer, headerFormatted, null);
         }
      }
   }

   @Override
   public void sendPlayerListFooter(@NotNull final Component footer) {
      if (this.tabList != null) {
         Object footerFormatted = this.createMessage(footer, this.tabList);
         if (footerFormatted == null) {
            return;
         }

         for (V viewer : this.viewers) {
            this.tabList.send(viewer, null, footerFormatted);
         }
      }
   }

   @Override
   public void sendPlayerListHeaderAndFooter(@NotNull final Component header, @NotNull final Component footer) {
      if (this.tabList != null) {
         Object headerFormatted = this.createMessage(header, this.tabList);
         Object footerFormatted = this.createMessage(footer, this.tabList);
         if (headerFormatted == null || footerFormatted == null) {
            return;
         }

         for (V viewer : this.viewers) {
            this.tabList.send(viewer, headerFormatted, footerFormatted);
         }
      }
   }

   @NotNull
   @Override
   public Pointers pointers() {
      if (this.pointers == null) {
         synchronized (this) {
            if (this.pointers == null) {
               V viewer = this.viewer;
               if (viewer == null) {
                  return Pointers.empty();
               }

               Pointers.Builder builder = Pointers.builder();
               this.contributePointers(builder);

               for (Facet.Pointers<V> provider : this.pointerProviders) {
                  if (provider.isApplicable(viewer)) {
                     provider.contributePointers(viewer, builder);
                  }
               }

               return this.pointers = builder.build();
            }
         }
      }

      return this.pointers;
   }

   @ApiStatus.OverrideOnly
   protected void contributePointers(final Pointers.Builder builder) {
   }

   @Override
   public void close() {
      if (this.bossBars != null) {
         for (BossBar bar : new LinkedList<>(this.bossBars.keySet())) {
            this.hideBossBar(bar);
         }

         this.bossBars.clear();
      }

      for (V viewer : this.viewers) {
         this.removeViewer(viewer);
      }

      this.viewers.clear();
   }

   @Nullable
   private Object createMessage(@NotNull final Component original, @NotNull final Facet.Message<V, Object> facet) {
      Component message = this.provider.componentRenderer.render(original, this);
      V viewer = this.viewer;
      return viewer == null ? null : facet.createMessage(viewer, message);
   }
}
