package net.ess3.nms.refl.providers;

import net.ess3.provider.WorldInfoProvider;
import net.essentialsx.providers.ProviderData;
import net.essentialsx.providers.ProviderTest;
import org.bukkit.World;
import org.bukkit.World.Environment;

@ProviderData(
   description = "Reflection World Info Provider",
   weight = 1
)
public class ReflDataWorldInfoProvider implements WorldInfoProvider {
   @Override
   public int getMaxHeight(World world) {
      return world.getMaxHeight();
   }

   @Override
   public int getLogicalHeight(World world) {
      return world.getEnvironment() == Environment.NETHER ? 128 : 256;
   }

   @Override
   public int getMinHeight(World world) {
      return 0;
   }

   @ProviderTest
   public static boolean test() {
      return false;
   }
}
