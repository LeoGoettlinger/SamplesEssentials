package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.ess3.api.TranslatableException;
import net.essentialsx.api.v2.events.WarpModifyEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;

public class Commanddelwarp extends EssentialsCommand {
   public Commanddelwarp() {
      super("delwarp");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0) {
         throw new NotEnoughArgumentsException();
      } else if (this.ess.getWarps().isWarp(args[0])) {
         Location location;
         try {
            location = this.ess.getWarps().getWarp(args[0]);
         } catch (Exception var7) {
            location = null;
         }

         WarpModifyEvent event = new WarpModifyEvent(sender.getUser(), args[0], location, null, WarpModifyEvent.WarpModifyCause.DELETE);
         Bukkit.getServer().getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            this.ess.getWarps().removeWarp(args[0]);
            sender.sendTl("deleteWarp", args[0]);
         }
      } else {
         throw new TranslatableException("warpNotExist");
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return (List<String>)(args.length == 1 ? new ArrayList<>(this.ess.getWarps().getList()) : Collections.emptyList());
   }
}
