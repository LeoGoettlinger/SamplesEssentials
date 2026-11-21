package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.advancedcombat.abilities.ClassAbilityProjectileCarrier;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.combatsystem.DamageBreakdown;
import com.magmaguy.elitemobs.combatsystem.LevelScaling;
import com.magmaguy.elitemobs.combatsystem.NaturalEliteCombatTweak;
import com.magmaguy.elitemobs.combatsystem.PlayerAttackCooldownTracker;
import com.magmaguy.elitemobs.combatsystem.PotionCombatModifierCalculator;
import com.magmaguy.elitemobs.combatsystem.WeaponOffenseCalculator;
import com.magmaguy.elitemobs.combatsystem.antiexploit.AutoclickerThrottle;
import com.magmaguy.elitemobs.config.ItemSettingsConfig;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.dungeons.EliteMobsWorld;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.magmacore.enchantments.EnchantmentInputs;
import com.magmaguy.elitemobs.magmacore.util.Round;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.utils.DebugMessage;
import com.magmaguy.elitemobs.utils.EntityFinder;
import com.magmaguy.elitemobs.utils.EventCaller;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Trident;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.entity.EnderDragon.Phase;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.inventory.ItemStack;

public final class EliteMobDamagedByPlayerEventFilter implements Listener {
   private static SkillType getWeaponSkillType(Player player) {
      return EliteMobDamagedByPlayerEvent.getWeaponSkillType(player);
   }

   public static void applyModeledProjectileHit(Player player, EliteEntity eliteEntity, Projectile projectile, double rawDamage) {
      if (player != null && eliteEntity != null && projectile != null) {
         LivingEntity target = eliteEntity.getLivingEntity();
         if (target != null && target.isValid()) {
            EntityDamageByEntityEvent syntheticEvent = new EntityDamageByEntityEvent(
               projectile, target, DamageCause.PROJECTILE, DamageSource.builder(DamageType.MOB_ATTACK).build(), rawDamage
            );
            new EventCaller(syntheticEvent);
            if (!syntheticEvent.isCancelled()) {
               double finalDamage = Math.max(0.0, syntheticEvent.getDamage());
               if (!(finalDamage <= 0.0) && eliteEntity.isValid() && !target.isDead()) {
                  double newHealth = target.getHealth() - finalDamage;
                  target.setLastDamageCause(syntheticEvent);
                  if (newHealth <= 0.0) {
                     eliteEntity.syncPluginHealth(0.0);
                     target.setHealth(0.0);
                  } else {
                     eliteEntity.setHealth(newHealth);
                  }
               }
            }
         }
      }
   }

   private static int getEliteThornsLevel(Player player) {
      if (!ItemSettingsConfig.isUseEliteEnchantments()) {
         return 0;
      } else {
         ElitePlayerInventory elitePlayerInventory = ElitePlayerInventory.getPlayer(player);
         int thornsLevel = 0;
         if (elitePlayerInventory.helmet.thornsLevel > Enchantment.THORNS.getMaxLevel()) {
            thornsLevel += elitePlayerInventory.helmet.thornsLevel - Enchantment.THORNS.getMaxLevel();
         }

         if (elitePlayerInventory.chestplate.thornsLevel > Enchantment.THORNS.getMaxLevel()) {
            thornsLevel += elitePlayerInventory.chestplate.thornsLevel - Enchantment.THORNS.getMaxLevel();
         }

         if (elitePlayerInventory.leggings.thornsLevel > Enchantment.THORNS.getMaxLevel()) {
            thornsLevel += elitePlayerInventory.leggings.thornsLevel - Enchantment.THORNS.getMaxLevel();
         }

         if (elitePlayerInventory.boots.thornsLevel > Enchantment.THORNS.getMaxLevel()) {
            thornsLevel += elitePlayerInventory.boots.thornsLevel - Enchantment.THORNS.getMaxLevel();
         }

         return thornsLevel;
      }
   }

   private static double getSecondaryEnchantmentMultiplier(Player player, LivingEntity livingEntity) {
      if (!ItemSettingsConfig.isUseEliteEnchantments()) {
         return 1.0;
      } else if (!(livingEntity instanceof Spider) && !(livingEntity instanceof Silverfish)) {
         if (!(livingEntity instanceof Zombie)
            && !(livingEntity instanceof Skeleton)
            && !(livingEntity instanceof Wither)
            && !(livingEntity instanceof SkeletonHorse)
            && !(livingEntity instanceof ZombieHorse)
            && !livingEntity.getType().equals(EntityType.ZOMBIFIED_PIGLIN)) {
            return 1.0;
         } else {
            int level = ElitePlayerInventory.playerInventories.get(player.getUniqueId())
               .mainhand
               .getDamageUndeadLevel(player.getInventory().getItemInMainHand(), false);
            level -= Enchantment.SMITE.getMaxLevel();
            return level < 1 ? 1.0 : 1.0 + level * 0.025;
         }
      } else {
         int level = ElitePlayerInventory.playerInventories.get(player.getUniqueId())
            .mainhand
            .getDamageArthropodsLevel(player.getInventory().getItemInMainHand(), false);
         level -= Enchantment.BANE_OF_ARTHROPODS.getMaxLevel();
         return level < 1 ? 1.0 : 1.0 + level * 0.025;
      }
   }

