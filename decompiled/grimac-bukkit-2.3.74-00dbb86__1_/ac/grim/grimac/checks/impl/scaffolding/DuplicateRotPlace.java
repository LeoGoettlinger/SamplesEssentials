package ac.grim.grimac.checks.impl.scaffolding;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.BlockPlaceCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.BlockPlace;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(
   name = "DuplicateRotPlace",
   stableKey = "grim.scaffolding.duplicate_rot_place",
   description = "Repeated the same rotation delta while placing blocks",
   experimental = true
)
public class DuplicateRotPlace extends BlockPlaceCheck {
   private static final Verbose V = Verbose.of("x={f64} xdots={f64} y={f64}");
   private float deltaX;
   private float deltaY;
   private float lastPlacedDeltaX;
   private double lastPlacedDeltaDotsX;
   private double deltaDotsX;
   private boolean rotated = false;

   public DuplicateRotPlace(GrimPlayer player) {
      super(player);
   }

   @Override
   public void process(RotationUpdate rotationUpdate) {
      this.deltaX = rotationUpdate.getDeltaXRotABS();
      this.deltaY = rotationUpdate.getDeltaYRotABS();
      this.deltaDotsX = rotationUpdate.getProcessor().deltaDotsX;
      this.rotated = true;
   }

   @Override
   public void onPostFlyingBlockPlace(BlockPlace place) {
      if (this.rotated && !this.player.inVehicle()) {
         if (this.deltaX > 2.0F) {
            float xDiff = Math.abs(this.deltaX - this.lastPlacedDeltaX);
            double xDiffDots = Math.abs(this.deltaDotsX - this.lastPlacedDeltaDotsX);
            if (xDiff < 1.0E-4) {
               this.flag(V.write(this.verbose()).f64(xDiff).f64(xDiffDots).f64(this.deltaY));
            } else {
               this.reward();
            }
         } else {
            this.reward();
         }

         this.lastPlacedDeltaX = this.deltaX;
         this.lastPlacedDeltaDotsX = this.deltaDotsX;
         this.rotated = false;
      }
   }
}
