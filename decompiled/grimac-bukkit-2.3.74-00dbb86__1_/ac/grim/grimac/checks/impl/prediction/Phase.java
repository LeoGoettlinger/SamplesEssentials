package ac.grim.grimac.checks.impl.prediction;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.player.ClientVersion;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import ac.grim.grimac.utils.collisions.datatypes.SimpleCollisionBox;
import ac.grim.grimac.utils.nmsutil.Collisions;
import java.util.ArrayList;
import java.util.List;

@CheckData(
   name = "Phase",
   stableKey = "grim.prediction.phase",
   description = "Moved into a solid block during movement prediction",
   setback = 1.0,
   decay = 0.005
)
public class Phase extends Check implements PostPredictionCheck {
   private SimpleCollisionBox oldBB;

   public Phase(GrimPlayer player) {
      super(player);
      this.oldBB = player.boundingBox;
   }

   @Override
   public void onPredictionComplete(PredictionComplete predictionComplete) {
      label44: {
         if (!this.player.getSetbackTeleportUtil().blockOffsets && !predictionComplete.getData().isTeleport() && predictionComplete.isChecked()) {
            SimpleCollisionBox newBB = this.player.boundingBox;
            List<SimpleCollisionBox> boxes = new ArrayList<>();
            Collisions.getCollisionBoxes(this.player, newBB, boxes, false);

            for (SimpleCollisionBox box : boxes) {
               if (newBB.isIntersected(box) && !this.oldBB.isIntersected(box)) {
                  if (!this.player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8)) {
                     break label44;
                  }

                  WrappedBlockState state = this.player
                     .compensatedWorld
                     .getBlock((box.minX + box.maxX) / 2.0, (box.minY + box.maxY) / 2.0, (box.minZ + box.maxZ) / 2.0);
                  if (!BlockTags.ANVIL.contains(state.getType()) && state.getType() != StateTypes.CHEST && state.getType() != StateTypes.TRAPPED_CHEST) {
                     break label44;
                  }
               }
            }
         }

         this.oldBB = this.player.boundingBox;
         this.reward();
         return;
      }

      this.flagWithSetback();
   }
}
