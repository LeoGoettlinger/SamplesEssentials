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

public class CommandExecuteEvent extends GrimVerboseCheckEvent<CommandExecuteEvent.Channel> {
   private String command;

   public CommandExecuteEvent() {
   }

   public CommandExecuteEvent(GrimUser player, AbstractCheck check, String verbose, String command) {
      super(player, check, verbose);
      this.command = command;
   }

   public CommandExecuteEvent(GrimUser player, AbstractCheck check, Supplier<String> verboseSupplier, String command) {
      super(player, check, verboseSupplier);
      this.command = command;
   }

   @ApiStatus.Internal
   public void init(GrimUser user, AbstractCheck check, String verbose, String command) {
      super.init(user, check, verbose);
      this.command = command;
   }

   @ApiStatus.Internal
   public void init(GrimUser user, AbstractCheck check, Supplier<String> verboseSupplier, String command) {
      super.init(user, check, verboseSupplier);
      this.command = command;
   }

   public String getCommand() {
      return this.command;
   }

   public static final class Channel extends EventChannel<CommandExecuteEvent, CommandExecuteEvent.SupplierHandler> {
      private static final AtomicBoolean STRING_HANDLER_WARNING = new AtomicBoolean();
      private final ThreadLocal<CommandExecuteEvent> legacyPool = ThreadLocal.withInitial(CommandExecuteEvent::new);

      public Channel() {
         super(CommandExecuteEvent.class, CommandExecuteEvent.SupplierHandler.class);
      }

      public void onCommandExecuteSupplier(@NotNull GrimPlugin plugin, @NotNull CommandExecuteEvent.SupplierHandler handler) {
         this.subscribe(handler, 0, false, plugin, null);
      }

      public void onCommandExecuteSupplier(@NotNull GrimPlugin plugin, @NotNull CommandExecuteEvent.SupplierHandler handler, int priority) {
         this.subscribe(handler, priority, false, plugin, null);
      }

      public void onCommandExecuteSupplier(
         @NotNull GrimPlugin plugin, @NotNull CommandExecuteEvent.SupplierHandler handler, int priority, boolean ignoreCancelled
      ) {
         this.subscribe(handler, priority, ignoreCancelled, plugin, null);
      }

      @Deprecated
      public void onCommandExecute(@NotNull GrimPlugin plugin, @NotNull CommandExecuteEvent.Handler handler) {
         warnStringHandler(plugin);
         this.onCommandExecuteSupplier(plugin, adapt(handler));
      }

      @Deprecated
      public void onCommandExecute(@NotNull GrimPlugin plugin, @NotNull CommandExecuteEvent.Handler handler, int priority) {
         warnStringHandler(plugin);
         this.onCommandExecuteSupplier(plugin, adapt(handler), priority);
      }

      @Deprecated
      public void onCommandExecute(@NotNull GrimPlugin plugin, @NotNull CommandExecuteEvent.Handler handler, int priority, boolean ignoreCancelled) {
         warnStringHandler(plugin);
         this.onCommandExecuteSupplier(plugin, adapt(handler), priority, ignoreCancelled);
      }

      @Deprecated
      public void onCommandExecuteSupplier(@NotNull Object pluginContext, @NotNull CommandExecuteEvent.SupplierHandler handler) {
         this.onCommandExecuteSupplier(this.resolvePlugin(pluginContext), handler);
      }

      @Deprecated
      public void onCommandExecuteSupplier(@NotNull Object pluginContext, @NotNull CommandExecuteEvent.SupplierHandler handler, int priority) {
         this.onCommandExecuteSupplier(this.resolvePlugin(pluginContext), handler, priority);
      }

      @Deprecated
      public void onCommandExecuteSupplier(
         @NotNull Object pluginContext, @NotNull CommandExecuteEvent.SupplierHandler handler, int priority, boolean ignoreCancelled
      ) {
         this.onCommandExecuteSupplier(this.resolvePlugin(pluginContext), handler, priority, ignoreCancelled);
      }

      @Deprecated
      public void onCommandExecute(@NotNull Object pluginContext, @NotNull CommandExecuteEvent.Handler handler) {
         this.onCommandExecute(this.resolvePlugin(pluginContext), handler);
      }

      @Deprecated
      public void onCommandExecute(@NotNull Object pluginContext, @NotNull CommandExecuteEvent.Handler handler, int priority) {
         this.onCommandExecute(this.resolvePlugin(pluginContext), handler, priority);
      }

