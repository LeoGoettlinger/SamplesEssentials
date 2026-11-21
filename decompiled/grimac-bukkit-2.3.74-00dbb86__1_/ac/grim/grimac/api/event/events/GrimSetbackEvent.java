package ac.grim.grimac.api.event.events;

import ac.grim.grimac.api.GrimUser;
import ac.grim.grimac.api.event.AbstractEventChannel;
import ac.grim.grimac.api.event.EventChannel;
import ac.grim.grimac.api.event.GrimEvent;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

public abstract class GrimSetbackEvent<CHANNEL extends EventChannel<?, ?>> extends GrimEvent<CHANNEL> {
   protected GrimSetbackEvent() {
      super(true);
   }

   public static final class Channel extends AbstractEventChannel<GrimSetbackEvent<?>, GrimSetbackEvent.Handler> {
      public Channel() {
         super(GrimSetbackEvent.class, GrimSetbackEvent.Handler.class);
      }

      public void onAnySetback(@NotNull GrimPlugin plugin, @NotNull GrimSetbackEvent.Handler handler) {
         this.subscribeAbstract(handler, 0, false, plugin);
      }

      public void onAnySetback(@NotNull GrimPlugin plugin, @NotNull GrimSetbackEvent.Handler handler, int priority) {
         this.subscribeAbstract(handler, priority, false, plugin);
      }

      @Deprecated
      public void onAnySetback(@NotNull Object pluginContext, @NotNull GrimSetbackEvent.Handler handler) {
         this.subscribeAbstractResolving(pluginContext, handler, 0, false);
      }

      @Deprecated
      public void onAnySetback(@NotNull Object pluginContext, @NotNull GrimSetbackEvent.Handler handler, int priority) {
         this.subscribeAbstractResolving(pluginContext, handler, priority, false);
      }
   }

   @FunctionalInterface
   public interface Handler {
      void onAnySetback(@NotNull GrimUser var1, long var2);
   }
}
