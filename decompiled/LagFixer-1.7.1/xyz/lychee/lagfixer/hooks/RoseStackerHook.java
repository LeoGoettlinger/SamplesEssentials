package xyz.lychee.lagfixer.hooks;

import dev.rosewood.rosestacker.api.RoseStackerAPI;
import dev.rosewood.rosestacker.stack.StackedItem;
import java.util.Collection;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.objects.AbstractHook;

public class RoseStackerHook extends AbstractHook implements HookManager.StackerContainer {
   public RoseStackerHook(LagFixer plugin, HookManager manager) {
      super(plugin, "RoseStacker", manager);
   }

   @Override
   public void addItemsToList(Item bItem, Collection<ItemStack> items) {
      ItemStack is = bItem.getItemStack();
      StackedItem stackedItem = RoseStackerAPI.getInstance().getStackedItem(bItem);
      if (stackedItem == null) {
         items.add(is);
      } else {
         int amount = stackedItem.getStackSize();

         for (int maxStack = is.getMaxStackSize(); amount > 0; amount -= maxStack) {
            ItemStack clone = is.clone();
            clone.setAmount(Math.min(amount, maxStack));
            items.add(clone);
         }
      }
   }

   @Override
   public boolean isStacked(LivingEntity entity) {
      return RoseStackerAPI.getInstance().isEntityStacked(entity);
   }

   @Override
   public void load() {
   }

   @Override
   public void disable() {
   }
}
