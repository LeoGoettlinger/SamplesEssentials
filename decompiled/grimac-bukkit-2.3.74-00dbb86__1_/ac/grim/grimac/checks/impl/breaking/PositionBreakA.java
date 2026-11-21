package ac.grim.grimac.checks.impl.breaking;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.impl.verbose.VerboseCodecs;
import ac.grim.grimac.checks.type.BlockBreakCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.DiggingAction;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ac.grim.grimac.utils.anticheat.update.BlockBreak;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;

@CheckData(
   name = "PositionBreakA",
   stableKey = "grim.breaking.position_break_a",
   description = "Tried to break a block face from an impossible eye position"
)
public class PositionBreakA extends Check implements BlockBreakCheck {
   private static final Verbose V = Verbose.of("action={digging}, face={face}");

   public PositionBreakA(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onBlockBreak(BlockBreak blockBreak) {
      if (!this.player.inVehicle() && blockBreak.action != DiggingAction.CANCELLED_DIGGING && blockBreak.block.getType() != StateTypes.REDSTONE_WIRE) {
         SimpleCollisionBox combined = blockBreak.getCombinedBox();
         double[] possibleEyeHeights = this.player.getPossibleEyeHeights();
         double minEyeHeight = Double.MAX_VALUE;
         double maxEyeHeight = Double.MIN_VALUE;

         for (double height : possibleEyeHeights) {
            minEyeHeight = Math.min(minEyeHeight, height);
            maxEyeHeight = Math.max(maxEyeHeight, height);
         }

         SimpleCollisionBox eyePositions = new SimpleCollisionBox(
            this.player.x, this.player.y + minEyeHeight, this.player.z, this.player.x, this.player.y + maxEyeHeight, this.player.z
         );
         if (!this.player.packetStateData.didLastMovementIncludePosition || this.player.canSkipTicks()) {
            eyePositions.expand(this.player.getMovementThreshold());
         }

         if (!eyePositions.isIntersected(combined)) {
            boolean flag = switch (blockBreak.face) {
               case NORTH -> eyePositions.minZ > combined.minZ;
               case SOUTH -> eyePositions.maxZ < combined.maxZ;
               case EAST -> eyePositions.maxX < combined.maxX;
               case WEST -> eyePositions.minX > combined.minX;
               case UP -> eyePositions.maxY < combined.maxY;
               case DOWN -> eyePositions.minY > combined.minY;
               default -> false;
            };
            if (flag
               && this.flag(V.write(this.verbose()).uint(VerboseCodecs.enumId(blockBreak.action)).uint(VerboseCodecs.enumId(blockBreak.face)))
               && this.shouldModifyPackets()) {
               blockBreak.cancel();
            }
         }
      }
   }
}
