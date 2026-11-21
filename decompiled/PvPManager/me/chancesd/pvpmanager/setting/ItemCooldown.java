package me.chancesd.pvpmanager.setting;

public class ItemCooldown {
   private int combatCooldown;
   private int globalCooldown;

   public ItemCooldown(int combatCooldown, int globalCooldown) {
      this.combatCooldown = combatCooldown;
      this.globalCooldown = globalCooldown;
   }

   public int getCombatCooldown() {
      return this.combatCooldown;
   }

   public int getGlobalCooldown() {
      return this.globalCooldown;
   }

   public void setCombatCooldown(int combatCooldown) {
      this.combatCooldown = combatCooldown;
   }

   public void setGlobalCooldown(int globalCooldown) {
      this.globalCooldown = globalCooldown;
   }
}
