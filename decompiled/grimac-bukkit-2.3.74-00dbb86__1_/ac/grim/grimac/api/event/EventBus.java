package ac.grim.grimac.api.event;

import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;

public interface EventBus {
   @NotNull
   <E extends GrimEvent<C>, C extends EventChannel<? extends E, ?>> C get(@NotNull Class<E> var1);

   <E extends GrimEvent<C>, C extends EventChannel<? extends E, ?>> void register(@NotNull Class<E> var1, @NotNull C var2);

   @Deprecated
   void registerAnnotatedListeners(@NotNull Object var1, @NotNull Object var2);

   @Deprecated
   void registerAnnotatedListeners(@NotNull GrimPlugin var1, @NotNull Object var2);

   @Deprecated
   void registerStaticAnnotatedListeners(@NotNull Object var1, @NotNull Class<?> var2);

   @Deprecated
   void registerStaticAnnotatedListeners(@NotNull GrimPlugin var1, @NotNull Class<?> var2);

   @Deprecated
   void unregisterListeners(@NotNull Object var1, @NotNull Object var2);

   @Deprecated
   void unregisterListeners(@NotNull GrimPlugin var1, @NotNull Object var2);

   @Deprecated
   void unregisterStaticListeners(@NotNull Object var1, @NotNull Class<?> var2);

   @Deprecated
   void unregisterStaticListeners(@NotNull GrimPlugin var1, @NotNull Class<?> var2);

   void unregisterAllListeners(@NotNull Object var1);

   void unregisterAllListeners(@NotNull GrimPlugin var1);

   @Deprecated
   void unregisterListener(@NotNull Object var1, @NotNull GrimEventListener<?> var2);

   @Deprecated
   void unregisterListener(@NotNull GrimPlugin var1, @NotNull GrimEventListener<?> var2);

   @Deprecated
   @ApiStatus.Internal
   void post(@NotNull GrimEvent<?> var1);

   @Deprecated
   <T extends GrimEvent<?>> void subscribe(
      @NotNull Object var1, @NotNull Class<T> var2, @NotNull GrimEventListener<T> var3, int var4, boolean var5, @NotNull Class<?> var6
   );

   @Deprecated
   <T extends GrimEvent<?>> void subscribe(
      @NotNull GrimPlugin var1, @NotNull Class<T> var2, @NotNull GrimEventListener<T> var3, int var4, boolean var5, @NotNull Class<?> var6
   );

   @Deprecated
   default <T extends GrimEvent<?>> void subscribe(
      @NotNull Object pluginContext, @NotNull Class<T> eventType, @NotNull GrimEventListener<T> listener, int priority, boolean ignoreCancelled
   ) {
      this.subscribe(pluginContext, eventType, listener, priority, ignoreCancelled, listener.getClass());
   }

   @Deprecated
   default <T extends GrimEvent<?>> void subscribe(
      @NotNull GrimPlugin plugin, @NotNull Class<T> eventType, @NotNull GrimEventListener<T> listener, int priority, boolean ignoreCancelled
   ) {
      this.subscribe(plugin, eventType, listener, priority, ignoreCancelled, listener.getClass());
   }

   @Deprecated
   default <T extends GrimEvent<?>> void subscribe(@NotNull Object pluginContext, @NotNull Class<T> eventType, @NotNull GrimEventListener<T> listener) {
      this.subscribe(pluginContext, eventType, listener, 0, false);
   }

   @Deprecated
   default <T extends GrimEvent<?>> void subscribe(@NotNull GrimPlugin plugin, @NotNull Class<T> eventType, @NotNull GrimEventListener<T> listener) {
      this.subscribe(plugin, eventType, listener, 0, false);
   }
}
