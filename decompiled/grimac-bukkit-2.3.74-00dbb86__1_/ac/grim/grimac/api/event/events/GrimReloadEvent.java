package ac.grim.grimac.api.event.events;

import ac.grim.grimac.api.event.EventChannel;
import ac.grim.grimac.api.event.GrimEvent;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

public class GrimReloadEvent extends GrimEvent<GrimReloadEvent.Channel> {
   private boolean success;

   public GrimReloadEvent() {
      super(true);
   }

   public GrimReloadEvent(boolean success) {
      super(true);
      this.success = success;
   }

   @ApiStatus.Internal
   public void init(boolean success) {
      this.resetForReuse();
      this.success = success;
   }

   public boolean isSuccess() {
      return this.success;
   }

   public static final class Channel extends EventChannel<GrimReloadEvent, GrimReloadEvent.Handler> {
      private final ThreadLocal<GrimReloadEvent> legacyPool = ThreadLocal.withInitial(GrimReloadEvent::new);

      public Channel() {
         super(GrimReloadEvent.class, GrimReloadEvent.Handler.class);
      }

      public void onReload(@NotNull GrimPlugin plugin, @NotNull GrimReloadEvent.Handler handler) {
         this.subscribe(handler, 0, false, plugin, null);
      }

      public void onReload(@NotNull GrimPlugin plugin, @NotNull GrimReloadEvent.Handler handler, int priority) {
         this.subscribe(handler, priority, false, plugin, null);
      }

      @Deprecated
      public void onReload(@NotNull Object pluginContext, @NotNull GrimReloadEvent.Handler handler) {
         this.onReload(this.resolvePlugin(pluginContext), handler);
      }

      @Deprecated
      public void onReload(@NotNull Object pluginContext, @NotNull GrimReloadEvent.Handler handler, int priority) {
         this.onReload(this.resolvePlugin(pluginContext), handler, priority);
      }

      public void fire(boolean success) {
         EventChannel.Entry<GrimReloadEvent.Handler>[] entries = this.entries();
         if (entries.length != 0) {
            if (!this.hasLegacy()) {
               for (EventChannel.Entry<GrimReloadEvent.Handler> e : entries) {
                  try {
                     e.handler.onReload(success);
                  } catch (Throwable var9) {
                     var9.printStackTrace();
                  }
               }
            } else {
               GrimReloadEvent pooled = this.legacyPool.get();
               pooled.init(success);

               for (EventChannel.Entry<GrimReloadEvent.Handler> e : entries) {
                  try {
                     if (e.legacyListener != null) {
                        e.<GrimReloadEvent>legacyListenerAs().handle(pooled);
                     } else {
                        e.handler.onReload(success);
                     }
                  } catch (Throwable var10) {
                     var10.printStackTrace();
                  }
               }
            }
         }
      }

      protected boolean dispatchTypedFromLegacy(@NotNull GrimReloadEvent event, @NotNull GrimReloadEvent.Handler handler, boolean cancelled) {
         handler.onReload(event.isSuccess());
         return false;
      }

      @ApiStatus.Internal
      @NotNull
      public static GrimReloadEvent.Handler bridgeFromAny(@NotNull GrimEvent.Handler abstractHandler) {
         return success -> abstractHandler.onAnyEvent(GrimReloadEvent.class, false);
      }
   }

   @FunctionalInterface
   public interface Handler {
      void onReload(boolean var1);
   }
}
