package com.magmaguy.elitemobs.advancedcombat.weapons;

import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatEnemyAuthorization;
import com.magmaguy.elitemobs.advancedcombat.damage.AdvancedDamageScaling;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEventFilter;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.ItemDurability;
import com.magmaguy.elitemobs.magmacore.util.Logger;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.freeminecraftmodels.api.magic.MagicAttackKind;
import com.magmaguy.freeminecraftmodels.api.magic.MagicAttackRequest;
import com.magmaguy.freeminecraftmodels.api.magic.MagicAttackResolver;
import com.magmaguy.freeminecraftmodels.api.magic.MagicDamageApplication;
import com.magmaguy.freeminecraftmodels.api.magic.MagicTargetRequest;
import com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponAPI;
import com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponKind;
import java.util.Map;
import java.util.Objects;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

final class FmmMagicWeaponAdapter implements AdvancedMagicWeaponIntegration.Connection, MagicAttackResolver {
   private static final int REQUIRED_CAPABILITY_VERSION = 7;
   private final Plugin owner;
   private volatile boolean registered;
   private boolean calculationWarningSent;

   private FmmMagicWeaponAdapter(Plugin owner) {
      this.owner = Objects.requireNonNull(owner, "owner");
   }

   static AdvancedMagicWeaponIntegration.ConnectAttempt connect(Plugin owner) {
      if (MagicWeaponAPI.capabilityVersion() < 7) {
         return AdvancedMagicWeaponIntegration.ConnectAttempt.unavailable(AdvancedMagicWeaponIntegration.ConnectStatus.INCOMPATIBLE);
      } else if (!MagicWeaponAPI.isServiceAvailable()) {
         return AdvancedMagicWeaponIntegration.ConnectAttempt.unavailable(AdvancedMagicWeaponIntegration.ConnectStatus.WAITING_FOR_SERVICE);
      } else if (!MagicWeaponAPI.isOperational()) {
         return AdvancedMagicWeaponIntegration.ConnectAttempt.unavailable(AdvancedMagicWeaponIntegration.ConnectStatus.CONTENT_UNAVAILABLE);
      } else {
         FmmMagicWeaponAdapter adapter = new FmmMagicWeaponAdapter(owner);
         if (!MagicWeaponAPI.registerResolver(owner, adapter)) {
            return AdvancedMagicWeaponIntegration.ConnectAttempt.unavailable(AdvancedMagicWeaponIntegration.ConnectStatus.REGISTRATION_REJECTED);
         } else {
            adapter.registered = true;
            return AdvancedMagicWeaponIntegration.ConnectAttempt.connected(adapter);
         }
      }
   }

   @Override
   public boolean isOperational() {
      return this.registered && this.owner.isEnabled() && MagicWeaponAPI.isOperational();
   }

   public Map<String, Double> capture(Player player, ItemStack weapon, MagicAttackKind attackKind) {
      ElitePlayerInventory inventory = ElitePlayerInventory.getPlayer(player);
      int skillLevel = 1;
      if (!SkillsConfig.isWorldExcludedFromSkills(player) && PlayerData.isDataLoaded(player.getUniqueId())) {
         skillLevel = Math.max(1, PlayerData.getSkillLevel(player.getUniqueId(), progressionSkill(attackKind.weaponKind())));
      }

      return Map.of(
         "critical",
         EliteMobDamagedByPlayerEventFilter.captureCriticalHit(player) ? 1.0 : 0.0,
         "loud_strikes",
         inventory == null ? 0.0 : inventory.getLoudStrikesBonusMultiplier(true),
         "skill_level",
         (double)skillLevel
      );
   }

