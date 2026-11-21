package ac.grim.grimac.api.event.events;

import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.api.event.AbstractEventChannel;
import ac.grim.grimac.api.event.Cancellable;
import ac.grim.grimac.api.event.EventChannel;
import ac.grim.grimac.api.event.GrimEvent;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import lombok.Generated;

public abstract class GrimCheckEvent<CHANNEL extends EventChannel<?, ?>> extends GrimEvent<CHANNEL> implements GrimUserEvent, Cancellable {
   private GrimUser user;
   protected AbstractCheck check;
   private boolean cancelled;

   protected GrimCheckEvent() {
      super(true);
   }

   public GrimCheckEvent(GrimUser user, AbstractCheck check) {
      super(true);
      this.user = user;
      this.check = check;
   }

   @ApiStatus.Internal
   protected void init(GrimUser user, AbstractCheck check) {
      this.resetForReuse();
      this.user = user;
      this.check = check;
      this.cancelled = false;
   }

   @Override
   public GrimUser getUser() {
      return this.user;
   }

   @Override
   public boolean isCancelled() {
      return this.cancelled;
   }

   @Override
   public void setCancelled(boolean cancelled) {
      this.cancelled = cancelled;
   }

   @Override
   public boolean isCancellable() {
      return true;
   }

   public double getViolations() {
      return this.check.getViolations();
   }

   public boolean isSetback() {
      return this.check.getViolations() > this.check.getSetbackVL();
   }

   @Generated
   public AbstractCheck getCheck() {
      return this.check;
   }

   public static final class Channel extends AbstractEventChannel<GrimCheckEvent<?>, GrimCheckEvent.Handler> {
      public Channel() {
         super(GrimCheckEvent.class, GrimCheckEvent.Handler.class);
      }

      public void onCheck(@NotNull GrimPlugin plugin, @NotNull GrimCheckEvent.Handler handler) {
         this.subscribeAbstract(handler, 0, false, plugin);
      }

      public void onCheck(@NotNull GrimPlugin plugin, @NotNull GrimCheckEvent.Handler handler, int priority) {
         this.subscribeAbstract(handler, priority, false, plugin);
      }

      public void onCheck(@NotNull GrimPlugin plugin, @NotNull GrimCheckEvent.Handler handler, int priority, boolean ignoreCancelled) {
         this.subscribeAbstract(handler, priority, ignoreCancelled, plugin);
      }

      @Deprecated
      public void onCheck(@NotNull Object pluginContext, @NotNull GrimCheckEvent.Handler handler) {
         this.subscribeAbstractResolving(pluginContext, handler, 0, false);
      }

      @Deprecated
      public void onCheck(@NotNull Object pluginContext, @NotNull GrimCheckEvent.Handler handler, int priority) {
         this.subscribeAbstractResolving(pluginContext, handler, priority, false);
      }

      @Deprecated
      public void onCheck(@NotNull Object pluginContext, @NotNull GrimCheckEvent.Handler handler, int priority, boolean ignoreCancelled) {
         this.subscribeAbstractResolving(pluginContext, handler, priority, ignoreCancelled);
      }
   }

   @FunctionalInterface
   public interface Handler {
      boolean onCheck(@NotNull GrimUser var1, @NotNull AbstractCheck var2, boolean var3);
   }
}
