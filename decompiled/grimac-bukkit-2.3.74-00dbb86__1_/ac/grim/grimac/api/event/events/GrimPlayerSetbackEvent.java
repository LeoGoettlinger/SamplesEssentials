package ac.grim.grimac.api.event.events;

import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.api.event.EventChannel;
import ac.grim.grimac.api.event.GrimEvent;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

public final class GrimPlayerSetbackEvent extends GrimSetbackEvent<GrimPlayerSetbackEvent.Channel> {
   private GrimPlayerSetbackEvent() {
   }

   public static final class Channel extends EventChannel<GrimPlayerSetbackEvent, GrimPlayerSetbackEvent.Handler> {
      public Channel() {
         super(GrimPlayerSetbackEvent.class, GrimPlayerSetbackEvent.Handler.class);
      }

      public void onPlayerSetback(@NotNull GrimPlugin plugin, @NotNull GrimPlayerSetbackEvent.Handler handler) {
         this.subscribe(handler, 0, false, plugin, null);
      }

      public void onPlayerSetback(@NotNull GrimPlugin plugin, @NotNull GrimPlayerSetbackEvent.Handler handler, int priority) {
         this.subscribe(handler, priority, false, plugin, null);
      }

      @Deprecated
      public void onPlayerSetback(@NotNull Object pluginContext, @NotNull GrimPlayerSetbackEvent.Handler handler) {
         this.onPlayerSetback(this.resolvePlugin(pluginContext), handler);
      }

      @Deprecated
      public void onPlayerSetback(@NotNull Object pluginContext, @NotNull GrimPlayerSetbackEvent.Handler handler, int priority) {
         this.onPlayerSetback(this.resolvePlugin(pluginContext), handler, priority);
      }

      public void fire(@NotNull GrimUser user, int teleportId, double x, double y, double z, long timestamp) {
         EventChannel.Entry<GrimPlayerSetbackEvent.Handler>[] entries = this.entries();

         for (EventChannel.Entry<GrimPlayerSetbackEvent.Handler> e : entries) {
            try {
               e.handler.onPlayerSetback(user, teleportId, x, y, z, timestamp);
            } catch (Throwable var17) {
               var17.printStackTrace();
            }
         }
      }

      protected boolean dispatchTypedFromLegacy(@NotNull GrimPlayerSetbackEvent event, @NotNull GrimPlayerSetbackEvent.Handler handler, boolean cancelled) {
         throw new UnsupportedOperationException("GrimPlayerSetbackEvent has no legacy representation");
      }

      @ApiStatus.Internal
      @NotNull
      public static GrimPlayerSetbackEvent.Handler bridgeFromSetback(@NotNull GrimSetbackEvent.Handler abstractHandler) {
         return (user, id, x, y, z, ts) -> abstractHandler.onAnySetback(user, ts);
      }

      @ApiStatus.Internal
      @NotNull
      public static GrimPlayerSetbackEvent.Handler bridgeFromAny(@NotNull GrimEvent.Handler abstractHandler) {
         return (user, id, x, y, z, ts) -> abstractHandler.onAnyEvent(GrimPlayerSetbackEvent.class, false);
      }
   }

   @FunctionalInterface
   public interface Handler {
      void onPlayerSetback(@NotNull GrimUser var1, int var2, double var3, double var5, double var7, long var9);
   }
}
