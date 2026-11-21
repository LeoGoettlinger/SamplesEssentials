package me.chancesd.pvpmanager.player;

public record ProtectionResult(ProtectionType type, boolean isAttacker) {
   public ProtectionResult(ProtectionType type) {
      this(type, true);
   }

   public boolean isProtected() {
      return !this.type.canAttack();
   }

   public boolean isVulnerable() {
      return this.type.canAttack();
   }
}
