package com.magmaguy.elitemobs.combatsystem;

import com.magmaguy.elitemobs.skills.MagicStrike;
import com.magmaguy.elitemobs.skills.SkillType;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.UUID;

public final class CombatDamageContext {
   private static final CombatDamageContext.DamageOverride DEFAULT_OVERRIDE = new CombatDamageContext.DamageOverride(false, 1.0);
   private static final ThreadLocal<Deque<CombatDamageContext.PendingOverride>> PLAYER_TO_ELITE = new ThreadLocal<>();
   private static final ThreadLocal<Deque<CombatDamageContext.PendingOverride>> ELITE_TO_PLAYER = new ThreadLocal<>();
   private static final ThreadLocal<Integer> ACTIVE_PLAYER_TO_ELITE_BYPASS = new ThreadLocal<>();
   private static final ThreadLocal<Boolean> ACTIVE_DAMAGE_TRANSFER = new ThreadLocal<>();
   private static final ThreadLocal<Deque<CombatDamageContext.ClassAbilityDamageDomain>> ACTIVE_CLASS_ABILITY_DAMAGE = new ThreadLocal<>();
   private static final ThreadLocal<Deque<CombatDamageContext.PlayerDamageSource>> ACTIVE_PLAYER_TO_ELITE_SOURCES = new ThreadLocal<>();

   private CombatDamageContext() {
   }

   public static CombatDamageContext.Scope bypassPlayerToElite() {
      return push(PLAYER_TO_ELITE, new CombatDamageContext.DamageOverride(true, 1.0));
   }

   public static CombatDamageContext.Scope bypassEliteToPlayer() {
      return push(ELITE_TO_PLAYER, new CombatDamageContext.DamageOverride(true, 1.0));
   }

   public static CombatDamageContext.Scope multiplyEliteToPlayer(double multiplier) {
      double safeMultiplier = Double.isFinite(multiplier) && multiplier >= 0.0 ? multiplier : 1.0;
      return push(ELITE_TO_PLAYER, new CombatDamageContext.DamageOverride(false, safeMultiplier));
   }

   public static void runPlayerToEliteBypass(Runnable damageCall) {
      runPlayerToEliteBypass(null, damageCall);
   }

   public static void runPlayerToEliteBypass(CombatDamageContext.PlayerDamageSource source, Runnable damageCall) {
      runPlayerToEliteBypass(source, new CombatDamageContext.DamageOverride(true, 1.0), damageCall);
   }

   public static void runMagicWeaponHit(CombatDamageContext.PlayerDamageSource source, MagicStrike strike, Runnable damageCall) {
      if (source != null && source.progressionSkill() != null && strike != null) {
         runPlayerToEliteBypass(
            source,
            new CombatDamageContext.DamageOverride(true, 1.0, new CombatDamageContext.MagicHit(source.attackId(), strike, source.progressionSkill())),
            damageCall
         );
      } else {
         runPlayerToEliteBypass(source, damageCall);
      }
   }

   private static void runPlayerToEliteBypass(CombatDamageContext.PlayerDamageSource source, CombatDamageContext.DamageOverride override, Runnable damageCall) {
      Integer previousDepth = ACTIVE_PLAYER_TO_ELITE_BYPASS.get();
      ACTIVE_PLAYER_TO_ELITE_BYPASS.set(previousDepth == null ? 1 : previousDepth + 1);
      if (source != null) {
         stackForPush(ACTIVE_PLAYER_TO_ELITE_SOURCES).addLast(source);
      }

      try (CombatDamageContext.Scope ignored = push(PLAYER_TO_ELITE, override)) {
         damageCall.run();
      } finally {
         if (source != null) {
            Deque<CombatDamageContext.PlayerDamageSource> sources = ACTIVE_PLAYER_TO_ELITE_SOURCES.get();
            sources.removeLastOccurrence(source);
            if (sources.isEmpty()) {
               ACTIVE_PLAYER_TO_ELITE_SOURCES.remove();
            }
         }

         if (previousDepth == null) {
            ACTIVE_PLAYER_TO_ELITE_BYPASS.remove();
         } else {
            ACTIVE_PLAYER_TO_ELITE_BYPASS.set(previousDepth);
         }
      }
   }

   public static void runClassAbilityDamage(Runnable damageCall) {
      runClassAbilityDamage(CombatDamageContext.ClassAbilityDamageDomain.SINGLE_TARGET_DIRECT, damageCall);
   }

