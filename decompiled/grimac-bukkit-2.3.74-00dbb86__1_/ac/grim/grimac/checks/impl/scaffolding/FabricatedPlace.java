package ac.grim.grimac.checks.impl.scaffolding;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.BlockPlaceCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import ac.grim.grimac.shaded.com.github.retrooper.packetevents.util.Vector3f;
import ac.grim.grimac.utils.anticheat.update.BlockPlace;
import ac.grim.grimac.utils.nmsutil.Materials;

@CheckData(
   name = "FabricatedPlace",
   stableKey = "grim.scaffolding.fabricated_place",
   description = "Sent out of bounds cursor position"
)
public class FabricatedPlace extends BlockPlaceCheck {
   private static final Verbose V = Verbose.of("cursor={cursor} limit={f64:%.16f}");
   private static final double MAX_DOUBLE_ERROR = Math.ulp(3.0E7) * 2.0;
   private static final double FLOAT_STEP_AT_ONE = Math.ulp(1.0F);

   public FabricatedPlace(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onBlockPlace(BlockPlace place) {
      Vector3f cursor = place.cursor;
      if (cursor != null) {
         boolean isExtended = Materials.isShapeExceedsCube(place.getPlacedAgainstMaterial()) || place.getPlacedAgainstMaterial() == StateTypes.LECTERN;
         double maxBound = isExtended ? 1.5 : 1.0;
         double minBound = 1.0 - maxBound;
         if (!(cursor.getX() < minBound - MAX_DOUBLE_ERROR) && !(cursor.getY() < minBound - MAX_DOUBLE_ERROR) && !(cursor.getZ() < minBound - MAX_DOUBLE_ERROR)
            )
          {
            double upperTolerance = FLOAT_STEP_AT_ONE;
            if (cursor.getX() > maxBound + upperTolerance || cursor.getY() > maxBound + upperTolerance || cursor.getZ() > maxBound + upperTolerance) {
               double limit = maxBound + upperTolerance;
               Verbose.Writer buf = V.write(this.verbose()).cursor(cursor.x, cursor.y, cursor.z).f64(limit);
               if (this.flag(buf) && this.shouldModifyPackets() && this.shouldCancel()) {
                  place.resync();
               }
            }
         } else {
            double limit = minBound - MAX_DOUBLE_ERROR;
            Verbose.Writer buf = V.write(this.verbose()).cursor(cursor.x, cursor.y, cursor.z).f64(limit);
            if (this.flag(buf) && this.shouldModifyPackets() && this.shouldCancel()) {
               place.resync();
            }
         }
      }
   }
}