   private static double getCooldownAttackSpeed(Player player, double itemAttackSpeed, boolean isRangedWeaponMelee) {
      if (isRangedWeaponMelee) {
         return 4.0;
      } else {
         AttributeInstance attackSpeedAttribute = player.getAttribute(Attribute.ATTACK_SPEED);
         if (attackSpeedAttribute == null) {
            return itemAttackSpeed;
         } else {
            double attributeAttackSpeed = attackSpeedAttribute.getValue();
            if (!Double.isFinite(attributeAttackSpeed) || attributeAttackSpeed <= 0.0) {
               return itemAttackSpeed;
            } else {
               return itemAttackSpeed < 4.0 && attributeAttackSpeed == 4.0 ? itemAttackSpeed : attributeAttackSpeed;
            }
         }
      }
   }

   private static double getCustomDamageModifier(EliteEntity eliteEntity, Material itemStackType) {
      return !(eliteEntity instanceof CustomBossEntity) ? 1.0 : ((CustomBossEntity)eliteEntity).getDamageModifier(itemStackType);
   }

   public static boolean captureCriticalHit(Player player) {
      ElitePlayerInventory inventory = ElitePlayerInventory.getPlayer(player);
      double criticalStrike = inventory == null ? 0.0 : inventory.getCritChance(true);
      return ThreadLocalRandom.current().nextDouble() < criticalStrike;
   }

   private static boolean isCriticalHit(Player player, EntityDamageByEntityEvent event) {
      CombatDamageContext.PlayerDamageSource captured = CombatDamageContext.currentPlayerToEliteSource().orElse(null);
      if (captured != null && captured.criticalHit() != null) {
         return captured.criticalHit();
      } else {
         return event.getDamager() instanceof Projectile projectile ? ItemTagger.getArrowCritical(projectile) : captureCriticalHit(player);
      }
   }

   private static int getPlayerWeaponSkillLevel(Player player) {
      if (SkillsConfig.isWorldExcludedFromSkills(player)) {
         return 1;
      } else {
         SkillType weaponSkillType = getWeaponSkillType(player);
         if (weaponSkillType == null) {
            return 1;
         } else {
            long skillXP = PlayerData.getSkillXP(player.getUniqueId(), weaponSkillType);
            return Math.max(1, SkillXPCalculator.levelFromTotalXP(skillXP));
         }
      }
   }

