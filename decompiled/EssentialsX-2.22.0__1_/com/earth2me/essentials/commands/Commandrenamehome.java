package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.NumberUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import net.ess3.api.IUser;
import net.ess3.api.TranslatableException;
import net.essentialsx.api.v2.events.HomeModifyEvent;
import org.bukkit.Bukkit;
import org.bukkit.Server;

public class Commandrenamehome extends EssentialsCommand {
   public Commandrenamehome() {
      super("renamehome");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      User usersHome = user;
      String oldName;
      String newName;
      if (args.length == 2) {
         String[] nameParts = args[0].split(":", 2);
         newName = args[1].toLowerCase(Locale.ENGLISH);
         if (nameParts.length == 2) {
            oldName = nameParts[1].toLowerCase(Locale.ENGLISH);
            if (user.isAuthorized("essentials.renamehome.others")) {
               usersHome = this.getPlayer(server, nameParts[0], true, true);
               if (usersHome == null) {
                  throw new PlayerNotFoundException();
               }
            }
         } else {
            oldName = args[0].toLowerCase(Locale.ENGLISH);
         }
      } else {
         if (args.length != 3) {
            throw new NotEnoughArgumentsException();
         }

         if (!user.isAuthorized("essentials.renamehome.others")) {
            throw new NotEnoughArgumentsException();
         }

         usersHome = this.getPlayer(server, args[0], true, true);
         if (usersHome == null) {
            throw new PlayerNotFoundException();
         }

         oldName = args[1].toLowerCase(Locale.ENGLISH);
         newName = args[2].toLowerCase(Locale.ENGLISH);
      }

      if (!"bed".equals(newName) && !NumberUtil.isInt(newName) && !"bed".equals(oldName) && !NumberUtil.isInt(oldName)) {
         HomeModifyEvent event = new HomeModifyEvent(user, usersHome, oldName, newName, usersHome.getHome(oldName));
         Bukkit.getServer().getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            if (this.ess.getSettings().isDebug()) {
               this.ess.getLogger().info("HomeModifyEvent canceled for /renamehome execution by " + user.getDisplayName());
            }
         } else {
            usersHome.renameHome(oldName, newName);
            user.sendTl("homeRenamed", oldName, newName);
            usersHome.setLastHomeConfirmation(null);
         }
      } else {
         throw new TranslatableException("invalidHomeName");
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      IUser user = sender.getUser();
      if (args.length != 1) {
         return Collections.emptyList();
      } else {
         List<String> homes = (List<String>)(user == null ? new ArrayList<>() : user.getHomes());
         boolean canRenameOthers = sender.isAuthorized("essentials.renamehome.others");
         if (canRenameOthers) {
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
            }
         }

         return homes;
      }
   }
}
