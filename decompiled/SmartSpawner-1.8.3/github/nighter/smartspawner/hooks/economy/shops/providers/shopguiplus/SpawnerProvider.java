package github.nighter.smartspawner.hooks.economy.shops.providers.shopguiplus;

import github.nighter.smartspawner.SmartSpawner;
import github.nighter.smartspawner.spawner.item.SpawnerItemFactory;
import net.brcdev.shopgui.spawner.external.provider.ExternalSpawnerProvider;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

public class SpawnerProvider implements ExternalSpawnerProvider {
   private final SpawnerItemFactory spawnerItemFactory;

   public SpawnerProvider(SmartSpawner plugin) {
      this.spawnerItemFactory = plugin.getSpawnerItemFactory();
   }

   public String getName() {
      return "SmartSpawner";
   }

   public ItemStack getSpawnerItem(EntityType entityType) {
      return this.spawnerItemFactory.createSmartSpawnerItem(entityType);
   }

   public EntityType getSpawnerEntityType(ItemStack itemStack) {
      if (itemStack != null && itemStack.getType() == Material.SPAWNER) {
         if (!(itemStack.getItemMeta() instanceof BlockStateMeta blockMeta)) {
            return null;
         } else {
            CreatureSpawner spawner = (CreatureSpawner)blockMeta.getBlockState();
            EntityType entityType = spawner.getSpawnedType();
            return entityType != null && entityType != EntityType.UNKNOWN ? entityType : null;
         }
      } else {
         return null;
      }
   }
}
