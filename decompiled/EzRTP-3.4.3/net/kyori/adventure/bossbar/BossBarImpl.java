package net.kyori.adventure.bossbar;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.Services;
import org.jspecify.annotations.Nullable;

final class BossBarImpl extends HackyBossBarPlatformBridge implements BossBar {
   private final List<BossBar.Listener> listeners = new CopyOnWriteArrayList<>();
   private Component name;
   private float progress;
   private BossBar.Color color;
   private BossBar.Overlay overlay;
   private final Set<BossBar.Flag> flags = EnumSet.noneOf(BossBar.Flag.class);
   @Nullable
   BossBarImplementation implementation;

   BossBarImpl(final Component name, final float progress, final BossBar.Color color, final BossBar.Overlay overlay) {
      this.name = Objects.requireNonNull(name, "name");
      this.progress = progress;
      this.color = Objects.requireNonNull(color, "color");
      this.overlay = Objects.requireNonNull(overlay, "overlay");
   }

   BossBarImpl(final Component name, final float progress, final BossBar.Color color, final BossBar.Overlay overlay, final Set<BossBar.Flag> flags) {
      this(name, progress, color, overlay);
      this.flags.addAll(flags);
   }

   @Override
   public Component name() {
      return this.name;
   }

   @Override
   public BossBar name(final Component newName) {
      Objects.requireNonNull(newName, "name");
      Component oldName = this.name;
      this.name = newName;
      this.forEachListener(listener -> listener.bossBarNameChanged(this, oldName, newName));
      return this;
   }

   @Override
   public float progress() {
      return this.progress;
   }

   @Override
   public BossBar progress(final float newProgress) {
      checkProgress(newProgress);
      float oldProgress = this.progress;
      if (newProgress != oldProgress) {
         this.progress = newProgress;
         this.forEachListener(listener -> listener.bossBarProgressChanged(this, oldProgress, newProgress));
      }

      return this;
   }

   static void checkProgress(final float progress) {
      if (progress < 0.0F || progress > 1.0F) {
         throw new IllegalArgumentException("progress must be between 0.0 and 1.0, was " + progress);
      }
   }

   @Override
   public BossBar.Color color() {
      return this.color;
   }

   @Override
   public BossBar color(final BossBar.Color newColor) {
      Objects.requireNonNull(newColor, "color");
      BossBar.Color oldColor = this.color;
      if (newColor != oldColor) {
         this.color = newColor;
         this.forEachListener(listener -> listener.bossBarColorChanged(this, oldColor, newColor));
      }

      return this;
   }

   @Override
   public BossBar.Overlay overlay() {
      return this.overlay;
   }

   @Override
   public BossBar overlay(final BossBar.Overlay newOverlay) {
      Objects.requireNonNull(newOverlay, "overlay");
      BossBar.Overlay oldOverlay = this.overlay;
      if (newOverlay != oldOverlay) {
         this.overlay = newOverlay;
         this.forEachListener(listener -> listener.bossBarOverlayChanged(this, oldOverlay, newOverlay));
      }

      return this;
   }

   @Override
   public Set<BossBar.Flag> flags() {
      return Collections.unmodifiableSet(this.flags);
   }

   @Override
   public BossBar flags(final Set<BossBar.Flag> newFlags) {
      if (newFlags.isEmpty() && !this.flags.isEmpty()) {
         Set<BossBar.Flag> oldFlags = EnumSet.copyOf(this.flags);
         this.flags.clear();
         this.forEachListener(listener -> listener.bossBarFlagsChanged(this, Set.of(), oldFlags));
      } else if (!this.flags.equals(newFlags)) {
         Set<BossBar.Flag> oldFlags = EnumSet.copyOf(this.flags);
         this.flags.clear();
         this.flags.addAll(newFlags);
         Set<BossBar.Flag> added = EnumSet.copyOf(newFlags);
         added.removeIf(oldFlags::contains);
         Set<BossBar.Flag> removed = EnumSet.copyOf(oldFlags);
         removed.removeIf(this.flags::contains);
         this.forEachListener(listener -> listener.bossBarFlagsChanged(this, added, removed));
      }

      return this;
   }

   @Override
   public boolean hasFlag(final BossBar.Flag flag) {
      return this.flags.contains(flag);
   }

   @Override
   public BossBar addFlag(final BossBar.Flag flag) {
      return this.editFlags(flag, Set::add, BossBarImpl::onFlagsAdded);
   }

   @Override
   public BossBar removeFlag(final BossBar.Flag flag) {
      return this.editFlags(flag, Set::remove, BossBarImpl::onFlagsRemoved);
   }

