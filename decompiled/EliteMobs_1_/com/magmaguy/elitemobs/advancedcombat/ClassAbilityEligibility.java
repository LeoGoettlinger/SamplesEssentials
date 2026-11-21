package com.magmaguy.elitemobs.advancedcombat;

import java.util.Objects;
import java.util.function.Predicate;
import org.bukkit.entity.Player;

public final class ClassAbilityEligibility {
   private static final ClassAbilityEligibility.Policy DENY_ALL = new ClassAbilityEligibility.Policy(ignored -> false, ignored -> false);
   private static volatile ClassAbilityEligibility.Policy policy = DENY_ALL;

   private ClassAbilityEligibility() {
   }

   public static boolean isEligible(Player player) {
      Objects.requireNonNull(player, "player");
      return policy.isEligible(player);
   }

   static void install(Predicate<Player> eligibleCombatContent, Predicate<Player> outsideControlOptIn) {
      policy = new ClassAbilityEligibility.Policy(
         Objects.requireNonNull(eligibleCombatContent, "eligibleCombatContent"), Objects.requireNonNull(outsideControlOptIn, "outsideControlOptIn")
      );
   }

   static void clear() {
      policy = DENY_ALL;
   }

   private record Policy(Predicate<Player> eligibleCombatContent, Predicate<Player> outsideControlOptIn) {
      private boolean isEligible(Player player) {
         return this.eligibleCombatContent.test(player) || this.outsideControlOptIn.test(player);
      }
   }
}