   public static void runClassAbilityDamage(CombatDamageContext.ClassAbilityDamageDomain domain, Runnable damageCall) {
      if (domain == null) {
         throw new IllegalArgumentException("domain must not be null");
      } else if (damageCall == null) {
         throw new IllegalArgumentException("damageCall must not be null");
      } else {
         Deque<CombatDamageContext.ClassAbilityDamageDomain> domains = stackForPush(ACTIVE_CLASS_ABILITY_DAMAGE);
         domains.addLast(domain);

         try {
            runPlayerToEliteBypass(damageCall);
         } finally {
            domains.removeLast();
            if (domains.isEmpty()) {
               ACTIVE_CLASS_ABILITY_DAMAGE.remove();
            }
         }
      }
   }

   public static boolean isPlayerToEliteBypassActive() {
      Integer depth = ACTIVE_PLAYER_TO_ELITE_BYPASS.get();
      return depth != null && depth > 0;
   }

   public static boolean isDamageTransferActive() {
      return Boolean.TRUE.equals(ACTIVE_DAMAGE_TRANSFER.get());
   }

   public static void runPlayerToEliteTransfer(Runnable damageCall) {
      if (isDamageTransferActive()) {
         throw new IllegalStateException("Recursive damage transfer");
      } else {
         ACTIVE_DAMAGE_TRANSFER.set(true);

         try {
            runPlayerToEliteBypass(currentPlayerToEliteSource().orElse(null), damageCall);
         } finally {
            ACTIVE_DAMAGE_TRANSFER.remove();
         }
      }
   }

   public static boolean isClassAbilityDamageActive() {
      Deque<CombatDamageContext.ClassAbilityDamageDomain> domains = ACTIVE_CLASS_ABILITY_DAMAGE.get();
      if (domains == null) {
         return false;
      } else {
         boolean active = !domains.isEmpty();
         if (!active) {
            ACTIVE_CLASS_ABILITY_DAMAGE.remove();
         }

         return active;
      }
   }

   public static Optional<CombatDamageContext.ClassAbilityDamageDomain> currentClassAbilityDamageDomain() {
      Deque<CombatDamageContext.ClassAbilityDamageDomain> domains = ACTIVE_CLASS_ABILITY_DAMAGE.get();
      if (domains == null) {
         return Optional.empty();
      } else {
         CombatDamageContext.ClassAbilityDamageDomain domain = domains.peekLast();
         if (domains.isEmpty()) {
            ACTIVE_CLASS_ABILITY_DAMAGE.remove();
         }

         return Optional.ofNullable(domain);
      }
   }

   public static Optional<CombatDamageContext.PlayerDamageSource> currentPlayerToEliteSource() {
      Deque<CombatDamageContext.PlayerDamageSource> sources = ACTIVE_PLAYER_TO_ELITE_SOURCES.get();
      if (sources == null) {
         return Optional.empty();
      } else {
         CombatDamageContext.PlayerDamageSource source = sources.peekLast();
         if (sources.isEmpty()) {
            ACTIVE_PLAYER_TO_ELITE_SOURCES.remove();
         }

         return Optional.ofNullable(source);
      }
   }

   public static void runEliteToPlayerBypass(Runnable damageCall) {
      try (CombatDamageContext.Scope ignored = bypassEliteToPlayer()) {
         damageCall.run();
      }
   }

   public static void runEliteToPlayerMultiplier(double multiplier, Runnable damageCall) {
      try (CombatDamageContext.Scope ignored = multiplyEliteToPlayer(multiplier)) {
         damageCall.run();
      }
   }

   public static CombatDamageContext.DamageOverride consumePlayerToElite() {
      return consume(PLAYER_TO_ELITE);
   }

   public static CombatDamageContext.DamageOverride consumeEliteToPlayer() {
      return consume(ELITE_TO_PLAYER);
   }

   private static <T> Deque<T> stackForPush(ThreadLocal<Deque<T>> owner) {
      Deque<T> stack = owner.get();
      if (stack == null) {
         stack = new ArrayDeque<>();
         owner.set(stack);
      }

      return stack;
   }

