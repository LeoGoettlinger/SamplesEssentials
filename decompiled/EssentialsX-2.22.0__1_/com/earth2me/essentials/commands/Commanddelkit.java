package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Kit;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import org.bukkit.Server;

public class Commanddelkit extends EssentialsCommand {
   public Commanddelkit() {
      super("delkit");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0) {
         String kitList = this.ess.getKits().listKits(this.ess, null);
         sender.sendTl(kitList.length() > 0 ? "kits" : "noKits", kitList);
         throw new NoChargeException();
      } else {
         String kitName = this.ess.getKits().matchKit(args[0]);
         Kit kit = new Kit(kitName, this.ess);
         if (sender.getPlayer() != null) {
            kit.checkPerms(this.ess.getUser(sender.getPlayer()));
         }

         this.ess.getKits().removeKit(kitName);
         sender.sendTl("deleteKit", kitName);
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return (List<String>)(args.length == 1 ? Lists.newArrayList(this.ess.getKits().getKitKeys()) : Collections.emptyList());
   }
}
