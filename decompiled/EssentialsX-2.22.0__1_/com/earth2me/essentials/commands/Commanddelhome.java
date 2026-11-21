package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.IUser;
import com.earth2me.essentials.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.ess3.api.TranslatableException;
import net.essentialsx.api.v2.events.HomeModifyEvent;
import org.bukkit.Bukkit;
import org.bukkit.Server;

public class Commanddelhome extends EssentialsCommand {
   public Commanddelhome() {
      super("delhome");
   }

   private void deleteHome(CommandSource sender, User user, String home) {
      HomeModifyEvent event = new HomeModifyEvent(sender.getUser(), user, home, user.getHome(home), false);
      Bukkit.getServer().getPluginManager().callEvent(event);
      if (event.isCancelled()) {
         if (this.ess.getSettings().isDebug()) {
            this.ess.getLogger().info("HomeModifyEvent canceled for /delhome execution by " + sender.getDisplayName());
         }
      } else {
         try {
            user.delHome(home);
            sender.sendTl("deleteHome", home);
         } catch (Exception var6) {
            sender.sendTl("invalidHome", home);
         }
      }
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User user = this.ess.getUser(sender.getPlayer());
         String[] nameParts = args[0].split(":");
         String[] expandedArg;
         if (nameParts[0].length() != args[0].length()) {
            expandedArg = nameParts;
         } else {
            expandedArg = args;
         }

         String name;
         if (expandedArg.length <= 1 || user != null && !user.isAuthorized("essentials.delhome.others")) {
            if (user == null) {
               throw new NotEnoughArgumentsException();
            }

            name = expandedArg[0].toLowerCase(Locale.ENGLISH);
         } else {
            user = this.getPlayer(server, expandedArg, 0, true, true);
            name = expandedArg[1].toLowerCase(Locale.ENGLISH);
         }

         switch (name) {
            case "bed":
               throw new TranslatableException("invalidHomeName");
            case "*":
               for (String home : user.getHomes()) {
                  this.deleteHome(sender, user, home);
               }
               break;
            default:
               this.deleteHome(sender, user, name);
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      IUser user = sender.getUser();
      boolean canDelOthers = sender.isAuthorized("essentials.delhome.others");
      if (args.length == 1) {
         List<String> homes = (List<String>)(user == null ? new ArrayList<>() : user.getHomes());
         if (canDelOthers) {
            int sepIndex = args[0].indexOf(58);
            if (sepIndex < 0) {
               this.getPlayers(sender).forEach(player -> homes.add(player + ":"));
            } else {
               String namePart = args[0].substring(0, sepIndex);

               User otherUser;
               try {
                  otherUser = this.getPlayer(server, new String[]{namePart}, 0, true, true);
               } catch (Exception var12) {
                  return homes;
               }

               otherUser.getHomes().forEach(home -> homes.add(namePart + ":" + home));
               homes.add(namePart + ":*");
            }
         }

         return homes;
      } else {
         return Collections.emptyList();
      }
   }
}
