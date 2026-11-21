package ac.grim.grimac.api.event.events;

import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.api.event.EventChannel;
import ac.grim.grimac.api.event.GrimEvent;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

public final class GrimTeleportEvent extends GrimEvent<GrimTeleportEvent.Channel> {
   private GrimTeleportEvent() {
   }

   public static final class Channel extends EventChannel<GrimTeleportEvent, GrimTeleportEvent.Handler> {
      public Channel() {
         super(GrimTeleportEvent.class, GrimTeleportEvent.Handler.class);
      }

      public void onTeleport(@NotNull GrimPlugin plugin, @NotNull GrimTeleportEvent.Handler handler) {
         this.subscribe(handler, 0, false, plugin, null);
      }

      public void onTeleport(@NotNull GrimPlugin plugin, @NotNull GrimTeleportEvent.Handler handler, int priority) {
         this.subscribe(handler, priority, false, plugin, null);
      }

      @Deprecated
      public void onTeleport(@NotNull Object pluginContext, @NotNull GrimTeleportEvent.Handler handler) {
         this.onTeleport(this.resolvePlugin(pluginContext), handler);
      }

      @Deprecated
      public void onTeleport(@NotNull Object pluginContext, @NotNull GrimTeleportEvent.Handler handler, int priority) {
         this.onTeleport(this.resolvePlugin(pluginContext), handler, priority);
      }

      public void fire(@NotNull GrimUser user, int teleportId, long timestamp) {
         EventChannel.Entry<GrimTeleportEvent.Handler>[] entries = this.entries();

         for (EventChannel.Entry<GrimTeleportEvent.Handler> e : entries) {
            try {
               e.handler.onTeleport(user, teleportId, timestamp);
            } catch (Throwable var11) {
               var11.printStackTrace();
            }
         }
      }

      protected boolean dispatchTypedFromLegacy(@NotNull GrimTeleportEvent event, @NotNull GrimTeleportEvent.Handler handler, boolean cancelled) {
         throw new UnsupportedOperationException("GrimTeleportEvent has no legacy representation");
      }

      @ApiStatus.Internal
      @NotNull
      public static GrimTeleportEvent.Handler bridgeFromAny(@NotNull GrimEvent.Handler abstractHandler) {
         return (user, id, ts) -> abstractHandler.onAnyEvent(GrimTeleportEvent.class, false);
      }
   }

   @FunctionalInterface
   public interface Handler {
      void onTeleport(@NotNull GrimUser var1, int var2, long var3);
   }
}
