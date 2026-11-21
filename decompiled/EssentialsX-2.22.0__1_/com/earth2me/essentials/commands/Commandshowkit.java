package com.earth2me.essentials.commands;

import com.earth2me.essentials.Kit;
import com.earth2me.essentials.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.Server;

public class Commandshowkit extends EssentialsCommand {
   public Commandshowkit() {
      super("showkit");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length != 1) {
         throw new NotEnoughArgumentsException();
      } else {
         for (String kitName : args[0].toLowerCase(Locale.ENGLISH).split(",")) {
            user.sendTl("kitContains", kitName);

            for (String s : new Kit(kitName, this.ess).getItems()) {
               user.sendTl("kitItem", s);
            }
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      return (List<String>)(args.length == 1 ? new ArrayList<>(this.ess.getKits().getKitKeys()) : Collections.emptyList());
   }
}
