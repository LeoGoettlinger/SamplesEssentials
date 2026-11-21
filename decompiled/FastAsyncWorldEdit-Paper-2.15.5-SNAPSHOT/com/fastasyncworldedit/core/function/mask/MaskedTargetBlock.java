package com.fastasyncworldedit.core.function.mask;

import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.TargetBlock;
import com.sk89q.worldedit.world.World;

public class MaskedTargetBlock extends TargetBlock {
   private final Mask mask;
   private final World world;

   public MaskedTargetBlock(Mask mask, Player player, int maxDistance, double checkDistance) {
      super(player, maxDistance, checkDistance);
      this.mask = mask;
      this.world = player.getWorld();
   }

   public Location getMaskedTargetBlock(boolean useLastBlock) {
      boolean searchForLastBlock = true;
      Location lastBlock = null;

      while (this.getNextBlock() != null) {
         Location current = this.getCurrentBlock();
         if (this.mask.test(current.toBlockPoint())) {
            break;
         }

         if (searchForLastBlock) {
            lastBlock = current;
            if (current.getBlockY() <= this.world.getMinY() || current.getBlockY() >= this.world.getMaxY()) {
               searchForLastBlock = false;
            }
         } else if (current.getBlockY() <= this.world.getMinY()) {
            break;
         }
      }

      Location currentBlock = this.getCurrentBlock();
      return currentBlock == null && useLastBlock ? lastBlock : currentBlock;
   }
}