   private static double playerToEliteDamageFormula(Player player, EliteEntity eliteEntity, EntityDamageByEntityEvent event) {
      int mobLevel = eliteEntity.getLevel();
      ItemStack weapon = player.getInventory().getItemInMainHand();
      boolean isRanged = event.getCause() == DamageCause.PROJECTILE;
      boolean isSweep = event.getCause() == DamageCause.ENTITY_SWEEP_ATTACK;
      boolean skillsExcluded = SkillsConfig.isWorldExcludedFromSkills(player);
      boolean isRangedWeaponMelee = false;
      if (!isRanged && weapon != null) {
         Material weaponType = weapon.getType();
         if (weaponType == Material.BOW || weaponType == Material.CROSSBOW) {
            isRangedWeaponMelee = true;
         }
      }

      double baseDamage = NaturalEliteCombatTweak.getTweakedBaseDamageToElite(eliteEntity, mobLevel);
      double attackSpeed = isRangedWeaponMelee ? 4.0 : EliteItemManager.getAttackSpeed(weapon);
      double cooldownAttackSpeed = getCooldownAttackSpeed(player, attackSpeed, isRangedWeaponMelee);
      double attackSpeedFactor = 1.0;
      if (!isRanged) {
         attackSpeedFactor = WeaponOffenseCalculator.getAttackSpeedFactor(weapon, attackSpeed);
      }

      double weaponLevel;
      int weaponSkillLevel;
      if (isRanged) {
         Projectile projectile = (Projectile)event.getDamager();
         if (projectile instanceof Trident trident) {
            weaponLevel = WeaponOffenseCalculator.getEffectiveWeaponLevel(trident.getItem());
         } else {
            double storedLevel = ItemTagger.getArrowWeaponLevel(projectile);
            weaponLevel = storedLevel >= 0.0 ? storedLevel : 0.0;
         }

         Projectile proj = (Projectile)event.getDamager();
         int storedSkillLevel = ItemTagger.getArrowSkillLevel(proj);
         weaponSkillLevel = storedSkillLevel >= 0 ? storedSkillLevel : 1;
      } else if (isRangedWeaponMelee) {
         weaponLevel = 0.0;
         weaponSkillLevel = 1;
      } else {
         weaponLevel = WeaponOffenseCalculator.getEffectiveWeaponLevel(weapon);
         weaponSkillLevel = getPlayerWeaponSkillLevel(player);
      }

      if (skillsExcluded) {
         weaponSkillLevel = 1;
      }

      double skillAdjustment = LevelScaling.calculateOffensiveSkillAdjustment(weaponSkillLevel, mobLevel);
      double weaponAdjustment = WeaponOffenseCalculator.getWeaponAdjustment(weaponLevel, mobLevel);
      long ticksSinceLastHit = -1L;
      double cooldownOrVelocity;
      if (isRanged) {
         Projectile rangedProjectile = (Projectile)event.getDamager();
         double launchVelocity = ItemTagger.getArrowLaunchVelocity(rangedProjectile);
         if (launchVelocity >= 0.0) {
            cooldownOrVelocity = WeaponOffenseCalculator.normalizeArrowVelocity(launchVelocity);
         } else {
            cooldownOrVelocity = WeaponOffenseCalculator.normalizeArrowVelocity(rangedProjectile);
         }
      } else if (event.getCause() == DamageCause.ENTITY_SWEEP_ATTACK) {
         cooldownOrVelocity = 1.0;
      } else {
         ticksSinceLastHit = PlayerAttackCooldownTracker.recordHit(player);
         double rechargeTicks = cooldownAttackSpeed > 0.0 ? 20.0 / cooldownAttackSpeed : 20.0;
         cooldownOrVelocity = ticksSinceLastHit == -1L ? 1.0 : Math.min(ticksSinceLastHit / rechargeTicks, 1.0);
      }

      double sweepMultiplier = WeaponOffenseCalculator.getSweepMultiplier(event);
      double potionMultiplier = PotionCombatModifierCalculator.getOutgoingDamageMultiplier(player);
      ElitePlayerInventory elitePlayerInventory = ElitePlayerInventory.getPlayer(player);
      double equipmentEnchantmentBonus = elitePlayerInventory != null ? elitePlayerInventory.getEliteEnchantmentDamage(true) : 0.0;
      double equipmentEnchantmentMultiplier = 1.0 + equipmentEnchantmentBonus;
      LivingEntity target = eliteEntity.getLivingEntity();
      double enchantmentMultiplier = target != null ? getSecondaryEnchantmentMultiplier(player, target) : 1.0;
      double arrowDamageMultiplier = 1.0;
      if (isRanged) {
         double storedMultiplier = ItemTagger.getArrowDamageMultiplier((Projectile)event.getDamager());
         if (storedMultiplier > 0.0) {
            arrowDamageMultiplier = storedMultiplier;
         }
      }

      double formulaDamage = baseDamage
         * attackSpeedFactor
         * skillAdjustment
         * weaponAdjustment
         * cooldownOrVelocity
         * sweepMultiplier
         * potionMultiplier
         * equipmentEnchantmentMultiplier
         * enchantmentMultiplier
         * arrowDamageMultiplier;
      DamageBreakdown breakdown = DamageBreakdown.getActiveBreakdown(player);
      if (breakdown != null) {
         breakdown.setBaseDamage(baseDamage);
         breakdown.setAttackSpeedFactor(attackSpeedFactor);
         breakdown.setSkillAdjustment(skillAdjustment);
         breakdown.setWeaponAdjustment(weaponAdjustment);
         breakdown.setCooldownOrVelocity(cooldownOrVelocity);
         breakdown.setSweepMultiplier(sweepMultiplier);
         breakdown.setPotionMultiplier(potionMultiplier);
         breakdown.setEquipmentEnchantmentMultiplier(equipmentEnchantmentMultiplier);
         breakdown.setEnchantmentMultiplier(enchantmentMultiplier);
         breakdown.setArrowDamageMultiplier(arrowDamageMultiplier);
         breakdown.setPlayerSkillLevel(weaponSkillLevel);
         breakdown.setItemLevel((int)weaponLevel);
         breakdown.setEliteLevel(mobLevel);
         breakdown.setWeaponType(weapon.getType().name());
         breakdown.setRangedAttack(isRanged);
         breakdown.setSweepAttack(isSweep);
      }

      if (DebugMessage.isDebugEnabled(player)) {
         DebugMessage.log(
            player,
            "[Formula] Base="
               + String.format("%.1f", baseDamage)
               + " Speed="
               + String.format("%.2f", attackSpeedFactor)
               + " Skill="
               + String.format("%.3f", skillAdjustment)
               + " (Lv"
               + weaponSkillLevel
               + " vs "
               + mobLevel
               + ") Wpn="
               + String.format("%.2f", weaponAdjustment)
               + " (Lv"
               + (int)weaponLevel
               + ") CD="
               + String.format("%.2f", cooldownOrVelocity)
               + (ticksSinceLastHit != -1L ? " Δticks=" + ticksSinceLastHit : "")
               + (potionMultiplier != 1.0 ? " Pot=" + String.format("%.2f", potionMultiplier) : "")
               + (isSweep ? " Sweep=" + String.format("%.2f", sweepMultiplier) : "")
               + (equipmentEnchantmentMultiplier != 1.0 ? " EquipEnchant=" + String.format("%.2f", equipmentEnchantmentMultiplier) + "x" : "")
               + (arrowDamageMultiplier != 1.0 ? " ArrowMult=" + String.format("%.2f", arrowDamageMultiplier) : "")
               + " = "
               + String.format("%.1f", formulaDamage)
         );
         DebugMessage.send(player, "§e── Formula (playerToEliteDamageFormula) ──");
         DebugMessage.send(
            player,
            "§7Inputs: weaponSkillLv=§f"
               + weaponSkillLevel
               + " §7weaponLv=§f"
               + (int)weaponLevel
               + " §7mobLv-in-formula=§f"
               + mobLevel
               + " §8(mob level is overridden by player skill level when scaled combat is on)"
         );
         DebugMessage.send(player, "§7Base = mobHP_at_this_level / TARGET_HITS_TO_KILL_MOB (" + String.format("%.1f", 3.0) + ")");
         DebugMessage.send(
            player,
            "§7   = §f" + String.format("%.2f", baseDamage) + " §8(damage needed per hit to kill in TARGET_HITS hits; uses legacy or recommended HP curve)"
         );
         DebugMessage.send(
            player,
            "§7× Attack speed factor = tuned melee pacing curve = §f" + String.format("%.3f", attackSpeedFactor) + " §8(1.0 for ranged; family+speed adjusted)"
         );
         if (!isRanged && cooldownAttackSpeed != attackSpeed) {
            DebugMessage.send(
               player, "§7  ↳ cooldown speed = §f" + String.format("%.3f", cooldownAttackSpeed) + " §8(player ATTACK_SPEED attribute used for recharge timing)"
            );
         }

         DebugMessage.send(
            player,
            "§7× Skill adjustment = 2^((skillLv − mobLv)/"
               + String.format("%.1f", 7.5)
               + ") = §f"
               + String.format("%.3f", skillAdjustment)
               + " §8(>1 if you out-skill the mob)"
         );
         DebugMessage.send(
            player,
            "§7× Weapon adjustment = §f"
               + String.format("%.3f", weaponAdjustment)
               + " §8(piecewise linear from weapon vs mob level; 0.5 under, 1.0 matched, 1.25 over)"
         );
         DebugMessage.send(
            player,
            "§7× "
               + (isRanged ? "Arrow velocity" : "Attack cooldown")
               + " = §f"
               + String.format("%.3f", cooldownOrVelocity)
               + " §8(0..1; partial swings/slow arrows scale damage down)"
         );
         if (event.getCause() == DamageCause.ENTITY_ATTACK) {
            String gapDisplay = ticksSinceLastHit == -1L
               ? "first hit (no prior swing tracked)"
               : ticksSinceLastHit + " ticks (" + String.format("%.2f", ticksSinceLastHit / 20.0) + "s)";
            DebugMessage.send(
               player,
               "§7  ↳ ticks since last melee hit by this player = §f"
                  + gapDisplay
                  + " §8(the attack charge above is now derived from this real-time gap ÷ the weapon's recharge window, NOT Bukkit getAttackCooldown())"
            );
         }

         DebugMessage.send(
            player, "§7× Sweep multiplier = §f" + String.format("%.3f", sweepMultiplier) + " §8(SWEEP_DAMAGE_FRACTION for sweep secondaries, 1.0 otherwise)"
         );
         DebugMessage.send(player, "§7× Potion multiplier (outgoing) = §f" + String.format("%.3f", potionMultiplier) + " §8(strength/weakness on you)");
         DebugMessage.send(
            player,
            "§7× Equipment enchantment multiplier = §f"
               + String.format("%.3f", equipmentEnchantmentMultiplier)
               + " §8(Sharpness/Power from all equipped slots; +"
               + String.format("%.1f", equipmentEnchantmentBonus * 100.0)
               + "% damage)"
         );
         DebugMessage.send(
            player, "§7× Enchantment multiplier = §f" + String.format("%.3f", enchantmentMultiplier) + " §8(Smite/Bane elite-only levels above vanilla max)"
         );
         DebugMessage.send(
            player,
            "§7× Arrow damage multiplier = §f"
               + String.format("%.3f", arrowDamageMultiplier)
               + " §8(set by Multishot/Arrow Rain skills on extra arrows; 1.0 for normal shots)"
         );
         DebugMessage.send(player, "§7= formulaDamage (BEFORE outer combat/config multiplier and crit) = §f" + String.format("%.2f", formulaDamage));
      }

      return formulaDamage;
   }

