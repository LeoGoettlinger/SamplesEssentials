package com.magmaguy.elitemobs.items.potioneffects;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.potioneffects.custom.Harm;
import com.magmaguy.elitemobs.items.potioneffects.custom.Heal;
import com.magmaguy.elitemobs.items.potioneffects.custom.Saturation;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.utils.EntityFinder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerPotionEffects implements Listener {
   private static PlayerPotionEffects active;
   private PlayerPotionEffects.PotionMutation mutation;
   private static final int STANDARD_REFRESH_THRESHOLD_TICKS = 20;
   private static final int NIGHT_VISION_REFRESH_THRESHOLD_TICKS = 240;
   private final Map<UUID, Map<PotionEffectType, PlayerPotionEffects.AppliedContinuousEffect>> appliedContinuousEffects = new HashMap<>();
   private final Set<UUID> failedEquipmentQueries = new HashSet<>();

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPotionChanged(EntityPotionEffectEvent event) {
      if (event.getEntity() instanceof Player player) {
         if (event.getAction() != Action.CHANGED || event.isOverride()) {
            PotionEffectType type = event.getModifiedType();
            if (this.mutation == null || !this.mutation.player.equals(player.getUniqueId()) || !this.mutation.type.equals(type) || ++this.mutation.events != 1) {
               Map<PotionEffectType, PlayerPotionEffects.AppliedContinuousEffect> effects = this.appliedContinuousEffects.get(player.getUniqueId());
               if (effects != null) {
                  PlayerPotionEffects.AppliedContinuousEffect owned = effects.get(type);
                  if (owned != null) {
                     owned.externallyChanged = true;
                  }
               }
            }
         }
      }
   }

   private boolean removeOwnedEffect(Player player, PotionEffectType type) {
      PlayerPotionEffects.PotionMutation previous = this.mutation;
      this.mutation = new PlayerPotionEffects.PotionMutation(player, type);

      boolean var4;
      try {
         player.removePotionEffect(type);
         var4 = player.getPotionEffect(type) == null;
      } finally {
         this.mutation = previous;
      }

      return var4;
   }

   private PlayerPotionEffects.AppliedContinuousEffect installEffect(Player player, PotionEffect effect) {
      PlayerPotionEffects.PotionMutation previous = this.mutation;
      PlayerPotionEffects.PotionMutation attempt = new PlayerPotionEffects.PotionMutation(player, effect.getType());
      this.mutation = attempt;

      PlayerPotionEffects.AppliedContinuousEffect var6;
      try {
         player.addPotionEffect(effect);
         PotionEffect actual = player.getPotionEffect(effect.getType());
         var6 = attempt.events == 1 && effect.equals(actual) ? new PlayerPotionEffects.AppliedContinuousEffect(actual) : null;
      } finally {
         this.mutation = previous;
      }

      return var6;
   }

   public static void shutdown() {
      if (active != null) {
         for (UUID playerId : new ArrayList<>(active.appliedContinuousEffects.keySet())) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
               active.removeOwnedContinuousEffects(player);
            }
         }

         active.appliedContinuousEffects.clear();
         active = null;
      }
   }

   public PlayerPotionEffects() {
      active = this;
      (new BukkitRunnable() {
            public void run() {
               for (Player player : Bukkit.getOnlinePlayers()) {
                  ElitePlayerInventory inventory = ElitePlayerInventory.playerInventories.get(player.getUniqueId());
                  if (!player.isDead() && inventory != null && PlayerData.getPlayerData(player.getUniqueId()) != null) {
                     try {
                        PlayerPotionEffects.this.applyContinuousPotionEffects(inventory.getContinuousPotionEffects(true), player);
                        PlayerPotionEffects.this.failedEquipmentQueries.remove(player.getUniqueId());
                     } catch (RuntimeException var5) {
                        PlayerPotionEffects.this.reconcileContinuousPotionEffects(player, Map.of());
                        if (PlayerPotionEffects.this.failedEquipmentQueries.add(player.getUniqueId())) {
                           MetadataHandler.PLUGIN
                              .getLogger()
                              .log(Level.WARNING, "Could not refresh equipment effects for " + player.getUniqueId(), (Throwable)var5);
                        }
                     }
                  } else {
                     PlayerPotionEffects.this.reconcileContinuousPotionEffects(player, Map.of());
                  }
               }
            }
         })
         .runTaskTimer(MetadataHandler.PLUGIN, 20L, 20L);
   }

   public static void addOnHitCooldown(final Set<UUID> cooldownList, Player player, long delay) {
      final UUID playerUUID = player.getUniqueId();
      cooldownList.add(playerUUID);
      (new BukkitRunnable() {
         public void run() {
            cooldownList.remove(playerUUID);
         }
      }).runTaskLater(MetadataHandler.PLUGIN, delay);
   }

   private void applyContinuousPotionEffects(List<ElitePotionEffect> elitePotionEffects, Player player) {
      Map<PotionEffectType, ElitePotionEffect> desiredPotionEffects = new HashMap<>();

      for (ElitePotionEffect elitePotionEffect : elitePotionEffects) {
         if (elitePotionEffect.isEnabled()) {
            PotionEffect potionEffect = elitePotionEffect.getPotionEffect();
            if (potionEffect != null) {
               PotionEffectType potionEffectType = potionEffect.getType();
               if (!potionEffectType.equals(PotionEffectType.ABSORPTION) && !potionEffectType.equals(PotionEffectType.HEALTH_BOOST)) {
                  if (potionEffectType.equals(PotionEffectType.INSTANT_HEALTH)) {
                     Heal.doHeal(player, elitePotionEffect);
                  } else if (potionEffectType.equals(PotionEffectType.SATURATION)) {
                     Saturation.doSaturation(player, elitePotionEffect);
                  } else if (potionEffectType.equals(PotionEffectType.INSTANT_DAMAGE)) {
                     Harm.doHarm(player, elitePotionEffect);
                  } else {
                     desiredPotionEffects.merge(potionEffectType, elitePotionEffect, PlayerPotionEffects::strongerPotionEffect);
                  }
               }
            }
         }
      }

      this.reconcileContinuousPotionEffects(player, desiredPotionEffects);
   }

   private void reconcileContinuousPotionEffects(Player player, Map<PotionEffectType, ElitePotionEffect> desiredPotionEffects) {
      UUID playerUUID = player.getUniqueId();
      Map<PotionEffectType, PlayerPotionEffects.AppliedContinuousEffect> appliedEffects = this.appliedContinuousEffects.get(playerUUID);
      if (appliedEffects != null || !desiredPotionEffects.isEmpty()) {
         if (appliedEffects == null) {
            appliedEffects = new HashMap<>();
            this.appliedContinuousEffects.put(playerUUID, appliedEffects);
         }

         Iterator<Entry<PotionEffectType, PlayerPotionEffects.AppliedContinuousEffect>> iterator = appliedEffects.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<PotionEffectType, PlayerPotionEffects.AppliedContinuousEffect> entry = iterator.next();
            PotionEffectType potionEffectType = entry.getKey();
            PlayerPotionEffects.AppliedContinuousEffect appliedEffect = entry.getValue();
            ElitePotionEffect desiredEffect = desiredPotionEffects.get(potionEffectType);
            PotionEffect currentEffect = player.getPotionEffect(potionEffectType);
            if (desiredEffect == null) {
               if (!appliedEffect.matchesCurrentLease(currentEffect) || this.removeOwnedEffect(player, potionEffectType)) {
                  iterator.remove();
               }
            } else if (!appliedEffect.matchesCurrentLease(currentEffect)) {
               iterator.remove();
            } else if (!appliedEffect.hasSameDefinition(desiredEffect.getPotionEffect()) && this.removeOwnedEffect(player, potionEffectType)) {
               iterator.remove();
            }
         }

         for (Entry<PotionEffectType, ElitePotionEffect> entry : desiredPotionEffects.entrySet()) {
            PotionEffectType potionEffectType = entry.getKey();
            PotionEffect desiredEffect = entry.getValue().getPotionEffect();
            PlayerPotionEffects.AppliedContinuousEffect appliedEffect = appliedEffects.get(potionEffectType);
            PotionEffect currentEffect = player.getPotionEffect(potionEffectType);
            if (appliedEffect != null && appliedEffect.matchesCurrentLease(currentEffect)) {
               if (currentEffect.getDuration() > refreshThreshold(potionEffectType, desiredEffect) || !this.removeOwnedEffect(player, potionEffectType)) {
                  continue;
               }

               currentEffect = null;
               appliedEffects.remove(potionEffectType);
            }

            if (currentEffect == null) {
               PlayerPotionEffects.AppliedContinuousEffect installed = this.installEffect(player, desiredEffect);
               if (installed != null) {
                  appliedEffects.put(potionEffectType, installed);
               }
            }
         }

         if (appliedEffects.isEmpty()) {
            this.appliedContinuousEffects.remove(playerUUID);
         }
      }
   }

   private static ElitePotionEffect strongerPotionEffect(ElitePotionEffect first, ElitePotionEffect second) {
      PotionEffect firstEffect = first.getPotionEffect();
      PotionEffect secondEffect = second.getPotionEffect();
      if (secondEffect.getAmplifier() != firstEffect.getAmplifier()) {
         return secondEffect.getAmplifier() > firstEffect.getAmplifier() ? second : first;
      } else {
         return secondEffect.getDuration() > firstEffect.getDuration() ? second : first;
      }
   }

   private static int refreshThreshold(PotionEffectType potionEffectType, PotionEffect desiredEffect) {
      return potionEffectType.equals(PotionEffectType.NIGHT_VISION) ? 240 : Math.min(20, Math.max(1, desiredEffect.getDuration() / 2));
   }

   @EventHandler
   public void onPlayerQuit(PlayerQuitEvent event) {
      this.failedEquipmentQueries.remove(event.getPlayer().getUniqueId());
      this.removeOwnedContinuousEffects(event.getPlayer());
   }

   private void removeOwnedContinuousEffects(Player player) {
      Map<PotionEffectType, PlayerPotionEffects.AppliedContinuousEffect> appliedEffects = this.appliedContinuousEffects.remove(player.getUniqueId());
      if (appliedEffects != null) {
         for (Entry<PotionEffectType, PlayerPotionEffects.AppliedContinuousEffect> entry : appliedEffects.entrySet()) {
            if (entry.getValue().matchesCurrentLease(player.getPotionEffect(entry.getKey())) && !this.removeOwnedEffect(player, entry.getKey())) {
               MetadataHandler.PLUGIN.getLogger().warning("Equipment effect removal was refused for " + player.getUniqueId() + ": " + entry.getKey().getKey());
            }
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true,
      priority = EventPriority.HIGHEST
   )
   public void onPlayerHitWithPotionEffect(EntityDamageByEntityEvent event) {
      LivingEntity damager = EntityFinder.getRealDamager(event);
      if (damager != null && damager.getType().equals(EntityType.PLAYER)) {
         Player player = (Player)damager;
         if (!player.hasMetadata("NPC")) {
            if (ElitePlayerInventory.playerInventories.get(player.getUniqueId()) != null) {
               if (event.getEntity() instanceof LivingEntity) {
                  LivingEntity damagee = (LivingEntity)event.getEntity();

                  for (ElitePotionEffect elitePotionEffect : ElitePlayerInventory.playerInventories.get(player.getUniqueId()).getOnHitPotionEffects(true)) {
                     this.doOnHitPotionEffect(elitePotionEffect, player, damagee);
                  }
               }
            }
         }
      }
   }

   private void doOnHitPotionEffect(ElitePotionEffect elitePotionEffect, Player player, LivingEntity damagee) {
      if (elitePotionEffect.isEnabled()) {
         if (!elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.ABSORPTION)) {
            if (!elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.HEALTH_BOOST)) {
               switch (elitePotionEffect.getTarget()) {
                  case SELF:
                     if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.INSTANT_HEALTH)) {
                        Heal.doHeal(player, elitePotionEffect);
                     } else if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.SATURATION)) {
                        Saturation.doSaturation(player, elitePotionEffect);
                     } else {
                        if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.INSTANT_DAMAGE)) {
                           Harm.doHarm(player, elitePotionEffect);
                           return;
                        }

                        player.addPotionEffect(elitePotionEffect.getPotionEffect());
                     }
                     break;
                  case TARGET:
                     if (elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.LEVITATION)
                        || elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.SLOWNESS)
                        || elitePotionEffect.getPotionEffect().getType().equals(PotionEffectType.BLINDNESS)) {
                        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(damagee);
                        if (eliteEntity != null && eliteEntity.getHealthMultiplier() > 1.0) {
                           return;
                        }
                     }

                     damagee.addPotionEffect(elitePotionEffect.getPotionEffect());
               }
            }
         }
      }
   }

   private static final class AppliedContinuousEffect {
      private final PotionEffect appliedEffect;
      private boolean externallyChanged;

      private AppliedContinuousEffect(PotionEffect appliedEffect) {
         this.appliedEffect = appliedEffect;
      }

      private boolean hasSameDefinition(PotionEffect potionEffect) {
         return potionEffect != null
            && this.appliedEffect.getType().equals(potionEffect.getType())
            && this.appliedEffect.getAmplifier() == potionEffect.getAmplifier()
            && this.appliedEffect.getDuration() == potionEffect.getDuration()
            && this.appliedEffect.isAmbient() == potionEffect.isAmbient()
            && this.appliedEffect.hasParticles() == potionEffect.hasParticles()
            && this.appliedEffect.hasIcon() == potionEffect.hasIcon();
      }

      private boolean matchesCurrentLease(PotionEffect potionEffect) {
         return !this.externallyChanged
            && potionEffect != null
            && this.appliedEffect.getType().equals(potionEffect.getType())
            && this.appliedEffect.getAmplifier() == potionEffect.getAmplifier()
            && potionEffect.getDuration() > 0
            && potionEffect.getDuration() <= this.appliedEffect.getDuration()
            && this.appliedEffect.isAmbient() == potionEffect.isAmbient()
            && this.appliedEffect.hasParticles() == potionEffect.hasParticles()
            && this.appliedEffect.hasIcon() == potionEffect.hasIcon();
      }
   }

   private static final class PotionMutation {
      private final UUID player;
      private final PotionEffectType type;
      private int events;

      private PotionMutation(Player player, PotionEffectType type) {
         this.player = player.getUniqueId();
         this.type = type;
      }
   }
}
