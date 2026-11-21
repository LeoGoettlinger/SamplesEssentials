package net.ess3.provider.providers;

import net.ess3.provider.SpawnEggProvider;
import net.essentialsx.providers.ProviderData;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.material.SpawnEgg;

@ProviderData(
   description = "1.8 Spawn Egg Provider"
)
public class LegacySpawnEggProvider implements SpawnEggProvider {
   @Override
   public ItemStack createEggItem(final EntityType type) throws IllegalArgumentException {
      return new SpawnEgg(type).toItemStack();
   }

   @Override
   public EntityType getSpawnedType(final ItemStack eggItem) throws IllegalArgumentException {
      MaterialData data = eggItem.getData();
      if (data instanceof SpawnEgg) {
         return ((SpawnEgg)data).getSpawnedType();
      } else {
         throw new IllegalArgumentException("Item is missing data");
      }
   }
}
