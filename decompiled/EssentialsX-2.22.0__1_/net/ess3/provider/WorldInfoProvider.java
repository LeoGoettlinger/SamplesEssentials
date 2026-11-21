package net.ess3.provider;

import org.bukkit.World;

public interface WorldInfoProvider extends Provider {
   int getMaxHeight(World world);

   int getLogicalHeight(World world);

   int getMinHeight(World world);
}
