package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.utils.FormatUtil;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;
import org.bukkit.World;

public class Commandbroadcastworld extends EssentialsCommand {
   public Commandbroadcastworld() {
      super("broadcastworld");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         World world = user.getWorld();
         String message = getFinalArg(args, 0);
         if (this.ess.getSettings().isAllowWorldInBroadcastworld()) {
            World argWorld = this.ess.getWorld(args[0]);
            if (argWorld != null) {
               world = argWorld;
               message = getFinalArg(args, 1);
            }
         }

         this.sendBroadcast(world, user.getDisplayName(), message);
      }
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         World world = this.ess.getWorld(args[0]);
         if (world == null) {
            throw new TranslatableException("invalidWorld");
         } else {
            this.sendBroadcast(world, sender.getSender().getName(), getFinalArg(args, 1));
         }
      }
   }

   private void sendBroadcast(final World world, final String name, final String message) throws Exception {
      if (message.isEmpty()) {
         throw new NotEnoughArgumentsException();
      } else {
         String formatted = FormatUtil.replaceFormat(message).replace("\\n", "\n");
         this.ess
            .broadcastTl(
               null,
               u -> !u.getBase().getWorld().equals(world),
               true,
               "broadcast",
               new Object[]{
                  AdventureUtil.parsed(this.ess.getAdventureFacet().legacyToMiniWithUrls(this.ess.getAdventureFacet().escapeTags(formatted))),
                  AdventureUtil.parsed(this.ess.getAdventureFacet().legacyToMini(name))
               }
            );
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1 && (!sender.isPlayer() || this.ess.getSettings().isAllowWorldInBroadcastworld())) {
         List<String> worlds = Lists.newArrayList();

         for (World world : server.getWorlds()) {
            worlds.add(world.getName());
         }

         return worlds;
      } else {
         return Collections.emptyList();
      }
   }
}
