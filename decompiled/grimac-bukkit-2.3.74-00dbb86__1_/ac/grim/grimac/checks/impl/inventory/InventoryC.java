package ac.grim.grimac.checks.impl.inventory;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.InventoryCheck;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.BlockPlace;

@CheckData(
   name = "InventoryC",
   stableKey = "grim.inventory.place_open",
   setback = 3.0,
   description = "Placed a block while inventory is open"
)
public class InventoryC extends InventoryCheck {
   public InventoryC(GrimPlayer player) {
      super(player);
   }

   @Override
   public void onBlockPlace(BlockPlace place) {
      if (this.player.hasInventoryOpen) {
         if (this.flag()) {
            if (this.shouldModifyPackets()) {
               place.resync();
            }

            if (!this.isNoSetbackPermission()) {
               this.closeInventory();
            }
         }
      } else {
         this.reward();
      }
   }
}
