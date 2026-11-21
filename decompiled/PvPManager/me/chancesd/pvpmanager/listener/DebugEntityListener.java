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
import me.chancesd.pvpmanager.sdutils.utils.Log;
import me.chancesd.pvpmanager.sdutils.utils.MCVersion;
import me.chancesd.pvpmanager.setting.Conf;
import me.chancesd.pvpmanager.setting.Permissions;
import me.chancesd.pvpmanager.utils.CombatUtils;
import org.bukkit.Effect;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.event.weather.LightningStrikeEvent.Cause;
import org.bukkit.potion.PotionEffectType;

public class DebugEntityListener implements Listener {
   private static final String CONSIDERED_NOT_PVP = "Ignoring damage because it wasn't considered PvP";
   private final PlayerManager ph;
   private final WorldGuardDependency wg;
   private final Cache<LightningStrike, Location> lightningCache = CacheBuilder.newBuilder().expireAfterWrite(2L, TimeUnit.SECONDS).build();

   public DebugEntityListener(PlayerManager ph) {
      this.ph = ph;
      this.wg = (WorldGuardDependency)ph.getPlugin().getDependencyManager().getDependency(Hook.WORLDGUARD);
   }

   private String getEntityNames(EntityDamageByEntityEvent event) {
      return event.getDamager().getName() + " and " + event.getEntity().getName();
   }

   @EventHandler(
      priority = EventPriority.NORMAL,
      ignoreCancelled = true
   )
   public final void onPlayerDamage(EntityDamageByEntityEvent event) {
      Log.debug("- Processing damage between " + this.getEntityNames(event) + ". Cause: " + event.getCause());
      String worldName = event.getEntity().getWorld().getName();
      if (CombatUtils.isWorldExcluded(worldName)) {
         Log.debug("Ignoring damage between " + this.getEntityNames(event) + " because world " + worldName + " is excluded");
      } else if (CombatUtils.isPvP(event)) {
         Player attacker = this.getAttacker(event.getDamager());
         Player attacked = (Player)event.getEntity();
         ProtectionResult result = this.ph.checkProtection(attacker, attacked);
         if (result.isProtected()) {
            event.setCancelled(true);
            Log.debug("Blocking PvP damage. Protection result: " + result);
         } else {
            Log.debug("Allowing PvP because neither player had protection. Protection result: " + result);
         }
      } else if (!(event.getEntity() instanceof Player)) {
         Log.debug("Ignoring damage because it wasn't considered PvP");
      } else {
         CombatPlayer attacked = this.ph.get((Player)event.getEntity());
         if (attacked.isNewbie() && Conf.NEWBIE_GODMODE.asBool()) {
            event.setCancelled(true);
            Log.debug("Blocking damage to newbie because newbie god mode is enabled");
         } else {
            if (event.getDamager() instanceof LightningStrike lightning) {
               if (!this.lightningCache.asMap().containsKey(lightning)) {
                  Log.debug("Ignoring damage because it wasn't considered PvP");
                  return;
               }

               if (!attacked.hasPvPEnabled() || attacked.isNewbie() || attacked.hasRespawnProtection()) {
                  event.setCancelled(true);
                  Log.debug("Blocking lightning damage because it came from a player");
                  return;
               }
            }

            Log.debug("Ignoring damage because it wasn't considered PvP");
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGHEST
   )
   public final void onPlayerDamageOverride(EntityDamageByEntityEvent event) {
      if (CombatUtils.isPvP(event) && !CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName()) && event.isCancelled()) {
         ProtectionResult protectionResult = this.ph.checkProtection(this.getAttacker(event.getDamager()), (Player)event.getEntity());
         if (protectionResult.type() == ProtectionType.FAIL_OVERRIDE) {
            event.setCancelled(false);
            Log.debug("Force allowing PvP even though a plugin blocked it because a player has override or Vulnerable is enabled");
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
         this.onDamageActions(attacker, attacked);
         Log.debug("Finished processing damage.");
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public final void onPlayerDamageMon(EntityDamageByEntityEvent event) {
      if (CombatUtils.isPvP(event) && !CombatUtils.isWorldExcluded(event.getEntity().getWorld().getName()) && event.isCancelled()) {
         ProtectionResult protectionResult = this.ph.checkProtection(this.getAttacker(event.getDamager()), (Player)event.getEntity());
         if (protectionResult.isVulnerable()) {
            Log.debug("Damage was cancelled by another plugin, doing nothing");
         }
      }
   }

   public void onDamageActions(Player attacker, Player defender) {
      CombatPlayer pvpAttacker = this.ph.get(attacker);
      CombatPlayer pvpDefender = this.ph.get(defender);
      if (Conf.PVP_BLOOD.asBool()) {
         defender.getWorld().playEffect(defender.getLocation(), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
      }

      if (!pvpAttacker.hasPerm(Permissions.EXEMPT_DISABLE_ACTIONS)) {
         if (Conf.DISABLE_FLY.asBool()) {
            if (CombatUtils.canFly(attacker)) {
               pvpAttacker.disableFly();
            }

            if (!pvpDefender.hasPerm(Permissions.EXEMPT_DISABLE_ACTIONS) && CombatUtils.canFly(defender)) {
               pvpDefender.disableFly();
            }
         }

         if (Conf.DISABLE_GAMEMODE.asBool() && !attacker.getGameMode().equals(GameMode.SURVIVAL)) {
            attacker.setGameMode(GameMode.SURVIVAL);
         }

         if (Conf.DISABLE_DISGUISE.asBool()) {
            this.ph.getPlugin().getDependencyManager().disableDisguise(attacker);
         }

         if (Conf.DISABLE_INVISIBILITY.asBool() && attacker.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            attacker.removePotionEffect(PotionEffectType.INVISIBILITY);
         }

         if (Conf.DISABLE_GODMODE.asBool()) {
            this.ph.getPlugin().getDependencyManager().disableGodMode(attacker);
         }
      } else {
         Log.debug("Didn't disable fly/gamemode/etc because attacker has permission " + Permissions.EXEMPT_DISABLE_ACTIONS);
      }

      if (Conf.COMBAT_TAG_ENABLED.asBool()) {
         if (Conf.VULNERABLE_ENABLED.asBool()
            && this.wg != null
            && !Conf.VULNERABLE_RENEW_TAG.asBool()
            && this.wg.hasDenyPvPFlag(attacker)
            && this.wg.hasDenyPvPFlag(defender)) {
            return;
         }

         Log.debug("Tagging players " + pvpAttacker.getName() + " and " + pvpDefender.getName());
         pvpAttacker.tag(true, pvpDefender);
         pvpDefender.tag(false, pvpAttacker);
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
