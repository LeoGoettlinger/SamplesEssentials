package com.magmaguy.elitemobs.advancedcombat.minions;

public record GateBlockPosition(int x, int y, int z) {
   GateBlockPosition offset(int xOffset, int yOffset, int zOffset) {
      return new GateBlockPosition(this.x + xOffset, this.y + yOffset, this.z + zOffset);
   }
}
