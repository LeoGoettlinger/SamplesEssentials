package xyz.lychee.lagfixer.objects;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public interface ISupportNms {
   double getTps();

   ItemStack createSkull(String var1);

   int getTileEntitiesCount(Chunk var1);

   int getPlayerPing(Player var1);

   void setViewDistance(World var1, int var2);

   void setSimulationDistance(World var1, int var2);

   void hideEntity(Plugin var1, Player var2, Entity var3);

   void showEntity(Plugin var1, Player var2, Entity var3);

   void setEntityAi(Entity var1, boolean var2);
}
