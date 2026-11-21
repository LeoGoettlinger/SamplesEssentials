package ac.grim.grimac.api.event.events;

import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.api.event.EventChannel;
import ac.grim.grimac.api.event.GrimEvent;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

public final class GrimVehicleSetbackEvent extends GrimSetbackEvent<GrimVehicleSetbackEvent.Channel> {
   private GrimVehicleSetbackEvent() {
   }

   public static final class Channel extends EventChannel<GrimVehicleSetbackEvent, GrimVehicleSetbackEvent.Handler> {
      public Channel() {
         super(GrimVehicleSetbackEvent.class, GrimVehicleSetbackEvent.Handler.class);
      }

      public void onVehicleSetback(@NotNull GrimPlugin plugin, @NotNull GrimVehicleSetbackEvent.Handler handler) {
         this.subscribe(handler, 0, false, plugin, null);
      }

      public void onVehicleSetback(@NotNull GrimPlugin plugin, @NotNull GrimVehicleSetbackEvent.Handler handler, int priority) {
         this.subscribe(handler, priority, false, plugin, null);
      }

      @Deprecated
      public void onVehicleSetback(@NotNull Object pluginContext, @NotNull GrimVehicleSetbackEvent.Handler handler) {
         this.onVehicleSetback(this.resolvePlugin(pluginContext), handler);
      }

      @Deprecated
      public void onVehicleSetback(@NotNull Object pluginContext, @NotNull GrimVehicleSetbackEvent.Handler handler, int priority) {
         this.onVehicleSetback(this.resolvePlugin(pluginContext), handler, priority);
      }

      public void fire(@NotNull GrimUser user, double x, double y, double z, long timestamp) {
         EventChannel.Entry<GrimVehicleSetbackEvent.Handler>[] entries = this.entries();

         for (EventChannel.Entry<GrimVehicleSetbackEvent.Handler> e : entries) {
            try {
               e.handler.onVehicleSetback(user, x, y, z, timestamp);
            } catch (Throwable var16) {
               var16.printStackTrace();
            }
         }
      }

      protected boolean dispatchTypedFromLegacy(@NotNull GrimVehicleSetbackEvent event, @NotNull GrimVehicleSetbackEvent.Handler handler, boolean cancelled) {
         throw new UnsupportedOperationException("GrimVehicleSetbackEvent has no legacy representation");
      }

      @ApiStatus.Internal
      @NotNull
      public static GrimVehicleSetbackEvent.Handler bridgeFromSetback(@NotNull GrimSetbackEvent.Handler abstractHandler) {
         return (user, x, y, z, ts) -> abstractHandler.onAnySetback(user, ts);
      }

      @ApiStatus.Internal
      @NotNull
      public static GrimVehicleSetbackEvent.Handler bridgeFromAny(@NotNull GrimEvent.Handler abstractHandler) {
         return (user, x, y, z, ts) -> abstractHandler.onAnyEvent(GrimVehicleSetbackEvent.class, false);
      }
   }

   @FunctionalInterface
   public interface Handler {
      void onVehicleSetback(@NotNull GrimUser var1, double var2, double var4, double var6, long var8);
   }
}
