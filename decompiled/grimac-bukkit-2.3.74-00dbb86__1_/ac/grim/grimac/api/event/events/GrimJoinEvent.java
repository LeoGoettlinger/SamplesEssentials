package ac.grim.grimac.api.event.events;

import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.api.event.EventChannel;
import ac.grim.grimac.api.event.GrimEvent;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

public class GrimJoinEvent extends GrimEvent<GrimJoinEvent.Channel> implements GrimUserEvent {
   private GrimUser user;

   public GrimJoinEvent() {
      super(true);
   }

   public GrimJoinEvent(GrimUser user) {
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

   public static final class Channel extends EventChannel<GrimJoinEvent, GrimJoinEvent.Handler> {
      private final ThreadLocal<GrimJoinEvent> legacyPool = ThreadLocal.withInitial(GrimJoinEvent::new);

      public Channel() {
         super(GrimJoinEvent.class, GrimJoinEvent.Handler.class);
      }

      public void onJoin(@NotNull GrimPlugin plugin, @NotNull GrimJoinEvent.Handler handler) {
         this.subscribe(handler, 0, false, plugin, null);
      }

      public void onJoin(@NotNull GrimPlugin plugin, @NotNull GrimJoinEvent.Handler handler, int priority) {
         this.subscribe(handler, priority, false, plugin, null);
      }

      @Deprecated
      public void onJoin(@NotNull Object pluginContext, @NotNull GrimJoinEvent.Handler handler) {
         this.onJoin(this.resolvePlugin(pluginContext), handler);
      }

      @Deprecated
      public void onJoin(@NotNull Object pluginContext, @NotNull GrimJoinEvent.Handler handler, int priority) {
         this.onJoin(this.resolvePlugin(pluginContext), handler, priority);
      }

      public void fire(@NotNull GrimUser user) {
         EventChannel.Entry<GrimJoinEvent.Handler>[] entries = this.entries();
         if (entries.length != 0) {
            if (!this.hasLegacy()) {
               for (EventChannel.Entry<GrimJoinEvent.Handler> e : entries) {
                  try {
                     e.handler.onJoin(user);
                  } catch (Throwable var9) {
                     var9.printStackTrace();
                  }
               }
            } else {
               GrimJoinEvent pooled = this.legacyPool.get();
               pooled.init(user);

               for (EventChannel.Entry<GrimJoinEvent.Handler> e : entries) {
                  try {
                     if (e.legacyListener != null) {
                        e.<GrimJoinEvent>legacyListenerAs().handle(pooled);
                     } else {
                        e.handler.onJoin(user);
                     }
                  } catch (Throwable var10) {
                     var10.printStackTrace();
                  }
               }
            }
         }
      }

      protected boolean dispatchTypedFromLegacy(@NotNull GrimJoinEvent event, @NotNull GrimJoinEvent.Handler handler, boolean cancelled) {
         handler.onJoin(event.getUser());
         return false;
      }

      @ApiStatus.Internal
      @NotNull
      public static GrimJoinEvent.Handler bridgeFromAny(@NotNull GrimEvent.Handler abstractHandler) {
         return user -> abstractHandler.onAnyEvent(GrimJoinEvent.class, false);
      }
   }

   @FunctionalInterface
   public interface Handler {
      void onJoin(@NotNull GrimUser var1);
   }
}
