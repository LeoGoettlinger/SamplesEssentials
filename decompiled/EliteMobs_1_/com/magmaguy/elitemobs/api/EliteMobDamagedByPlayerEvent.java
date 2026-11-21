package com.magmaguy.elitemobs.api;

import com.magmaguy.elitemobs.combatsystem.ClassAbilityDamageAttribution;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.items.ItemTagger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.WeaponIdentityResolver;
import com.magmaguy.elitemobs.skills.bonuses.PlayerSkillSelection;
import com.magmaguy.elitemobs.skills.bonuses.ProcRoll;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ConditionalSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.CooldownSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcCooldownTracker;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.ProcSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.StackingSkill;
import com.magmaguy.elitemobs.skills.bonuses.interfaces.TargetDebuffBonus;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.RangersFocusSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.bows.WindRunnerSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.HeavyBoltsSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.crossbows.QuickReloadSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.DeathsEmbraceSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.AvatarOfJudgmentSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.maces.StunningForceSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.RiposteSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.tridents.PoseidonsFavorSkill;
import com.magmaguy.elitemobs.utils.DebugMessage;
import java.util.List;
import java.util.Set;
import lombok.Generated;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EliteMobDamagedByPlayerEvent extends EliteDamageEvent {
   private static final HandlerList handlers = new HandlerList();
   private static final List<String> TARGET_DEBUFF_SKILL_IDS = List.of(
      "bows_hunters_mark", "hoes_death_mark", "maces_judgment", "swords_expose_weakness", "wands_hex_brand", "wands_unravel"
   );
   private final ClassAbilityDamageAttribution classAbilityDamageAttribution = new ClassAbilityDamageAttribution();
   private final Entity entity;
   private final EliteEntity eliteMobEntity;
   private final Player player;
   private final EntityDamageByEntityEvent entityDamageByEntityEvent;
   private final boolean criticalStrike;
   private boolean criticalStrikeDamageApplied;
   private final boolean isCustomDamage;
   private final double damageModifier;
   public boolean rangedAttack;
   private SkillType rangedSkillType = null;
   private int rangedSkillLevel = 0;
   private CombatDamageContext.MagicHit magicHit = null;

   public MagicStrike getMagicStrike() {
      return this.magicHit == null ? null : this.magicHit.strike();
   }

   public EliteMobDamagedByPlayerEvent(
      EliteEntity eliteEntity,
      Player player,
      EntityDamageByEntityEvent event,
      double damage,
      boolean criticalStrike,
      boolean isCustomDamage,
      double damageModifier
   ) {
      super(damage, event);
      this.entity = eliteEntity.getLivingEntity();
      this.eliteMobEntity = eliteEntity;
      this.player = player;
      this.entityDamageByEntityEvent = event;
      this.rangedAttack = event != null && event.getDamager() instanceof Projectile;
      this.criticalStrike = criticalStrike;
      this.criticalStrikeDamageApplied = criticalStrike;
      this.isCustomDamage = isCustomDamage;
      this.damageModifier = damageModifier;
   }

   public EliteMobDamagedByPlayerEvent(EliteEntity eliteEntity, Player player, double damage, boolean isRangedAttack) {
      super(damage, null);
      this.entity = eliteEntity.getLivingEntity();
      this.eliteMobEntity = eliteEntity;
      this.player = player;
      this.entityDamageByEntityEvent = null;
      this.rangedAttack = isRangedAttack;
      this.criticalStrike = false;
      this.criticalStrikeDamageApplied = false;
      this.isCustomDamage = false;
      this.damageModifier = 1.0;
   }

   public EliteMobDamagedByPlayerEvent(EliteEntity eliteEntity, Player player, double damage, boolean isRangedAttack, boolean criticalStrike) {
      super(damage, null);
      this.entity = eliteEntity.getLivingEntity();
      this.eliteMobEntity = eliteEntity;
      this.player = player;
      this.entityDamageByEntityEvent = null;
      this.rangedAttack = isRangedAttack;
      this.criticalStrike = criticalStrike;
      this.criticalStrikeDamageApplied = false;
      this.isCustomDamage = false;
      this.damageModifier = 1.0;
   }

   public static HandlerList getHandlerList() {
      return handlers;
   }

   @Override
   public HandlerList getHandlers() {
      return handlers;
   }

   public double getDamageWithoutCriticalStrike() {
      return !this.criticalStrikeDamageApplied ? this.getDamage() : this.getDamage() / 1.5;
   }

   public void applyClassAbilityDamageMultiplier(String abilityId, double multiplier) {
      double modifiedDamage = this.classAbilityDamageAttribution.applyMultiplier(abilityId, this.getDamage(), multiplier);
      super.setDamage(modifiedDamage);
   }

   public double getClassAbilityBonusDamage() {
      return this.classAbilityDamageAttribution.bonusDamage(this.getDamage());
   }

   public Set<String> getClassAbilityDamageSources() {
      return this.classAbilityDamageAttribution.sources();
   }

   @Override
   public void setDamage(double damage) {
      double previousDamage = this.getDamage();
      super.setDamage(damage);
      this.classAbilityDamageAttribution.observeReplacement(previousDamage, damage);
   }

   static double mergeOffensiveMultiplier(double accumulatedMultiplier, double candidateMultiplier) {
      return accumulatedMultiplier + (candidateMultiplier - 1.0);
   }

   public void applySkillBonuses() {
      if (this.entityDamageByEntityEvent == null
         || !(this.entityDamageByEntityEvent.getDamager() instanceof Projectile projectile && ItemTagger.isSecondaryArrow(projectile))) {
         if (this.player != null && !this.player.hasMetadata("NPC")) {
            if (!SkillsConfig.isWorldExcludedFromSkills(this.player)) {
               if (ElitePlayerInventory.playerInventories.containsKey(this.player.getUniqueId())) {
                  boolean debug = DebugMessage.isDebugEnabled(this.player);
                  int skillLevel;
                  SkillType weaponSkillType;
                  if (this.magicHit != null) {
                     weaponSkillType = this.magicHit.skill();
                     skillLevel = SkillBonusRegistry.getPlayerSkillLevel(this.player, weaponSkillType);
                  } else if (this.rangedAttack) {
                     if (this.rangedSkillType == null) {
                        return;
                     }

                     weaponSkillType = this.rangedSkillType;
                     skillLevel = this.rangedSkillLevel > 0 ? this.rangedSkillLevel : SkillBonusRegistry.getPlayerSkillLevel(this.player, weaponSkillType);
                  } else {
                     weaponSkillType = getWeaponSkillType(this.player);
                     if (weaponSkillType == null) {
                        return;
                     }

                     skillLevel = SkillBonusRegistry.getPlayerSkillLevel(this.player, weaponSkillType);
                  }

                  List<String> activeSkillIds = PlayerSkillSelection.getActiveSkills(this.player.getUniqueId(), weaponSkillType);
                  double damageMultiplier = 1.0;
                  StringBuilder debugLog = null;
                  if (debug) {
                     debugLog = new StringBuilder("[SkillBonuses] ");
                  }

                  for (String skillId : activeSkillIds) {
                     SkillBonus skill = SkillBonusRegistry.getSkillById(skillId);
                     if (skill != null && skill.isEnabled() && skill.meetsLevelRequirement(skillLevel)) {
                        if (skill instanceof DeathsEmbraceSkill deathsEmbrace) {
                           double passiveMultiplier = 1.0 + deathsEmbrace.getPassiveDamageBonus(skillLevel);
                           damageMultiplier = mergeOffensiveMultiplier(damageMultiplier, passiveMultiplier);
                           if (debug) {
                              debugLog.append(skill.getBonusName()).append("=").append(String.format("%.2fx", passiveMultiplier)).append(" ");
                           }
                        } else if (!skill.affectsDamage()) {
                           if (skill instanceof AvatarOfJudgmentSkill avatar && AvatarOfJudgmentSkill.hasAvatarBuff(this.player)) {
                              double avatarBoost = avatar.getDamageBoost(skillLevel);
                              if (debug) {
                                 debugLog.append(skill.getBonusName()).append("=").append(String.format("%.2fx", avatarBoost)).append(" ");
                              }

                              damageMultiplier = mergeOffensiveMultiplier(damageMultiplier, avatarBoost);
                           }
                        } else {
                           double skillMultiplier = this.processOffensiveSkill(skill, skillLevel);
                           if (debug && skillMultiplier != 1.0) {
                              debugLog.append(skill.getBonusName()).append("=").append(String.format("%.2fx", skillMultiplier)).append(" ");
                           }

                           damageMultiplier = mergeOffensiveMultiplier(damageMultiplier, skillMultiplier);
                        }
                     }
                  }

                  if (this.eliteMobEntity != null && this.eliteMobEntity.getLivingEntity() != null) {
                     LivingEntity target = this.eliteMobEntity.getLivingEntity();

                     for (String debuffSkillId : TARGET_DEBUFF_SKILL_IDS) {
                        if (SkillBonusRegistry.getSkillById(debuffSkillId) instanceof TargetDebuffBonus targetDebuff
                           && targetDebuff.appliesTo(target, this.player)) {
                           int debuffLevel = SkillBonusRegistry.getPlayerSkillLevel(this.player, targetDebuff.levelSource());
                           double debuffBonus = targetDebuff.bonusFor(this.player, target, debuffLevel);
                           damageMultiplier = mergeOffensiveMultiplier(damageMultiplier, 1.0 + debuffBonus);
                           if (debug) {
                              debugLog.append(targetDebuff.debugLabel()).append(String.format("+%.2f", debuffBonus)).append(" ");
                           }
                        }
                     }

                     if (weaponSkillType == SkillType.SWORDS && RiposteSkill.hasRiposteReady(this.player.getUniqueId())) {
                        SkillBonus riposteSkill = SkillBonusRegistry.getSkillById("swords_riposte");
                        if (riposteSkill != null
                           && riposteSkill.isActive(this.player)
                           && riposteSkill.meetsLevelRequirement(skillLevel)
                           && riposteSkill instanceof RiposteSkill riposte) {
                           riposte.onProc(this.player, this);
                        }
                     }
                  }

                  if (damageMultiplier != 1.0) {
                     double oldDamage = this.getDamage();
                     this.setDamage(oldDamage * damageMultiplier);
                     if (debug) {
                        debugLog.append("| Total=")
                           .append(String.format("%.2fx", damageMultiplier))
                           .append(" | Damage: ")
                           .append(String.format("%.1f", oldDamage))
                           .append(" -> ")
                           .append(String.format("%.1f", this.getDamage()));
                        DebugMessage.log(this.player, debugLog.toString());
                     }
                  }

                  for (String skillIdx : activeSkillIds) {
                     SkillBonus skill = SkillBonusRegistry.getSkillById(skillIdx);
                     if (skill != null && skill.isEnabled() && skill.meetsLevelRequirement(skillLevel) && !skill.affectsDamage()) {
                        this.processSideEffectSkill(skill, skillLevel);
                     }
                  }
               }
            }
         }
      }
   }

   private void processSideEffectSkill(SkillBonus skill, int skillLevel) {
      switch (skill.getBonusType()) {
         case PROC:
            if (skill instanceof ProcSkill procSkill && procSkill.canProc(this.player, this)) {
               double procChance = procSkill.getProcChance(skillLevel);
               if (ProcRoll.rolls(this.player, skill.getSkillId(), procChance)
                  && ProcCooldownTracker.tryConsume(this.player, skill.getSkillId(), procSkill.getInternalCooldownMillis())) {
                  procSkill.onProc(this.player, this);
                  skill.incrementProcCount(this.player);
                  SkillBonus.sendSkillActionBar(this.player, skill);
               }
            }
            break;
         case STACKING:
            if (skill instanceof StackingSkill stackingSkill) {
               if (skill instanceof RangersFocusSkill rf && this.getEliteMobEntity() != null && this.getEliteMobEntity().getLivingEntity() != null) {
                  rf.setTargetedEnemy(this.player, this.getEliteMobEntity().getLivingEntity().getUniqueId());
               }

               if (stackingSkill.stacksOnHit(this)) {
                  if (stackingSkill.banksStacksExternally()) {
                     skill.incrementProcCount(this.player);
                     SkillBonus.sendStackingSkillActionBar(this.player, skill, stackingSkill.getCurrentStacks(this.player), stackingSkill.getMaxStacks());
                  } else {
                     stackingSkill.addStack(this.player);
                     skill.incrementProcCount(this.player);
                     int stacks = stackingSkill.getCurrentStacks(this.player);
                     SkillBonus.sendStackingSkillActionBar(this.player, skill, stacks, stackingSkill.getMaxStacks());
                  }
               }
            }
            break;
         case CONDITIONAL:
            if (skill instanceof ConditionalSkill conditionalSkill && conditionalSkill.conditionMet(this.player, this)) {
               conditionalSkill.onConditionMet(this.player, this);
               skill.incrementProcCount(this.player);
               SkillBonus.sendSkillActionBar(this.player, skill);
            }
            break;
         case COOLDOWN:
            if (skill instanceof CooldownSkill cooldownSkill
               && cooldownSkill.triggersOnOffensiveHit()
               && !cooldownSkill.isOnCooldown(this.player)
               && cooldownSkill.tryActivate(this.player, this)
               && cooldownSkill.isOnCooldown(this.player)) {
               skill.incrementProcCount(this.player);
               SkillBonus.sendSkillActionBar(this.player, skill);
            }
            break;
         case PASSIVE:
            if (skill instanceof HeavyBoltsSkill hb && this.getEliteMobEntity() != null && this.getEliteMobEntity().getLivingEntity() != null) {
               hb.applyKnockback(this.player, this.getEliteMobEntity().getLivingEntity());
               skill.incrementProcCount(this.player);
               SkillBonus.sendSkillActionBar(this.player, skill);
            }

            if (skill instanceof StunningForceSkill sf) {
               sf.onHit(this.player, this);
               skill.incrementProcCount(this.player);
            }

            if (skill instanceof WindRunnerSkill wr) {
               wr.applySpeedBoost(this.player);
               skill.incrementProcCount(this.player);
               SkillBonus.sendSkillActionBar(this.player, skill);
            }

            if (skill instanceof QuickReloadSkill qr) {
               qr.applyHaste(this.player);
               skill.incrementProcCount(this.player);
               SkillBonus.sendSkillActionBar(this.player, skill);
            }
      }
   }

   private double processOffensiveSkill(SkillBonus skill, int skillLevel) {
      return switch (skill.getBonusType()) {
         case PROC -> {
            if (skill instanceof ProcSkill procSkill && procSkill.canProc(this.player, this)) {
               double procChance = procSkill.getProcChance(skillLevel);
               if (ProcRoll.rolls(this.player, skill.getSkillId(), procChance)
                  && ProcCooldownTracker.tryConsume(this.player, skill.getSkillId(), procSkill.getInternalCooldownMillis())) {
                  procSkill.onProc(this.player, this);
                  skill.incrementProcCount(this.player);
                  SkillBonus.sendSkillActionBar(this.player, skill);
                  yield 1.0 + skill.getBonusValue(skillLevel);
               }
            }

            yield 1.0;
         }
         case STACKING -> {
            if (skill instanceof StackingSkill stackingSkill) {
               if (!stackingSkill.stacksOnHit(this)) {
                  yield 1.0;
               } else {
                  if (skill instanceof RangersFocusSkill rf && this.eliteMobEntity != null && this.eliteMobEntity.getLivingEntity() != null) {
                     rf.setTargetedEnemy(this.player, this.eliteMobEntity.getLivingEntity().getUniqueId());
                  }

                  int stacks = stackingSkill.getCurrentStacks(this.player);
                  if (stackingSkill.banksStacksExternally()) {
                     skill.incrementProcCount(this.player);
                     SkillBonus.sendStackingSkillActionBar(this.player, skill, stacks, stackingSkill.getMaxStacks());
                  } else {
                     stackingSkill.addStack(this.player);
                     skill.incrementProcCount(this.player);
                     SkillBonus.sendStackingSkillActionBar(this.player, skill, stacks + 1, stackingSkill.getMaxStacks());
                  }

                  yield 1.0 + stacks * stackingSkill.getBonusPerStack(skillLevel);
               }
            } else {
               yield 1.0;
            }
         }
         case CONDITIONAL -> {
            if (skill instanceof ConditionalSkill conditionalSkill && conditionalSkill.conditionMet(this.player, this)) {
               conditionalSkill.onConditionMet(this.player, this);
               skill.incrementProcCount(this.player);
               SkillBonus.sendSkillActionBar(this.player, skill);
               yield 1.0 + conditionalSkill.getConditionalBonus(this.player, skillLevel);
            }

            yield 1.0;
         }
         case COOLDOWN -> {
            if (skill instanceof CooldownSkill cooldownSkill) {
               if (!cooldownSkill.triggersOnOffensiveHit()) {
                  yield 1.0;
               }

               if (cooldownSkill.sharesActivation(this.player, this)) {
                  yield 1.0 + skill.getBonusValue(skillLevel);
               }

               if (!cooldownSkill.isOnCooldown(this.player)) {
                  if (!cooldownSkill.tryActivate(this.player, this)) {
                     yield 1.0;
                  } else {
                     cooldownSkill.startCooldown(this.player, skillLevel);
                     skill.incrementProcCount(this.player);
                     SkillBonus.sendSkillActionBar(this.player, skill);
                     yield 1.0 + skill.getBonusValue(skillLevel);
                  }
               }
            }

            yield 1.0;
         }
         case PASSIVE -> {
            if (skill instanceof PoseidonsFavorSkill pf) {
               pf.onHit(this.player, this);
               SkillBonus.sendSkillActionBar(this.player, skill);
            }

            if (skill instanceof HeavyBoltsSkill hb && this.eliteMobEntity != null && this.eliteMobEntity.getLivingEntity() != null) {
               hb.applyKnockback(this.player, this.eliteMobEntity.getLivingEntity());
               SkillBonus.sendSkillActionBar(this.player, skill);
            }

            skill.incrementProcCount(this.player);
            yield 1.0 + skill.getBonusValue(skillLevel);
         }
      };
   }

   static SkillType getWeaponSkillType(Player player) {
      return WeaponIdentityResolver.progressionSkill(player.getInventory().getItemInMainHand());
   }

   @Generated
   public Entity getEntity() {
      return this.entity;
   }

   @Generated
   public EliteEntity getEliteMobEntity() {
      return this.eliteMobEntity;
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   @Generated
   public EntityDamageByEntityEvent getEntityDamageByEntityEvent() {
      return this.entityDamageByEntityEvent;
   }

   @Generated
   public boolean isCriticalStrike() {
      return this.criticalStrike;
   }

   @Generated
   public boolean isCriticalStrikeDamageApplied() {
      return this.criticalStrikeDamageApplied;
   }

   @Generated
   public boolean isCustomDamage() {
      return this.isCustomDamage;
   }

   @Generated
   public double getDamageModifier() {
      return this.damageModifier;
   }

   @Generated
   public boolean isRangedAttack() {
      return this.rangedAttack;
   }

   @Generated
   public SkillType getRangedSkillType() {
      return this.rangedSkillType;
   }

   @Generated
   public void setRangedSkillType(SkillType rangedSkillType) {
      this.rangedSkillType = rangedSkillType;
   }

   @Generated
   public int getRangedSkillLevel() {
      return this.rangedSkillLevel;
   }

   @Generated
   public void setRangedSkillLevel(int rangedSkillLevel) {
      this.rangedSkillLevel = rangedSkillLevel;
   }

   @Generated
   public CombatDamageContext.MagicHit getMagicHit() {
      return this.magicHit;
   }

   @Generated
   public void setMagicHit(CombatDamageContext.MagicHit magicHit) {
      this.magicHit = magicHit;
   }
}
