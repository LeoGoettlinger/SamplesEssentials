package com.magmaguy.elitemobs.magmacore.util;

import java.lang.reflect.Method;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityKnockbackByEntityEvent;
import org.bukkit.event.entity.EntityKnockbackEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

public final class KnockbackEvents {
   private KnockbackEvents() {
   }

   public static void registerEntityScale(Plugin plugin, Listener listener, BiFunction<Entity, Entity, Double> multiplier) {
      Class<? extends Event> type = eventType("com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent", EntityKnockbackByEntityEvent.class);
      boolean paper = !type.getName().startsWith("org.bukkit.");

      try {
         Method source = type.getMethod(paper ? "getHitBy" : "getSourceEntity");
         Method vector = type.getMethod(paper ? "getKnockback" : "getFinalKnockback");
         Method setter = type.getMethod(paper ? "setKnockback" : "setFinalKnockback", Vector.class);
         plugin.getServer().getPluginManager().registerEvent(type, listener, EventPriority.HIGHEST, (ignored, event) -> {
            if (type.isInstance(event)) {
               try {
                  double scale = multiplier.apply(((EntityEvent)event).getEntity(), (Entity)source.invoke(event));
                  if (Math.abs(scale - 1.0) >= 1.0E-9) {
                     setter.invoke(event, ((Vector)vector.invoke(event)).clone().multiply(scale));
                  }
               } catch (ReflectiveOperationException var9) {
                  throw new EventException(var9);
               }
            }
         }, plugin, true);
      } catch (NoSuchMethodException var8) {
         throw new IllegalStateException("Unsupported knockback event API: " + type.getName(), var8);
      }
   }

   public static void registerCancellation(Plugin plugin, Listener listener, Predicate<Entity> cancel) {
      Class<? extends Event> type = eventType("io.papermc.paper.event.entity.EntityKnockbackEvent", EntityKnockbackEvent.class);
      plugin.getServer().getPluginManager().registerEvent(type, listener, EventPriority.HIGHEST, (ignored, event) -> {
         if (type.isInstance(event) && cancel.test(((EntityEvent)event).getEntity())) {
            ((Cancellable)event).setCancelled(true);
         }
      }, plugin, true);
   }

   private static Class<? extends Event> eventType(String paperName, Class<? extends Event> spigot) {
      try {
         return Class.forName(paperName).asSubclass(Event.class);
      } catch (ClassNotFoundException var3) {
         return spigot;
      }
   }
}
