package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.metadata.FixedMetadataValue;

public class Commandnuke extends EssentialsCommand {
   public static final String NUKE_META_KEY = "ess_tnt_proj";

   public Commandnuke() {
      super("nuke");
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws NotEnoughArgumentsException, PlayerNotFoundException {
      Iterable<User> targets;
      if (args.length > 0) {
         targets = new ArrayList<>();

         for (int i = 0; i < args.length; i++) {
            ((ArrayList)targets).add(this.getPlayer(server, sender, args, i));
         }
      } else {
         targets = this.ess.getOnlineUsers();
      }

      for (User user : targets) {
         if (user != null) {
            user.sendTl("nuke");
            Location loc = user.getLocation();
            World world = loc.getWorld();
            if (world != null) {
               for (int x = -10; x <= 10; x += 5) {
                  for (int z = -10; z <= 10; z += 5) {
                     TNTPrimed entity = (TNTPrimed)world.spawn(
                        new Location(world, loc.getBlockX() + x, world.getHighestBlockYAt(loc) + 64, loc.getBlockZ() + z), TNTPrimed.class
                     );
                     entity.setMetadata("ess_tnt_proj", new FixedMetadataValue(this.ess, true));
                  }
               }
            }
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getPlayers(sender) : Collections.emptyList();
   }
}
