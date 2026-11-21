package su.nightexpress.nightcore.ui.menu.click;

import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

@Deprecated
public enum ClickKey {
   LEFT,
   RIGHT,
   SHIFT_LEFT,
   SHIFT_RIGHT,
   DROP_KEY,
   SWAP_KEY,
   DRAG_N_DROP;

   @NonNull
   public static ClickKey from(@NonNull InventoryClickEvent event) {
      ItemStack cursor = event.getCursor();
      ItemStack target = event.getCurrentItem();
      if (!cursor.getType().isAir() && target != null && !target.getType().isAir()) {
         return DRAG_N_DROP;
      } else if (event.getClick() == ClickType.DROP) {
         return DROP_KEY;
      } else if (event.getClick() == ClickType.SWAP_OFFHAND) {
         return SWAP_KEY;
      } else if (event.isShiftClick()) {
         return event.isRightClick() ? SHIFT_RIGHT : SHIFT_LEFT;
      } else {
         return event.isRightClick() ? RIGHT : LEFT;
      }
   }
}
