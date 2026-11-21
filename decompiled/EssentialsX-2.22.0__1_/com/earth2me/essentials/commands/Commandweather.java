package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;
import org.bukkit.World;

public class Commandweather extends EssentialsCommand {
   public Commandweather() {
      super("weather");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      boolean isStorm;
      if (args.length == 0) {
         if (commandLabel.endsWith("sun")) {
            isStorm = false;
         } else {
            if (!commandLabel.endsWith("storm") && !commandLabel.endsWith("rain")) {
               throw new NotEnoughArgumentsException();
            }

            isStorm = true;
         }
      } else {
         isStorm = args[0].equalsIgnoreCase("storm");
      }

      World world = user.getWorld();
      if (args.length > 1) {
         world.setStorm(isStorm);
         world.setWeatherDuration(Integer.parseInt(args[1]) * 20);
         user.sendTl(isStorm ? "weatherStormFor" : "weatherSunFor", world.getName(), args[1]);
      } else {
         world.setStorm(isStorm);
         user.sendTl(isStorm ? "weatherStorm" : "weatherSun", world.getName());
      }
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 2) {
         throw new Exception("When running from console, usage is: /" + commandLabel + " <world> <storm/sun> [duration]");
      } else {
         boolean isStorm = args[1].equalsIgnoreCase("storm");
         World world = server.getWorld(args[0]);
         if (world == null) {
            throw new TranslatableException("weatherInvalidWorld", args[0]);
         } else if (args.length > 2) {
            world.setStorm(isStorm);
            world.setWeatherDuration(Integer.parseInt(args[2]) * 20);
            sender.sendTl(isStorm ? "weatherStormFor" : "weatherSunFor", world.getName(), args[2]);
         } else {
            world.setStorm(isStorm);
            sender.sendTl(isStorm ? "weatherStorm" : "weatherSun", world.getName());
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return Lists.newArrayList(new String[]{"storm", "sun"});
      } else {
         return args.length == 2 ? COMMON_DURATIONS : Collections.emptyList();
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length != 1) {
         if (args.length == 2) {
            return Lists.newArrayList(new String[]{"storm", "sun"});
         } else {
            return args.length == 3 ? COMMON_DURATIONS : Collections.emptyList();
         }
      } else {
         List<String> worlds = Lists.newArrayList();

         for (World world : server.getWorlds()) {
            worlds.add(world.getName());
         }

         return worlds;
      }
   }
}
