package net.ess3.nms.refl.providers;

import net.ess3.nms.refl.SpawnEggRefl;
import net.ess3.provider.SpawnEggProvider;
import net.essentialsx.providers.ProviderData;
import net.essentialsx.providers.ProviderTest;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

@ProviderData(
   description = "1.9-1.12.2 Spawn Egg Provider",
   weight = 1
)
public class ReflSpawnEggProvider implements SpawnEggProvider {
   @Override
   public ItemStack createEggItem(final EntityType type) throws IllegalArgumentException {
      try {
         return new SpawnEggRefl(type).toItemStack();
      } catch (Exception var3) {
         throw new IllegalArgumentException(var3.getMessage(), var3.getCause());
      }
   }

   @Override
   public EntityType getSpawnedType(final ItemStack eggItem) throws IllegalArgumentException {
      try {
         return SpawnEggRefl.fromItemStack(eggItem).getSpawnedType();
      } catch (Exception var3) {
         throw new IllegalArgumentException(var3.getMessage(), var3.getCause());
      }
   }

   @ProviderTest
   public static boolean test() {
      try {
         Class.forName("org.bukkit.entity.Shulker");
         return true;
      } catch (Throwable var1) {
         return false;
      }
   }
}
