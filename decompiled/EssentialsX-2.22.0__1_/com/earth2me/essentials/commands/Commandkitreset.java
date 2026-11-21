package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;

public class Commandkitreset extends EssentialsCommand {
   public Commandkitreset() {
      super("kitreset");
   }

   @Override
   protected void run(Server server, User user, String commandLabel, String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         String kitName = args[0];
         if (this.ess.getKits().getKit(kitName) == null) {
            throw new TranslatableException("kitNotFound");
         } else {
            User target = user;
            if (args.length > 1 && user.isAuthorized("essentials.kitreset.others")) {
               target = this.getPlayer(server, user, args, 1);
            }

            target.setKitTimestamp(kitName, 0L);
            if (user.equals(target)) {
               user.sendTl("kitReset", kitName);
            } else {
               user.sendTl("kitResetOther", kitName, target.getDisplayName());
            }
         }
      }
   }

   @Override
   protected void run(Server server, CommandSource sender, String commandLabel, String[] args) throws Exception {
      if (args.length < 2) {
         throw new NotEnoughArgumentsException();
      } else {
         String kitName = args[0];
         if (this.ess.getKits().getKit(kitName) == null) {
            throw new TranslatableException("kitNotFound");
         } else {
            User target = this.getPlayer(server, sender, args, 1);
            target.setKitTimestamp(kitName, 0L);
            sender.sendTl("kitResetOther", kitName, target.getDisplayName());
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(Server server, CommandSource sender, String commandLabel, String[] args) {
      if (args.length == 1) {
         return new ArrayList<>(this.ess.getKits().getKitKeys());
      } else {
         return args.length == 2 && sender.isAuthorized("essentials.kitreset.others") ? this.getPlayers(sender) : Collections.emptyList();
      }
   }
}
