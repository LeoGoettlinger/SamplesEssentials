package ac.grim.grimac.api.event.events;

import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.api.event.AbstractEventChannel;
import ac.grim.grimac.api.event.EventChannel;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public abstract class GrimVerboseCheckEvent<CHANNEL extends EventChannel<?, ?>> extends GrimCheckEvent<CHANNEL> {
   private Supplier<String> verboseSupplier = () -> "";

   protected GrimVerboseCheckEvent() {
   }

   public GrimVerboseCheckEvent(GrimUser user, AbstractCheck check, String verbose) {
      super(user, check);
      this.setVerboseSupplier(VerboseSuppliers.constant(verbose));
   }

   public GrimVerboseCheckEvent(GrimUser user, AbstractCheck check, Supplier<String> verboseSupplier) {
      super(user, check);
      this.setVerboseSupplier(verboseSupplier);
   }

   @ApiStatus.Internal
   protected void init(GrimUser user, AbstractCheck check, String verbose) {
      this.init(user, check, VerboseSuppliers.constant(verbose));
   }

   @ApiStatus.Internal
   protected void init(GrimUser user, AbstractCheck check, Supplier<String> verboseSupplier) {
      super.init(user, check);
      this.setVerboseSupplier(verboseSupplier);
   }

   @NotNull
   public String getVerbose() {
      return this.verboseSupplier.get();
   }

   @ApiStatus.Internal
   @NotNull
   final Supplier<String> verboseSupplier() {
      return this.verboseSupplier;
   }

   private void setVerboseSupplier(Supplier<String> verboseSupplier) {
      this.verboseSupplier = VerboseSuppliers.memoize(verboseSupplier);
   }

   public static final class Channel extends AbstractEventChannel<GrimVerboseCheckEvent<?>, GrimVerboseCheckEvent.SupplierHandler> {
      private static final AtomicBoolean STRING_HANDLER_WARNING = new AtomicBoolean();

      public Channel() {
         super(GrimVerboseCheckEvent.class, GrimVerboseCheckEvent.SupplierHandler.class);
      }

      public void onVerboseCheckSupplier(@NotNull GrimPlugin plugin, @NotNull GrimVerboseCheckEvent.SupplierHandler handler) {
         this.subscribeAbstract(handler, 0, false, plugin);
      }

      public void onVerboseCheckSupplier(@NotNull GrimPlugin plugin, @NotNull GrimVerboseCheckEvent.SupplierHandler handler, int priority) {
         this.subscribeAbstract(handler, priority, false, plugin);
      }

      public void onVerboseCheckSupplier(
         @NotNull GrimPlugin plugin, @NotNull GrimVerboseCheckEvent.SupplierHandler handler, int priority, boolean ignoreCancelled
      ) {
         this.subscribeAbstract(handler, priority, ignoreCancelled, plugin);
      }

      @Deprecated
      public void onVerboseCheck(@NotNull GrimPlugin plugin, @NotNull GrimVerboseCheckEvent.Handler handler) {
         warnStringHandler(plugin);
         this.onVerboseCheckSupplier(plugin, adapt(handler));
      }

      @Deprecated
      public void onVerboseCheck(@NotNull GrimPlugin plugin, @NotNull GrimVerboseCheckEvent.Handler handler, int priority) {
         warnStringHandler(plugin);
         this.onVerboseCheckSupplier(plugin, adapt(handler), priority);
      }

      @Deprecated
      public void onVerboseCheck(@NotNull GrimPlugin plugin, @NotNull GrimVerboseCheckEvent.Handler handler, int priority, boolean ignoreCancelled) {
         warnStringHandler(plugin);
         this.onVerboseCheckSupplier(plugin, adapt(handler), priority, ignoreCancelled);
      }

      @Deprecated
      public void onVerboseCheckSupplier(@NotNull Object pluginContext, @NotNull GrimVerboseCheckEvent.SupplierHandler handler) {
         this.subscribeAbstractResolving(pluginContext, handler, 0, false);
      }

      @Deprecated
      public void onVerboseCheckSupplier(@NotNull Object pluginContext, @NotNull GrimVerboseCheckEvent.SupplierHandler handler, int priority) {
         this.subscribeAbstractResolving(pluginContext, handler, priority, false);
      }

      @Deprecated
      public void onVerboseCheckSupplier(
         @NotNull Object pluginContext, @NotNull GrimVerboseCheckEvent.SupplierHandler handler, int priority, boolean ignoreCancelled
      ) {
         this.subscribeAbstractResolving(pluginContext, handler, priority, ignoreCancelled);
      }

      @Deprecated
      public void onVerboseCheck(@NotNull Object pluginContext, @NotNull GrimVerboseCheckEvent.Handler handler) {
         GrimPlugin plugin = this.resolvePlugin(pluginContext);
         this.onVerboseCheck(plugin, handler);
      }

      @Deprecated
      public void onVerboseCheck(@NotNull Object pluginContext, @NotNull GrimVerboseCheckEvent.Handler handler, int priority) {
         GrimPlugin plugin = this.resolvePlugin(pluginContext);
         this.onVerboseCheck(plugin, handler, priority);
      }

      @Deprecated
      public void onVerboseCheck(@NotNull Object pluginContext, @NotNull GrimVerboseCheckEvent.Handler handler, int priority, boolean ignoreCancelled) {
         GrimPlugin plugin = this.resolvePlugin(pluginContext);
         this.onVerboseCheck(plugin, handler, priority, ignoreCancelled);
      }

      @NotNull
      private static GrimVerboseCheckEvent.SupplierHandler adapt(@NotNull GrimVerboseCheckEvent.Handler handler) {
         return (user, check, verbose, cancelled) -> handler.onVerboseCheck(user, check, verbose.get(), cancelled);
      }

      private static void warnStringHandler(@NotNull GrimPlugin plugin) {
         if (STRING_HANDLER_WARNING.compareAndSet(false, true)) {
            plugin.getLogger()
               .warning(
                  "Deprecated Grim verbose string listener registered; use the Supplier<String> verbose handler and call verbose.get() only when text is needed."
               );
         }
      }
   }

   @FunctionalInterface
   public interface Handler {
      boolean onVerboseCheck(@NotNull GrimUser var1, @NotNull AbstractCheck var2, @NotNull String var3, boolean var4);
   }

   @FunctionalInterface
   public interface SupplierHandler {
      boolean onVerboseCheck(@NotNull GrimUser var1, @NotNull AbstractCheck var2, @NotNull Supplier<String> var3, boolean var4);
   }
}
