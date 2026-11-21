package com.earth2me.essentials;

import com.earth2me.essentials.craftbukkit.Inventories;
import com.earth2me.essentials.utils.VersionUtil;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.inventory.ItemStack;

public class EssentialsEntityListener implements Listener {
   private static final transient Pattern powertoolPlayer = Pattern.compile("\\{player\\}");
   private final net.ess3.api.IEssentials ess;

   public EssentialsEntityListener(final net.ess3.api.IEssentials ess) {
      this.ess = ess;
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onEntityDamage(final EntityDamageByEntityEvent event) {
      Entity eAttack = event.getDamager();
      Entity eDefend = event.getEntity();
      if (eAttack instanceof Player) {
         User attacker = this.ess.getUser((Player)eAttack);
         if (eDefend instanceof Player) {
            this.onPlayerVsPlayerDamage(event, (Player)eDefend, attacker);
         } else if (eDefend instanceof Ageable) {
            ItemStack hand = Inventories.getItemInMainHand(attacker.getBase());
            if (this.ess.getSettings().isMilkBucketEasterEggEnabled() && hand != null && hand.getType() == Material.MILK_BUCKET) {
               ((Ageable)eDefend).setBaby();
               hand.setType(Material.BUCKET);
               Inventories.setItemInMainHand(attacker.getBase(), hand);
               attacker.getBase().updateInventory();
               event.setCancelled(true);
            }
         }

         attacker.updateActivityOnInteract(true);
      } else if (eAttack instanceof Projectile && eDefend instanceof Player) {
         Projectile projectile = (Projectile)event.getDamager();
         Object shooter = projectile.getShooter();
         if (shooter instanceof Player) {
            User attacker = this.ess.getUser((Player)shooter);
            this.onPlayerVsPlayerDamage(event, (Player)eDefend, attacker);
            attacker.updateActivityOnInteract(true);
         }
      }
   }

   private void onPlayerVsPlayerDamage(final EntityDamageByEntityEvent event, final Player defender, final User attacker) {
      if (this.ess.getSettings().getLoginAttackDelay() > 0L
         && System.currentTimeMillis() < attacker.getLastLogin() + this.ess.getSettings().getLoginAttackDelay()
         && !attacker.isAuthorized("essentials.pvpdelay.exempt")) {
         event.setCancelled(true);
      }

      if (!defender.equals(attacker.getBase()) && (attacker.hasInvulnerabilityAfterTeleport() || this.ess.getUser(defender).hasInvulnerabilityAfterTeleport())) {
         event.setCancelled(true);
      }

      if (attacker.isGodModeEnabled() && !attacker.isAuthorized("essentials.god.pvp")) {
         event.setCancelled(true);
      }

      if (attacker.isHidden() && !attacker.isAuthorized("essentials.vanish.pvp")) {
         event.setCancelled(true);
      }

      if (attacker.arePowerToolsEnabled()) {
         this.onPlayerVsPlayerPowertool(event, defender, attacker);
      }
   }

   private void onPlayerVsPlayerPowertool(final EntityDamageByEntityEvent event, final Player defender, final User attacker) {
      List<String> commandList = attacker.getPowertool(Inventories.getItemInHand(attacker.getBase()));
      if (commandList != null && !commandList.isEmpty()) {
         for (String tempCommand : commandList) {
            final String command = powertoolPlayer.matcher(tempCommand).replaceAll(defender.getName());
            if (command != null && !command.isEmpty() && !command.equals(tempCommand)) {
               class PowerToolInteractTask implements Runnable {
                  @Override
                  public void run() {
                     attacker.getBase().chat("/" + command);
                     EssentialsEntityListener.this.ess
                        .getLogger()
                        .log(Level.INFO, String.format("[PT] %s issued server command: /%s", attacker.getName(), command));
                  }
               }

               this.ess.scheduleSyncDelayedTask(new PowerToolInteractTask());
               event.setCancelled(true);
               return;
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onEntityDamage(final EntityDamageEvent event) {
      if (event.getEntity() instanceof Player && this.ess.getUser((Player)event.getEntity()).isGodModeEnabled()) {
         Player player = (Player)event.getEntity();
         player.setFireTicks(0);
         player.setRemainingAir(player.getMaximumAir());
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onEntityCombust(final EntityCombustEvent event) {
      if (event.getEntity() instanceof Player && this.ess.getUser((Player)event.getEntity()).isGodModeEnabled()) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR
   )
   public void onEntityCombustByEntity(final EntityCombustByEntityEvent event) {
      if (event.getCombuster() instanceof Arrow && event.getEntity() instanceof Player) {
         Arrow combuster = (Arrow)event.getCombuster();
         if (combuster.getShooter() instanceof Player) {
            Player shooter = (Player)combuster.getShooter();
            if (shooter.hasMetadata("NPC")) {
               return;
            }

            User srcCombuster = this.ess.getUser(shooter.getUniqueId());
            if (srcCombuster.isGodModeEnabled() && !srcCombuster.isAuthorized("essentials.god.pvp")) {
               event.setCancelled(true);
            }

            if (srcCombuster.isHidden() && !srcCombuster.isAuthorized("essentials.vanish.pvp")) {
               event.setCancelled(true);
            }
         }
      }
   }

   @EventHandler(
      priority = EventPriority.HIGH,
      ignoreCancelled = true
   )
   public void onPlayerDeathEvent(final PlayerDeathEvent event) {
      Entity entity = event.getEntity();
      if (!entity.hasMetadata("NPC")) {
         User user = this.ess.getUser(event.getEntity());
         if (this.ess.getSettings().infoAfterDeath()) {
            Location loc = user.getLocation();
            user.sendTl("infoAfterDeath", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
         }

         if (user.isAuthorized("essentials.back.ondeath") && !this.ess.getSettings().isCommandDisabled("back")) {
            user.setLastLocation();
            user.sendTl("backAfterDeath");
         }

         if (!this.ess.getSettings().areDeathMessagesEnabled()) {
            event.setDeathMessage("");
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerDeathExpEvent(final PlayerDeathEvent event) {
      User user = this.ess.getUser(event.getEntity());
      if (user.isAuthorized("essentials.keepxp")) {
         event.setKeepLevel(true);
         event.setDroppedExp(0);
      }
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onPlayerDeathInvEvent(final PlayerDeathEvent event) {
      User user = this.ess.getUser(event.getEntity());
      if (user.isAuthorized("essentials.keepinv")) {
         event.setKeepInventory(true);
         ItemStack[] inventory = Inventories.getInventory(event.getEntity(), true);

         for (ItemStack item : inventory) {
            if (item != null) {
               event.getDrops().remove(item);
            }
         }

         ISettings.KeepInvPolicy vanish = this.ess.getSettings().getVanishingItemsPolicy();
         ISettings.KeepInvPolicy bind = this.ess.getSettings().getBindingItemsPolicy();
         if (VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_11_2_R01)
            && (vanish != ISettings.KeepInvPolicy.KEEP || bind != ISettings.KeepInvPolicy.KEEP)) {
            Inventories.removeItems(user.getBase(), stack -> {
               if (vanish != ISettings.KeepInvPolicy.KEEP && stack.getEnchantments().containsKey(Enchantment.VANISHING_CURSE)) {
                  if (vanish == ISettings.KeepInvPolicy.DROP) {
                     event.getDrops().add(stack.clone());
                  }

                  return true;
               } else if (bind != ISettings.KeepInvPolicy.KEEP && stack.getEnchantments().containsKey(Enchantment.BINDING_CURSE)) {
                  if (bind == ISettings.KeepInvPolicy.DROP) {
                     event.getDrops().add(stack.clone());
                  }

                  return true;
               } else {
                  return false;
               }
            }, true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onFoodLevelChange(final FoodLevelChangeEvent event) {
      if (event.getEntity() instanceof Player) {
         User user = this.ess.getUser((Player)event.getEntity());
         if (user.isGodModeEnabled()) {
            if (user.isGodModeEnabledRaw()) {
               user.getBase().setFoodLevel(20);
               user.getBase().setSaturation(10.0F);
            }

            event.setCancelled(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onEntityRegainHealth(final EntityRegainHealthEvent event) {
      if (event.getRegainReason() == RegainReason.SATIATED
         && event.getEntity() instanceof Player
         && this.ess.getUser((Player)event.getEntity()).isAfk()
         && this.ess.getSettings().getFreezeAfkPlayers()) {
         event.setCancelled(true);
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onPotionSplashEvent(final PotionSplashEvent event) {
      for (LivingEntity entity : event.getAffectedEntities()) {
         if (entity instanceof Player && this.ess.getUser((Player)entity).isGodModeEnabled()) {
            event.setIntensity(entity, 0.0);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onEntityShootBow(final EntityShootBowEvent event) {
      if (event.getEntity() instanceof Player) {
         User user = this.ess.getUser((Player)event.getEntity());
         if (user.isAfk()) {
            user.updateActivityOnInteract(true);
         }
      }
   }

   @EventHandler(
      priority = EventPriority.LOW,
      ignoreCancelled = true
   )
   public void onEntityTarget(final EntityTargetEvent event) {
      if (event.getTarget() instanceof Player) {
         User user = this.ess.getUser((Player)event.getTarget());
         if (user.isVanished()) {
            event.setCancelled(true);
         }
      }
   }
}
