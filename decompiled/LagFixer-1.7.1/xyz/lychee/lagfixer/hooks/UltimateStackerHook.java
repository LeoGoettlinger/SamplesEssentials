package xyz.lychee.lagfixer.hooks;

import com.songoda.ultimatestacker.api.UltimateStackerApi;
import com.songoda.ultimatestacker.api.stack.item.StackedItem;
import java.util.Collection;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.objects.AbstractHook;

public class UltimateStackerHook extends AbstractHook implements HookManager.StackerContainer {
   public UltimateStackerHook(LagFixer plugin, HookManager manager) {
      super(plugin, "UltimateStacker", manager);
   }

   @Override
   public void addItemsToList(Item bItem, Collection<ItemStack> items) {
      ItemStack is = bItem.getItemStack();
      StackedItem stackedItem = UltimateStackerApi.getStackedItemManager().getStackedItem(bItem);
      int amount = stackedItem.getAmount();

      for (int maxStack = is.getMaxStackSize(); amount > 0; amount -= maxStack) {
         ItemStack clone = is.clone();
         clone.setAmount(Math.min(amount, maxStack));
         items.add(clone);
      }
   }

   @Override
   public boolean isStacked(LivingEntity entity) {
      return UltimateStackerApi.getEntityStackManager().isStackedEntity(entity);
   }

   @Override
   public void load() {
   }

   @Override
   public void disable() {
   }
}
