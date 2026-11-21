package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.combatsystem.ArmorDefenseCalculator;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.combatsystem.DamageBreakdown;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.easyminecraftgoals.NMSManager;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.GrimReachSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.LongReachSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.PolearmMasterySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.PoiseSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.SwiftStrikesSkill;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Generated;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

public class CombatSimulator {
   private static final String COMBAT_DUMMY_CONFIG = "training_dummy_combat_lv50.yml";
   private static final String DAMAGE_TEST_DUMMY_CONFIG = "damage_test_dummy.yml";
   private static UUID testingPlayer;
   private boolean blockingOverride = false;
   private final Player player;
   private final CombatTestEquipment equipment;
   private final CombatBreakdownDiagnostics breakdownDiagnostics;
   private final Map<String, CustomBossEntity> dummies = new HashMap<>();
   private final Map<String, Location> dummyLocations = new HashMap<>();
   private LivingEntity currentDummyEntity = null;
   private static final String DUMMY_CONFIG = "training_dummy_lv50.yml";
   private double testDamageOverride = -1.0;
   private static CombatSimulator.DamageOperation damageOperation;
   private Location waterBlockLocation = null;
   private static final double SPAWN_DISTANCE = 5.0;
   private BlockData savedBlockData = null;
   private double lastProjectileDamage = 0.0;
   private String lastProjectileTarget = null;
   private double lastTargetHealthBefore = 0.0;
   private static final double FATAL_HIT_STARTING_HEALTH = 5.0;

   public static boolean isTestingActive(Player player) {
      return player != null && player.getUniqueId().equals(testingPlayer);
   }

   public static boolean ownsIncomingDamage(EntityDamageByEntityEvent event) {
      CombatSimulator.DamageOperation operation = damageOperation;
      if (operation != null && operation.player == event.getEntity() && operation.attacker == event.getDamager()) {
         if (operation.event == null) {
            operation.event = event;
         }

         return operation.event == event;
      } else {
         return false;
      }
   }

   public static boolean isBlockingOverride(EntityDamageByEntityEvent event) {
      return ownsIncomingDamage(event) && damageOperation.blocking;
   }

   public static double getTestDamageOverride(EntityDamageByEntityEvent event) {
      return ownsIncomingDamage(event) ? damageOperation.damage : -1.0;
   }

   private void damagePlayer(double amount, LivingEntity attacker) {
      CombatSimulator.DamageOperation previous = damageOperation;
      damageOperation = new CombatSimulator.DamageOperation(this.player, attacker, this.testDamageOverride, this.blockingOverride);

      try {
         this.player.damage(amount, attacker);
      } finally {
         damageOperation = previous;
      }
   }

   public CombatSimulator(Player player) {
      this.player = player;
      this.equipment = new CombatTestEquipment(player);
      this.breakdownDiagnostics = new CombatBreakdownDiagnostics(this, player);
   }

   public boolean spawnSingleDummy(SkillBonus skill) {
      this.killCurrentDummy();
      this.removeAllDummies();
      this.resetProjectileTracking();
      Vector direction = this.player.getLocation().getDirection();
      direction.setY(0).normalize();
      Location spawnLoc = this.player.getLocation().add(direction.multiply(5));
      int highestY = spawnLoc.getWorld().getHighestBlockYAt(spawnLoc);
      spawnLoc.setY(highestY + 1);
      spawnLoc.setDirection(this.player.getLocation().toVector().subtract(spawnLoc.toVector()).normalize());
      return this.spawnDummyForSkill(skill, spawnLoc);
   }

   private void killCurrentDummy() {
      if (this.currentDummyEntity != null) {
         if (this.currentDummyEntity.isValid() && !this.currentDummyEntity.isDead()) {
            this.currentDummyEntity.setHealth(0.0);
            this.currentDummyEntity.remove();
         }

         this.currentDummyEntity = null;
      }
   }