   private static double scaledPlayerToEliteDamage(Player player, EliteEntity eliteEntity, EntityDamageByEntityEvent event) {
      boolean projectileAttack = event.getCause() == DamageCause.PROJECTILE;
      boolean isRangedMelee = !projectileAttack;
      if (!projectileAttack) {
         ItemStack mainHand = player.getInventory().getItemInMainHand();
         if (mainHand != null) {
            Material type = mainHand.getType();
            isRangedMelee = type == Material.BOW || type == Material.CROSSBOW;
         } else {
            isRangedMelee = false;
         }
      }

      int simulatedMobLevel = projectileAttack ? 1 : (isRangedMelee ? 1 : getPlayerWeaponSkillLevel(player));
      if (simulatedMobLevel <= 0) {
         simulatedMobLevel = 1;
      }

      if (!SkillsConfig.isWorldExcludedFromSkills(player) && event.getCause() == DamageCause.PROJECTILE && event.getDamager() instanceof Projectile proj) {
         int storedSkillLevel = ItemTagger.getArrowSkillLevel(proj);
         if (storedSkillLevel > 0) {
            simulatedMobLevel = storedSkillLevel;
         }
      }

      int realMobLevel = eliteEntity.getLevel();
      eliteEntity.setLevel(simulatedMobLevel);

      double formulaDamage;
      try {
         formulaDamage = playerToEliteDamageFormula(player, eliteEntity, event);
      } finally {
         eliteEntity.setLevel(realMobLevel);
      }

      double simulatedMobHP = NaturalEliteCombatTweak.getTweakedMobHealthForLevel(eliteEntity, simulatedMobLevel, eliteEntity.getHealthMultiplier());
      double actualBossMaxHP = eliteEntity.getMaxHealth();
      double damagePercentage = formulaDamage / simulatedMobHP;
      double rescaledDamage = damagePercentage * actualBossMaxHP;
      if (DebugMessage.isDebugEnabled(player)) {
         DebugMessage.send(player, "§e── Scaled-combat rescale ──");
         DebugMessage.send(
            player,
            "§7Real mob level (restored): §f"
               + realMobLevel
               + " §7  Simulated mob level used in formula: §f"
               + simulatedMobLevel
               + " §8(player's weapon skill level; 1 for ranged-in-melee)"
         );
         DebugMessage.send(player, "§7formulaDamage @ simulated level = §f" + String.format("%.2f", formulaDamage));
         DebugMessage.send(
            player,
            "§7Simulated mob HP @ simulated level = §f"
               + String.format("%.2f", simulatedMobHP)
               + " §8(NaturalEliteCombatTweak.getTweakedMobHealthForLevel × healthMultiplier=§7"
               + String.format("%.2f", eliteEntity.getHealthMultiplier())
               + "§8)"
         );
         DebugMessage.send(player, "§7Actual boss max HP = §f" + String.format("%.2f", actualBossMaxHP));
         DebugMessage.send(
            player,
            "§7damagePercentage = formulaDamage / simulatedMobHP = §f" + String.format("%.4f", damagePercentage) + " §8(% of simulated mob HP this hit took)"
         );
         DebugMessage.send(
            player,
            "§7rescaledDamage = damagePercentage × actualBossMaxHP = §f"
               + String.format("%.2f", rescaledDamage)
               + " §8(equivalent % of REAL mob HP; combat feels level-agnostic)"
         );
      }

      return rescaledDamage;
   }

