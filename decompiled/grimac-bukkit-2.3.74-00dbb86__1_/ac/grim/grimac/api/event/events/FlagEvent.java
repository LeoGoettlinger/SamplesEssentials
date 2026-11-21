package ac.grim.grimac.api.event.events;

import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.api.event.EventChannel;
import ac.grim.grimac.api.event.GrimEvent;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public class FlagEvent extends GrimVerboseCheckEvent<FlagEvent.Channel> {
   public FlagEvent() {
   }

   public FlagEvent(GrimUser user, AbstractCheck check, String verbose) {
      super(user, check, verbose);
   }

   public FlagEvent(GrimUser user, AbstractCheck check, Supplier<String> verboseSupplier) {
      super(user, check, verboseSupplier);
   }

   @ApiStatus.Internal
   @Override
   public void init(GrimUser user, AbstractCheck check, String verbose) {
      super.init(user, check, verbose);
   }

   @ApiStatus.Internal
   @Override
   public void init(GrimUser user, AbstractCheck check, Supplier<String> verboseSupplier) {
      super.init(user, check, verboseSupplier);
   }

   public static final class Channel extends EventChannel<FlagEvent, FlagEvent.SupplierHandler> {
      private static final AtomicBoolean STRING_HANDLER_WARNING = new AtomicBoolean();
      private final ThreadLocal<FlagEvent> legacyPool = ThreadLocal.withInitial(FlagEvent::new);

      public Channel() {
         super(FlagEvent.class, FlagEvent.SupplierHandler.class);
      }

      public void onFlagSupplier(@NotNull GrimPlugin plugin, @NotNull FlagEvent.SupplierHandler handler) {
         this.subscribe(handler, 0, false, plugin, null);
      }

      public void onFlagSupplier(@NotNull GrimPlugin plugin, @NotNull FlagEvent.SupplierHandler handler, int priority) {
         this.subscribe(handler, priority, false, plugin, null);
      }

      public void onFlagSupplier(@NotNull GrimPlugin plugin, @NotNull FlagEvent.SupplierHandler handler, int priority, boolean ignoreCancelled) {
         this.subscribe(handler, priority, ignoreCancelled, plugin, null);
      }

      @Deprecated
      public void onFlag(@NotNull GrimPlugin plugin, @NotNull FlagEvent.Handler handler) {
         warnStringHandler(plugin);
         this.onFlagSupplier(plugin, adapt(handler));
      }

      @Deprecated
      public void onFlag(@NotNull GrimPlugin plugin, @NotNull FlagEvent.Handler handler, int priority) {
         warnStringHandler(plugin);
         this.onFlagSupplier(plugin, adapt(handler), priority);
      }

      @Deprecated
      public void onFlag(@NotNull GrimPlugin plugin, @NotNull FlagEvent.Handler handler, int priority, boolean ignoreCancelled) {
         warnStringHandler(plugin);
         this.onFlagSupplier(plugin, adapt(handler), priority, ignoreCancelled);
      }

      @Deprecated
      public void onFlagSupplier(@NotNull Object pluginContext, @NotNull FlagEvent.SupplierHandler handler) {
         this.onFlagSupplier(this.resolvePlugin(pluginContext), handler);
      }

      @Deprecated
      public void onFlagSupplier(@NotNull Object pluginContext, @NotNull FlagEvent.SupplierHandler handler, int priority) {
         this.onFlagSupplier(this.resolvePlugin(pluginContext), handler, priority);
      }

      @Deprecated
      public void onFlagSupplier(@NotNull Object pluginContext, @NotNull FlagEvent.SupplierHandler handler, int priority, boolean ignoreCancelled) {
         this.onFlagSupplier(this.resolvePlugin(pluginContext), handler, priority, ignoreCancelled);
      }

      @Deprecated
      public void onFlag(@NotNull Object pluginContext, @NotNull FlagEvent.Handler handler) {
         this.onFlag(this.resolvePlugin(pluginContext), handler);
      }

      @Deprecated
      public void onFlag(@NotNull Object pluginContext, @NotNull FlagEvent.Handler handler, int priority) {
         this.onFlag(this.resolvePlugin(pluginContext), handler, priority);
      }

      @Deprecated
      public void onFlag(@NotNull Object pluginContext, @NotNull FlagEvent.Handler handler, int priority, boolean ignoreCancelled) {
         this.onFlag(this.resolvePlugin(pluginContext), handler, priority, ignoreCancelled);
      }

      public boolean fire(@NotNull GrimUser user, @NotNull AbstractCheck check, @NotNull String verbose) {
         return this.fire(user, check, VerboseSuppliers.constant(verbose));
      }

      public boolean fire(@NotNull GrimUser user, @NotNull AbstractCheck check, @NotNull Supplier<String> verboseSupplier) {
         EventChannel.Entry<FlagEvent.SupplierHandler>[] entries = this.entries();
         if (entries.length == 0) {
            return false;
         } else {
            Supplier<String> verbose = VerboseSuppliers.memoize(verboseSupplier);
            boolean cancelled = false;
            if (!this.hasLegacy()) {
               for (EventChannel.Entry<FlagEvent.SupplierHandler> e : entries) {
                  if (!cancelled || e.ignoreCancelled) {
                     try {
                        cancelled = e.handler.onFlag(user, check, verbose, cancelled);
                     } catch (Throwable var13) {
                        var13.printStackTrace();
                     }
                  }
               }

               return cancelled;
            } else {
               FlagEvent pooled = this.legacyPool.get();
               pooled.init(user, check, verbose);

               for (EventChannel.Entry<FlagEvent.SupplierHandler> ex : entries) {
                  if (!cancelled || ex.ignoreCancelled) {
                     try {
                        if (ex.legacyListener != null) {
                           pooled.setCancelled(cancelled);
                           ex.<FlagEvent>legacyListenerAs().handle(pooled);
                           cancelled = pooled.isCancelled();
                        } else {
                           cancelled = ex.handler.onFlag(user, check, verbose, cancelled);
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

      protected boolean dispatchTypedFromLegacy(@NotNull FlagEvent event, @NotNull FlagEvent.SupplierHandler handler, boolean cancelled) {
         return handler.onFlag(event.getUser(), event.getCheck(), event.verboseSupplier(), cancelled);
      }

      @ApiStatus.Internal
      @NotNull
      public static FlagEvent.SupplierHandler bridgeFromCheck(@NotNull GrimCheckEvent.Handler abstractHandler) {
         return (user, check, verbose, cancelled) -> abstractHandler.onCheck(user, check, cancelled);
      }

      @ApiStatus.Internal
      @NotNull
      public static FlagEvent.SupplierHandler bridgeFromVerboseCheck(@NotNull GrimVerboseCheckEvent.SupplierHandler abstractHandler) {
         return (user, check, verbose, cancelled) -> abstractHandler.onVerboseCheck(user, check, verbose, cancelled);
      }

      @ApiStatus.Internal
      @NotNull
      public static FlagEvent.SupplierHandler bridgeFromAny(@NotNull GrimEvent.Handler abstractHandler) {
         return (user, check, verbose, cancelled) -> {
            abstractHandler.onAnyEvent(FlagEvent.class, cancelled);
            return cancelled;
         };
      }

      @NotNull
      private static FlagEvent.SupplierHandler adapt(@NotNull FlagEvent.Handler handler) {
         return (user, check, verbose, cancelled) -> handler.onFlag(user, check, verbose.get(), cancelled);
      }

      private static void warnStringHandler(@NotNull GrimPlugin plugin) {
         if (STRING_HANDLER_WARNING.compareAndSet(false, true)) {
            plugin.getLogger()
               .warning(
                  "Deprecated Grim flag string listener registered; use the Supplier<String> verbose handler and call verbose.get() only when text is needed."
               );
         }
      }
   }

   @FunctionalInterface
   public interface Handler {
      boolean onFlag(@NotNull GrimUser var1, @NotNull AbstractCheck var2, @NotNull String var3, boolean var4);
   }

   @FunctionalInterface
   public interface SupplierHandler {
      boolean onFlag(@NotNull GrimUser var1, @NotNull AbstractCheck var2, @NotNull Supplier<String> var3, boolean var4);
   }
}
