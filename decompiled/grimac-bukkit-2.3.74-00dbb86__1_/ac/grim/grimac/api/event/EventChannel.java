package ac.grim.grimac.api.event;

import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.Arrays;
import java.util.function.Predicate;

public abstract class EventChannel<E extends GrimEvent<?>, H> {
   private final Class<E> eventClass;
   private final Class<H> handlerType;
   private final Object writeLock = new Object();
   private volatile EventChannel.Entry<H>[] entries;
   private volatile int legacyCount;
   @Nullable
   private volatile EventChannel.PluginResolver pluginResolver;

   protected EventChannel(@NotNull Class<E> eventClass, @NotNull Class<H> handlerType) {
      this.eventClass = eventClass;
      this.handlerType = handlerType;
      this.entries = new EventChannel.Entry[0];
   }

   @NotNull
   public final Class<E> eventClass() {
      return this.eventClass;
   }

   @NotNull
   public final Class<H> handlerType() {
      return this.handlerType;
   }

   @NotNull
   protected final EventChannel.Entry<H>[] entries() {
      return this.entries;
   }

   public final boolean hasLegacy() {
      return this.legacyCount != 0;
   }

   public final boolean isEmpty() {
      return this.entries.length == 0;
   }

   protected final void subscribe(@NotNull H handler, int priority, boolean ignoreCancelled, @Nullable Object pluginContext, @Nullable Class<?> declaringClass) {
      this.addEntry(new EventChannel.Entry<>(handler, null, null, priority, ignoreCancelled, pluginContext, declaringClass));
   }

   @ApiStatus.Internal
   public final void subscribeTyped(@NotNull H handler, int priority, boolean ignoreCancelled, @Nullable Object pluginContext) {
      this.subscribe(handler, priority, ignoreCancelled, pluginContext, null);
   }

   @ApiStatus.Internal
   @NotNull
   protected final GrimPlugin resolvePlugin(@NotNull Object pluginContext) {
      if (pluginContext instanceof GrimPlugin) {
         return (GrimPlugin)pluginContext;
      } else {
         EventChannel.PluginResolver r = this.pluginResolver;
         if (r == null) {
            throw new IllegalStateException(
               "EventChannel "
                  + this.eventClass.getSimpleName()
                  + " has no plugin resolver installed; pass a GrimPlugin directly instead of a platform-specific context."
            );
         } else {
            return r.resolve(pluginContext);
         }
      }
   }

   @ApiStatus.Internal
   public final void setPluginResolver(@Nullable EventChannel.PluginResolver resolver) {
      this.pluginResolver = resolver;
   }

   public void unsubscribe(@NotNull H handler) {
      this.removeMatching(e -> e.handler == handler);
   }

   @ApiStatus.Internal
   public final <GE extends GrimEvent<?>> void subscribeLegacy(
      @NotNull GrimEventListener<GE> listener,
      @NotNull Class<GE> legacyEventClass,
      int priority,
      boolean ignoreCancelled,
      @Nullable Object pluginContext,
      @Nullable Class<?> declaringClass
   ) {
      this.addEntry(new EventChannel.Entry<>(null, listener, legacyEventClass, priority, ignoreCancelled, pluginContext, declaringClass));
   }

   @ApiStatus.Internal
   public final void unsubscribeLegacy(@NotNull GrimEventListener<?> listener) {
      this.removeMatching(e -> e.legacyListener == listener);
   }

   @ApiStatus.Internal
   public final void unsubscribeAllFromPlugin(@NotNull Object pluginContext) {
      this.removeMatching(e -> pluginContext.equals(e.pluginContext));
   }

   @ApiStatus.Internal
   public final void unsubscribeLegacyFromClass(@NotNull Object pluginContext, @NotNull Class<?> declaringClass) {
      this.removeMatching(e -> e.legacyListener != null && pluginContext.equals(e.pluginContext) && declaringClass.equals(e.declaringClass));
   }