   public void resolve(MagicAttackRequest request, MagicDamageApplication application) {
      Objects.requireNonNull(request, "request");
      Objects.requireNonNull(application, "application");
      if (!EliteItemManager.isOnLastDamage(request.weapon())) {
         SkillType progressionSkill = progressionSkill(request.attackKind().weaponKind());

         EliteEntity elite;
         try {
            elite = EntityTracker.getEliteMobEntity(request.target());
            if (elite != null && !elite.isValid()) {
               elite = null;
            }
         } catch (RuntimeException var9) {
            this.warnCalculationFailure(var9);
            application.apply(0.0);
            return;
         }

         if (elite == null) {
            application.apply(request.balance().standaloneDamage() * EliteItemManager.getEliteDamageEnchantmentMultiplier(request.weapon()));
         } else {
            double damage;
            try {
               damage = this.scaledDamage(request, elite, progressionSkill);
            } catch (RuntimeException var8) {
               this.warnCalculationFailure(var8);
               application.apply(0.0);
               return;
            }

            applyWithEliteMobsBypass(request, progressionSkill, damage, application);
         }
      }
   }

   public boolean canAttack(Player player, ItemStack weapon, MagicAttackKind attackKind) {
      ItemDurability.prepareMagicWeapon(weapon);
      return !EliteItemManager.isOnLastDamage(weapon);
   }

   public boolean isTargetEligible(MagicTargetRequest request) {
      Objects.requireNonNull(request, "request");
      return AdvancedCombatEnemyAuthorization.canTargetWithMagicWeapon(request.attacker(), request.target());
   }

   public int targetPriority(MagicTargetRequest request) {
      Objects.requireNonNull(request, "request");

      try {
         EliteEntity elite = EntityTracker.getEliteMobEntity(request.target());
         if (elite != null && elite.isValid()) {
            return 0;
         }
      } catch (RuntimeException var3) {
      }

      return MagicAttackResolver.defaultTargetPriority(request.target());
   }

   private double scaledDamage(MagicAttackRequest request, EliteEntity elite, SkillType progressionSkill) {
      int skillLevel = (int)fact(request, "skill_level");
      ItemStack weapon = request.weapon();
      double itemLevel = EliteItemManager.getItemLevel(weapon);
      if (!Double.isFinite(itemLevel) || itemLevel < 0.0) {
         itemLevel = 0.0;
      }

      Material carrierMaterial = weapon.getType();
      double basePower = request.balance().basePower() * request.balance().impactScale();
      return AdvancedDamageScaling.magicWeapon(elite, skillLevel, itemLevel, basePower, carrierMaterial)
         * EliteItemManager.getEliteDamageEnchantmentMultiplier(weapon);
   }

   private static void applyWithEliteMobsBypass(MagicAttackRequest request, SkillType progressionSkill, double damage, MagicDamageApplication application) {
      CombatDamageContext.runMagicWeaponHit(
         new CombatDamageContext.PlayerDamageSource(request.attackId(), progressionSkill, fact(request, "critical") == 1.0, fact(request, "loud_strikes")),
         strike(request.attackKind()),
         () -> application.apply(damage)
      );
   }

   private static MagicStrike strike(MagicAttackKind attackKind) {
      return switch (attackKind) {
         case STAFF_FIREBALL -> MagicStrike.STAFF_FIREBALL;
         case STAFF_MELEE -> MagicStrike.STAFF_MELEE;
         case WAND_MISSILE -> MagicStrike.WAND_MISSILE;
         default -> null;
      };
   }

   private static double fact(MagicAttackRequest request, String key) {
      Double value = (Double)request.resolverFacts().get(key);
      if (value != null && Double.isFinite(value) && !(value < 0.0)) {
         return value;
      } else {
         throw new IllegalArgumentException("Missing or invalid launch fact: " + key);
      }
   }

   private void warnCalculationFailure(RuntimeException failure) {
      if (!this.calculationWarningSent) {
         this.calculationWarningSent = true;
         Logger.warn("EliteMobs could not scale an FMM magic impact. The impact was rejected: " + failure.getMessage());
      }
   }

   static SkillType progressionSkill(MagicWeaponKind weaponKind) {
      return switch ((MagicWeaponKind)Objects.requireNonNull(weaponKind, "weaponKind")) {
         case STAFF -> SkillType.STAVES;
         case WAND -> SkillType.WANDS;
         default -> throw new MatchException(null, null);
      };
   }

   @Override
   public void close() {
      if (this.registered) {
         this.registered = false;
         MagicWeaponAPI.unregisterResolver(this.owner);
      }
   }
}
