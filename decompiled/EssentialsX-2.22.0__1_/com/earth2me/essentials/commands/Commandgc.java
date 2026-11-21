package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.NumberUtil;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.logging.Level;
import net.ess3.provider.TileEntityProvider;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Server;
import org.bukkit.World;

public class Commandgc extends EssentialsCommand {
   public Commandgc() {
      super("gc");
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      double tps = this.ess.getTimer().getAverageTPS();
      ChatColor color;
      if (tps >= 18.0) {
         color = ChatColor.GREEN;
      } else if (tps >= 15.0) {
         color = ChatColor.YELLOW;
      } else {
         color = ChatColor.RED;
      }

      sender.sendTl("uptime", DateUtil.formatDateDiff(ManagementFactory.getRuntimeMXBean().getStartTime()));
      sender.sendTl("tps", "" + color + NumberUtil.formatDouble(tps));
      sender.sendTl("gcmax", Runtime.getRuntime().maxMemory() / 1024L / 1024L);
      sender.sendTl("gctotal", Runtime.getRuntime().totalMemory() / 1024L / 1024L);
      sender.sendTl("gcfree", Runtime.getRuntime().freeMemory() / 1024L / 1024L);
      List<World> worlds = server.getWorlds();
      TileEntityProvider tileEntityProvider = this.ess.provider(TileEntityProvider.class);

      for (World w : worlds) {
         String worldType = "World";
         switch (w.getEnvironment()) {
            case NETHER:
               worldType = "Nether";
               break;
            case THE_END:
               worldType = "The End";
         }

         int tileEntities = 0;

         try {
            for (Chunk chunk : w.getLoadedChunks()) {
               tileEntities += tileEntityProvider.getTileEntities(chunk).length;
            }
         } catch (ClassCastException var18) {
            this.ess.getLogger().log(Level.SEVERE, "Corrupted chunk data on world " + w, (Throwable)var18);
         }

         sender.sendTl("gcWorld", worldType, w.getName(), w.getLoadedChunks().length, w.getEntities().size(), tileEntities);
      }
   }
}
