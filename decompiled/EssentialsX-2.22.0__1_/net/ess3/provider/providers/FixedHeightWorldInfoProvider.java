package net.ess3.provider.providers;

import net.ess3.provider.WorldInfoProvider;
import net.essentialsx.providers.ProviderData;
import org.bukkit.World;
import org.bukkit.World.Environment;

@ProviderData(
   description = "Fixed Height World Info Provider"
)
public class FixedHeightWorldInfoProvider implements WorldInfoProvider {
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
}
