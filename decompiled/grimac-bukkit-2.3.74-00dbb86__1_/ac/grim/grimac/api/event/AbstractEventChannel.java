package ac.grim.grimac.api.event;

import ac.grim.grimac.shaded.jetbrains.annotations.ApiStatus;
import ac.grim.grimac.shaded.jetbrains.annotations.NotNull;
import ac.grim.grimac.shaded.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public abstract class AbstractEventChannel<E extends GrimEvent<?>, H> extends EventChannel<E, H> {
   private final List<AbstractEventChannel.AbstractSub<H>> abstractSubs = new CopyOnWriteArrayList<>();
   private final Map<Class<? extends E>, AbstractEventChannel.ChildEntry<H, ?>> children = new ConcurrentHashMap<>();

   protected AbstractEventChannel(@NotNull Class<E> eventClass, @NotNull Class<H> handlerType) {
      super(eventClass, handlerType);
   }

   @ApiStatus.Internal
   public <CH> void registerSubtype(
      @NotNull Class<? extends E> concreteType, @NotNull EventChannel<?, CH> concreteChannel, @NotNull Function<H, CH> bridgeFactory
   ) {
      AbstractEventChannel.ChildEntry<H, CH> entry = new AbstractEventChannel.ChildEntry<>(concreteChannel, bridgeFactory);
      this.children.put(concreteType, entry);

      for (AbstractEventChannel.AbstractSub<H> sub : this.abstractSubs) {
         this.installBridge(sub, entry);
      }
   }

   protected final void subscribeAbstract(@NotNull H handler, int priority, boolean ignoreCancelled, @Nullable Object pluginContext) {
      AbstractEventChannel.AbstractSub<H> sub = new AbstractEventChannel.AbstractSub<>(handler, pluginContext, priority, ignoreCancelled);
      this.abstractSubs.add(sub);

      for (AbstractEventChannel.ChildEntry<H, ?> entry : this.children.values()) {
         this.installBridge(sub, entry);
      }
   }

   protected final void subscribeAbstractResolving(@NotNull Object pluginContext, @NotNull H handler, int priority, boolean ignoreCancelled) {
      this.subscribeAbstract(handler, priority, ignoreCancelled, this.resolvePlugin(pluginContext));
   }

   @Override
   public void unsubscribe(@NotNull H handler) {
      AbstractEventChannel.AbstractSub<H> target = null;
      synchronized (this.abstractSubs) {
         for (AbstractEventChannel.AbstractSub<H> sub : this.abstractSubs) {
            if (sub.handler == handler) {
               target = sub;
               break;
            }
         }

         if (target == null) {
            return;
         }

         this.abstractSubs.remove(target);
      }

      for (AbstractEventChannel.Bridge b : target.bridges) {
         b.sweep();
      }
   }

   @Override
   protected final boolean dispatchTypedFromLegacy(@NotNull E event, @NotNull H handler, boolean cancelled) {
      throw new UnsupportedOperationException("AbstractEventChannel has no direct entries — bridges dispatch through concrete children");
   }

   private <CH> void installBridge(AbstractEventChannel.AbstractSub<H> sub, AbstractEventChannel.ChildEntry<H, CH> entry) {
      CH bridgeHandler = entry.bridgeFactory.apply(sub.handler);
      entry.channel.subscribeTyped(bridgeHandler, sub.priority, sub.ignoreCancelled, sub.pluginContext);
      sub.bridges.add(new AbstractEventChannel.Bridge(entry.channel, bridgeHandler));
   }

   private static final class AbstractSub<H> {
      final H handler;
      @Nullable
      final Object pluginContext;
      final int priority;
      final boolean ignoreCancelled;
      final List<AbstractEventChannel.Bridge> bridges = new ArrayList<>();

      AbstractSub(H handler, @Nullable Object pluginContext, int priority, boolean ignoreCancelled) {
         this.handler = handler;
         this.pluginContext = pluginContext;
         this.priority = priority;
         this.ignoreCancelled = ignoreCancelled;
      }
   }

   private static final class Bridge {
      final EventChannel<?, ?> channel;
      final Object handler;

      Bridge(EventChannel<?, ?> channel, Object handler) {
         this.channel = channel;
         this.handler = handler;
      }

      void sweep() {
         ((EventChannel<?, Object>)this.channel).unsubscribe(this.handler);
      }
   }

   private static final class ChildEntry<H, CH> {
      final EventChannel<?, CH> channel;
      final Function<H, CH> bridgeFactory;

      ChildEntry(EventChannel<?, CH> channel, Function<H, CH> bridgeFactory) {
         this.channel = channel;
         this.bridgeFactory = bridgeFactory;
      }
   }
}
