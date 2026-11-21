package ac.grim.grimac.api.event.events;

import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.api.event.EventChannel;
import ac.grim.grimac.api.event.GrimEvent;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

public final class GrimTransactionSendEvent extends GrimEvent<GrimTransactionSendEvent.Channel> {
   private GrimTransactionSendEvent() {
   }

   public static final class Channel extends EventChannel<GrimTransactionSendEvent, GrimTransactionSendEvent.Handler> {
      public Channel() {
         super(GrimTransactionSendEvent.class, GrimTransactionSendEvent.Handler.class);
      }

      public void onTransactionSend(@NotNull GrimPlugin plugin, @NotNull GrimTransactionSendEvent.Handler handler) {
         this.subscribe(handler, 0, false, plugin, null);
      }

      public void onTransactionSend(@NotNull GrimPlugin plugin, @NotNull GrimTransactionSendEvent.Handler handler, int priority) {
         this.subscribe(handler, priority, false, plugin, null);
      }

      @Deprecated
      public void onTransactionSend(@NotNull Object pluginContext, @NotNull GrimTransactionSendEvent.Handler handler) {
         this.onTransactionSend(this.resolvePlugin(pluginContext), handler);
      }

      @Deprecated
      public void onTransactionSend(@NotNull Object pluginContext, @NotNull GrimTransactionSendEvent.Handler handler, int priority) {
         this.onTransactionSend(this.resolvePlugin(pluginContext), handler, priority);
      }

      public void fire(@NotNull GrimUser user, int transactionId, long timestamp) {
         EventChannel.Entry<GrimTransactionSendEvent.Handler>[] entries = this.entries();

         for (EventChannel.Entry<GrimTransactionSendEvent.Handler> e : entries) {
            try {
               e.handler.onTransactionSend(user, transactionId, timestamp);
            } catch (Throwable var11) {
               var11.printStackTrace();
            }
         }
      }

      protected boolean dispatchTypedFromLegacy(@NotNull GrimTransactionSendEvent event, @NotNull GrimTransactionSendEvent.Handler handler, boolean cancelled) {
         throw new UnsupportedOperationException("GrimTransactionSendEvent has no legacy representation");
      }

      @ApiStatus.Internal
      @NotNull
      public static GrimTransactionSendEvent.Handler bridgeFromAny(@NotNull GrimEvent.Handler abstractHandler) {
         return (user, id, ts) -> abstractHandler.onAnyEvent(GrimTransactionSendEvent.class, false);
      }
   }

   @FunctionalInterface
   public interface Handler {
      void onTransactionSend(@NotNull GrimUser var1, int var2, long var3);
   }
}
