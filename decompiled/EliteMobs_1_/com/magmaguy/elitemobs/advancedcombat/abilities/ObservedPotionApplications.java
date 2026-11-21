package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.utils.GameClock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Cause;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

final class ObservedPotionApplications implements Listener, AutoCloseable {
   private final Plugin plugin;
   private final Map<ObservedPotionApplications.Key, ObservedPotionApplications.Pending> pending = new HashMap<>();
   private boolean closed;

   ObservedPotionApplications(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   boolean apply(UUID source, LivingEntity target, PotionEffect effect, Consumer<PotionEffect> accepted, Runnable unconfirmed) {
      if (this.closed) {
         return false;
      } else {
         ObservedPotionApplications.Key key = new ObservedPotionApplications.Key(target.getUniqueId(), effect.getType());
         ObservedPotionApplications.Pending previous = this.pending.get(key);
         if (previous != null) {
            this.finish(previous, false, null);
         }

         ObservedPotionApplications.Pending request = new ObservedPotionApplications.Pending(key, source, target, effect, accepted, unconfirmed);
         this.pending.put(key, request);

         try {
            target.addPotionEffect(effect);
            PotionEffect actual = target.getPotionEffect(effect.getType());
            if (confirmed(request, actual)) {
               this.finish(request, true, actual);
               return true;
            } else if (!request.ambiguous && (request.event == null || !request.event.isCancelled())) {
               request.task = Bukkit.getScheduler().runTask(this.plugin, () -> {
                  if (this.pending.get(key) == request) {
                     PotionEffect observed = target.isValid() && !target.isDead() ? target.getPotionEffect(effect.getType()) : null;
                     this.finish(request, confirmed(request, observed), observed);
                  }
               });
               return false;
            } else {
               this.finish(request, false, null);
               return false;
            }
         } catch (RuntimeException var10) {
            this.pending.remove(key, request);
            if (request.task != null) {
               request.task.cancel();
            }

            throw var10;
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onPotionChange(EntityPotionEffectEvent event) {
      if (!this.pending.isEmpty()) {
         ObservedPotionApplications.Pending request = this.pending
            .get(new ObservedPotionApplications.Key(event.getEntity().getUniqueId(), event.getModifiedType()));
         if (request != null) {
            if (request.event != null || event.getCause() != Cause.PLUGIN || !request.effect.equals(event.getNewEffect())) {
               request.ambiguous = true;
            }

            request.event = event;
         }
      }
   }

   private static boolean confirmed(ObservedPotionApplications.Pending request, PotionEffect actual) {
      if (!request.ambiguous
         && request.event != null
         && !request.event.isCancelled()
         && (request.event.getOldEffect() == null || request.event.isOverride())
         && actual != null) {
         long remaining = request.effect.getDuration() - (GameClock.getCurrentTick() - request.startedTick);
         return actual.getAmplifier() == request.effect.getAmplifier()
            && actual.isAmbient() == request.effect.isAmbient()
            && actual.hasParticles() == request.effect.hasParticles()
            && actual.hasIcon() == request.effect.hasIcon()
            && Math.abs(actual.getDuration() - remaining) <= 1L;
      } else {
         return false;
      }
   }

   private void finish(ObservedPotionApplications.Pending request, boolean applied, PotionEffect actual) {
      if (this.pending.remove(request.key, request)) {
         if (request.task != null) {
            request.task.cancel();
         }

         if (applied) {
            request.accepted.accept(actual);
         } else {
            request.unconfirmed.run();
         }
      }
   }

   void clearSource(UUID source) {
      for (ObservedPotionApplications.Pending request : List.copyOf(this.pending.values())) {
         if (request.source.equals(source)) {
            this.finish(request, false, null);
         }
      }
   }

   @Override
   public void close() {
      this.closed = true;

      for (ObservedPotionApplications.Pending request : List.copyOf(this.pending.values())) {
         this.finish(request, false, null);
      }

      HandlerList.unregisterAll(this);
   }

   private record Key(UUID target, PotionEffectType type) {
   }

   private static final class Pending {
      private final ObservedPotionApplications.Key key;
      private final UUID source;
      private final PotionEffect effect;
      private final Consumer<PotionEffect> accepted;
      private final Runnable unconfirmed;
      private final long startedTick = GameClock.getCurrentTick();
      private EntityPotionEffectEvent event;
      private boolean ambiguous;
      private BukkitTask task;

      private Pending(
         ObservedPotionApplications.Key key, UUID source, LivingEntity target, PotionEffect effect, Consumer<PotionEffect> accepted, Runnable unconfirmed
      ) {
         this.key = key;
         this.source = source;
         this.effect = effect;
         this.accepted = accepted;
         this.unconfirmed = unconfirmed;
      }
   }
}
