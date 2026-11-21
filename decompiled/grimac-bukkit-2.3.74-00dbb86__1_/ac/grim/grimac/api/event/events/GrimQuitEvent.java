package ac.grim.grimac.api.event.events;

import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.api.event.EventChannel;
import ac.grim.grimac.api.event.GrimEvent;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

public class GrimQuitEvent extends GrimEvent<GrimQuitEvent.Channel> implements GrimUserEvent {
   private GrimUser user;

   public GrimQuitEvent() {
      super(true);
   }

   public GrimQuitEvent(GrimUser user) {
      super(true);
      this.user = user;
   }

   @ApiStatus.Internal
   public void init(GrimUser user) {
      this.resetForReuse();
      this.user = user;
   }

   @Override
   public GrimUser getUser() {
      return this.user;
   }

   public static final class Channel extends EventChannel<GrimQuitEvent, GrimQuitEvent.Handler> {
      private final ThreadLocal<GrimQuitEvent> legacyPool = ThreadLocal.withInitial(GrimQuitEvent::new);

      public Channel() {
         super(GrimQuitEvent.class, GrimQuitEvent.Handler.class);
      }

      public void onQuit(@NotNull GrimPlugin plugin, @NotNull GrimQuitEvent.Handler handler) {
         this.subscribe(handler, 0, false, plugin, null);
      }

      public void onQuit(@NotNull GrimPlugin plugin, @NotNull GrimQuitEvent.Handler handler, int priority) {
         this.subscribe(handler, priority, false, plugin, null);
      }

      @Deprecated
      public void onQuit(@NotNull Object pluginContext, @NotNull GrimQuitEvent.Handler handler) {
         this.onQuit(this.resolvePlugin(pluginContext), handler);
      }

      @Deprecated
      public void onQuit(@NotNull Object pluginContext, @NotNull GrimQuitEvent.Handler handler, int priority) {
         this.onQuit(this.resolvePlugin(pluginContext), handler, priority);
      }

      public void fire(@NotNull GrimUser user) {
         EventChannel.Entry<GrimQuitEvent.Handler>[] entries = this.entries();
         if (entries.length != 0) {
            if (!this.hasLegacy()) {
               for (EventChannel.Entry<GrimQuitEvent.Handler> e : entries) {
                  try {
                     e.handler.onQuit(user);
                  } catch (Throwable var9) {
                     var9.printStackTrace();
                  }
               }
            } else {
               GrimQuitEvent pooled = this.legacyPool.get();
               pooled.init(user);

               for (EventChannel.Entry<GrimQuitEvent.Handler> e : entries) {
                  try {
                     if (e.legacyListener != null) {
                        e.<GrimQuitEvent>legacyListenerAs().handle(pooled);
                     } else {
                        e.handler.onQuit(user);
                     }
                  } catch (Throwable var10) {
                     var10.printStackTrace();
                  }
               }
            }
         }
      }

      protected boolean dispatchTypedFromLegacy(@NotNull GrimQuitEvent event, @NotNull GrimQuitEvent.Handler handler, boolean cancelled) {
         handler.onQuit(event.getUser());
         return false;
      }

      @ApiStatus.Internal
      @NotNull
      public static GrimQuitEvent.Handler bridgeFromAny(@NotNull GrimEvent.Handler abstractHandler) {
         return user -> abstractHandler.onAnyEvent(GrimQuitEvent.class, false);
      }
   }

   @FunctionalInterface
   public interface Handler {
      void onQuit(@NotNull GrimUser var1);
   }
}
