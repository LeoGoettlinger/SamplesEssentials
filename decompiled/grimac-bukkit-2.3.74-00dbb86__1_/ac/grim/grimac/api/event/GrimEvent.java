package ac.grim.grimac.api.event;

import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import lombok.Generated;

public abstract class GrimEvent<CHANNEL extends EventChannel<?, ?>> {
   private boolean cancelled = false;
   private final boolean async;

   protected GrimEvent() {
      this(false);
   }

   protected GrimEvent(boolean async) {
      this.async = async;
   }

   public void setCancelled(boolean cancelled) {
      if (!this.isCancellable()) {
         throw new IllegalStateException("Event " + this.getEventName() + " is not cancellable");
      } else {
         this.cancelled = cancelled;
      }
   }

   public boolean isCancellable() {
      return false;
   }

   public String getEventName() {
      return this.getClass().getSimpleName();
   }

   @ApiStatus.Internal
   protected void resetForReuse() {
      this.cancelled = false;
   }

   @Generated
   public boolean isCancelled() {
      return this.cancelled;
   }

   public static final class Channel extends AbstractEventChannel<GrimEvent<?>, GrimEvent.Handler> {
      public Channel() {
         super(GrimEvent.class, GrimEvent.Handler.class);
      }

      public void onAnyEvent(@NotNull GrimPlugin plugin, @NotNull GrimEvent.Handler handler) {
         this.subscribeAbstract(handler, 0, false, plugin);
      }

      public void onAnyEvent(@NotNull GrimPlugin plugin, @NotNull GrimEvent.Handler handler, int priority) {
         this.subscribeAbstract(handler, priority, false, plugin);
      }

      public void onAnyEvent(@NotNull GrimPlugin plugin, @NotNull GrimEvent.Handler handler, int priority, boolean ignoreCancelled) {
         this.subscribeAbstract(handler, priority, ignoreCancelled, plugin);
      }

      @Deprecated
      public void onAnyEvent(@NotNull Object pluginContext, @NotNull GrimEvent.Handler handler) {
         this.subscribeAbstractResolving(pluginContext, handler, 0, false);
      }

      @Deprecated
      public void onAnyEvent(@NotNull Object pluginContext, @NotNull GrimEvent.Handler handler, int priority) {
         this.subscribeAbstractResolving(pluginContext, handler, priority, false);
      }

      @Deprecated
      public void onAnyEvent(@NotNull Object pluginContext, @NotNull GrimEvent.Handler handler, int priority, boolean ignoreCancelled) {
         this.subscribeAbstractResolving(pluginContext, handler, priority, ignoreCancelled);
      }
   }

   @FunctionalInterface
   public interface Handler {
      void onAnyEvent(@NotNull Class<? extends GrimEvent<?>> var1, boolean var2);
   }
}
