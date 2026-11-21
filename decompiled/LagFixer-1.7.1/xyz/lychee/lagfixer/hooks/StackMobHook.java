package xyz.lychee.lagfixer.hooks;

import java.util.Collection;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import uk.antiperson.stackmob.StackMob;
import xyz.lychee.lagfixer.LagFixer;
import xyz.lychee.lagfixer.managers.HookManager;
import xyz.lychee.lagfixer.objects.AbstractHook;

public class StackMobHook extends AbstractHook implements HookManager.StackerContainer {
   private StackMobHook.StackMobHolder holder;

   public StackMobHook(LagFixer plugin, HookManager manager) {
      super(plugin, "StackMob", manager);
   }

   @Override
   public void addItemsToList(Item bItem, Collection<ItemStack> items) {
      items.add(bItem.getItemStack());
   }

   @Override
   public boolean isStacked(LivingEntity entity) {
      return this.holder.isStacked(entity);
   }

   @Override
   public void load() {
      this.holder = new StackMobHook.StackMobHolder();
   }

   @Override
   public void disable() {
   }

   static class StackMobHolder {
      private final StackMob stackMob = (StackMob)JavaPlugin.getPlugin(StackMob.class);

      public StackMobHolder() {
      }

      public boolean isStacked(LivingEntity entity) {
         return this.stackMob.getEntityManager().isStackedEntity(entity);
      }
   }
}
