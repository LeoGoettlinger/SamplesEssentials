package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.LocationUtil;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.TNTPrimed;

public class Commandantioch extends EssentialsCommand {
   public Commandantioch() {
      super("antioch");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length > 0) {
         this.ess.broadcastMessage(user, "...lobbest thou thy Holy Hand Grenade of Antioch towards thy foe,");
         this.ess.broadcastMessage(user, "who being naughty in My sight, shall snuff it.");
      }

      Location loc = LocationUtil.getTarget(user.getBase());
      loc.getWorld().spawn(loc, TNTPrimed.class);
   }
}
