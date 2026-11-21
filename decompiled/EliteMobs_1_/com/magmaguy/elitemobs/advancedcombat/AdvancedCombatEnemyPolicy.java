package com.magmaguy.elitemobs.advancedcombat;

final class AdvancedCombatEnemyPolicy {
   private AdvancedCombatEnemyPolicy() {
   }

   static boolean permits(AdvancedCombatEnemyPolicy.TargetFacts target) {
      return target.casterReady()
         && target.candidateReady()
         && !target.npc()
         && !target.friendly()
         && !target.classMinion()
         && target.validElite()
         && target.instanceAuthorized();
   }

   static boolean permitsMagicWeapon(AdvancedCombatEnemyPolicy.TargetFacts target) {
      return target.casterReady() && target.candidateReady() && !target.npc() && !target.friendly() && !target.classMinion() && target.instanceAuthorized();
   }

   record TargetFacts(
      boolean casterReady, boolean candidateReady, boolean npc, boolean friendly, boolean classMinion, boolean validElite, boolean instanceAuthorized
   ) {
   }
}