      @Deprecated
      public void onCommandExecute(@NotNull Object pluginContext, @NotNull CommandExecuteEvent.Handler handler, int priority, boolean ignoreCancelled) {
         this.onCommandExecute(this.resolvePlugin(pluginContext), handler, priority, ignoreCancelled);
      }

      public boolean fire(@NotNull GrimUser user, @NotNull AbstractCheck check, @NotNull String verbose, @NotNull String command) {
         return this.fire(user, check, VerboseSuppliers.constant(verbose), command);
      }

      public boolean fire(@NotNull GrimUser user, @NotNull AbstractCheck check, @NotNull Supplier<String> verboseSupplier, @NotNull String command) {
         EventChannel.Entry<CommandExecuteEvent.SupplierHandler>[] entries = this.entries();
         if (entries.length == 0) {
            return false;
         } else {
            Supplier<String> verbose = VerboseSuppliers.memoize(verboseSupplier);
            boolean cancelled = false;
            if (!this.hasLegacy()) {
               for (EventChannel.Entry<CommandExecuteEvent.SupplierHandler> e : entries) {
                  if (!cancelled || e.ignoreCancelled) {
                     try {
                        cancelled = e.handler.onCommandExecute(user, check, verbose, command, cancelled);
                     } catch (Throwable var14) {
                        var14.printStackTrace();
                     }
                  }
               }

               return cancelled;
            } else {
               CommandExecuteEvent pooled = this.legacyPool.get();
               pooled.init(user, check, verbose, command);

               for (EventChannel.Entry<CommandExecuteEvent.SupplierHandler> ex : entries) {
                  if (!cancelled || ex.ignoreCancelled) {
                     try {
                        if (ex.legacyListener != null) {
                           pooled.setCancelled(cancelled);
                           ex.<CommandExecuteEvent>legacyListenerAs().handle(pooled);
                           cancelled = pooled.isCancelled();
                        } else {
                           cancelled = ex.handler.onCommandExecute(user, check, verbose, command, cancelled);
                        }
                     } catch (Throwable var15) {
                        var15.printStackTrace();
                     }
                  }
               }

               return cancelled;
            }
         }
      }

      protected boolean dispatchTypedFromLegacy(@NotNull CommandExecuteEvent event, @NotNull CommandExecuteEvent.SupplierHandler handler, boolean cancelled) {
         return handler.onCommandExecute(event.getUser(), event.getCheck(), event.verboseSupplier(), event.getCommand(), cancelled);
      }

      @ApiStatus.Internal
      @NotNull
      public static CommandExecuteEvent.SupplierHandler bridgeFromCheck(@NotNull GrimCheckEvent.Handler abstractHandler) {
         return (user, check, verbose, command, cancelled) -> abstractHandler.onCheck(user, check, cancelled);
      }

      @ApiStatus.Internal
      @NotNull
      public static CommandExecuteEvent.SupplierHandler bridgeFromVerboseCheck(@NotNull GrimVerboseCheckEvent.SupplierHandler abstractHandler) {
         return (user, check, verbose, command, cancelled) -> abstractHandler.onVerboseCheck(user, check, verbose, cancelled);
      }

      @ApiStatus.Internal
      @NotNull
      public static CommandExecuteEvent.SupplierHandler bridgeFromAny(@NotNull GrimEvent.Handler abstractHandler) {
         return (user, check, verbose, command, cancelled) -> {
            abstractHandler.onAnyEvent(CommandExecuteEvent.class, cancelled);
            return cancelled;
         };
      }

      @NotNull
      private static CommandExecuteEvent.SupplierHandler adapt(@NotNull CommandExecuteEvent.Handler handler) {
         return (user, check, verbose, command, cancelled) -> handler.onCommandExecute(user, check, verbose.get(), command, cancelled);
      }

      private static void warnStringHandler(@NotNull GrimPlugin plugin) {
         if (STRING_HANDLER_WARNING.compareAndSet(false, true)) {
            plugin.getLogger()
               .warning(
                  "Deprecated Grim command-execute string listener registered; use the Supplier<String> verbose handler and call verbose.get() only when text is needed."
               );
         }
      }
   }

   @FunctionalInterface
   public interface Handler {
      boolean onCommandExecute(@NotNull GrimUser var1, @NotNull AbstractCheck var2, @NotNull String var3, @NotNull String var4, boolean var5);
   }

   @FunctionalInterface
   public interface SupplierHandler {
      boolean onCommandExecute(@NotNull GrimUser var1, @NotNull AbstractCheck var2, @NotNull Supplier<String> var3, @NotNull String var4, boolean var5);
   }
}