   @ApiStatus.Internal
   public final void dispatchLegacy(@NotNull E event) {
      EventChannel.Entry<H>[] arr = this.entries;
      if (arr.length != 0) {
         boolean isCan = event instanceof Cancellable;
         boolean cancelled = isCan && ((Cancellable)event).isCancelled();

         for (EventChannel.Entry<H> e : arr) {
            if (!cancelled || e.ignoreCancelled) {
               try {
                  if (e.legacyListener != null) {
                     if (isCan) {
                        ((Cancellable)event).setCancelled(cancelled);
                     }

                     GrimEventListener<E> listener = (GrimEventListener<E>)e.legacyListener;
                     listener.handle(event);
                     if (isCan) {
                        cancelled = ((Cancellable)event).isCancelled();
                     }
                  } else {
                     cancelled = this.dispatchTypedFromLegacy(event, e.handler, cancelled);
                  }
               } catch (Throwable var10) {
                  var10.printStackTrace();
               }
            }
         }
      }
   }

   protected abstract boolean dispatchTypedFromLegacy(@NotNull E var1, @NotNull H var2, boolean var3);

   private void addEntry(EventChannel.Entry<H> entry) {
      synchronized (this.writeLock) {
         EventChannel.Entry<H>[] old = this.entries;
         EventChannel.Entry<H>[] next = Arrays.copyOf(old, old.length + 1);

         int i;
         for (i = old.length; i > 0 && old[i - 1].priority > entry.priority; i--) {
            next[i] = old[i - 1];
         }

         next[i] = entry;
         this.entries = next;
         if (entry.legacyListener != null) {
            this.legacyCount++;
         }
      }
   }

   private void removeMatching(@NotNull Predicate<EventChannel.Entry<H>> filter) {
      synchronized (this.writeLock) {
         EventChannel.Entry<H>[] old = this.entries;
         int keep = 0;

         for (EventChannel.Entry<H> e : old) {
            if (!filter.test(e)) {
               keep++;
            }
         }

         if (keep != old.length) {
            EventChannel.Entry<H>[] next = new EventChannel.Entry[keep];
            int removedLegacy = 0;
            int idx = 0;

            for (EventChannel.Entry<H> ex : old) {
               if (filter.test(ex)) {
                  if (ex.legacyListener != null) {
                     removedLegacy++;
                  }
               } else {
                  next[idx++] = ex;
               }
            }

            this.entries = next;
            this.legacyCount -= removedLegacy;
         }
      }
   }

   public static final class Entry<H> {
      @Nullable
      public final H handler;
      @Nullable
      public final GrimEventListener<? extends GrimEvent<?>> legacyListener;
      @Nullable
      public final Class<? extends GrimEvent<?>> legacyEventClass;
      public final int priority;
      public final boolean ignoreCancelled;
      @Nullable
      public final Object pluginContext;
      @Nullable
      public final Class<?> declaringClass;

      Entry(
         @Nullable H handler,
         @Nullable GrimEventListener<? extends GrimEvent<?>> legacyListener,
         @Nullable Class<? extends GrimEvent<?>> legacyEventClass,
         int priority,
         boolean ignoreCancelled,
         @Nullable Object pluginContext,
         @Nullable Class<?> declaringClass
      ) {
         this.handler = handler;
         this.legacyListener = legacyListener;
         this.legacyEventClass = legacyEventClass;
         this.priority = priority;
         this.ignoreCancelled = ignoreCancelled;
         this.pluginContext = pluginContext;
         this.declaringClass = declaringClass;
      }

      public boolean isLegacy() {
         return this.legacyListener != null;
      }

      @NotNull
      public <GE extends GrimEvent<?>> GrimEventListener<GE> legacyListenerAs() {
         return (GrimEventListener<GE>)this.legacyListener;
      }
   }

   @FunctionalInterface
   public interface PluginResolver {
      @NotNull
      GrimPlugin resolve(@NotNull Object var1);
   }
}
