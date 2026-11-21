package github.nighter.smartspawner.spawner.lootgen.loot;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

public record LootItem(
   ItemStack template, Material material, int minAmount, int maxAmount, double chance, Integer minDurability, Integer maxDurability, double sellPrice
) {
   public ItemStack createItemStack() {
      if (this.template == null) {
         return null;
      } else {
         ItemStack item = this.template.clone();
         if (this.minDurability != null && this.maxDurability != null && item.getItemMeta() instanceof Damageable damageable) {
            damageable.setDamage(ThreadLocalRandom.current().nextInt(this.maxDurability - this.minDurability + 1) + this.minDurability);
            item.setItemMeta(damageable);
         }

         return item;
      }
   }

   public int generateAmount(Random random) {
      return random.nextInt(this.maxAmount - this.minAmount + 1) + this.minAmount;
   }

   public double getAverageAmount() {
      return (this.maxAmount + this.minAmount) / 2.0;
   }

   public boolean isAvailable() {
      return this.template != null;
   }
}
