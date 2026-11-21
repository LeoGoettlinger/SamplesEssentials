package ch.njol.skript;

import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.timings.SkriptTimings;
import ch.njol.skript.util.Task;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.Nullable;

public final class SkriptEventHandler {
   private static final SkriptEventHandler.PriorityListener[] listeners;
   private static final Multimap<Class<? extends Event>, Trigger> triggers;
   private static long startEvent;
   private static long startTrigger;
   @Deprecated(
      since = "2.9.0",
      forRemoval = true
   )
   public static final Set<Class<? extends Event>> listenCancelled;
   private static final Map<Class<? extends Event>, Method> handlerListMethods;
   private static final Map<Method, WeakReference<HandlerList>> handlerListCache;

   private SkriptEventHandler() {
   }

   private static List<Trigger> getTriggers(Class<? extends Event> event) {
      HandlerList eventHandlerList = getHandlerList(event);

      assert eventHandlerList != null;

      return triggers.asMap()
         .entrySet()
         .stream()
         .filter(entry -> ((Class)entry.getKey()).isAssignableFrom(event) && getHandlerList((Class<? extends Event>)entry.getKey()) == eventHandlerList)
         .flatMap(entry -> ((Collection)entry.getValue()).stream())
         .distinct()
         .collect(Collectors.toList());
   }

   private static void check(Event event, EventPriority priority) {
      List<Trigger> triggers = getTriggers((Class<? extends Event>)event.getClass());
      if (!triggers.isEmpty()) {
         boolean isCancelled = isCancelled(event);
         logEventStart(event, priority);

         for (Trigger trigger : triggers) {
            SkriptEvent triggerEvent = trigger.getEvent();
            if (triggerEvent.getEventPriority() == priority && triggerEvent.getListeningBehavior().matches(isCancelled)) {
               execute(trigger, event);
            }
         }

         logEventEnd();
      }
   }

   private static boolean isCancelled(Event event) {
      if (event instanceof Cancellable cancellable) {
         return event instanceof PlayerInteractEvent interactEvent ? isPlayerInteractEventCancelled(interactEvent) : cancellable.isCancelled();
      } else {
         return false;
      }
   }

   private static boolean isPlayerInteractEventCancelled(PlayerInteractEvent event) {
      return switch (event.getAction()) {
         case LEFT_CLICK_AIR, RIGHT_CLICK_AIR -> event.useItemInHand() == Result.DENY;
         case RIGHT_CLICK_BLOCK -> event.useItemInHand() == Result.DENY && event.useInteractedBlock() == Result.DENY;
         case PHYSICAL -> event.useInteractedBlock() == Result.DENY;
         default -> event.useItemInHand() == Result.DENY || event.useInteractedBlock() == Result.DENY;
      };
   }

   private static void execute(Trigger trigger, Event event) {
      Runnable execute = () -> {
         logTriggerStart(trigger);
         Object timing = SkriptTimings.start(trigger.getDebugLabel());
         trigger.execute(event);
         SkriptTimings.stop(timing);
         logTriggerEnd(trigger);
      };
      if (trigger.getEvent().canExecuteAsynchronously()) {
         if (trigger.getEvent().check(event)) {
            execute.run();
         }
      } else {
         Task.callSync(() -> {
            if (trigger.getEvent().check(event)) {
               execute.run();
            }

            return null;
         });
      }
   }

   public static void logEventStart(Event event) {
      logEventStart(event, null);
   }

   public static void logEventStart(Event event, @Nullable EventPriority priority) {
      startEvent = System.nanoTime();
      if (Skript.logVeryHigh()) {
         Skript.info("");
         String message = "== " + event.getClass().getName();
         if (priority != null) {
            message = message + " with priority " + priority;
         }

         if (event instanceof Cancellable && ((Cancellable)event).isCancelled()) {
            message = message + " (cancelled)";
         }

         Skript.info(message + " ==");
      }
   }

   public static void logEventEnd() {
      if (Skript.logVeryHigh()) {
         Skript.info("== took " + 1.0 * (System.nanoTime() - startEvent) / 1000000.0 + " milliseconds ==");
      }
   }

   public static void logTriggerStart(Trigger trigger) {
      startTrigger = System.nanoTime();
      if (Skript.logVeryHigh()) {
         Skript.info("# " + trigger.getName());
      }
   }

   public static void logTriggerEnd(Trigger t) {
      if (Skript.logVeryHigh()) {
         Skript.info("# " + t.getName() + " took " + 1.0 * (System.nanoTime() - startTrigger) / 1000000.0 + " milliseconds");
      }
   }

   @Deprecated(
      since = "2.7.0",
      forRemoval = true
   )
   public static void addSelfRegisteringTrigger(Trigger t) {
   }

