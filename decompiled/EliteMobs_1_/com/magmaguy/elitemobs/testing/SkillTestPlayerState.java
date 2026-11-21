package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.ArmorSkillHealthBonus;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.PlayerSkillSelection;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.GrimReachSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.LongReachSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.PolearmMasterySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.FlurrySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.PoiseSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.SwiftStrikesSkill;
import com.magmaguy.elitemobs.utils.GameClock;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

final class SkillTestPlayerState {
   private Player player;
   private final UUID playerId;
   private final CombatSimulator simulator;
   private final Map<SkillType, Long> skillXp = new EnumMap<>(SkillType.class);
   private final Map<SkillType, List<String>> skillSelections = new EnumMap<>(SkillType.class);
   private double attackSpeed;
   private double knockbackResistance;
   private double maxHealth;
   private double maxAbsorption;
   private float walkSpeed;
   private boolean captured;
   private double health;
   private double absorption;
   private long absorptionExpiresAt = Long.MAX_VALUE;
   private LinkedHashMap<String, Runnable> restoration;

   SkillTestPlayerState(Player player, CombatSimulator simulator) {
      this.player = player;
      this.playerId = player.getUniqueId();
      this.simulator = simulator;
   }

   void captureAndPrepare() {
      if (!this.captured) {
         for (SkillType type : SkillType.values()) {
            this.skillXp.put(type, PlayerData.getSkillXP(this.playerId, type));
            this.skillSelections.put(type, new ArrayList<>(PlayerSkillSelection.getActiveSkills(this.playerId, type)));
         }

         this.attackSpeed = this.baseValue(Attribute.ATTACK_SPEED);
         this.knockbackResistance = this.baseValue(Attribute.KNOCKBACK_RESISTANCE);
         this.maxHealth = this.baseValue(Attribute.MAX_HEALTH);
         this.maxAbsorption = this.baseValue(Attribute.MAX_ABSORPTION);
         this.walkSpeed = this.player.getWalkSpeed();
         this.health = this.player.getHealth();
         this.absorption = this.player.getAbsorptionAmount();
         PotionEffect effect = this.player.getPotionEffect(PotionEffectType.ABSORPTION);
         if (effect != null && !effect.isInfinite()) {
            this.absorptionExpiresAt = GameClock.getCurrentTick() + effect.getDuration();
         }

         this.captured = true;
         this.simulator.savePlayerArmor();
         this.setBaseValue(Attribute.ATTACK_SPEED, 100.0);
         this.setBaseValue(Attribute.KNOCKBACK_RESISTANCE, 1.0);
         this.setBaseValue(Attribute.MAX_ABSORPTION, 2000.0);
      }
   }

   void restore() {
      if (this.captured) {
         Player current = Bukkit.getPlayer(this.playerId);
         if (current != null) {
            this.player = current;
         }

         if (this.restoration == null) {
            this.prepareRestoration();
         }

         RuntimeException failure = null;

         for (Entry<String, Runnable> entry : new ArrayList<>(this.restoration.entrySet())) {
            try {
               entry.getValue().run();
               this.restoration.remove(entry.getKey());
            } catch (RuntimeException var6) {
               if (failure == null) {
                  failure = new IllegalStateException("Unfinished diagnostic player restoration");
               }

               failure.addSuppressed(new IllegalStateException(entry.getKey(), var6));
            }
         }

         if (failure != null) {
            throw failure;
         } else {
            this.captured = false;
         }
      }
   }

   private void prepareRestoration() {
      this.restoration = new LinkedHashMap<>();
      this.restoration.put("remove test bonuses", () -> SkillBonusRegistry.removeAllBonuses(this.player));

      for (SkillType type : SkillType.values()) {
         this.restoration.put("skill " + type, () -> {
            Long savedXp = this.skillXp.get(type);
            if (savedXp != null) {
               PlayerData.setSkillXP(this.playerId, type, savedXp);
            }

            for (String id : new ArrayList<>(PlayerSkillSelection.getActiveSkills(this.playerId, type))) {
               PlayerSkillSelection.removeActiveSkill(this.playerId, type, id);
            }

            for (String id : this.skillSelections.getOrDefault(type, List.of())) {
               PlayerSkillSelection.addActiveSkill(this.playerId, type, id);
            }
         });
      }

      this.restoration.put("attack speed", () -> this.setBaseValue(Attribute.ATTACK_SPEED, this.attackSpeed));
      this.restoration.put("knockback resistance", () -> this.setBaseValue(Attribute.KNOCKBACK_RESISTANCE, this.knockbackResistance));
      this.restoration.put("maximum health", () -> this.setBaseValue(Attribute.MAX_HEALTH, this.maxHealth));
      this.restoration.put("maximum absorption", () -> this.setBaseValue(Attribute.MAX_ABSORPTION, this.maxAbsorption));
      this.restoration.put("walk speed", () -> this.player.setWalkSpeed(this.walkSpeed));
      this.restoration.put("passive modifiers", this::removePassiveModifiers);
      this.restoration.put("equipment", this.simulator::restorePlayerArmor);
      this.restoration.put("original bonuses", () -> {
         if (this.restoration.keySet().stream().anyMatch(key -> !key.equals("original bonuses") && !key.equals("vitals"))) {
            throw new IllegalStateException("Original bonuses await restored skills, attributes and equipment");
         } else {
            ArmorSkillHealthBonus.applyHealthBonus(this.player);
            SkillBonusRegistry.applyAllBonuses(this.player);
         }
      });
      this.restoration.put("vitals", () -> {
         if (this.restoration.size() != 1) {
            throw new IllegalStateException("Vitals await restored maximum attributes");
         } else if (this.player.isDead()) {
            throw new IllegalStateException("Cannot restore vitals of a dead diagnostic player");
         } else {
            this.player.setHealth(Math.min(this.health, this.player.getMaxHealth()));
            AttributeInstance maximum = this.player.getAttribute(Attribute.MAX_ABSORPTION);
            double allowed = maximum == null ? 0.0 : maximum.getValue();
            double remaining = GameClock.getCurrentTick() < this.absorptionExpiresAt ? this.absorption : 0.0;
            this.player.setAbsorptionAmount(Math.min(remaining, allowed));
         }
      });
   }

   private double baseValue(Attribute attribute) {
      AttributeInstance instance = this.player.getAttribute(attribute);
      return instance == null ? 0.0 : instance.getBaseValue();
   }

   private void setBaseValue(Attribute attribute, double value) {
      AttributeInstance instance = this.player.getAttribute(attribute);
      if (instance != null) {
         instance.setBaseValue(value);
      }
   }

   private void removePassiveModifiers() {
      SwiftStrikesSkill.removeSpeedBonus(this.player);
      PoiseSkill.removeKnockbackResistance(this.player);
      FlurrySkill.removeAttackSpeedModifier(this.player);
      GrimReachSkill.removeReachBonus(this.player);
      LongReachSkill.removeReachBonus(this.player);
      PolearmMasterySkill.removeAttackSpeedBonus(this.player);
   }
}
