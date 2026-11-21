package su.nightexpress.nightcore.menu.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import su.nightexpress.nightcore.menu.click.ClickAction;

@Deprecated
public class AutoFill<T> {
   private int[] slots;
   private List<T> items;
   private Function<T, ItemStack> itemCreator;
   private Function<T, ClickAction> clickAction;

   public AutoFill() {
      this.setSlots();
      this.setItems(new ArrayList<>());
      this.setItemCreator(obj -> new ItemStack(Material.AIR));
      this.setClickAction(obj -> (viewer, event) -> {});
   }

   public int[] getSlots() {
      return this.slots;
   }

   public void setSlots(int... slots) {
      this.slots = slots;
   }

   @NonNull
   public List<T> getItems() {
      return this.items;
   }

   public void setItems(@NonNull Collection<T> items) {
      this.setItems(new ArrayList<>(items));
   }

   public void setItems(@NonNull List<T> items) {
      this.items = items;
   }

   @NonNull
   public Function<T, ItemStack> getItemCreator() {
      return this.itemCreator;
   }

   public void setItemCreator(@NonNull Function<T, ItemStack> itemCreator) {
      this.itemCreator = itemCreator;
   }

   @NonNull
   public Function<T, ClickAction> getClickAction() {
      return this.clickAction;
   }

   public void setClickAction(@NonNull Function<T, ClickAction> clickAction) {
      this.clickAction = clickAction;
   }
}