   public void markDummyAsTesting(String skillId, String skillName, SkillType skillType) {
      CustomBossEntity dummy = this.dummies.get(skillId);
      if (dummy != null && dummy.isValid() && dummy.getLivingEntity() != null) {
         String displayName = "§6⚔ " + skillType.getDisplayName() + " §7- §e" + skillName + " §6[TESTING]";
         dummy.getLivingEntity().setCustomName(displayName);
      }
   }

   public void markDummyAsPassed(String skillId, String skillName, SkillType skillType, double procRate) {
      CustomBossEntity dummy = this.dummies.get(skillId);
      if (dummy != null && dummy.isValid() && dummy.getLivingEntity() != null) {
         String displayName = String.format("§a✓ %s §7- §f%s §a[%.0f%%]", skillType.getDisplayName(), skillName, procRate * 100.0);
         dummy.getLivingEntity().setCustomName(displayName);
         dummy.getLivingEntity().setGlowing(false);
      }
   }

   public void markDummyAsFailed(String skillId, String skillName, SkillType skillType, String reason) {
      CustomBossEntity dummy = this.dummies.get(skillId);
      if (dummy != null && dummy.isValid() && dummy.getLivingEntity() != null) {
         String displayName = "§c✗ " + skillType.getDisplayName() + " §7- §f" + skillName + " §c[FAILED]";
         dummy.getLivingEntity().setCustomName(displayName);
      }
   }

   public CustomBossEntity getDummy(String skillId) {
      return this.dummies.get(skillId);
   }

   public LivingEntity getDummyEntity(String skillId) {
      CustomBossEntity dummy = this.dummies.get(skillId);
      return dummy != null && dummy.isValid() ? dummy.getLivingEntity() : null;
   }

   public boolean hasDummy(String skillId) {
      LivingEntity entity = this.getDummyEntity(skillId);
      return entity != null && entity.isValid() && !entity.isDead();
   }

   public void healDummy(String skillId) {
      CustomBossEntity dummy = this.dummies.get(skillId);
      if (dummy != null && dummy.isValid() && dummy.getLivingEntity() != null) {
         dummy.fullHeal();
      }
   }

   static void setTestingActive(Player player, boolean active) {
      if (active) {
         testingPlayer = player.getUniqueId();
      } else if (isTestingActive(player)) {
         testingPlayer = null;
      }
   }

   public boolean respawnIfDead(String skillId, SkillBonus skill) {
      CustomBossEntity dummy = this.dummies.get(skillId);
      if (dummy != null && dummy.isValid() && dummy.getLivingEntity() != null && !dummy.getLivingEntity().isDead()) {
         return true;
      } else {
         Location spawnLoc = this.dummyLocations.get(skillId);
         if (spawnLoc == null) {
            spawnLoc = this.player.getLocation().add(5.0, 0.0, 0.0);
         }

         this.dummies.remove(skillId);
         if (skillId.equals(this.lastProjectileTarget)) {
            this.lastProjectileTarget = null;
            this.lastTargetHealthBefore = 0.0;
         }

         return this.spawnDummyForSkill(skill, spawnLoc);
      }
   }

   public void removeDummy(String skillId) {
      CustomBossEntity dummy = this.dummies.remove(skillId);
      if (dummy != null && dummy.isValid() && dummy.getLivingEntity() != null) {
         LivingEntity entity = dummy.getLivingEntity();
         entity.setHealth(0.0);
         entity.remove();
         if (entity == this.currentDummyEntity) {
            this.currentDummyEntity = null;
         }
      }

      this.dummyLocations.remove(skillId);
   }

   public void removeAllDummies() {
      for (CustomBossEntity dummy : this.dummies.values()) {
         if (dummy != null && dummy.isValid() && dummy.getLivingEntity() != null) {
            LivingEntity entity = dummy.getLivingEntity();
            entity.setHealth(0.0);
            entity.remove();
         }
      }

      this.dummies.clear();
      this.dummyLocations.clear();
      this.killCurrentDummy();
   }

