package com.magmaguy.elitemobs.advancedcombat.abilities;

import com.magmaguy.elitemobs.advancedcombat.MonotonicTickClock;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Cause;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

final class BukkitBlinkPotionFlash implements Listener, AutoCloseable {
   private static final Map<BukkitBlinkPotionFlash.FlashType, PotionEffect> FLASHES = flashEffects();
   private final Plugin plugin;
   private final Map<UUID, BukkitBlinkPotionFlash.ActiveFlash> active = new HashMap<>();
   private long nextToken;
   private boolean mutating;
   private boolean closed;

   BukkitBlinkPotionFlash(Plugin plugin) {
      this.plugin = Objects.requireNonNull(plugin, "plugin");
      Bukkit.getPluginManager().registerEvents(this, plugin);
   }

   void flash(Player player) {
      Objects.requireNonNull(player, "player");
      if (!this.closed && player.isOnline()) {
         UUID playerId = player.getUniqueId();
         BukkitBlinkPotionFlash.ActiveFlash previous = this.active.get(playerId);
         long token = ++this.nextToken;
         long startedAt = MonotonicTickClock.currentTick();
         EnumMap<BukkitBlinkPotionFlash.FlashType, BukkitBlinkPotionFlash.EffectLease> leases = new EnumMap<>(BukkitBlinkPotionFlash.FlashType.class);
         this.mutating = true;

         try {
            for (BukkitBlinkPotionFlash.FlashType type : BukkitBlinkPotionFlash.FlashType.values()) {
               BukkitBlinkPotionFlash.EffectLease priorLease = previous == null ? null : previous.leases().get(type);
               PotionEffect baseline = priorLease != null && !priorLease.externallyChanged()
                  ? priorLease.previous()
                  : player.getPotionEffect(type.bukkitType());
               long baselineStartedAt = priorLease != null && !priorLease.externallyChanged() ? priorLease.baselineStartedAtTick() : startedAt;
               PotionEffect flash = FLASHES.get(type);
               boolean stronger = baseline != null && baseline.getAmplifier() > flash.getAmplifier();
               PotionEffect before = player.getPotionEffect(type.bukkitType());
               boolean installed = !stronger
                  && (player.addPotionEffect(flash, true) || sameIdentity(before, flash) || sameIdentity(player.getPotionEffect(type.bukkitType()), flash));
               leases.put(type, new BukkitBlinkPotionFlash.EffectLease(baseline, installed, false, baselineStartedAt));
            }
         } finally {
            this.mutating = false;
         }

         this.active.put(playerId, new BukkitBlinkPotionFlash.ActiveFlash(token, startedAt, leases));
         Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.cleanup(playerId, token), 1L);
      }
   }

   void deactivate(Player player) {
      if (player != null) {
         BukkitBlinkPotionFlash.ActiveFlash current = this.active.get(player.getUniqueId());
         if (current != null) {
            this.cleanup(player.getUniqueId(), current.token());
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPotionEffect(EntityPotionEffectEvent event) {
      if (!this.mutating && event.getEntity() instanceof Player player) {
         BukkitBlinkPotionFlash.ActiveFlash current = this.active.get(player.getUniqueId());
         if (current != null) {
            BukkitBlinkPotionFlash.FlashType type = BukkitBlinkPotionFlash.FlashType.from(event.getModifiedType());
            if (type != null) {
               BukkitBlinkPotionFlash.EffectLease lease = current.leases().get(type);
               PotionEffect ownedFlash = FLASHES.get(type);
               if (event.getCause() != Cause.EXPIRATION || !sameIdentity(event.getOldEffect(), ownedFlash)) {
                  current.leases().put(type, lease.withExternalChange());
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onQuit(PlayerQuitEvent event) {
      this.deactivate(event.getPlayer());
   }

   @Override
   public void close() {
      if (!this.closed) {
         for (Entry<UUID, BukkitBlinkPotionFlash.ActiveFlash> entry : new ArrayList<>(this.active.entrySet())) {
            this.cleanup(entry.getKey(), entry.getValue().token());
         }

         this.active.clear();
         this.closed = true;
         HandlerList.unregisterAll(this);
      }
   }

   private void cleanup(UUID playerId, long scheduledToken) {
      BukkitBlinkPotionFlash.ActiveFlash currentFlash = this.active.get(playerId);
      if (currentFlash != null && currentFlash.token() == scheduledToken) {
         Player player = Bukkit.getPlayer(playerId);
         if (player == null) {
            this.active.remove(playerId, currentFlash);
         } else {
            this.mutating = true;

            try {
               for (BukkitBlinkPotionFlash.FlashType type : BukkitBlinkPotionFlash.FlashType.values()) {
                  BukkitBlinkPotionFlash.EffectLease lease = currentFlash.leases().get(type);
                  PotionEffect flash = FLASHES.get(type);
                  PotionEffect current = player.getPotionEffect(type.bukkitType());
                  BlinkPotionFlashPolicy.CleanupAction action = BlinkPotionFlashPolicy.cleanup(
                     scheduledToken, currentFlash.token(), lease.externallyChanged(), lease.installed(), state(flash), state(current), state(lease.previous())
                  );
                  switch (action) {
                     case NOOP:
                     default:
                        break;
                     case REMOVE_FLASH:
                        player.removePotionEffect(type.bukkitType());
                        break;
                     case RESTORE_PREVIOUS:
                        PotionEffect restored = remaining(lease.previous(), MonotonicTickClock.currentTick() - lease.baselineStartedAtTick());
                        if (restored != null) {
                           player.addPotionEffect(restored, true);
                        } else if (sameIdentity(current, flash)) {
                           player.removePotionEffect(type.bukkitType());
                        }
                  }
               }
            } finally {
               this.mutating = false;
               this.active.remove(playerId, currentFlash);
            }
         }
      }
   }

   private static PotionEffect remaining(PotionEffect effect, long elapsedTicks) {
      if (effect == null) {
         return null;
      } else {
         int duration = BlinkPotionFlashPolicy.restoredDuration(effect.getDuration(), effect.isInfinite(), elapsedTicks);
         return duration == 0
            ? null
            : new PotionEffect(effect.getType(), duration, effect.getAmplifier(), effect.isAmbient(), effect.hasParticles(), effect.hasIcon());
      }
   }

   private static BlinkPotionFlashPolicy.EffectState state(PotionEffect effect) {
      return effect == null
         ? null
         : new BlinkPotionFlashPolicy.EffectState(effect.getAmplifier(), effect.getDuration(), effect.isAmbient(), effect.hasParticles(), effect.hasIcon());
   }

   private static boolean sameIdentity(PotionEffect first, PotionEffect second) {
      BlinkPotionFlashPolicy.EffectState firstState = state(first);
      BlinkPotionFlashPolicy.EffectState secondState = state(second);
      return firstState != null && firstState.sameIdentity(secondState);
   }

   private static Map<BukkitBlinkPotionFlash.FlashType, PotionEffect> flashEffects() {
      EnumMap<BukkitBlinkPotionFlash.FlashType, PotionEffect> effects = new EnumMap<>(BukkitBlinkPotionFlash.FlashType.class);
      effects.put(BukkitBlinkPotionFlash.FlashType.DARKNESS, new PotionEffect(PotionEffectType.DARKNESS, 1, 0, false, false, false));
      effects.put(BukkitBlinkPotionFlash.FlashType.SPEED, new PotionEffect(PotionEffectType.SPEED, 1, 2, false, false, false));
      return Map.copyOf(effects);
   }

   private record ActiveFlash(long token, long startedAtTick, EnumMap<BukkitBlinkPotionFlash.FlashType, BukkitBlinkPotionFlash.EffectLease> leases) {
   }

   private record EffectLease(PotionEffect previous, boolean installed, boolean externallyChanged, long baselineStartedAtTick) {
      BukkitBlinkPotionFlash.EffectLease withExternalChange() {
         return new BukkitBlinkPotionFlash.EffectLease(this.previous, this.installed, true, this.baselineStartedAtTick);
      }
   }

   private static enum FlashType {
      DARKNESS(PotionEffectType.DARKNESS),
      SPEED(PotionEffectType.SPEED);

      private final PotionEffectType bukkitType;

      private FlashType(PotionEffectType bukkitType) {
         this.bukkitType = bukkitType;
      }

      PotionEffectType bukkitType() {
         return this.bukkitType;
      }

      static BukkitBlinkPotionFlash.FlashType from(PotionEffectType type) {
         for (BukkitBlinkPotionFlash.FlashType candidate : values()) {
            if (candidate.bukkitType.equals(type)) {
               return candidate;
            }
         }

         return null;
      }
   }
}