   private static CombatDamageContext.Scope push(ThreadLocal<Deque<CombatDamageContext.PendingOverride>> owner, CombatDamageContext.DamageOverride override) {
      CombatDamageContext.PendingOverride pendingOverride = new CombatDamageContext.PendingOverride(override);
      Deque<CombatDamageContext.PendingOverride> overrides = stackForPush(owner);
      overrides.addLast(pendingOverride);
      return () -> {
         Deque<CombatDamageContext.PendingOverride> currentOverrides = owner.get();
         if (currentOverrides != null) {
            currentOverrides.removeLastOccurrence(pendingOverride);
            if (currentOverrides.isEmpty()) {
               owner.remove();
            }
         }
      };
   }

   private static CombatDamageContext.DamageOverride consume(ThreadLocal<Deque<CombatDamageContext.PendingOverride>> owner) {
      Deque<CombatDamageContext.PendingOverride> overrides = owner.get();
      if (overrides == null) {
         return DEFAULT_OVERRIDE;
      } else {
         CombatDamageContext.PendingOverride pendingOverride = overrides.pollLast();
         if (overrides.isEmpty()) {
            owner.remove();
         }

         return pendingOverride == null ? DEFAULT_OVERRIDE : pendingOverride.override;
      }
   }

   public static enum ClassAbilityArchetype {
      OTHER,
      TRAP,
      BLAST;
   }

   public record ClassAbilityDamageDomain(
      CombatDamageContext.ClassAbilityTargetShape targetShape,
      CombatDamageContext.ClassAbilityDelivery delivery,
      CombatDamageContext.ClassAbilityArchetype archetype,
      CombatDamageContext.ClassAbilityStrikeQuality strikeQuality
   ) {
      public static final CombatDamageContext.ClassAbilityDamageDomain SINGLE_TARGET_DIRECT = new CombatDamageContext.ClassAbilityDamageDomain(
         CombatDamageContext.ClassAbilityTargetShape.SINGLE_TARGET,
         CombatDamageContext.ClassAbilityDelivery.DIRECT,
         CombatDamageContext.ClassAbilityArchetype.OTHER,
         CombatDamageContext.ClassAbilityStrikeQuality.NORMAL
      );
      public static final CombatDamageContext.ClassAbilityDamageDomain SINGLE_TARGET_PROJECTILE = new CombatDamageContext.ClassAbilityDamageDomain(
         CombatDamageContext.ClassAbilityTargetShape.SINGLE_TARGET,
         CombatDamageContext.ClassAbilityDelivery.PROJECTILE,
         CombatDamageContext.ClassAbilityArchetype.OTHER,
         CombatDamageContext.ClassAbilityStrikeQuality.NORMAL
      );
      public static final CombatDamageContext.ClassAbilityDamageDomain AREA_DIRECT = new CombatDamageContext.ClassAbilityDamageDomain(
         CombatDamageContext.ClassAbilityTargetShape.AREA,
         CombatDamageContext.ClassAbilityDelivery.DIRECT,
         CombatDamageContext.ClassAbilityArchetype.OTHER,
         CombatDamageContext.ClassAbilityStrikeQuality.NORMAL
      );
      public static final CombatDamageContext.ClassAbilityDamageDomain AREA_PERIODIC = new CombatDamageContext.ClassAbilityDamageDomain(
         CombatDamageContext.ClassAbilityTargetShape.AREA,
         CombatDamageContext.ClassAbilityDelivery.PERIODIC,
         CombatDamageContext.ClassAbilityArchetype.OTHER,
         CombatDamageContext.ClassAbilityStrikeQuality.NORMAL
      );
      public static final CombatDamageContext.ClassAbilityDamageDomain AREA_TRAP = new CombatDamageContext.ClassAbilityDamageDomain(
         CombatDamageContext.ClassAbilityTargetShape.AREA,
         CombatDamageContext.ClassAbilityDelivery.PERIODIC,
         CombatDamageContext.ClassAbilityArchetype.TRAP,
         CombatDamageContext.ClassAbilityStrikeQuality.NORMAL
      );
      public static final CombatDamageContext.ClassAbilityDamageDomain AREA_BLAST = new CombatDamageContext.ClassAbilityDamageDomain(
         CombatDamageContext.ClassAbilityTargetShape.AREA,
         CombatDamageContext.ClassAbilityDelivery.DIRECT,
         CombatDamageContext.ClassAbilityArchetype.BLAST,
         CombatDamageContext.ClassAbilityStrikeQuality.NORMAL
      );
      public static final CombatDamageContext.ClassAbilityDamageDomain SINGLE_TARGET_SUMMON = new CombatDamageContext.ClassAbilityDamageDomain(
         CombatDamageContext.ClassAbilityTargetShape.SINGLE_TARGET,
         CombatDamageContext.ClassAbilityDelivery.SUMMON,
         CombatDamageContext.ClassAbilityArchetype.OTHER,
         CombatDamageContext.ClassAbilityStrikeQuality.NORMAL
      );

      public ClassAbilityDamageDomain(CombatDamageContext.ClassAbilityTargetShape targetShape, CombatDamageContext.ClassAbilityDelivery delivery) {
         this(targetShape, delivery, CombatDamageContext.ClassAbilityArchetype.OTHER, CombatDamageContext.ClassAbilityStrikeQuality.NORMAL);
      }

      public ClassAbilityDamageDomain(
         CombatDamageContext.ClassAbilityTargetShape targetShape,
         CombatDamageContext.ClassAbilityDelivery delivery,
         CombatDamageContext.ClassAbilityArchetype archetype
      ) {
         this(targetShape, delivery, archetype, CombatDamageContext.ClassAbilityStrikeQuality.NORMAL);
      }

      public ClassAbilityDamageDomain(
         CombatDamageContext.ClassAbilityTargetShape targetShape,
         CombatDamageContext.ClassAbilityDelivery delivery,
         CombatDamageContext.ClassAbilityArchetype archetype,
         CombatDamageContext.ClassAbilityStrikeQuality strikeQuality
      ) {
         if (targetShape == null) {
            throw new IllegalArgumentException("targetShape must not be null");
         } else if (delivery == null) {
            throw new IllegalArgumentException("delivery must not be null");
         } else if (archetype == null) {
            throw new IllegalArgumentException("archetype must not be null");
         } else if (strikeQuality == null) {
            throw new IllegalArgumentException("strikeQuality must not be null");
         } else {
            this.targetShape = targetShape;
            this.delivery = delivery;
            this.archetype = archetype;
            this.strikeQuality = strikeQuality;
         }
      }
   }