   public static double calculateExpectedDefensiveDamage(int armorSkillLevel, int mobLevel, Player player) {
      double playerMaxHealth = 20.0 + Math.max(0, armorSkillLevel - 1) * 2.0;
      double baseDamage = playerMaxHealth / 5.0;
      double skillAdjustment = Math.pow(2.0, (mobLevel - armorSkillLevel) / 7.5);
      double gearScore = ArmorDefenseCalculator.getGearScore(player, ArmorDefenseCalculator.DamageType.MELEE);
      double gearAdjustment = ArmorDefenseCalculator.getGearAdjustment(gearScore, mobLevel);
      double rawDamage = Math.max(baseDamage * skillAdjustment * gearAdjustment, 1.0);
      return Math.min(rawDamage, playerMaxHealth - 1.0);
   }

   public void equipWeapon(SkillType skillType) {
      this.equipment.equipWeapon(skillType);
   }

   private boolean spawnDummyForSkill(SkillBonus skill, Location location) {
      String configToUse = "damage_test_dummy.yml";
      CustomBossesConfigFields config = CustomBossesConfig.getCustomBoss(configToUse);
      if (config == null) {
         configToUse = skill.getSkillType() == SkillType.ARMOR ? "training_dummy_combat_lv50.yml" : "training_dummy_lv50.yml";
         config = CustomBossesConfig.getCustomBoss(configToUse);
      }

      if (config == null) {
         config = CustomBossesConfig.getCustomBoss("training_dummy_lv50.yml");
         configToUse = "training_dummy_lv50.yml";
      }

      if (config == null) {
         return false;
      } else {
         CustomBossEntity dummy = RegionalBossEntity.createTemporaryRegionalBossEntity(configToUse, location);
         if (dummy == null) {
            return false;
         } else {
            dummy.spawn(true);
            if (dummy.getLivingEntity() != null) {
               LivingEntity entity = dummy.getLivingEntity();
               entity.setMaximumNoDamageTicks(0);
               NMSManager.getAdapter().setDamageCooldownTicks(entity, 0);
               String skillName = skill.getBonusName();
               String typeName = skill.getSkillType().getDisplayName();
               String displayName = "§e" + typeName + " §7- §f" + skillName + " §8[Pending]";
               entity.setCustomName(displayName);
               entity.setCustomNameVisible(true);
               entity.setGlowing(true);
               this.currentDummyEntity = entity;
               this.dummies.put(skill.getSkillId(), dummy);
               this.dummyLocations.put(skill.getSkillId(), location);
               return true;
            } else {
               return false;
            }
         }
      }
   }

   public void equipArmorSet(int level) {
      this.equipment.equipArmorSet(level);
   }

   public void savePlayerArmor() {
      this.equipment.capture();
   }

   public void restorePlayerArmor() {
      this.equipment.restore();
   }

   public double simulateMeleeAttack(String skillId) {
      LivingEntity target = this.getDummyEntity(skillId);
      if (target != null && target.isValid()) {
         NMSManager.getAdapter().setDamageCooldownTicks(target, 0);
         double healthBefore = target.getHealth();
         this.player.attack(target);
         double healthAfter = target.getHealth();
         return Math.max(0.0, healthBefore - healthAfter);
      } else {
         return -1.0;
      }
   }

   public void setDummyHealthPercent(String skillId, double percent) {
      CustomBossEntity dummy = this.dummies.get(skillId);
      if (dummy != null && dummy.isValid() && dummy.getLivingEntity() != null) {
         LivingEntity entity = dummy.getLivingEntity();
         entity.setHealth(Math.max(1.0, entity.getMaxHealth() * percent));
      }
   }

   public void resetProjectileTracking() {
      this.lastProjectileDamage = 0.0;
      this.lastProjectileTarget = null;
      this.lastTargetHealthBefore = 0.0;
   }

