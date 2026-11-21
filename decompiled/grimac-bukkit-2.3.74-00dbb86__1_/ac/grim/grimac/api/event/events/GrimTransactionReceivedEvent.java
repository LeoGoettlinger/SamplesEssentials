package ac.grim.grimac.api.event.events;

import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.api.event.EventChannel;
import ac.grim.grimac.api.event.GrimEvent;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

public final class GrimTransactionReceivedEvent extends GrimEvent<GrimTransactionReceivedEvent.Channel> {
   private GrimTransactionReceivedEvent() {
   }

   public static final class Channel extends EventChannel<GrimTransactionReceivedEvent, GrimTransactionReceivedEvent.Handler> {
      public Channel() {
         super(GrimTransactionReceivedEvent.class, GrimTransactionReceivedEvent.Handler.class);
      }

      public void onTransactionReceived(@NotNull GrimPlugin plugin, @NotNull GrimTransactionReceivedEvent.Handler handler) {
         this.subscribe(handler, 0, false, plugin, null);
      }

      public void onTransactionReceived(@NotNull GrimPlugin plugin, @NotNull GrimTransactionReceivedEvent.Handler handler, int priority) {
         this.subscribe(handler, priority, false, plugin, null);
      }

      @Deprecated
      public void onTransactionReceived(@NotNull Object pluginContext, @NotNull GrimTransactionReceivedEvent.Handler handler) {
         this.onTransactionReceived(this.resolvePlugin(pluginContext), handler);
      }

      @Deprecated
      public void onTransactionReceived(@NotNull Object pluginContext, @NotNull GrimTransactionReceivedEvent.Handler handler, int priority) {
         this.onTransactionReceived(this.resolvePlugin(pluginContext), handler, priority);
      }

      public void fire(@NotNull GrimUser user, int transactionId, boolean packetCancelled, long timestamp) {
         EventChannel.Entry<GrimTransactionReceivedEvent.Handler>[] entries = this.entries();

         for (EventChannel.Entry<GrimTransactionReceivedEvent.Handler> e : entries) {
            try {
               e.handler.onTransactionReceived(user, transactionId, packetCancelled, timestamp);
            } catch (Throwable var12) {
               var12.printStackTrace();
            }
         }
      }

      protected boolean dispatchTypedFromLegacy(
         @NotNull GrimTransactionReceivedEvent event, @NotNull GrimTransactionReceivedEvent.Handler handler, boolean cancelled
      ) {
         throw new UnsupportedOperationException("GrimTransactionReceivedEvent has no legacy representation");
      }

      @ApiStatus.Internal
      @NotNull
      public static GrimTransactionReceivedEvent.Handler bridgeFromAny(@NotNull GrimEvent.Handler abstractHandler) {
         return (user, id, c, ts) -> abstractHandler.onAnyEvent(GrimTransactionReceivedEvent.class, false);
      }
   }

   @FunctionalInterface
   public interface Handler {
      void onTransactionReceived(@NotNull GrimUser var1, int var2, boolean var3, long var4);
   }
}
