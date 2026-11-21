package me.chancesd.pvpmanager.player;

public class PlayerExemptions {
   private boolean canBypassTeleportBlock;

   public boolean canBypassTeleportBlock() {
      return this.canBypassTeleportBlock;
   }

   public void setCanBypassTeleportBlock(boolean canBypassTeleportBlock) {
      this.canBypassTeleportBlock = canBypassTeleportBlock;
   }
}
