package ac.grim.grimac.internal.event;

import ac.grim.grimac.api.event.EventBus;
import ac.grim.grimac.api.event.EventChannel;
import ac.grim.grimac.api.event.GrimEvent;
import ac.grim.grimac.api.event.GrimEventHandler;
import ac.grim.grimac.api.event.GrimEventListener;
import ac.grim.grimac.api.event.events.CommandExecuteEvent;
import ac.grim.grimac.api.event.events.CompletePredictionEvent;
import ac.grim.grimac.api.event.events.FlagEvent;
import ac.grim.grimac.api.event.events.GrimCheckEvent;
import ac.grim.grimac.api.event.events.GrimJoinEvent;
import ac.grim.grimac.api.event.events.GrimPlayerSetbackEvent;
import ac.grim.grimac.api.event.events.GrimQuitEvent;
import ac.grim.grimac.api.event.events.GrimReloadEvent;
import ac.grim.grimac.api.event.events.GrimSetbackEvent;
import ac.grim.grimac.api.event.events.GrimTeleportEvent;
import ac.grim.grimac.api.event.events.GrimTransactionReceivedEvent;
import ac.grim.grimac.api.event.events.GrimTransactionSendEvent;
import ac.grim.grimac.api.event.events.GrimVehicleSetbackEvent;
import ac.grim.grimac.api.event.events.GrimVerboseCheckEvent;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.grim.grimac.internal.plugin.resolver.GrimExtensionManager;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class OptimizedEventBus implements EventBus {
   private final Lookup lookup = MethodHandles.lookup();
   private final GrimExtensionManager extensionManager;
   private final ConcurrentMap<Class<?>, EventChannel<?, ?>> channels = new ConcurrentHashMap<>();
   private final Map<Object, Map<Object, List<OptimizedEventBus.Registration>>> instanceRegistrations = new IdentityHashMap<>();

   public OptimizedEventBus(GrimExtensionManager extensionManager) {
      this.extensionManager = extensionManager;
      FlagEvent.Channel flagCh = new FlagEvent.Channel();
      CommandExecuteEvent.Channel commandCh = new CommandExecuteEvent.Channel();
      CompletePredictionEvent.Channel completeCh = new CompletePredictionEvent.Channel();
      GrimJoinEvent.Channel joinCh = new GrimJoinEvent.Channel();
      GrimQuitEvent.Channel quitCh = new GrimQuitEvent.Channel();
      GrimReloadEvent.Channel reloadCh = new GrimReloadEvent.Channel();
      GrimTeleportEvent.Channel teleportCh = new GrimTeleportEvent.Channel();
      GrimTransactionSendEvent.Channel txSendCh = new GrimTransactionSendEvent.Channel();
      GrimTransactionReceivedEvent.Channel txRecvCh = new GrimTransactionReceivedEvent.Channel();
      GrimPlayerSetbackEvent.Channel playerSetbackCh = new GrimPlayerSetbackEvent.Channel();
      GrimVehicleSetbackEvent.Channel vehicleSetbackCh = new GrimVehicleSetbackEvent.Channel();
      this.installChannel(FlagEvent.class, flagCh);
      this.installChannel(CommandExecuteEvent.class, commandCh);
      this.installChannel(CompletePredictionEvent.class, completeCh);
      this.installChannel(GrimJoinEvent.class, joinCh);
      this.installChannel(GrimQuitEvent.class, quitCh);
      this.installChannel(GrimReloadEvent.class, reloadCh);
      this.installChannel(GrimTeleportEvent.class, teleportCh);
      this.installChannel(GrimTransactionSendEvent.class, txSendCh);
      this.installChannel(GrimTransactionReceivedEvent.class, txRecvCh);
      this.installChannel(GrimPlayerSetbackEvent.class, playerSetbackCh);
      this.installChannel(GrimVehicleSetbackEvent.class, vehicleSetbackCh);
      GrimEvent.Channel anyCh = new GrimEvent.Channel();
      GrimCheckEvent.Channel checkCh = new GrimCheckEvent.Channel();
      GrimVerboseCheckEvent.Channel verboseCheckCh = new GrimVerboseCheckEvent.Channel();
      GrimSetbackEvent.Channel setbackCh = new GrimSetbackEvent.Channel();
      this.installAbstractChannel(GrimEvent.class, anyCh);
      this.installAbstractChannel(GrimCheckEvent.class, checkCh);
      this.installAbstractChannel(GrimVerboseCheckEvent.class, verboseCheckCh);
      this.installAbstractChannel(GrimSetbackEvent.class, setbackCh);
      checkCh.registerSubtype(FlagEvent.class, flagCh, FlagEvent.Channel::bridgeFromCheck);
      checkCh.registerSubtype(CommandExecuteEvent.class, commandCh, CommandExecuteEvent.Channel::bridgeFromCheck);
      checkCh.registerSubtype(CompletePredictionEvent.class, completeCh, CompletePredictionEvent.Channel::bridgeFromCheck);
      verboseCheckCh.registerSubtype(FlagEvent.class, flagCh, FlagEvent.Channel::bridgeFromVerboseCheck);
      verboseCheckCh.registerSubtype(CommandExecuteEvent.class, commandCh, CommandExecuteEvent.Channel::bridgeFromVerboseCheck);
      setbackCh.registerSubtype(GrimPlayerSetbackEvent.class, playerSetbackCh, GrimPlayerSetbackEvent.Channel::bridgeFromSetback);
      setbackCh.registerSubtype(GrimVehicleSetbackEvent.class, vehicleSetbackCh, GrimVehicleSetbackEvent.Channel::bridgeFromSetback);
      anyCh.registerSubtype(FlagEvent.class, flagCh, FlagEvent.Channel::bridgeFromAny);
      anyCh.registerSubtype(CommandExecuteEvent.class, commandCh, CommandExecuteEvent.Channel::bridgeFromAny);
      anyCh.registerSubtype(CompletePredictionEvent.class, completeCh, CompletePredictionEvent.Channel::bridgeFromAny);
      anyCh.registerSubtype(GrimJoinEvent.class, joinCh, GrimJoinEvent.Channel::bridgeFromAny);
      anyCh.registerSubtype(GrimQuitEvent.class, quitCh, GrimQuitEvent.Channel::bridgeFromAny);
      anyCh.registerSubtype(GrimReloadEvent.class, reloadCh, GrimReloadEvent.Channel::bridgeFromAny);
      anyCh.registerSubtype(GrimTeleportEvent.class, teleportCh, GrimTeleportEvent.Channel::bridgeFromAny);
      anyCh.registerSubtype(GrimTransactionSendEvent.class, txSendCh, GrimTransactionSendEvent.Channel::bridgeFromAny);
      anyCh.registerSubtype(GrimTransactionReceivedEvent.class, txRecvCh, GrimTransactionReceivedEvent.Channel::bridgeFromAny);
      anyCh.registerSubtype(GrimPlayerSetbackEvent.class, playerSetbackCh, GrimPlayerSetbackEvent.Channel::bridgeFromAny);
      anyCh.registerSubtype(GrimVehicleSetbackEvent.class, vehicleSetbackCh, GrimVehicleSetbackEvent.Channel::bridgeFromAny);
   }

   private <E extends GrimEvent<C>, C extends EventChannel<? extends E, ?>> void installChannel(Class<E> eventClass, C channel) {
      channel.setPluginResolver(this.extensionManager::getPlugin);
      this.channels.put(eventClass, channel);
   }

   private <E, C extends EventChannel<?, ?>> void installAbstractChannel(Class<E> eventClass, C channel) {
      channel.setPluginResolver(this.extensionManager::getPlugin);
      this.channels.put(eventClass, channel);
   }

   @NotNull
   @Override
   public <E extends GrimEvent<C>, C extends EventChannel<? extends E, ?>> C get(@NotNull Class<E> eventClass) {
      EventChannel<?, ?> ch = this.channels.get(eventClass);
      if (ch == null) {
         throw new IllegalArgumentException(
            "No EventChannel registered for " + eventClass.getName() + " — addons must call EventBus.register(Class, Channel) before first use."
         );
      } else {
         return (C)ch;
      }
   }

   @Override
   public <E extends GrimEvent<C>, C extends EventChannel<? extends E, ?>> void register(@NotNull Class<E> eventClass, @NotNull C channel) {
      channel.setPluginResolver(this.extensionManager::getPlugin);
      this.channels.put(eventClass, channel);
   }

   @Override
   public void registerAnnotatedListeners(@NotNull Object pluginContext, @NotNull Object listener) {
      GrimPlugin plugin = this.extensionManager.getPlugin(pluginContext);
      this.registerAnnotatedListeners(plugin, listener);
   }

   @Override
   public void registerAnnotatedListeners(@NotNull GrimPlugin plugin, @NotNull Object listener) {
      this.registerMethods(plugin, listener, listener.getClass(), listener);
   }

   @Override
   public void registerStaticAnnotatedListeners(@NotNull Object pluginContext, @NotNull Class<?> clazz) {
      GrimPlugin plugin = this.extensionManager.getPlugin(pluginContext);
      this.registerStaticAnnotatedListeners(plugin, clazz);
   }

   @Override
   public void registerStaticAnnotatedListeners(@NotNull GrimPlugin plugin, @NotNull Class<?> clazz) {
      this.registerMethods(plugin, null, clazz, clazz);
   }

   private void registerMethods(@NotNull GrimPlugin plugin, @Nullable Object instance, @NotNull Class<?> clazz, @NotNull Object registrationKey) {
      for (Method method : clazz.getDeclaredMethods()) {
         GrimEventHandler annotation = method.getAnnotation(GrimEventHandler.class);
         if (annotation != null && method.getParameterCount() == 1) {
            Class<?> paramType = method.getParameterTypes()[0];
            if (GrimEvent.class.isAssignableFrom(paramType) && (instance != null || Modifier.isStatic(method.getModifiers()))) {
               Class<? extends GrimEvent<?>> eventClass = (Class<? extends GrimEvent<?>>)paramType;
               EventChannel<?, ?> channel = this.channels.get(paramType);
               if (channel != null) {
                  try {
                     method.setAccessible(true);
                     MethodHandle handle = this.lookup.unreflect(method);
                     GrimEventListener<GrimEvent<?>> listener = buildListener(instance, handle);
                     this.subscribeLegacyInternal(
                        plugin, channel, eventClass, listener, annotation.priority(), annotation.ignoreCancelled(), method.getDeclaringClass(), registrationKey
                     );
                  } catch (IllegalAccessException var15) {
                     var15.printStackTrace();
                  }
               }
            }
         }
      }
   }

   private static GrimEventListener<GrimEvent<?>> buildListener(@Nullable Object instance, MethodHandle handle) {
      return instance != null ? event -> {
         try {
            handle.invoke((Object)instance, (GrimEvent)event);
         } catch (Throwable var4) {
            throw new RuntimeException("Failed to invoke listener for " + event.getClass().getName(), var4);
         }
      } : event -> {
         try {
            handle.invoke((GrimEvent)event);
         } catch (Throwable var3) {
            throw new RuntimeException("Failed to invoke listener for " + event.getClass().getName(), var3);
         }
      };
   }

   @Override
   public <T extends GrimEvent<?>> void subscribe(
      @NotNull Object pluginContext,
      @NotNull Class<T> eventType,
      @NotNull GrimEventListener<T> listener,
      int priority,
      boolean ignoreCancelled,
      @NotNull Class<?> declaringClass
   ) {
      GrimPlugin plugin = this.extensionManager.getPlugin(pluginContext);
      this.subscribe(plugin, eventType, listener, priority, ignoreCancelled, declaringClass);
   }

   @Override
   public <T extends GrimEvent<?>> void subscribe(
      @NotNull GrimPlugin plugin,
      @NotNull Class<T> eventType,
      @NotNull GrimEventListener<T> listener,
      int priority,
      boolean ignoreCancelled,
      @NotNull Class<?> declaringClass
   ) {
      EventChannel<?, ?> channel = this.channels.get(eventType);
      if (channel == null) {
         throw new IllegalArgumentException("No EventChannel registered for " + eventType.getName());
      } else {
         this.subscribeLegacyInternal(plugin, channel, eventType, listener, priority, ignoreCancelled, declaringClass, listener);
      }
   }

   private void subscribeLegacyInternal(
      @NotNull GrimPlugin plugin,
      @NotNull EventChannel<?, ?> channel,
      @NotNull Class<? extends GrimEvent<?>> eventClass,
      @NotNull GrimEventListener<?> listener,
      int priority,
      boolean ignoreCancelled,
      @NotNull Class<?> declaringClass,
      @NotNull Object registrationKey
   ) {
      channel.subscribeLegacy(listener, eventClass, priority, ignoreCancelled, plugin, declaringClass);
      synchronized (this) {
         Map<Object, List<OptimizedEventBus.Registration>> byPlugin = this.instanceRegistrations.computeIfAbsent(plugin, p -> new IdentityHashMap<>());
         byPlugin.computeIfAbsent(registrationKey, k -> new ArrayList<>()).add(new OptimizedEventBus.Registration(channel, listener));
      }
   }

   @Override
   public void unregisterListeners(@NotNull Object pluginContext, @NotNull Object listener) {
      this.unregisterListeners(this.extensionManager.getPlugin(pluginContext), listener);
   }

   @Override
   public void unregisterListeners(@NotNull GrimPlugin plugin, @NotNull Object listener) {
      this.removeInstanceRegistrations(plugin, listener);
   }

   @Override
   public void unregisterStaticListeners(@NotNull Object pluginContext, @NotNull Class<?> clazz) {
      this.unregisterStaticListeners(this.extensionManager.getPlugin(pluginContext), clazz);
   }

   @Override
   public void unregisterStaticListeners(@NotNull GrimPlugin plugin, @NotNull Class<?> clazz) {
      this.removeInstanceRegistrations(plugin, clazz);
   }

   @Override
   public void unregisterAllListeners(@NotNull Object pluginContext) {
      this.unregisterAllListeners(this.extensionManager.getPlugin(pluginContext));
   }

   @Override
   public void unregisterAllListeners(@NotNull GrimPlugin plugin) {
      List<OptimizedEventBus.Registration> all = new ArrayList<>();
      synchronized (this) {
         Map<Object, List<OptimizedEventBus.Registration>> byPlugin = this.instanceRegistrations.remove(plugin);
         if (byPlugin != null) {
            for (List<OptimizedEventBus.Registration> regs : byPlugin.values()) {
               all.addAll(regs);
            }
         }
      }

      for (OptimizedEventBus.Registration r : all) {
         r.channel.unsubscribeLegacy(r.listener);
      }

      for (EventChannel<?, ?> ch : this.channels.values()) {
         ch.unsubscribeAllFromPlugin(plugin);
      }
   }

   @Override
   public void unregisterListener(@NotNull Object pluginContext, @NotNull GrimEventListener<?> listener) {
      this.unregisterListener(this.extensionManager.getPlugin(pluginContext), listener);
   }

   @Override
   public void unregisterListener(@NotNull GrimPlugin plugin, @NotNull GrimEventListener<?> listener) {
      for (EventChannel<?, ?> ch : this.channels.values()) {
         ch.unsubscribeLegacy(listener);
      }

      synchronized (this) {
         Map<Object, List<OptimizedEventBus.Registration>> byPlugin = this.instanceRegistrations.get(plugin);
         if (byPlugin != null) {
            for (List<OptimizedEventBus.Registration> regs : byPlugin.values()) {
               regs.removeIf(r -> r.listener == listener);
            }
         }
      }
   }

   private void removeInstanceRegistrations(@NotNull GrimPlugin plugin, @NotNull Object key) {
      List<OptimizedEventBus.Registration> toRemove;
      synchronized (this) {
         Map<Object, List<OptimizedEventBus.Registration>> byPlugin = this.instanceRegistrations.get(plugin);
         if (byPlugin == null) {
            return;
         }

         List<OptimizedEventBus.Registration> regs = byPlugin.remove(key);
         if (regs == null) {
            return;
         }

         toRemove = regs;
      }

      for (OptimizedEventBus.Registration r : toRemove) {
         r.channel.unsubscribeLegacy(r.listener);
      }
   }

   @Override
   public void post(@NotNull GrimEvent<?> event) {
      for (Class<?> c = event.getClass(); c != null && GrimEvent.class.isAssignableFrom(c); c = c.getSuperclass()) {
         EventChannel<?, ?> ch = this.channels.get(c);
         if (ch != null) {
            ((EventChannel<GrimEvent<?>, ?>)ch).dispatchLegacy(event);
         }
      }
   }

   private static final class Registration {
      final EventChannel<?, ?> channel;
      final GrimEventListener<?> listener;

      Registration(EventChannel<?, ?> channel, GrimEventListener<?> listener) {
         this.channel = channel;
         this.listener = listener;
      }
   }
}
