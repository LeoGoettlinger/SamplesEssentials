package com.magmaguy.elitemobs.advancedcombat.minions;

import java.util.List;
import java.util.Objects;

public record AbyssalGateFootprint(GateAxis axis, List<GateBlockPosition> blocks) {
   public AbyssalGateFootprint(GateAxis axis, List<GateBlockPosition> blocks) {
      Objects.requireNonNull(axis, "axis");
      blocks = List.copyOf(Objects.requireNonNull(blocks, "blocks"));
      if (blocks.size() != 6) {
         throw new IllegalArgumentException("An Abyssal Gate requires six blocks");
      } else {
         this.axis = axis;
         this.blocks = blocks;
      }
   }
}
