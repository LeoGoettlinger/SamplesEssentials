package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.utils.DescParseTickFormat;
import com.earth2me.essentials.utils.NumberUtil;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.Map.Entry;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class Commandtime extends EssentialsCommand {
   private final List<String> subCommands = Arrays.asList("add", "set");
   private final List<String> timeNames = Arrays.asList("sunrise", "day", "morning", "noon", "afternoon", "sunset", "night", "midnight");
   private final List<String> timeNumbers = Arrays.asList("1000", "2000", "3000", "4000", "5000");

   public Commandtime() {
      super("time");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      boolean add = false;
      long timeTick;
      Set<World> worlds;
      if (args.length == 0) {
         worlds = this.getWorlds(server, sender, null);
         if (!commandLabel.endsWith("day") && !commandLabel.endsWith("night")) {
            this.getWorldsTime(sender, worlds);
            return;
         }

         timeTick = DescParseTickFormat.parse(commandLabel.toLowerCase(Locale.ENGLISH).replace("e", ""));
      } else if (args.length == 1) {
         worlds = this.getWorlds(server, sender, null);

         try {
            timeTick = DescParseTickFormat.parse(NumberUtil.isInt(args[0]) ? args[0] + "t" : args[0]);
         } catch (NumberFormatException var21) {
            throw new NotEnoughArgumentsException(var21);
         }
      } else if (!args[0].equalsIgnoreCase("set") && !args[0].equalsIgnoreCase("add")) {
         try {
            timeTick = DescParseTickFormat.parse(NumberUtil.isInt(args[0]) ? args[0] + "t" : args[0]);
            worlds = this.getWorlds(server, sender, args[1]);
         } catch (NumberFormatException var19) {
            throw new NotEnoughArgumentsException(var19);
         }
      } else {
         try {
            add = args[0].equalsIgnoreCase("add");
            timeTick = DescParseTickFormat.parse(NumberUtil.isInt(args[1]) ? args[1] + "t" : args[1]);
            worlds = this.getWorlds(server, sender, args.length > 2 ? args[2] : null);
         } catch (NumberFormatException var20) {
            throw new NotEnoughArgumentsException(var20);
         }
      }

      if (!sender.isAuthorized("essentials.time.set")) {
         throw new TranslatableException("timeSetPermission");
      } else {
         for (World world : worlds) {
            if (!this.canUpdateWorld(sender, world)) {
               throw new TranslatableException("timeSetWorldPermission", sender.getUser().getBase().getWorld().getName());
            }
         }

         StringJoiner joiner = new StringJoiner(", ");

         for (World worldx : worlds) {
            Map<Player, Long> ptimePlayers = new HashMap<>();

            for (Player player : worldx.getPlayers()) {
               if (player.getPlayerTimeOffset() != 0L && player.isPlayerTimeRelative()) {
                  ptimePlayers.put(player, player.getPlayerTime());
               }
            }

            long time = worldx.getTime();
            if (!add) {
               time -= time % 24000L;
            }

            worldx.setTime(time + (add ? 0 : 24000) + timeTick);
            long newWorldTime = worldx.getTime();

            for (Entry<Player, Long> entry : ptimePlayers.entrySet()) {
               entry.getKey().setPlayerTime(entry.getValue() - newWorldTime, true);
            }

            joiner.add(worldx.getName());
         }

         sender.sendTl(add ? "timeWorldAdd" : "timeWorldSet", DescParseTickFormat.formatTicks(timeTick), joiner.toString());
      }
   }

   private void getWorldsTime(final CommandSource sender, final Collection<World> worlds) {
      if (worlds.size() == 1) {
         Iterator<World> iter = worlds.iterator();
         sender.sendComponent(this.ess.getAdventureFacet().deserializeMiniMessage(DescParseTickFormat.format(iter.next().getTime())));
      } else {
         for (World world : worlds) {
            sender.sendTl("timeWorldCurrent", world.getName(), AdventureUtil.parsed(DescParseTickFormat.format(world.getTime())));
         }
      }
   }

   private Set<World> getWorlds(final Server server, final CommandSource sender, final String selector) throws Exception {
      Set<World> worlds = new TreeSet<>(new WorldNameComparator());
      if (selector == null) {
         if (sender.isPlayer()) {
            worlds.add(sender.getPlayer().getWorld());
         } else {
            worlds.addAll(server.getWorlds());
         }

         return worlds;
      } else {
         World world = server.getWorld(selector);
         if (world != null) {
            worlds.add(world);
         } else {
            if (!selector.equalsIgnoreCase("*") && !selector.equalsIgnoreCase("all")) {
               throw new TranslatableException("invalidWorld");
            }

            worlds.addAll(server.getWorlds());
         }

         return worlds;
      }
   }

   private boolean canUpdateAll(final CommandSource sender) {
      return !this.ess.getSettings().isWorldTimePermissions() || sender.isAuthorized("essentials.time.world.all");
   }

   private boolean canUpdateWorld(final CommandSource sender, final World world) {
      return this.canUpdateAll(sender) || sender.isAuthorized("essentials.time.world." + this.normalizeWorldName(world));
   }

   private String normalizeWorldName(final World world) {
      return world.getName().toLowerCase().replaceAll("\\s+", "_");
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return sender.isAuthorized("essentials.time.set") ? this.subCommands : Collections.emptyList();
      } else if (args.length == 2) {
         if (args[0].equalsIgnoreCase("set")) {
            return this.timeNames;
         } else {
            return args[0].equalsIgnoreCase("add") ? this.timeNumbers : Collections.emptyList();
         }
      } else if (args.length == 3 && (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("add"))) {
         List<String> worlds = Lists.newArrayList();

         for (World world : server.getWorlds()) {
            if (sender.isAuthorized("essentials.time.world." + this.normalizeWorldName(world))) {
               worlds.add(world.getName());
            }
         }

         if (sender.isAuthorized("essentials.time.world.all")) {
            worlds.add("*");
         }

         return worlds;
      } else {
         return Collections.emptyList();
      }
   }
}