   public double simulateRangedAttack(String skillId) {
      LivingEntity target = this.getDummyEntity(skillId);
      if (target != null && target.isValid()) {
         NMSManager.getAdapter().setDamageCooldownTicks(target, 0);
         double healthBefore = target.getHealth();
         EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(target);
         if (eliteEntity != null && eliteEntity.isValid()) {
            double baseDamage = 8.0;
            EliteMobDamagedByPlayerEvent event = new EliteMobDamagedByPlayerEvent(eliteEntity, this.player, baseDamage, true);
            event.applySkillBonuses();
            double finalDamage = event.getDamage();
            CombatDamageContext.runPlayerToEliteBypass(() -> target.damage(finalDamage, this.player));
            double healthAfter = target.getHealth();
            return Math.max(0.0, healthBefore - healthAfter);
         } else {
            return -1.0;
         }
      } else {
         return -1.0;
      }
   }

   public double simulateTridentAttack(String skillId) {
      LivingEntity target = this.getDummyEntity(skillId);
      if (target != null && target.isValid()) {
         NMSManager.getAdapter().setDamageCooldownTicks(target, 0);
         double healthBefore = target.getHealth();
         EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(target);
         if (eliteEntity != null && eliteEntity.isValid()) {
            double baseDamage = 8.0;
            EliteMobDamagedByPlayerEvent event = new EliteMobDamagedByPlayerEvent(eliteEntity, this.player, baseDamage, true);
            event.applySkillBonuses();
            double finalDamage = event.getDamage();
            CombatDamageContext.runPlayerToEliteBypass(() -> target.damage(finalDamage, this.player));
            double healthAfter = target.getHealth();
            return Math.max(0.0, healthBefore - healthAfter);
         } else {
            return -1.0;
         }
      } else {
         return -1.0;
      }
   }

   public double simulateIncomingDamage(String skillId, double amount) {
      LivingEntity attacker = this.getDummyEntity(skillId);
      if (attacker == null) {
         return 0.0;
      } else {
         NMSManager.getAdapter().setDamageCooldownTicks(this.player, 0);
         this.player.setHealth(this.player.getMaxHealth());
         this.player.setAbsorptionAmount(1000.0);
         double healthBefore = this.player.getHealth();
         double absorptionBefore = this.player.getAbsorptionAmount();
         double totalBefore = healthBefore + absorptionBefore;
         this.damagePlayer(amount, attacker);
         double healthAfter = this.player.getHealth();
         double absorptionAfter = this.player.getAbsorptionAmount();
         double totalAfter = healthAfter + absorptionAfter;
         double actualDamage = totalBefore - totalAfter;
         this.player.setHealth(this.player.getMaxHealth());
         this.player.setAbsorptionAmount(0.0);
         return Math.max(0.0, actualDamage);
      }
   }

   public void cleanupTestEntities() {
      for (Entity entity : this.player.getNearbyEntities(30.0, 30.0, 30.0)) {
         if (entity instanceof Projectile projectile && projectile.getShooter() == this.player) {
            entity.remove();
         } else if (entity instanceof AreaEffectCloud cloud) {
            ProjectileSource source = cloud.getSource();
            if (source == this.player) {
               entity.remove();
            }
         }
      }
   }

   public void applyPassiveWeaponEffects(SkillType skillType, int skillLevel) {
      UUID uuid = this.player.getUniqueId();
      switch (skillType) {
         case SWORDS:
            if (SwiftStrikesSkill.hasActiveSkill(uuid)) {
               SwiftStrikesSkill.applySpeedBonus(this.player, skillLevel);
            }

            if (PoiseSkill.hasActiveSkill(uuid)) {
               PoiseSkill.applyKnockbackResistance(this.player, skillLevel);
            }
            break;
         case HOES:
            GrimReachSkill.applyReachBonus(this.player, skillLevel);
            break;
         case SPEARS:
            LongReachSkill.applyReachBonus(this.player, skillLevel);
            if (PolearmMasterySkill.hasActiveSkill(uuid)) {
               PolearmMasterySkill.applyAttackSpeedBonus(this.player, skillLevel);
            }
      }
   }