   public static enum ClassAbilityDelivery {
      DIRECT,
      PROJECTILE,
      PERIODIC,
      SUMMON;
   }

   public static enum ClassAbilityStrikeQuality {
      NORMAL,
      GUARANTEED_CRITICAL;
   }

   public static enum ClassAbilityTargetShape {
      SINGLE_TARGET,
      AREA;
   }

   public record DamageOverride(boolean bypass, double specialMultiplier, CombatDamageContext.MagicHit magicHit) {
      public DamageOverride(boolean bypass, double specialMultiplier) {
         this(bypass, specialMultiplier, null);
      }
   }

   public record MagicHit(UUID attackId, MagicStrike strike, SkillType skill) {
      public MagicHit(UUID attackId, MagicStrike strike, SkillType skill) {
         if (attackId != null && strike != null && skill != null) {
            this.attackId = attackId;
            this.strike = strike;
            this.skill = skill;
         } else {
            throw new IllegalArgumentException("A magic hit needs its attack, strike and skill");
         }
      }
   }

   private static final class PendingOverride {
      private final CombatDamageContext.DamageOverride override;

      private PendingOverride(CombatDamageContext.DamageOverride override) {
         this.override = override;
      }
   }

   public record PlayerDamageSource(UUID attackId, SkillType progressionSkill, Boolean criticalHit, Double loudStrikesBonus) {
      public PlayerDamageSource(UUID attackId, SkillType progressionSkill) {
         this(attackId, progressionSkill, null, null);
      }

      public PlayerDamageSource(UUID attackId, SkillType progressionSkill, Boolean criticalHit, Double loudStrikesBonus) {
         if (attackId == null) {
            throw new IllegalArgumentException("attackId must not be null");
         } else if (progressionSkill != null && !progressionSkill.isWeaponSkill()) {
            throw new IllegalArgumentException("progressionSkill must be a weapon skill");
         } else if (loudStrikesBonus == null || Double.isFinite(loudStrikesBonus) && !(loudStrikesBonus < 0.0)) {
            this.attackId = attackId;
            this.progressionSkill = progressionSkill;
            this.criticalHit = criticalHit;
            this.loudStrikesBonus = loudStrikesBonus;
         } else {
            throw new IllegalArgumentException("Invalid captured threat bonus");
         }
      }
   }

   @FunctionalInterface
   public interface Scope extends AutoCloseable {
      @Override
      void close();
   }
}
