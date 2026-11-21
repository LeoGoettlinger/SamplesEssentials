package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Server;

public class Commandwarpinfo extends EssentialsCommand {
   public Commandwarpinfo() {
      super("warpinfo");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0) {
         throw new NotEnoughArgumentsException();
      } else {
         String name = args[0];
         Location loc = this.ess.getWarps().getWarp(name);
         sender.sendTl("warpInfo", name);
         sender.sendTl("whoisLocation", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         if (this.ess.getSettings().getPerWarpPermission() && sender.isPlayer()) {
            List<String> list = new ArrayList<>();

            for (String curWarp : this.ess.getWarps().getList()) {
               if (sender.isAuthorized("essentials.warps." + curWarp)) {
                  list.add(curWarp);
               }
            }

            return list;
         } else {
            return new ArrayList<>(this.ess.getWarps().getList());
         }
      } else {
         return Collections.emptyList();
      }
   }
}
