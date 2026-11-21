package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;

public class Commanddeljail extends EssentialsCommand {
   public Commanddeljail() {
      super("deljail");
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0) {
         throw new NotEnoughArgumentsException();
      } else if (this.ess.getJails().getJail(args[0]) == null) {
         throw new TranslatableException("jailNotExist");
      } else {
         this.ess.getJails().removeJail(args[0]);
         sender.sendTl("deleteJail", args[0]);
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         try {
            return new ArrayList<>(this.ess.getJails().getList());
         } catch (Exception var6) {
            return Collections.emptyList();
         }
      } else {
         return Collections.emptyList();
      }
   }
}
