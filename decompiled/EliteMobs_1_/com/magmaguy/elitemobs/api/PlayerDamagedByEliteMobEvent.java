package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatRules;
import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatRuntime;
import com.magmaguy.elitemobs.collateralminecraftchanges.PlayerDeathMessageByEliteMob;
import com.magmaguy.elitemobs.combatsystem.ArmorDefenseCalculator;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.combatsystem.PotionCombatModifierCalculator;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.AttributeManager;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.ArmorSkillHealthBonus;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.skills.bonuses.PlayerSkillSelection;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.AdrenalineSurgeSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.EvasionSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.FortifySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.GritSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.IronStanceSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.LastStandSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.ReactiveShieldingSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.RetaliationSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.armor.SecondWindSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.DeathsEmbraceSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.DivineShieldSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.PhalanxSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.staves.BattlemagesGuardSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.staves.PhoenixMantleSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.ParrySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.RiposteSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.wands.ArcaneWardSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.wands.SpellMirrorSkill;
import com.magmaguy.elitemobs.testing.CombatSimulator;
import com.magmaguy.elitemobs.utils.DebugMessage;
import com.magmaguy.elitemobs.utils.EventCaller;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.EvokerFangs;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

public class PlayerDamagedByEliteMobEvent extends EliteDamageEvent {
   public static final double MAX_AGGREGATE_DEFENSIVE_REDUCTION = 0.85;
   private static final HandlerList handlers = new HandlerList();
   private final Entity entity;
   private final EliteEntity eliteEntity;
   private final Player player;
   private final EntityDamageByEntityEvent entityDamageByEntityEvent;
   private final Projectile projectile;
   private boolean playerBlocking;

