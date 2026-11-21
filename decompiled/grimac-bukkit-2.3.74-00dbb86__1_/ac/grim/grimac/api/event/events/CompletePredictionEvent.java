package ac.grim.grimac.api.event.events;

import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.api.event.EventChannel;
import ac.grim.grimac.api.event.GrimEvent;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

public class CompletePredictionEvent extends GrimCheckEvent<CompletePredictionEvent.Channel> {
   private double offset;

   public CompletePredictionEvent() {
   }

   public CompletePredictionEvent(GrimUser player, AbstractCheck check, double offset) {
      super(player, check);
      this.offset = offset;
   }

   @ApiStatus.Internal
   public void init(GrimUser user, AbstractCheck check, double offset) {
      super.init(user, check);
      this.offset = offset;
   }

   public double getOffset() {
      return this.offset;
   }

   public static final class Channel extends EventChannel<CompletePredictionEvent, CompletePredictionEvent.Handler> {
      private final ThreadLocal<CompletePredictionEvent> legacyPool = ThreadLocal.withInitial(CompletePredictionEvent::new);

      public Channel() {
         super(CompletePredictionEvent.class, CompletePredictionEvent.Handler.class);
      }

      public void onCompletePrediction(@NotNull GrimPlugin plugin, @NotNull CompletePredictionEvent.Handler handler) {
         this.subscribe(handler, 0, false, plugin, null);
      }

      public void onCompletePrediction(@NotNull GrimPlugin plugin, @NotNull CompletePredictionEvent.Handler handler, int priority) {
         this.subscribe(handler, priority, false, plugin, null);
      }

      public void onCompletePrediction(@NotNull GrimPlugin plugin, @NotNull CompletePredictionEvent.Handler handler, int priority, boolean ignoreCancelled) {
         this.subscribe(handler, priority, ignoreCancelled, plugin, null);
      }

      @Deprecated
      public void onCompletePrediction(@NotNull Object pluginContext, @NotNull CompletePredictionEvent.Handler handler) {
         this.onCompletePrediction(this.resolvePlugin(pluginContext), handler);
      }

      @Deprecated
      public void onCompletePrediction(@NotNull Object pluginContext, @NotNull CompletePredictionEvent.Handler handler, int priority) {
         this.onCompletePrediction(this.resolvePlugin(pluginContext), handler, priority);
      }

      @Deprecated
      public void onCompletePrediction(@NotNull Object pluginContext, @NotNull CompletePredictionEvent.Handler handler, int priority, boolean ignoreCancelled) {
         this.onCompletePrediction(this.resolvePlugin(pluginContext), handler, priority, ignoreCancelled);
      }

      public boolean fire(@NotNull GrimUser user, @NotNull AbstractCheck check, double offset) {
         EventChannel.Entry<CompletePredictionEvent.Handler>[] entries = this.entries();
         if (entries.length == 0) {
            return false;
         } else {
            boolean cancelled = false;
            if (!this.hasLegacy()) {
               for (EventChannel.Entry<CompletePredictionEvent.Handler> e : entries) {
                  if (!cancelled || e.ignoreCancelled) {
                     try {
                        cancelled = e.handler.onCompletePrediction(user, check, offset, cancelled);
                     } catch (Throwable var13) {
                        var13.printStackTrace();
                     }
                  }
               }

               return cancelled;
            } else {
               CompletePredictionEvent pooled = this.legacyPool.get();
               pooled.init(user, check, offset);

               for (EventChannel.Entry<CompletePredictionEvent.Handler> ex : entries) {
                  if (!cancelled || ex.ignoreCancelled) {
                     try {
                        if (ex.legacyListener != null) {
                           pooled.setCancelled(cancelled);
                           ex.<CompletePredictionEvent>legacyListenerAs().handle(pooled);
                           cancelled = pooled.isCancelled();
                        } else {
                           cancelled = ex.handler.onCompletePrediction(user, check, offset, cancelled);
                        }
                     } catch (Throwable var14) {
                        var14.printStackTrace();
                     }
                  }
               }

               return cancelled;
            }
         }
      }

      protected boolean dispatchTypedFromLegacy(@NotNull CompletePredictionEvent event, @NotNull CompletePredictionEvent.Handler handler, boolean cancelled) {
         return handler.onCompletePrediction(event.getUser(), event.getCheck(), event.getOffset(), cancelled);
      }

      @ApiStatus.Internal
      @NotNull
      public static CompletePredictionEvent.Handler bridgeFromCheck(@NotNull GrimCheckEvent.Handler abstractHandler) {
         return (user, check, offset, cancelled) -> abstractHandler.onCheck(user, check, cancelled);
      }

      @ApiStatus.Internal
      @NotNull
      public static CompletePredictionEvent.Handler bridgeFromAny(@NotNull GrimEvent.Handler abstractHandler) {
         return (user, check, offset, cancelled) -> {
            abstractHandler.onAnyEvent(CompletePredictionEvent.class, cancelled);
            return cancelled;
         };
      }
   }

   @FunctionalInterface
   public interface Handler {
      boolean onCompletePrediction(@NotNull GrimUser var1, @NotNull AbstractCheck var2, double var3, boolean var5);
   }
}
