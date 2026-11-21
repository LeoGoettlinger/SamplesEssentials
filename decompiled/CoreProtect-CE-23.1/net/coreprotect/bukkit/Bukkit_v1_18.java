package net.coreprotect.bukkit;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

public class Bukkit_v1_18 extends Bukkit_v1_17 {
   private Boolean hasAdjustMethod = null;

   @Override
   public ItemStack adjustIngredient(MerchantRecipe recipe, ItemStack itemStack) {
      if (itemStack == null) {
         return null;
      } else {
         try {
            if (this.hasAdjustMethod == null) {
               this.hasAdjustMethod = true;
               MerchantRecipe.class.getMethod("adjust", ItemStack.class);
            } else if (Boolean.FALSE.equals(this.hasAdjustMethod)) {
               return super.adjustIngredient(recipe, itemStack);
            }

            ItemStack adjustedStack = itemStack.clone();
            recipe.adjust(adjustedStack);
            return adjustedStack;
         } catch (Exception var4) {
            this.hasAdjustMethod = false;
            return super.adjustIngredient(recipe, itemStack);
         }
      }
   }
}