   public PlayerDamagedByEliteMobEvent(EliteEntity eliteEntity, Player player, EntityDamageByEntityEvent event, Projectile projectile, double damage) {
      super(damage, event);
      this.entity = event.getEntity();
      this.eliteEntity = eliteEntity;
      this.player = player;
      this.entityDamageByEntityEvent = event;
      this.projectile = projectile;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   public Entity getEntity() {
      return this.entity;
   }

   public EliteEntity getEliteMobEntity() {
      return this.eliteEntity;
   }

   public Player getPlayer() {
      return this.player;
   }

   public Projectile getProjectile() {
      return this.projectile;
   }

   public EntityDamageByEntityEvent getEntityDamageByEntityEvent() {
      return this.entityDamageByEntityEvent;
   }

   public LivingEntity getAttacker() {
      return this.eliteEntity != null && this.eliteEntity.getLivingEntity() != null ? this.eliteEntity.getLivingEntity() : null;
   }

   @Override
   public HandlerList getHandlers() {
      return handlers;
   }

   private void setSkillModifiedDamage(double modifiedDamage) {
      this.setDamage(Math.max(0.0, modifiedDamage));
   }

   public static double capAggregateReduction(double incomingDamage, double reducedDamage) {
      double flooredDamage = Math.max(0.0, reducedDamage);
      return incomingDamage <= 0.0 ? flooredDamage : Math.max(flooredDamage, incomingDamage * 0.15000000000000002);
   }

   public boolean applySkillBonuses() {
      double incomingDamage = this.getDamage();

      boolean var3;
      try {
         var3 = this.applyDefensiveSkillBonuses();
      } finally {
         if (!this.isCancelled()) {
            this.setDamage(capAggregateReduction(incomingDamage, this.getDamage()));
         }
      }

      return var3;
   }

   private boolean applyDefensiveSkillBonuses() {
      if (this.player != null && !this.player.hasMetadata("NPC")) {
         if (SkillsConfig.isWorldExcludedFromSkills(this.player)) {
            return false;
         } else if (!ElitePlayerInventory.playerInventories.containsKey(this.player.getUniqueId())) {
            return false;
         } else {
            int skillLevel = SkillBonusRegistry.getPlayerSkillLevel(this.player, SkillType.ARMOR);

            for (String skillId : PlayerSkillSelection.getActiveSkills(this.player.getUniqueId(), SkillType.ARMOR)) {
               SkillBonus skill = SkillBonusRegistry.getSkillById(skillId);
               if (skill != null && skill.isEnabled() && skill.meetsLevelRequirement(skillLevel)) {
                  if (skill instanceof EvasionSkill evasion) {
                     if (evasion.tryEvade(this.player, this)) {
                        skill.incrementProcCount(this.player);
                        SkillBonus.sendSkillActionBar(this.player, skill);
                        this.setCancelled(true);
                        return true;
                     }
                  } else if (skill instanceof RetaliationSkill retaliation) {
                     LivingEntity attacker = this.getAttacker();
                     if (attacker != null) {
                        retaliation.onDamageTaken(this.player, attacker, this.getDamage());
                     }
                  } else if (skill instanceof FortifySkill fortify) {
                     double modifiedDamage = fortify.modifyIncomingDamage(this.player, this.getDamage());
                     this.setSkillModifiedDamage(modifiedDamage);
                     skill.incrementProcCount(this.player);
                  } else if (skill instanceof ReactiveShieldingSkill reactiveShielding) {
                     double damagePercent = this.getDamage() / this.player.getMaxHealth();
                     reactiveShielding.checkTrigger(this.player, damagePercent);
                     double modifiedDamage = reactiveShielding.modifyIncomingDamage(this.player, this.getDamage());
                     if (modifiedDamage != this.getDamage()) {
                        this.setSkillModifiedDamage(modifiedDamage);
                        skill.incrementProcCount(this.player);
                        SkillBonus.sendSkillActionBar(this.player, skill);
                     }
                  } else if (skill instanceof AdrenalineSurgeSkill adrenaline) {
                     double modifiedDamage = adrenaline.modifyIncomingDamage(this.player, this.getDamage());
                     if (modifiedDamage != this.getDamage()) {
                        this.setSkillModifiedDamage(modifiedDamage);
                     }

                     double newHealthPercent = (this.player.getHealth() - this.getDamage()) / this.player.getMaxHealth();
                     adrenaline.checkTrigger(this.player, newHealthPercent);
                  } else if (skill instanceof SecondWindSkill secondWind) {
                     secondWind.afterHit(this.player, this.entityDamageByEntityEvent);
                  } else if (skill instanceof LastStandSkill lastStand) {
                     boolean fatal = this.player.getHealth() - this.getDamage() <= 0.0;
                     if (fatal && lastStand.preventDeath(this.player, this.getDamage())) {
                        skill.incrementProcCount(this.player);
                        SkillBonus.sendSkillActionBar(this.player, skill);
                        this.setCancelled(true);
                        return true;
                     }
                  } else if (skill instanceof IronStanceSkill ironStance) {
                     double modifiedDamage = ironStance.modifyIncomingDamage(this.player, this.getDamage(), this);
                     if (modifiedDamage != this.getDamage()) {
                        this.setSkillModifiedDamage(modifiedDamage);
                        skill.incrementProcCount(this.player);
                        SkillBonus.sendSkillActionBar(this.player, skill);
                     }
                  } else if (skill instanceof GritSkill grit) {
                     double modifiedDamage = grit.modifyIncomingDamage(this.player, this.getDamage(), this);
                     if (modifiedDamage != this.getDamage()) {
                        this.setSkillModifiedDamage(modifiedDamage);
                        skill.incrementProcCount(this.player);
                        SkillBonus.sendSkillActionBar(this.player, skill);
                     }
                  } else {
                     double multiplier = this.processPassiveDefensiveSkill(skill, skillLevel);
                     if (multiplier != 1.0) {
                        this.setSkillModifiedDamage(this.getDamage() * multiplier);
                     }
                  }
               }
            }

            double parryDamage = ParrySkill.applyParryReduction(this.player, this, this.getDamage());
            if (parryDamage != this.getDamage()) {
               this.setSkillModifiedDamage(parryDamage);
               SkillBonus parrySkill = SkillBonusRegistry.getSkillById("swords_parry");
               if (parrySkill != null) {
                  SkillBonus.sendSkillActionBar(this.player, parrySkill);
               }
            }

            double phalanxDamage = PhalanxSkill.applyFrontalReduction(this.player, this, this.getDamage());
            if (phalanxDamage != this.getDamage()) {
               this.setSkillModifiedDamage(phalanxDamage);
            }

            if (SpellMirrorSkill.tryMirror(this.player, this, this.getDamage())) {
               this.setCancelled(true);
               return true;
            } else {
               double guardedDamage = BattlemagesGuardSkill.applyGuard(this.player, this, this.getDamage());
               if (guardedDamage != this.getDamage()) {
                  this.setSkillModifiedDamage(guardedDamage);
               }

               double wardedDamage = ArcaneWardSkill.applyWard(this.player, this, this.getDamage());
               if (wardedDamage != this.getDamage()) {
                  this.setSkillModifiedDamage(wardedDamage);
               }

               if (this.player.getHealth() - this.getDamage() <= 0.0) {
                  if (DeathsEmbraceSkill.preventDeath(this.player)) {
                     this.setCancelled(true);
                     return true;
                  }

                  if (DivineShieldSkill.preventDeath(this.player, this.getDamage())) {
                     this.setCancelled(true);
                     return true;
                  }

                  if (PhoenixMantleSkill.preventDeath(this.player)) {
                     this.setCancelled(true);
                     return true;
                  }
               }

               return false;
            }
         }
      } else {
         return false;
      }
   }

   private double processPassiveDefensiveSkill(SkillBonus skill, int skillLevel) {
      skill.incrementProcCount(this.player);
      return 1.0 - SkillBonus.clampDefensiveReduction(skill.getBonusValue(skillLevel));
   }

   @Generated
   public boolean isPlayerBlocking() {
      return this.playerBlocking;
   }

   public static class PlayerDamagedByEliteMobEventFilter implements Listener {
      private static double eliteToPlayerDamageFormula(Player player, EliteEntity eliteEntity, EntityDamageByEntityEvent event, double specialMultiplier) {
         if (ElitePlayerInventory.getPlayer(player) == null) {
            return 0.0;
         } else {
            boolean skillsExcluded = SkillsConfig.isWorldExcludedFromSkills(player);
            long armorSkillXP = skillsExcluded ? 0L : PlayerData.getSkillXP(player.getUniqueId(), SkillType.ARMOR);
            int armorSkillLevel = skillsExcluded ? 1 : Math.max(1, SkillXPCalculator.levelFromTotalXP(armorSkillXP));
            double playerMaxHealth = ArmorSkillHealthBonus.getConfiguredMaxHealthForPlayer(player, armorSkillLevel);
            int mobLevel = eliteEntity.getLevel();
            if (eliteEntity.isScaledCombat()) {
               mobLevel = armorSkillLevel;
            }

            double baseDamage = playerMaxHealth / 5.0;
            double skillAdjustment = Math.pow(2.0, (mobLevel - armorSkillLevel) / 7.5);
            ArmorDefenseCalculator.DamageType damageType = ArmorDefenseCalculator.fromEvent(event);
            double gearScore = ArmorDefenseCalculator.getGearScore(player, damageType);
            double gearAdjustment = ArmorDefenseCalculator.getGearAdjustment(gearScore, mobLevel);
            double scaledDamage = baseDamage * skillAdjustment * gearAdjustment;
            if (eliteEntity.getLivingEntity() != null && player.isValid()) {
               if (eliteEntity.getLivingEntity().getType().equals(EntityType.CREEPER) && player.getWorld().equals(eliteEntity.getLivingEntity().getWorld())) {
                  Creeper creeper = (Creeper)eliteEntity.getLivingEntity();
                  double distance = player.getLocation().distance(eliteEntity.getLivingEntity().getLocation());
                  double distanceAttenuation = Math.max(0.0, 1.0 - distance / creeper.getExplosionRadius());
                  scaledDamage *= distanceAttenuation;
               } else if (eliteEntity.getLivingEntity().getType().equals(EntityType.GHAST)
                  && event.getDamager().getType().equals(EntityType.FIREBALL)
                  && player.getWorld().equals(event.getDamager().getWorld())) {
                  Fireball fireball = (Fireball)event.getDamager();
                  double distance = player.getLocation().distance(fireball.getLocation());
                  double distanceAttenuation = fireball.getYield() > 0.0F ? Math.max(0.0, 1.0 - distance / fireball.getYield()) : 0.0;
                  scaledDamage *= distanceAttenuation;
               }
            }

            double potionMultiplier = PotionCombatModifierCalculator.getCombinedDamageMultiplier(eliteEntity.getLivingEntity(), player);
            double customBossDamageMultiplier = eliteEntity.getDamageMultiplier();
            double configMultiplier;
            if (eliteEntity.isScaledCombat()) {
               configMultiplier = MobCombatSettingsConfig.getScaledDamageToPlayerMultiplier();
            } else if (eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.isNormalizedCombat()) {
               configMultiplier = MobCombatSettingsConfig.getNormalizedDamageToPlayerMultiplier();
            } else {
               configMultiplier = MobCombatSettingsConfig.getDamageToPlayerMultiplier();
            }

            double preCapDamage = Math.max(scaledDamage, 1.0) * potionMultiplier * customBossDamageMultiplier * specialMultiplier * configMultiplier;
            double actualMaxHealth = AttributeManager.getAttributeValue(player, "generic_max_health");
            double oneShotProtectionMaxHealth = AdvancedCombatRuntime.isActive(player)
               ? AdvancedCombatRules.ordinaryMaximumHealth(actualMaxHealth)
               : actualMaxHealth;
            double finalDamage = Math.min(preCapDamage, oneShotProtectionMaxHealth - 1.0);
            if (DebugMessage.isDebugEnabled(player)) {
               String combatPath;
               String configKey;
               if (eliteEntity.isScaledCombat()) {
                  combatPath = "SCALED";
                  configKey = "scaledDamageToPlayerMultiplier";
               } else if (eliteEntity instanceof CustomBossEntity cbForLog && cbForLog.isNormalizedCombat()) {
                  combatPath = "NORMALIZED";
                  configKey = "normalizedDamageToPlayerMultiplier";
               } else {
                  combatPath = "DEFAULT (V2)";
                  configKey = "damageToPlayerMultiplierV2";
               }

               boolean normalizedFlag = eliteEntity instanceof CustomBossEntity cbForFlag && cbForFlag.isNormalizedCombat();
               String mobName = eliteEntity.getLivingEntity() != null ? eliteEntity.getLivingEntity().getType().name() : "?";
               String entityClass = eliteEntity.getClass().getSimpleName();
               DebugMessage.send(player, "§6═════ EM DAMAGE: ELITE → YOU ═════");
               DebugMessage.send(player, "§7Mob: §f" + mobName + " §7Lv§f" + eliteEntity.getLevel() + " §8(EliteEntity class: §7" + entityClass + "§8)");
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
                     + " §8(from per-boss config field; defaults 1.0 for natural elites)"
               );
               DebugMessage.send(player, "§7Combat path: §e" + combatPath + " §8→ pulls config key §f" + configKey);
               DebugMessage.send(player, "§e── Formula (eliteToPlayerDamageFormula) ──");
               DebugMessage.send(
                  player,
                  "§7Player armor skill level: §f"
                     + armorSkillLevel
                     + " §7Effective mob level used in formula: §f"
                     + mobLevel
                     + " §8(armor level if scaled, otherwise real mob level)"
               );
               DebugMessage.send(player, "§7Base = playerMaxHP / TARGET_HITS_TO_KILL_PLAYER (5.0)");
               DebugMessage.send(
                  player,
                  "§7   = "
                     + String.format("%.2f", playerMaxHealth)
                     + " / 5.0 = §f"
                     + String.format("%.2f", baseDamage)
                     + " §8(damage needed per hit to kill in TARGET_HITS hits)"
               );
               DebugMessage.send(player, "§7× Skill adjustment = 2^((mobLv − armorLv)/" + String.format("%.1f", 7.5) + ")");
               DebugMessage.send(
                  player,
                  "§7   = 2^(("
                     + mobLevel
                     + " − "
                     + armorSkillLevel
                     + ")/"
                     + String.format("%.1f", 7.5)
                     + ") = §f"
                     + String.format("%.3f", skillAdjustment)
                     + " §8(>1 if mob over-level, <1 if you out-skill)"
               );
               DebugMessage.send(
                  player,
                  "§7× Gear adjustment = §f"
                     + String.format("%.3f", gearAdjustment)
                     + " §8(2.0×(1−gearReduction); 0.5 fully matched, 2.0 naked; damageType="
                     + damageType
                     + ", gearScore="
                     + String.format("%.2f", gearScore)
                     + ")"
               );
               DebugMessage.send(player, "§7= scaledDamage (after explosion attenuation if any) = §f" + String.format("%.2f", scaledDamage));
               DebugMessage.send(player, "§e── Outer multipliers ──");
               DebugMessage.send(
                  player, "§7× max(scaledDamage, 1) = §f" + String.format("%.2f", Math.max(scaledDamage, 1.0)) + " §8(floor so trivial hits still register 1)"
               );
               DebugMessage.send(
                  player,
                  "§7× Potion multiplier (source → target) = §f" + String.format("%.3f", potionMultiplier) + " §8(source strength/weakness × your resistance)"
               );
               DebugMessage.send(
                  player,
                  "§7× Per-mob damageMultiplier = §f"
                     + String.format("%.3f", customBossDamageMultiplier)
                     + " §8(eliteEntity.getDamageMultiplier(); per-boss YAML 'damageMultiplier')"
               );
               DebugMessage.send(
                  player,
                  "§7× Special multiplier = §f" + String.format("%.3f", specialMultiplier) + " §8(transient ability modifier; auto-resets to 1 after use)"
               );
               DebugMessage.send(
                  player, "§7× Config multiplier = §f" + String.format("%.3f", configMultiplier) + " §8(" + configKey + " in MobCombatSettings.yml)"
               );
               DebugMessage.send(player, "§e── Result ──");
               DebugMessage.send(player, "§7Pre-cap product = §f" + String.format("%.2f", preCapDamage));
               DebugMessage.send(
                  player,
                  "§71-shot cap (pre-mode maxHP − 1 = "
                     + String.format("%.2f", oneShotProtectionMaxHealth - 1.0)
                     + ") → final = §f"
                     + String.format("%.2f", finalDamage)
               );
               DebugMessage.send(
                  player,
                  "§a⇒ Formula returns: §f" + String.format("%.2f", finalDamage) + " §8(may still be adjusted by blocking/bypass/skill bonuses after this)"
               );
            }

            return finalDamage;
         }
      }

      @EventHandler(
         ignoreCancelled = true,
         priority = EventPriority.HIGHEST
      )
      public void explosionEvent(EntityExplodeEvent event) {
         if (event.getEntity().getType().equals(EntityType.CREEPER) && EntityTracker.isEliteMob(event.getEntity())) {
            Set<PotionEffect> potionEffects = new HashSet<>(((Creeper)event.getEntity()).getActivePotionEffects());
            potionEffects.forEach(potionEffectType -> ((Creeper)event.getEntity()).removePotionEffect(potionEffectType.getType()));
         }
      }

      @EventHandler
      public void onEliteDamagePlayer(EntityDamageByEntityEvent event) {
         CombatDamageContext.DamageOverride damageOverride = CombatDamageContext.consumeEliteToPlayer();
         boolean bypass = damageOverride.bypass();
         double specialMultiplier = damageOverride.specialMultiplier();
         if (!event.isCancelled() || event.getDamager() instanceof Explosive) {
            if (event.getEntity() instanceof Player player) {
               if (!player.hasMetadata("NPC") && ElitePlayerInventory.getPlayer(player) != null) {
                  Projectile projectile = null;
                  EliteEntity eliteEntity = null;
                  if (event.getDamager() instanceof LivingEntity) {
                     eliteEntity = EntityTracker.getEliteMobEntity(event.getDamager());
                  } else if (event.getDamager() instanceof Projectile && ((Projectile)event.getDamager()).getShooter() instanceof LivingEntity) {
                     eliteEntity = EntityTracker.getEliteMobEntity((LivingEntity)((Projectile)event.getDamager()).getShooter());
                     projectile = (Projectile)event.getDamager();
                  } else if (event.getDamager().getType().equals(EntityType.EVOKER_FANGS) && ((EvokerFangs)event.getDamager()).getOwner() != null) {
                     eliteEntity = EntityTracker.getEliteMobEntity(((EvokerFangs)event.getDamager()).getOwner());
                  }

                  if (eliteEntity != null && eliteEntity.getLivingEntity() != null) {
                     boolean blocking = false;
                     if (player.isBlocking() || CombatSimulator.isBlockingOverride(event)) {
                        blocking = true;
                        damageBlockingShield(player);
                        if (event.getDamager() instanceof Projectile) {
                           event.getDamager().remove();
                           return;
                        }

                        if (!SkillsConfig.isWorldExcludedFromSkills(player)) {
                           RiposteSkill.onPlayerBlock(player);
                        }
                     }

                     double newDamage = eliteToPlayerDamageFormula(player, eliteEntity, event, specialMultiplier);
                     double damageAfterFormula = newDamage;
                     boolean testOverrideHit = CombatSimulator.getTestDamageOverride(event) >= 0.0;
                     if (testOverrideHit) {
                        newDamage = CombatSimulator.getTestDamageOverride(event);
                     }

                     double damageAfterTestOverride = newDamage;
                     if (blocking) {
                        newDamage -= newDamage * MobCombatSettingsConfig.getBlockingDamageReduction();
                     }

                     double damageAfterBlocking = newDamage;

                     for (DamageModifier modifier : DamageModifier.values()) {
                        if (event.isApplicable(modifier) && modifier != DamageModifier.ABSORPTION) {
                           event.setDamage(modifier, 0.0);
                        }
                     }

                     double rawBypassDamage = 0.0;
                     if (bypass) {
                        rawBypassDamage = event.getOriginalDamage(DamageModifier.BASE);
                        newDamage = rawBypassDamage;
                     }

                     PlayerDamagedByEliteMobEvent playerDamagedByEliteMobEvent = new PlayerDamagedByEliteMobEvent(
                        eliteEntity, player, event, projectile, newDamage
                     );
                     playerDamagedByEliteMobEvent.playerBlocking = blocking;
                     new EventCaller(playerDamagedByEliteMobEvent);
                     newDamage = playerDamagedByEliteMobEvent.getDamage();
                     if (DebugMessage.isDebugEnabled(player)) {
                        DebugMessage.send(player, "§6── Post-formula adjustments ──");
                        DebugMessage.send(player, "§7After formula: §f" + String.format("%.2f", damageAfterFormula));
                        if (testOverrideHit) {
                           DebugMessage.send(player, "§7Test override (CombatSimulator) forced damage to §f" + String.format("%.2f", damageAfterTestOverride));
                        }

                        if (blocking) {
                           DebugMessage.send(
                              player,
                              "§7Blocking (× "
                                 + String.format("%.2f", 1.0 - MobCombatSettingsConfig.getBlockingDamageReduction())
                                 + ", blockingDamageReduction="
                                 + String.format("%.2f", MobCombatSettingsConfig.getBlockingDamageReduction())
                                 + "): §f"
                                 + String.format("%.2f", damageAfterBlocking)
                           );
                        } else {
                           DebugMessage.send(player, "§7Blocking: §cnot blocking §8(no reduction)");
                        }

                        if (bypass) {
                           DebugMessage.send(
                              player,
                              "§cbypass=true §7→ formula damage discarded; raw event damage = §f"
                                 + String.format("%.2f", rawBypassDamage)
                                 + " §8(power flagged this hit to skip the formula)"
                           );
                        } else {
                           DebugMessage.send(player, "§7bypass=false §8(formula damage kept)");
                        }

                        DebugMessage.send(player, "§7Entering PlayerDamagedByEliteMobEvent: §f" + String.format("%.2f", newDamage));
                        DebugMessage.send(
                           player,
                           "§7After event listeners (skill bonuses, etc.): §f"
                              + String.format("%.2f", newDamage)
                              + " §8(Evasion/Fortify/Reactive-Shielding/Parry/Phalanx etc. fire here)"
                        );
                        DebugMessage.send(player, "§7Event cancelled? §f" + playerDamagedByEliteMobEvent.isCancelled());
                        if (!playerDamagedByEliteMobEvent.isCancelled()) {
                           DebugMessage.send(player, "§a⇒ DAMAGE APPLIED TO YOU: §f" + String.format("%.2f", newDamage) + " §7HP");
                        }

                        String pathTag;
                        String keyTag;
                        double appliedKeyValue;
                        if (eliteEntity.isScaledCombat()) {
                           pathTag = "SCALED";
                           keyTag = "scaledDamageToPlayerMultiplier";
                           appliedKeyValue = MobCombatSettingsConfig.getScaledDamageToPlayerMultiplier();
                        } else if (eliteEntity instanceof CustomBossEntity cbTag && cbTag.isNormalizedCombat()) {
                           pathTag = "NORMALIZED";
                           keyTag = "normalizedDamageToPlayerMultiplier";
                           appliedKeyValue = MobCombatSettingsConfig.getNormalizedDamageToPlayerMultiplier();
                        } else {
                           pathTag = "DEFAULT_V2";
                           keyTag = "damageToPlayerMultiplierV2";
                           appliedKeyValue = MobCombatSettingsConfig.getDamageToPlayerMultiplier();
                        }

                        String mobType = eliteEntity.getLivingEntity() != null ? eliteEntity.getLivingEntity().getType().name() : "?";
                        DebugMessage.damageSummary(
                           player,
                           String.format(
                              "E->P source=%s Lv%d path=%s %s=%.3f formula=%.2f blocking=%s bypass=%s entered=%.2f finalApplied=%.2f cancelled=%s",
                              mobType,
                              eliteEntity.getLevel(),
                              pathTag,
                              keyTag,
                              appliedKeyValue,
                              damageAfterFormula,
                              blocking,
                              bypass,
                              newDamage,
                              newDamage,
                              playerDamagedByEliteMobEvent.isCancelled()
                           )
                        );
                        DebugMessage.send(player, "§6═════════════════════════════════════");
                     }

                     if (!playerDamagedByEliteMobEvent.isCancelled()) {
                        event.setDamage(DamageModifier.BASE, newDamage);
                        if (event.isApplicable(DamageModifier.ABSORPTION)) {
                           event.setDamage(DamageModifier.ABSORPTION, -Math.min(player.getAbsorptionAmount(), Math.max(0.0, newDamage)));
                        }

                        PlayerDeathMessageByEliteMob.recordHit(event, eliteEntity);
                     }
                  }
               }
            }
         }
      }

      private static void damageBlockingShield(Player player) {
         boolean mainHand = player.getInventory().getItemInMainHand().getType() == Material.SHIELD;
         boolean offHand = player.getInventory().getItemInOffHand().getType() == Material.SHIELD;
         if (mainHand || offHand) {
            ItemStack shield = mainHand ? player.getInventory().getItemInMainHand() : player.getInventory().getItemInOffHand();
            ItemMeta itemMeta = shield.getItemMeta();
            if (itemMeta instanceof Damageable damageable) {
               int unbreakingLevel = itemMeta.getEnchantLevel(Enchantment.UNBREAKING);
               if (unbreakingLevel <= 0 || ThreadLocalRandom.current().nextInt(unbreakingLevel + 1) == 0) {
                  int newDamage = damageable.getDamage() + 5;
                  if (newDamage >= shield.getType().getMaxDurability()) {
                     if (mainHand) {
                        player.getInventory().setItemInMainHand(null);
                     } else {
                        player.getInventory().setItemInOffHand(null);
                     }
                  } else {
                     damageable.setDamage(newDamage);
                     shield.setItemMeta(itemMeta);
                  }
               }
            }
         }
      }
   }
}