   public double simulateDirectAttack(String skillId) {
      LivingEntity target = this.getDummyEntity(skillId);
      if (target != null && target.isValid()) {
         NMSManager.getAdapter().setDamageCooldownTicks(target, 0);
         double healthBefore = target.getHealth();
         EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(target);
         if (eliteEntity != null && eliteEntity.isValid()) {
            double baseDamage = 8.0;
            EliteMobDamagedByPlayerEvent event = new EliteMobDamagedByPlayerEvent(eliteEntity, this.player, baseDamage, false);
            event.applySkillBonuses();
            double finalDamage = event.getDamage();
            double newHealth = Math.max(1.0, target.getHealth() - finalDamage);
            target.setHealth(newHealth);
            double healthAfter = target.getHealth();
            return Math.max(0.0, healthBefore - healthAfter);
         } else {
            return -1.0;
         }
      } else {
         return -1.0;
      }
   }

   public int getDummyCount() {
      return this.dummies.size();
   }

   public boolean simulateKill(String skillId) {
      LivingEntity target = this.getDummyEntity(skillId);
      if (target != null && target.isValid() && !target.isDead()) {
         EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(target);
         if (eliteEntity != null && eliteEntity.isValid()) {
            NMSManager.getAdapter().setDamageCooldownTicks(target, 0);
            target.setHealth(1.0);
            this.player.attack(target);
            boolean died = target.isDead() || target.getHealth() <= 0.0;
            if (!died) {
               Logger.warn("[SkillTest] simulateKill: dummy " + skillId + " survived a killing blow (health=" + target.getHealth() + ")");
            }

            return died;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public double simulateIncomingDamage(String skillId, double amount, double healthPercent) {
      LivingEntity attacker = this.getDummyEntity(skillId);
      if (attacker == null) {
         return 0.0;
      } else {
         NMSManager.getAdapter().setDamageCooldownTicks(this.player, 0);
         this.player.setHealth(this.player.getMaxHealth() * healthPercent);
         this.player.setAbsorptionAmount(1000.0);
         double healthBefore = this.player.getHealth();
         double absorptionBefore = this.player.getAbsorptionAmount();
         double totalBefore = healthBefore + absorptionBefore;
         this.testDamageOverride = amount;

         try {
            this.damagePlayer(amount, attacker);
         } finally {
            this.testDamageOverride = -1.0;
         }

         double healthAfter = this.player.getHealth();
         double absorptionAfter = this.player.getAbsorptionAmount();
         double totalAfter = healthAfter + absorptionAfter;
         double actualDamage = totalBefore - totalAfter;
         this.player.setHealth(this.player.getMaxHealth() * healthPercent);
         this.player.setAbsorptionAmount(0.0);
         return Math.max(0.0, actualDamage);
      }
   }

   public double simulateIncomingDamageWithOverride(String skillId, double amount) {
      LivingEntity attacker = this.getDummyEntity(skillId);
      return attacker == null ? 0.0 : this.takeHitWithOverride(attacker, amount);
   }

   double takeHitWithOverride(LivingEntity attacker, double amount) {
      NMSManager.getAdapter().setDamageCooldownTicks(this.player, 0);
      this.player.setHealth(this.player.getMaxHealth());
      this.player.setAbsorptionAmount(1000.0);
      double healthBefore = this.player.getHealth();
      double absorptionBefore = this.player.getAbsorptionAmount();
      double totalBefore = healthBefore + absorptionBefore;
      this.testDamageOverride = amount;

      try {
         this.damagePlayer(amount, attacker);
      } finally {
         this.testDamageOverride = -1.0;
      }

      double healthAfter = this.player.getHealth();
      double absorptionAfter = this.player.getAbsorptionAmount();
      double totalAfter = healthAfter + absorptionAfter;
      double actualDamage = totalBefore - totalAfter;
      this.player.setHealth(this.player.getMaxHealth());
      this.player.setAbsorptionAmount(0.0);
      return Math.max(0.0, actualDamage);
   }

   public double simulateHighDamageWithOverride(String skillId, double amount) {
      return this.simulateIncomingDamageWithOverride(skillId, amount);
   }

   public void placeWaterAtDummy(String skillId) {
      LivingEntity entity = this.getDummyEntity(skillId);
      if (entity != null) {
         Location loc = entity.getLocation().getBlock().getLocation();
         if (!loc.equals(this.waterBlockLocation)) {
            this.restoreWaterBlock();
            if (!loc.getBlock().getType().isAir()) {
               throw new IllegalStateException("Diagnostic water requires an empty block");
            } else {
               this.savedBlockData = loc.getBlock().getBlockData().clone();
               this.waterBlockLocation = loc;
               loc.getBlock().setType(Material.WATER, false);
            }
         }
      }
   }

   public void restoreWaterBlock() {
      if (this.waterBlockLocation != null && this.savedBlockData != null) {
         if (this.waterBlockLocation.getBlock().getBlockData().equals(Material.WATER.createBlockData())) {
            this.waterBlockLocation.getBlock().setBlockData(this.savedBlockData, false);
         }

         this.waterBlockLocation = null;
         this.savedBlockData = null;
      }
   }

   public double simulateCriticalAttack(String skillId) {
      LivingEntity target = this.getDummyEntity(skillId);
      if (target != null && target.isValid()) {
         NMSManager.getAdapter().setDamageCooldownTicks(target, 0);
         double healthBefore = target.getHealth();
         EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(target);
         if (eliteEntity != null && eliteEntity.isValid()) {
            double baseDamage = 8.0;
            EliteMobDamagedByPlayerEvent event = new EliteMobDamagedByPlayerEvent(eliteEntity, this.player, baseDamage, false, true);
            event.applySkillBonuses();
            double finalDamage = event.getDamage();
            double newHealth = Math.max(1.0, target.getHealth() - finalDamage);
            target.setHealth(newHealth);
            double healthAfter = target.getHealth();
            return Math.max(0.0, healthBefore - healthAfter);
         } else {
            return -1.0;
         }
      } else {
         return -1.0;
      }
   }

   public double simulateFatalIncomingDamage(String skillId) {
      LivingEntity attacker = this.getDummyEntity(skillId);
      if (attacker == null) {
         Logger.warn("[SkillTest] simulateFatalIncomingDamage: getDummyEntity returned null for " + skillId);
         return 0.0;
      } else {
         return Math.max(0.0, 5.0 - this.takeFatalHit(attacker));
      }
   }

   double takeFatalHit(LivingEntity attacker) {
      NMSManager.getAdapter().setDamageCooldownTicks(this.player, 0);
      this.player.setHealth(5.0);
      double fatalDamage = this.player.getMaxHealth() * 2.0;
      this.player.setAbsorptionAmount(fatalDamage + 1.0);
      this.testDamageOverride = fatalDamage;

      try {
         this.damagePlayer(fatalDamage, attacker);
      } finally {
         this.testDamageOverride = -1.0;
      }

      double healthAfter = this.player.getHealth();
      this.player.setHealth(this.player.getMaxHealth());
      this.player.setAbsorptionAmount(0.0);
      return healthAfter;
   }

   double takeProjectileHitWithOverride(LivingEntity shooter, double amount) {
      Arrow arrow = (Arrow)shooter.getWorld().spawn(shooter.getEyeLocation(), Arrow.class, spawned -> {
         spawned.setShooter(shooter);
         spawned.setGravity(false);
         spawned.setVelocity(new Vector());
      });

      double var10;
      try {
         NMSManager.getAdapter().setDamageCooldownTicks(this.player, 0);
         this.player.setHealth(this.player.getMaxHealth());
         this.player.setAbsorptionAmount(1000.0);
         double totalBefore = this.player.getHealth() + this.player.getAbsorptionAmount();
         CombatSimulator.DamageOperation previous = damageOperation;
         damageOperation = new CombatSimulator.DamageOperation(this.player, arrow, amount, false);

         try {
            this.player.damage(amount, DamageSource.builder(DamageType.ARROW).withDirectEntity(arrow).withCausingEntity(shooter).build());
         } finally {
            damageOperation = previous;
         }

         double taken = totalBefore - (this.player.getHealth() + this.player.getAbsorptionAmount());
         this.player.setHealth(this.player.getMaxHealth());
         this.player.setAbsorptionAmount(0.0);
         var10 = Math.max(0.0, taken);
      } finally {
         arrow.remove();
      }

      return var10;
   }

   void holdTestItem(ItemStack item) {
      this.equipment.hold(item);
   }

   public DamageBreakdown simulateMeleeAttackWithBreakdown(String skillId) {
      return this.breakdownDiagnostics.simulateMeleeAttack(skillId);
   }

   public String simulateMultipleAttacksWithBreakdown(String skillId, int hitCount) {
      return this.breakdownDiagnostics.simulateMultipleAttacks(skillId, hitCount);
   }

   public void sendBreakdownToPlayer(DamageBreakdown breakdown) {
      this.breakdownDiagnostics.sendToPlayer(breakdown);
   }

   public void runQuickDamageTest(int dummyLevel) {
      this.breakdownDiagnostics.runQuickDamageTest(dummyLevel);
   }

   public boolean hasActiveBreakdownTracking() {
      return this.breakdownDiagnostics.isTracking();
   }

   public DamageBreakdown stopBreakdownTracking() {
      return this.breakdownDiagnostics.stopTracking();
   }

   public double simulateBlockingIncomingDamage(String skillId, double amount) {
      this.equipment.equipWeapon(SkillType.SWORDS);
      this.blockingOverride = true;
      this.testDamageOverride = amount;

      double var4;
      try {
         var4 = this.simulateIncomingDamage(skillId, amount);
      } finally {
         this.blockingOverride = false;
         this.testDamageOverride = -1.0;
      }

      return var4;
   }

   public boolean spawnDamageTestDummy(String skillId) {
      this.killCurrentDummy();
      this.removeAllDummies();
      this.resetProjectileTracking();
      Vector direction = this.player.getLocation().getDirection();
      direction.setY(0).normalize();
      Location spawnLoc = this.player.getLocation().add(direction.multiply(5));
      int highestY = spawnLoc.getWorld().getHighestBlockYAt(spawnLoc);
      spawnLoc.setY(highestY + 1);
      spawnLoc.setDirection(this.player.getLocation().toVector().subtract(spawnLoc.toVector()).normalize());
      String configToUse = "damage_test_dummy.yml";
      CustomBossesConfigFields config = CustomBossesConfig.getCustomBoss(configToUse);
      if (config == null) {
         config = CustomBossesConfig.getCustomBoss("training_dummy_combat_lv50.yml");
         configToUse = "training_dummy_combat_lv50.yml";
      }

      if (config == null) {
         config = CustomBossesConfig.getCustomBoss("training_dummy_lv50.yml");
         configToUse = "training_dummy_lv50.yml";
      }

      if (config == null) {
         return false;
      } else {
         CustomBossEntity dummy = RegionalBossEntity.createTemporaryRegionalBossEntity(configToUse, spawnLoc);
         if (dummy == null) {
            return false;
         } else {
            dummy.spawn(true);
            if (dummy.getLivingEntity() != null) {
               LivingEntity entity = dummy.getLivingEntity();
               entity.setMaximumNoDamageTicks(0);
               NMSManager.getAdapter().setDamageCooldownTicks(entity, 0);
               entity.setCustomName("§eDamage Test Dummy §7[Baseline]");
               entity.setCustomNameVisible(true);
               entity.setGlowing(true);
               this.currentDummyEntity = entity;
               this.dummies.put(skillId, dummy);
               this.dummyLocations.put(skillId, spawnLoc);
               return true;
            } else {
               return false;
            }
         }
      }
   }

   @Generated
   public Player getPlayer() {
      return this.player;
   }

   private static final class DamageOperation {
      private final Player player;
      private final Entity attacker;
      private final double damage;
      private final boolean blocking;
      private EntityDamageByEntityEvent event;

      private DamageOperation(Player player, Entity attacker, double damage, boolean blocking) {
         this.player = player;
         this.attacker = attacker;
         this.damage = damage;
         this.blocking = blocking;
      }
   }
}