   public static void registerBukkitEvents(Trigger trigger, Class<? extends Event>[] events) {
      for (Class<? extends Event> event : events) {
         registerBukkitEvent(trigger, event);
      }
   }

   public static void registerBukkitEvent(Trigger trigger, Class<? extends Event> event) {
      HandlerList handlerList = getHandlerList(event);
      if (handlerList != null) {
         triggers.put(event, trigger);
         EventPriority priority = trigger.getEvent().getEventPriority();
         if (!isEventRegistered(handlerList, priority)) {
            SkriptEventHandler.PriorityListener listener = listeners[priority.ordinal()];
            Bukkit.getPluginManager().registerEvent(event, listener, priority, listener.executor, Skript.getInstance());
         }
      }
   }

   public static void unregisterBukkitEvents(Trigger trigger) {
      Iterator<Entry<Class<? extends Event>, Trigger>> entryIterator = triggers.entries().iterator();

      label51:
      while (entryIterator.hasNext()) {
         Entry<Class<? extends Event>, Trigger> entry = entryIterator.next();
         if (entry.getValue() == trigger) {
            Class<? extends Event> event = entry.getKey();
            entryIterator.remove();
            EventPriority priority = trigger.getEvent().getEventPriority();

            for (Trigger eventTrigger : triggers.get(event)) {
               if (eventTrigger.getEvent().getEventPriority() == priority) {
                  continue label51;
               }
            }

            HandlerList handlerList = getHandlerList(event);
            if (handlerList != null) {
               Skript skript = Skript.getInstance();

               for (RegisteredListener registeredListener : handlerList.getRegisteredListeners()) {
                  Listener listener = registeredListener.getListener();
                  if (registeredListener.getPlugin() == skript
                     && listener instanceof SkriptEventHandler.PriorityListener
                     && ((SkriptEventHandler.PriorityListener)listener).priority == priority) {
                     handlerList.unregister(listener);
                  }
               }
            }
         }
      }
   }

   @Nullable
   private static HandlerList getHandlerList(Class<? extends Event> eventClass) {
      try {
         Method method = getHandlerListMethod(eventClass);
         WeakReference<HandlerList> handlerListReference = handlerListCache.get(method);
         HandlerList handlerList = handlerListReference != null ? handlerListReference.get() : null;
         if (handlerList == null) {
            method.setAccessible(true);
            handlerList = (HandlerList)method.invoke(null);
            handlerListCache.put(method, new WeakReference<>(handlerList));
         }

         return handlerList;
      } catch (Exception var4) {
         Skript.exception(var4, "Failed to get HandlerList for event " + eventClass.getName());
         return null;
      }
   }

   private static Method getHandlerListMethod(Class<? extends Event> eventClass) {
      Method method;
      synchronized (handlerListMethods) {
         method = handlerListMethods.get(eventClass);
         if (method == null) {
            method = getHandlerListMethod_i(eventClass);
            if (method != null) {
               method.setAccessible(true);
            }

            handlerListMethods.put(eventClass, method);
         }
      }

      if (method == null) {
         throw new RuntimeException("No getHandlerList method found");
      } else {
         return method;
      }
   }

   @Nullable
   private static Method getHandlerListMethod_i(Class<? extends Event> eventClass) {
      try {
         return eventClass.getDeclaredMethod("getHandlerList");
      } catch (NoSuchMethodException var2) {
         return eventClass.getSuperclass() != null
               && !eventClass.getSuperclass().equals(Event.class)
               && Event.class.isAssignableFrom(eventClass.getSuperclass())
            ? getHandlerListMethod(eventClass.getSuperclass().asSubclass(Event.class))
            : null;
      }
   }

   private static boolean isEventRegistered(HandlerList handlerList, EventPriority priority) {
      for (RegisteredListener registeredListener : handlerList.getRegisteredListeners()) {
         Listener listener = registeredListener.getListener();
         if (registeredListener.getPlugin() == Skript.getInstance()
            && listener instanceof SkriptEventHandler.PriorityListener
            && ((SkriptEventHandler.PriorityListener)listener).priority == priority) {
            return true;
         }
      }

      return false;
   }

   static {
      EventPriority[] priorities = EventPriority.values();
      listeners = new SkriptEventHandler.PriorityListener[priorities.length];

      for (int i = 0; i < priorities.length; i++) {
         listeners[i] = new SkriptEventHandler.PriorityListener(priorities[i]);
      }

      triggers = ArrayListMultimap.create();
      listenCancelled = new HashSet<>();
      handlerListMethods = new HashMap<>();
      handlerListCache = new HashMap<>();
   }

   public static class PriorityListener implements Listener {
      public final EventPriority priority;
      public final EventExecutor executor = (listener, event) -> SkriptEventHandler.check(event, ((SkriptEventHandler.PriorityListener)listener).priority);

      public PriorityListener(EventPriority priority) {
         this.priority = priority;
      }
   }
}