   private static double calculateThornsDamage(Player player, EliteEntity eliteEntity) {
      int thornsLevel = getEliteThornsLevel(player);
      if (thornsLevel <= 0) {
         return 0.0;
      } else {
         double baseDamage = NaturalEliteCombatTweak.getTweakedBaseDamageToElite(eliteEntity, eliteEntity.getLevel());
         return baseDamage * thornsLevel * 0.02;
      }
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void onPlayerShootBow(EntityShootBowEvent event) {
      if (event.getEntity() instanceof Player player) {
         if (event.getProjectile() instanceof Projectile projectile) {
            snapshotProjectileCombatData(player, projectile, event.getBow());
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onPlayerShootArrow(ProjectileLaunchEvent event) {
      if (!ClassAbilityProjectileCarrier.isCarrier(event.getEntity())) {
         if (event.getEntity().getShooter() instanceof Player player) {
            Projectile projectile = event.getEntity();
            ItemTagger.setArrowLaunchVelocity(projectile, projectile.getVelocity().length());
            if (!(ItemTagger.getArrowWeaponLevel(projectile) >= 0.0)) {
               if (projectile instanceof Trident trident) {
                  snapshotProjectileCombatData(player, projectile, trident.getItem());
               } else {
                  ItemStack heldRangedWeapon = unambiguousHeldRangedWeapon(player);
                  if (heldRangedWeapon != null) {
                     snapshotProjectileCombatData(player, projectile, heldRangedWeapon);
                  }
               }
            }
         }
      }
   }

   private static void snapshotProjectileCombatData(Player player, Projectile projectile, ItemStack weapon) {
      EliteItemManager.tagArrow(projectile, weapon);
      ItemTagger.setArrowWeaponMaterial(projectile, weapon);
      double weaponLevel = WeaponOffenseCalculator.getEffectiveWeaponLevel(weapon);
      ItemTagger.setArrowWeaponLevel(projectile, weaponLevel);
      if (!ItemTagger.hasArrowEnchantmentCombat(projectile)) {
         ElitePlayerInventory inventory = ElitePlayerInventory.getPlayer(player);
         ItemTagger.setArrowEnchantmentCombat(projectile, captureCriticalHit(player), inventory == null ? 0.0 : inventory.getLoudStrikesBonusMultiplier(true));
      }

      SkillType skillType = WeaponIdentityResolver.progressionSkill(weapon);
      if (skillType != null) {
         ItemTagger.setArrowSkillType(projectile, skillType.name());
         long skillXP = SkillsConfig.isWorldExcludedFromSkills(player) ? 0L : PlayerData.getSkillXP(player.getUniqueId(), skillType);
         int skillLevel = SkillsConfig.isWorldExcludedFromSkills(player) ? 1 : Math.max(1, SkillXPCalculator.levelFromTotalXP(skillXP));
         ItemTagger.setArrowSkillLevel(projectile, skillLevel);
      }
   }

   private static ItemStack unambiguousHeldRangedWeapon(Player player) {
      ItemStack mainHand = player.getInventory().getItemInMainHand();
      ItemStack offHand = player.getInventory().getItemInOffHand();
      boolean mainRanged = isBowOrCrossbow(mainHand);
      boolean offRanged = isBowOrCrossbow(offHand);
      if (mainRanged == offRanged) {
         return null;
      } else {
         return mainRanged ? mainHand : offHand;
      }
   }

   private static boolean isBowOrCrossbow(ItemStack item) {
      SkillType skillType = WeaponIdentityResolver.progressionSkill(item);
      return skillType == SkillType.BOWS || skillType == SkillType.CROSSBOWS;
   }

   @EventHandler(
      priority = EventPriority.MONITOR,
      ignoreCancelled = true
   )
   public void trackNonEliteMeleeAttack(EntityDamageByEntityEvent event) {
      if (event.getCause() == DamageCause.ENTITY_ATTACK) {
         if (event.getDamager() instanceof Player player) {
            if (EntityTracker.getEliteMobEntity(event.getEntity()) == null) {
               PlayerAttackCooldownTracker.recordHit(player);
            }
         }
      }
   }

   @EventHandler(
      ignoreCancelled = true
   )
   public void onEliteMobAttacked(EntityDamageByEntityEvent event) {
      CombatDamageContext.DamageOverride damageOverride = CombatDamageContext.consumePlayerToElite();
      boolean bypass = damageOverride.bypass();
      if (bypass || CombatDamageContext.isPlayerToEliteBypassActive()) {
         EnchantmentInputs.markExplicitDamage(MetadataHandler.PLUGIN, event);
      }

      if (!event.getEntity().getType().equals(EntityType.ENDER_DRAGON) || !((EnderDragon)event.getEntity()).getPhase().equals(Phase.DYING)) {
         LivingEntity livingEntity = EntityFinder.filterRangedDamagers(event.getDamager());
         if (livingEntity != null) {
            if (livingEntity.getType().equals(EntityType.PLAYER)) {
               Player player = (Player)livingEntity;
               EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(event.getEntity());
               if (eliteEntity != null && eliteEntity.isValid()) {
                  if (!event.getCause().equals(DamageCause.PROJECTILE) || event.getDamager() instanceof Projectile) {
                     if (event.getCause() == DamageCause.ENTITY_ATTACK && !bypass && AutoclickerThrottle.shouldBlockHit(player)) {
                        event.setDamage(0.0);
                        event.setCancelled(true);
                     } else {
                        for (DamageModifier modifier : DamageModifier.values()) {
                           if (event.isApplicable(modifier) && modifier != DamageModifier.BASE) {
                              event.setDamage(modifier, 0.0);
                           }
                        }

                        boolean validPlayer = !player.hasMetadata("NPC") && ElitePlayerInventory.getPlayer(player) != null;
                        DamageBreakdown breakdown = validPlayer ? DamageBreakdown.getActiveBreakdown(player) : null;
                        double damage;
                        if (!validPlayer) {
                           damage = event.getDamage();
                        } else if (bypass) {
                           damage = event.getOriginalDamage(DamageModifier.BASE);
                        } else if (event.getCause().equals(DamageCause.THORNS)) {
                           if (eliteEntity.isScaledCombat()) {
                              int simulatedLevel = getPlayerWeaponSkillLevel(player);
                              if (simulatedLevel <= 0) {
                                 simulatedLevel = 1;
                              }

                              double baseDamage = NaturalEliteCombatTweak.getTweakedBaseDamageToElite(eliteEntity, simulatedLevel);
                              double thornsDamage = baseDamage * getEliteThornsLevel(player) * 0.02;
                              double simulatedMobHP = NaturalEliteCombatTweak.getTweakedMobHealthForLevel(
                                 eliteEntity, simulatedLevel, eliteEntity.getHealthMultiplier()
                              );
                              double actualBossMaxHP = eliteEntity.getMaxHealth();
                              damage = thornsDamage * (actualBossMaxHP / simulatedMobHP);
                           } else {
                              damage = calculateThornsDamage(player, eliteEntity);
                           }

                           if (breakdown != null) {
                              breakdown.setThornsDamage(damage);
                              breakdown.setThornsAttack(true);
                              breakdown.setEliteLevel(eliteEntity.getLevel());
                           }
                        } else if (!event.getCause().equals(DamageCause.ENTITY_ATTACK)
                           && !event.getCause().equals(DamageCause.ENTITY_SWEEP_ATTACK)
                           && !event.getCause().equals(DamageCause.PROJECTILE)) {
                           damage = event.getDamage();
                        } else if (eliteEntity.isScaledCombat()) {
                           damage = scaledPlayerToEliteDamage(player, eliteEntity, event);
                        } else {
                           damage = playerToEliteDamageFormula(player, eliteEntity, event);
                        }

                        double damageModifier = 1.0;
                        if (!bypass) {
                           if (event.getCause().equals(DamageCause.PROJECTILE)) {
                              damageModifier = getCustomDamageModifier(
                                 eliteEntity, event.getDamager() instanceof Projectile projectile ? ItemTagger.getArrowWeaponMaterial(projectile) : null
                              );
                           } else {
                              damageModifier = getCustomDamageModifier(eliteEntity, player.getInventory().getItemInMainHand().getType());
                           }
                        }

                        double combatMultiplier = 1.0;
                        if (!bypass) {
                           if (eliteEntity.isScaledCombat()) {
                              combatMultiplier = MobCombatSettingsConfig.getScaledDamageToEliteMultiplier();
                           } else if (eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.isNormalizedCombat()) {
                              combatMultiplier = MobCombatSettingsConfig.getNormalizedDamageToEliteMultiplier();
                           } else {
                              combatMultiplier = MobCombatSettingsConfig.getDamageToEliteMultiplier();
                           }
                        }

                        damage = Round.twoDecimalPlaces(damage * damageModifier * combatMultiplier);
                        if (breakdown != null) {
                           breakdown.setDamageModifier(damageModifier);
                           breakdown.setCombatMultiplier(combatMultiplier);
                        }

                        double damageAfterConfigMultipliers = damage;
                        boolean guaranteedClassCritical = !CombatDamageContext.isDamageTransferActive()
                           && CombatDamageContext.currentClassAbilityDamageDomain()
                              .map(domain -> domain.strikeQuality() == CombatDamageContext.ClassAbilityStrikeQuality.GUARANTEED_CRITICAL)
                              .orElse(false);
                        boolean criticalHit = false;
                        boolean capturedWeaponCritical = !CombatDamageContext.isDamageTransferActive()
                           && CombatDamageContext.currentPlayerToEliteSource().map(source -> source.criticalHit() != null).orElse(false);
                        if (validPlayer && (!bypass || guaranteedClassCritical || capturedWeaponCritical)) {
                           criticalHit = guaranteedClassCritical || isCriticalHit(player, event);
                           if (criticalHit) {
                              damage *= 1.5;
                              if (breakdown != null) {
                                 breakdown.setCriticalHit(true);
                                 breakdown.setCritMultiplier(1.5);
                              }
                           }

                           if (DebugMessage.isDebugEnabled(player)) {
                              DebugMessage.log(
                                 player,
                                 "[Combat] SkillLv"
                                    + getPlayerWeaponSkillLevel(player)
                                    + " vs EliteLv"
                                    + eliteEntity.getLevel()
                                    + " | Damage: "
                                    + String.format("%.1f", damage)
                                    + " | Elite HP: "
                                    + String.format("%.1f", eliteEntity.getHealth())
                                    + (criticalHit ? " | CRIT" : "")
                              );
                           }
                        }

                        if (DebugMessage.isDebugEnabled(player)) {
                           String combatPath;
                           String configKey;
                           if (bypass) {
                              combatPath = "CUSTOM/BYPASS";
                              configKey = "none";
                           } else if (eliteEntity.isScaledCombat()) {
                              combatPath = "SCALED";
                              configKey = "scaledDamageToEliteMultiplier";
                           } else if (eliteEntity instanceof CustomBossEntity cbForLog && cbForLog.isNormalizedCombat()) {
                              combatPath = "NORMALIZED";
                              configKey = "normalizedDamageToEliteMultiplier";
                           } else {
                              combatPath = "DEFAULT (V2)";
                              configKey = "damageToEliteMobMultiplierV2";
                           }

                           boolean normalizedFlag = eliteEntity instanceof CustomBossEntity cbForFlag && cbForFlag.isNormalizedCombat();
                           String mobName = eliteEntity.getLivingEntity() != null ? eliteEntity.getLivingEntity().getType().name() : "?";
                           String entityClass = eliteEntity.getClass().getSimpleName();
                           DebugMessage.send(player, "§6═════ EM DAMAGE: YOU → ELITE ═════");
                           DebugMessage.send(
                              player, "§7Target: §f" + mobName + " §7Lv§f" + eliteEntity.getLevel() + " §8(EliteEntity class: §7" + entityClass + "§8)"
                           );
                           DebugMessage.send(
                              player,
                              "§7Classification: isNaturalEntity=§f"
                                 + eliteEntity.isNaturalEntity()
                                 + " §7isScaledCombat=§f"
                                 + eliteEntity.isScaledCombat()
                                 + " §7isNormalizedCombat=§f"
                                 + normalizedFlag
                           );
                           DebugMessage.send(
                              player,
                              "§7Per-mob damageMultiplier=§f"
                                 + String.format("%.3f", eliteEntity.getDamageMultiplier())
                                 + " §7healthMultiplier=§f"
                                 + String.format("%.3f", eliteEntity.getHealthMultiplier())
                                 + " §8(per-boss config; defaults 1.0 for natural elites)"
                           );
                           DebugMessage.send(player, "§7Combat path: §e" + combatPath + " §8→ pulls config key §f" + configKey);
                           DebugMessage.send(player, "§7Cause: §f" + event.getCause() + " §7validPlayer=§f" + validPlayer + " §7bypass=§f" + bypass);
                           DebugMessage.send(player, "§e── Outer-handler multipliers ──");
                           DebugMessage.send(
                              player,
                              "§7Raw formula damage = §f"
                                 + String.format("%.2f", damage)
                                 + " §8(output of "
                                 + (eliteEntity.isScaledCombat() ? "scaledPlayerToEliteDamage" : "playerToEliteDamageFormula")
                                 + " above)"
                           );
                           DebugMessage.send(
                              player,
                              "§7× damageModifier = §f" + String.format("%.3f", damageModifier) + " §8(weak/resist material lookup from per-boss config)"
                           );
                           DebugMessage.send(
                              player, "§7× combatMultiplier = §f" + String.format("%.3f", combatMultiplier) + " §8(" + configKey + " in MobCombatSettings.yml)"
                           );
                           DebugMessage.send(player, "§7= " + String.format("%.2f", damageAfterConfigMultipliers));
                           if (criticalHit) {
                              DebugMessage.send(player, "§e× Crit (× 1.5) = §f" + String.format("%.2f", damage) + " §6CRIT!");
                           } else {
                              DebugMessage.send(player, "§7× Crit = §f1.000 §8(not a crit)");
                           }

                           DebugMessage.send(player, "§7Damage entering EliteMobDamagedByPlayerEvent: §f" + String.format("%.2f", damage));
                        }

                        if (breakdown != null) {
                           breakdown.compute();
                        }

                        EliteMobDamagedByPlayerEvent eliteMobDamagedByPlayerEvent = new EliteMobDamagedByPlayerEvent(
                           eliteEntity, player, event, damage, criticalHit, bypass, damageModifier
                        );
                        eliteMobDamagedByPlayerEvent.setMagicHit(damageOverride.magicHit());
                        if (event.getCause() == DamageCause.PROJECTILE && !ClassAbilityProjectileCarrier.isCarrier((Projectile)event.getDamager())) {
                           Projectile projectile = (Projectile)event.getDamager();
                           String storedType = ItemTagger.getArrowSkillType(projectile);
                           if (storedType != null) {
                              try {
                                 eliteMobDamagedByPlayerEvent.setRangedSkillType(SkillType.valueOf(storedType));
                              } catch (IllegalArgumentException var30) {
                              }
                           }

                           int storedLevel = ItemTagger.getArrowSkillLevel(projectile);
                           if (storedLevel >= 0) {
                              eliteMobDamagedByPlayerEvent.setRangedSkillLevel(storedLevel);
                           }
                        }

                        new EventCaller(eliteMobDamagedByPlayerEvent);
                        if (eliteMobDamagedByPlayerEvent.isCancelled()) {
                           if (DebugMessage.isDebugEnabled(player)) {
                              DebugMessage.send(player, "§cEvent cancelled by a listener — no damage applied.");
                              DebugMessage.send(player, "§6═════════════════════════════════════");
                           }

                           event.setCancelled(true);
                        } else {
                           damage = eliteMobDamagedByPlayerEvent.getDamage();
                           if (DebugMessage.isDebugEnabled(player)) {
                              DebugMessage.send(player, "§7After event listeners (weapon skill bonuses etc.): §f" + String.format("%.2f", damage));
                              DebugMessage.send(
                                 player,
                                 "§a⇒ DAMAGE APPLIED TO MOB: §f"
                                    + String.format("%.2f", damage)
                                    + " §7HP (mob has §f"
                                    + String.format("%.2f", eliteEntity.getHealth())
                                    + "§7 / §f"
                                    + String.format("%.2f", eliteEntity.getMaxHealth())
                                    + "§7 HP)"
                              );
                              String pathTag;
                              String keyTag;
                              double appliedKeyValue;
                              if (bypass) {
                                 pathTag = "CUSTOM";
                                 keyTag = "bypassDamage";
                                 appliedKeyValue = 1.0;
                              } else if (eliteEntity.isScaledCombat()) {
                                 pathTag = "SCALED";
                                 keyTag = "scaledDamageToEliteMultiplier";
                                 appliedKeyValue = MobCombatSettingsConfig.getScaledDamageToEliteMultiplier();
                              } else if (eliteEntity instanceof CustomBossEntity cbTag && cbTag.isNormalizedCombat()) {
                                 pathTag = "NORMALIZED";
                                 keyTag = "normalizedDamageToEliteMultiplier";
                                 appliedKeyValue = MobCombatSettingsConfig.getNormalizedDamageToEliteMultiplier();
                              } else {
                                 pathTag = "DEFAULT_V2";
                                 keyTag = "damageToEliteMobMultiplierV2";
                                 appliedKeyValue = MobCombatSettingsConfig.getDamageToEliteMultiplier();
                              }

                              String mobType = eliteEntity.getLivingEntity() != null ? eliteEntity.getLivingEntity().getType().name() : "?";
                              DebugMessage.damageSummary(
                                 player,
                                 String.format(
                                    "P->E target=%s Lv%d path=%s %s=%.3f rawFormula=%.2f dmgMod=%.3f cfgMult=%.3f crit=%s finalApplied=%.2f mobHP=%.1f/%.1f",
                                    mobType,
                                    eliteEntity.getLevel(),
                                    pathTag,
                                    keyTag,
                                    appliedKeyValue,
                                    damage,
                                    damageModifier,
                                    combatMultiplier,
                                    eliteMobDamagedByPlayerEvent.isCriticalStrikeDamageApplied() ? "1.5" : "1.0",
                                    damage,
                                    eliteEntity.getHealth(),
                                    eliteEntity.getMaxHealth()
                                 )
                              );
                              DebugMessage.send(player, "§6═════════════════════════════════════");
                           }

                           if (validPlayer) {
                              eliteEntity.addDamager(
                                 player,
                                 damage,
                                 progressionSkillFor(event, player),
                                 event.getDamager() instanceof Projectile projectilex
                                    ? ItemTagger.getArrowLoudStrikes(projectilex)
                                    : CombatDamageContext.currentPlayerToEliteSource()
                                       .map(CombatDamageContext.PlayerDamageSource::loudStrikesBonus)
                                       .orElse(null)
                              );
                           }

                           if (eliteEntity.getLivingEntity() != null
                              && eliteEntity.getLivingEntity().getType().equals(EntityType.ENDER_DRAGON)
                              && eliteEntity.getLivingEntity().getHealth() - damage < 1.0) {
                              if (eliteEntity.isDying()) {
                                 return;
                              }

                              damage = 0.0;
                              event.setCancelled(true);
                              ((EnderDragon)eliteEntity.getLivingEntity()).setPhase(Phase.DYING);
                              eliteEntity.setDying(true);
                              Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> EliteMobDeathEvent.callAndRemove(eliteEntity), 200L);
                           }

                           event.setDamage(DamageModifier.BASE, damage);
                           event.setDamage(damage);
                           eliteEntity.syncPluginHealth(((LivingEntity)event.getEntity()).getHealth());
                           this.runAntiexploit(eliteEntity, event, eliteMobDamagedByPlayerEvent);
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private static SkillType progressionSkillFor(EntityDamageByEntityEvent event, Player player) {
      Optional<CombatDamageContext.PlayerDamageSource> source = CombatDamageContext.currentPlayerToEliteSource();
      if (source.isPresent()) {
         return source.get().progressionSkill();
      } else if (CombatDamageContext.isPlayerToEliteBypassActive()) {
         return null;
      } else if (event.getDamager() instanceof Trident) {
         return SkillType.TRIDENTS;
      } else if (event.getDamager() instanceof Projectile projectile) {
         String storedType = ItemTagger.getArrowSkillType(projectile);
         if (storedType != null) {
            try {
               return SkillType.valueOf(storedType);
            } catch (IllegalArgumentException var6) {
               return null;
            }
         } else {
            return null;
         }
      } else {
         return WeaponIdentityResolver.progressionSkill(player.getInventory().getItemInMainHand());
      }
   }

   private void runAntiexploit(EliteEntity eliteEntity, EntityDamageByEntityEvent event, EliteMobDamagedByPlayerEvent eliteMobDamagedByPlayerEvent) {
      if (!EliteMobsWorld.isEliteMobsWorld(event.getDamager().getWorld().getUID())) {
         if (!eliteEntity.isEnderDragon()) {
            if (!eliteMobDamagedByPlayerEvent.isCustomDamage()) {
               if (EliteMobs.worldGuardIsEnabled) {
                  Boolean regionQuery = WorldGuardFlagChecker.checkNullableFlag(eliteEntity.getLocation(), WorldGuardCompatibility.getELITEMOBS_ANTIEXPLOIT());
                  if (regionQuery != null && !regionQuery) {
                     return;
                  }
               }

               if (event.getCause() == DamageCause.ENTITY_ATTACK
                  || event.getCause() == DamageCause.ENTITY_SWEEP_ATTACK
                  || event.getCause() == DamageCause.PROJECTILE) {
                  if (!eliteEntity.isInAntiExploitCooldown() && eliteEntity.getLivingEntity() != null) {
                     Bukkit.getServer().getPluginManager().callEvent(new EliteMobDamagedByPlayerAntiExploitEvent(eliteEntity, eliteMobDamagedByPlayerEvent));
                  }
               }
            }
         }
      }
   }
}