   private BossBar editFlags(
      final BossBar.Flag flag, final BiPredicate<Set<BossBar.Flag>, BossBar.Flag> predicate, final BiConsumer<BossBarImpl, Set<BossBar.Flag>> onChange
   ) {
      if (predicate.test(this.flags, flag)) {
         onChange.accept(this, Collections.singleton(flag));
      }

      return this;
   }

   @Override
   public BossBar addFlags(final BossBar.Flag... flags) {
      return this.editFlags(flags, Set::add, BossBarImpl::onFlagsAdded);
   }

   @Override
   public BossBar removeFlags(final BossBar.Flag... flags) {
      return this.editFlags(flags, Set::remove, BossBarImpl::onFlagsRemoved);
   }

   private BossBar editFlags(
      final BossBar.Flag[] flags, final BiPredicate<Set<BossBar.Flag>, BossBar.Flag> predicate, final BiConsumer<BossBarImpl, Set<BossBar.Flag>> onChange
   ) {
      if (flags.length == 0) {
         return this;
      } else {
         Set<BossBar.Flag> changes = null;

         for (BossBar.Flag flag : flags) {
            if (predicate.test(this.flags, flag)) {
               if (changes == null) {
                  changes = EnumSet.noneOf(BossBar.Flag.class);
               }

               changes.add(flag);
            }
         }

         if (changes != null) {
            onChange.accept(this, changes);
         }

         return this;
      }
   }

   @Override
   public BossBar addFlags(final Iterable<BossBar.Flag> flags) {
      return this.editFlags(flags, Set::add, BossBarImpl::onFlagsAdded);
   }

   @Override
   public BossBar removeFlags(final Iterable<BossBar.Flag> flags) {
      return this.editFlags(flags, Set::remove, BossBarImpl::onFlagsRemoved);
   }

   private BossBar editFlags(
      final Iterable<BossBar.Flag> flags,
      final BiPredicate<Set<BossBar.Flag>, BossBar.Flag> predicate,
      final BiConsumer<BossBarImpl, Set<BossBar.Flag>> onChange
   ) {
      Set<BossBar.Flag> changes = null;

      for (BossBar.Flag flag : flags) {
         if (predicate.test(this.flags, flag)) {
            if (changes == null) {
               changes = EnumSet.noneOf(BossBar.Flag.class);
            }

            changes.add(flag);
         }
      }

      if (changes != null) {
         onChange.accept(this, changes);
      }

      return this;
   }

   @Override
   public BossBar addListener(final BossBar.Listener listener) {
      this.listeners.add(listener);
      return this;
   }

   @Override
   public BossBar removeListener(final BossBar.Listener listener) {
      this.listeners.remove(listener);
      return this;
   }

   @Override
   public Iterable<? extends BossBarViewer> viewers() {
      return (Iterable<? extends BossBarViewer>)(this.implementation != null ? this.implementation.viewers() : List.of());
   }

   private void forEachListener(final Consumer<BossBar.Listener> consumer) {
      for (BossBar.Listener listener : this.listeners) {
         consumer.accept(listener);
      }
   }

   private static void onFlagsAdded(final BossBarImpl bar, final Set<BossBar.Flag> flagsAdded) {
      bar.forEachListener(listener -> listener.bossBarFlagsChanged(bar, flagsAdded, Set.of()));
   }

   private static void onFlagsRemoved(final BossBarImpl bar, final Set<BossBar.Flag> flagsRemoved) {
      bar.forEachListener(listener -> listener.bossBarFlagsChanged(bar, Set.of(), flagsRemoved));
   }

   @Override
   public String toString() {
      return "BossBarImpl{listeners="
         + this.listeners
         + ", name="
         + this.name
         + ", progress="
         + this.progress
         + ", color="
         + this.color
         + ", overlay="
         + this.overlay
         + ", flags="
         + this.flags
         + "}";
   }

   static final class ImplementationAccessor {
      private static final Optional<BossBarImplementation.Provider> SERVICE = Services.service(
         ServiceLoader.load(BossBarImplementation.Provider.class, BossBarImplementation.Provider.class.getClassLoader()), BossBarImplementation.Provider.class
      );

      private ImplementationAccessor() {
      }

      static <I extends BossBarImplementation> I get(final BossBar bar, final Class<I> type) {
         BossBarImplementation implementation = ((BossBarImpl)bar).implementation;
         if (implementation == null) {
            implementation = SERVICE.get().create(bar);
            ((BossBarImpl)bar).implementation = implementation;
         }

         return type.cast(implementation);
      }
   }
}
