package me.chancesd.pvpmanager.listener;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.TimeUnit;
import me.chancesd.pvpmanager.integration.Hook;
import me.chancesd.pvpmanager.integration.type.WorldGuardDependency;
import me.chancesd.pvpmanager.manager.PlayerManager;
import me.chancesd.pvpmanager.player.CombatPlayer;
import me.chancesd.pvpmanager.player.ProtectionResult;
import me.chancesd.pvpmanager.player.ProtectionType;
import me.chancesd.pvpmanager.sdutils.utils.MCVersion;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Lang;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.utils.CombatUtils;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.LightningStrikeEvent.Cause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

public class EntityListener implements Listener {
   private final PlayerManager playerHandler;
   private final WorldGuardDependency wg;
   private final Cache<LightningStrike, Location> lightningCache = CacheBuilder.newBuilder().expireAfterWrite(2L, TimeUnit.SECONDS).build();

   public EntityListener(PlayerManager ph) {
      this.playerHandler = ph;
      this.wg = (WorldGuardDependency)ph.getPlugin().getDependencyManager().getDependency(Hook.WORLDGUARD);
   }

   @EventHandler(
      priority = EventPriority.NORMAL,
      ignoreCancelled = true
   )
   public final void onPlayerDamage(EntityDamageByEntityEvent event) {
      if (!CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName())) {
         if (!CombatUtils.isPvP(event)) {
            if (event.getEntity() instanceof Player) {
               CombatPlayer attacked = this.playerHandler.get((Player)event.getEntity());
               if (attacked.isNewbie() && Conf.NEWBIE_GODMODE.asBool()) {
                  event.setCancelled(true);
               } else if (event.getDamager() instanceof LightningStrike lightning) {
                  if (!this.lightningCache.asMap().containsKey(lightning)) {
                     return;
                  }

                  if (!attacked.hasPvPEnabled() || attacked.isNewbie() || attacked.hasRespawnProtection()) {
                     event.setCancelled(true);
                  }
               }
            }
         } else {
            Player attacker = this.getAttacker(event.getDamager());
            Player attacked = (Player)event.getEntity();
            if (attacker != null) {
               ProtectionResult result = this.playerHandler.checkProtection(attacker, attacked);
               if (result.isProtected()) {
                  event.setCancelled(true);
                  Lang.messageProtection(result, attacker, attacked);
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public final void onPlayerDamageOverride(EntityDamageByEntityEvent event) {
      if (CombatUtils.isPvP(event) && !CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName()) && event.isCancelled()) {
         Player attacker = this.getAttacker(event.getDamager());
         if (attacker != null) {
            if (this.playerHandler.checkProtection(attacker, (Player)event.getEntity()).type() == ProtectionType.FAIL_OVERRIDE) {
               event.setCancelled(false);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public final void onPlayerDamageMonitor(EntityDamageByEntityEvent event) {
      if (CombatUtils.isPvP(event) && !CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName())) {
         Player attacker = this.getAttacker(event.getDamager());
         Player attacked = (Player)event.getEntity();
         this.processDamage(attacker, attacked);
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public final void onEntityCombust(EntityCombustByEntityEvent event) {
      if (!CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName())) {
         if (!CombatUtils.isPvP(event)) {
            if (event.getEntity() instanceof Player player && this.playerHandler.get(player).isNewbie() && Conf.NEWBIE_GODMODE.asBool()) {
               event.setCancelled(true);
            }
         } else {
            Player attacker = this.getAttacker(event.getCombuster());
            Player attacked = (Player)event.getEntity();
            if (attacker != null) {
               if (!this.playerHandler.canAttack(attacker, attacked)) {
                  event.setCancelled(true);
               }
            }
         }
      }
   }

   public void processDamage(Player attacker, Player defender) {
      CombatPlayer pvpAttacker = this.playerHandler.get(attacker);
      CombatPlayer pvpDefender = this.playerHandler.get(defender);
      if (Conf.PVP_BLOOD.asBool()) {
         defender.getWorld().playEffect(defender.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
      }

      this.disableActions(attacker, defender, pvpAttacker, pvpDefender);
      if (Conf.COMBAT_TAG_ENABLED.asBool()) {
         if (!defender.canSee(attacker)) {
            return;
         }

         if (Conf.VULNERABLE_ENABLED.asBool()
            && this.wg != null
            && !Conf.VULNERABLE_RENEW_TAG.asBool()
            && this.wg.hasDenyPvPFlag(attacker)
            && this.wg.hasDenyPvPFlag(defender)) {
            return;
         }

         pvpAttacker.tag(true, pvpDefender);
         pvpDefender.tag(false, pvpAttacker);
      }
   }

   private void disableActions(Player attacker, Player defender, CombatPlayer pvpAttacker, CombatPlayer pvpDefender) {
      boolean hasExemptPerm = pvpAttacker.hasPerm(Permissions.EXEMPT_DISABLE_ACTIONS);
      if (Conf.DISABLE_FLY.asBool()) {
         if (CombatUtils.canFly(attacker) && !hasExemptPerm) {
            pvpAttacker.disableFly();
         }

         if (CombatUtils.canFly(defender) && !pvpDefender.hasPerm(Permissions.EXEMPT_DISABLE_ACTIONS)) {
            pvpDefender.disableFly();
         }
      }

      if (Conf.DISABLE_ELYTRA.asBool()) {
         if (!hasExemptPerm) {
            CombatUtils.checkGlide(pvpAttacker);
         }

         if (!pvpDefender.hasPerm(Permissions.EXEMPT_DISABLE_ACTIONS)) {
            CombatUtils.checkGlide(pvpDefender);
         }
      }

      if (!hasExemptPerm) {
         if (Conf.DISABLE_GAMEMODE.asBool() && attacker.getGameMode() != GameMode.SURVIVAL) {
            attacker.setGameMode(GameMode.SURVIVAL);
         }

         if (Conf.DISABLE_DISGUISE.asBool()) {
            this.playerHandler.getPlugin().getDependencyManager().disableDisguise(attacker);
         }

         if (Conf.DISABLE_INVISIBILITY.asBool() && attacker.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            attacker.removePotionEffect(PotionEffectType.INVISIBILITY);
         }

         if (Conf.DISABLE_GODMODE.asBool()) {
            this.playerHandler.getPlugin().getDependencyManager().disableGodMode(attacker);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.NORMAL,
      ignoreCancelled = true
   )
   public final void onPotionSplash(PotionSplashEvent event) {
      if (this.shouldCheckPotionEvent(event)) {
         ThrownPotion potion = event.getPotion();
         Player player = (Player)potion.getShooter();
         if (player != null) {
            for (LivingEntity e : event.getAffectedEntities()) {
               if (e.getType() == EntityType.PLAYER && !e.equals(player)) {
                  Player attacked = (Player)e;
                  ProtectionResult result = this.playerHandler.checkProtection(player, attacked);
                  if (result.isProtected()) {
                     event.setIntensity(attacked, 0.0);
                     Lang.messageProtection(result, player, attacked);
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public final void onPotionSplashMonitor(PotionSplashEvent event) {
      if (this.shouldCheckPotionEvent(event)) {
         ThrownPotion potion = event.getPotion();
         Player player = (Player)potion.getShooter();

         for (LivingEntity e : event.getAffectedEntities()) {
            if (e.getType() == EntityType.PLAYER && !e.equals(player)) {
               Player attacked = (Player)e;
               this.processDamage(player, attacked);
            }
         }
      }
   }

   private boolean shouldCheckPotionEvent(PotionSplashEvent event) {
      if (CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName())) {
         return false;
      } else {
         ThrownPotion potion = event.getPotion();
         if (!event.getAffectedEntities().isEmpty() && !potion.getEffects().isEmpty() && potion.getShooter() instanceof Player) {
            for (PotionEffect effect : potion.getEffects()) {
               if (!CombatUtils.isHarmfulPotion(effect.getType())) {
                  return false;
               }
            }

            return true;
         } else {
            return false;
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onLightningStrike(LightningStrikeEvent event) {
      if (!CombatUtils.isWorldExcluded(event.getLightning().getWorld().getName())) {
         if (MCVersion.isAtLeast(MCVersion.V1_13_1)) {
            if (event.getCause() == Cause.TRIDENT) {
               this.lightningCache.put(event.getLightning(), event.getLightning().getLocation());
            }
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onBlockIgnite(BlockIgniteEvent event) {
      if (event.getCause() == IgniteCause.LIGHTNING) {
         if (!CombatUtils.isWorldExcluded(event.getBlock().getWorld().getName())) {
            Entity ignitingEntity = event.getIgnitingEntity();
            if (ignitingEntity instanceof LightningStrike lightningStrike && this.lightningCache.asMap().containsKey(ignitingEntity)) {
               for (Entity entity : lightningStrike.getNearbyEntities(2.0, 2.0, 2.0)) {
                  if (entity instanceof Player player) {
                     CombatPlayer attacked = this.playerHandler.get(player);
                     if (!attacked.hasPvPEnabled() || attacked.isNewbie() || attacked.hasRespawnProtection()) {
                        event.setCancelled(true);
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onProjectileHitEvent(ProjectileHitEvent event) {
      Projectile entity = event.getEntity();
      if (entity.getShooter() instanceof Player player) {
         CombatPlayer pvPlayer = this.playerHandler.get(player);
         if (pvPlayer.isInCombat()
            && (
               Conf.PEARL_RENEW_TAG.asBool() && entity.getType() == EntityType.ENDER_PEARL
                  || Conf.WIND_CHARGE_RENEW_TAG.asBool() && entity.getType() == EntityType.WIND_CHARGE
            )) {
            CombatPlayer enemy = pvPlayer.getEnemy();
            pvPlayer.tag(true, enemy != null ? enemy : pvPlayer);
         }
      }
   }

   @Nullable
   private Player getAttacker(Entity damager) {
      if (damager instanceof Player player) {
         return player;
      } else if (damager instanceof Projectile projectile) {
         return (Player)projectile.getShooter();
      } else {
         return damager instanceof TNTPrimed tnt ? (Player)tnt.getSource() : (Player)((AreaEffectCloud)damager).getSource();
      }
   }
}
