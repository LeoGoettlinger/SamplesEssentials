package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.LocationUtil;
import com.earth2me.essentials.utils.NumberUtil;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import net.ess3.api.TranslatableException;
import net.essentialsx.api.v2.events.HomeModifyEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;

public class Commandsethome extends EssentialsCommand {
   public Commandsethome() {
      super("sethome");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, String[] args) throws Exception {
      User usersHome = user;
      String name = "home";
      if (args.length > 0) {
         String[] nameParts = args[0].split(":");
         if (nameParts[0].length() != args[0].length()) {
            args = nameParts;
         }

         if (args.length < 2) {
            name = args[0].toLowerCase(Locale.ENGLISH);
         } else {
            name = args[1].toLowerCase(Locale.ENGLISH);
            if (user.isAuthorized("essentials.sethome.others")) {
               usersHome = this.getPlayer(server, args[0], true, true);
               if (usersHome == null) {
                  throw new PlayerNotFoundException();
               }
            }
         }
      }

      if (this.checkHomeLimit(user, usersHome, name)) {
         name = "home";
      }

      if (!"bed".equals(name) && !NumberUtil.isInt(name)) {
         Location location = user.getLocation();
         if ((!this.ess.getSettings().isTeleportSafetyEnabled() || !this.ess.getSettings().isForceDisableTeleportSafety())
            && LocationUtil.isBlockUnsafeForUser(this.ess, usersHome, location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
            throw new TranslatableException(
               "unsafeTeleportDestination", location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ()
            );
         } else if (!this.ess.getSettings().isConfirmHomeOverwrite()
            || !usersHome.hasHome(name)
            || name.equals(usersHome.getLastHomeConfirmation())
               && (
                  !name.equals(usersHome.getLastHomeConfirmation())
                     || System.currentTimeMillis() - usersHome.getLastHomeConfirmationTimestamp() <= TimeUnit.MINUTES.toMillis(2L)
               )) {
            Location prevHomeLoc = usersHome.getHome(name);
            HomeModifyEvent event;
            if (prevHomeLoc == null) {
               event = new HomeModifyEvent(user, usersHome, name, location, true);
            } else {
               event = new HomeModifyEvent(user, usersHome, name, prevHomeLoc, location);
            }

            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
               if (this.ess.getSettings().isDebug()) {
                  this.ess.getLogger().info("HomeModifyEvent canceled for /sethome execution by " + user.getDisplayName());
               }
            } else {
               usersHome.setHome(name, location);
               user.sendTl("homeSet", location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), name);
               usersHome.setLastHomeConfirmation(null);
            }
         } else {
            usersHome.setLastHomeConfirmation(name);
            usersHome.setLastHomeConfirmationTimestamp();
            user.sendTl("homeConfirmation", name);
         }
      } else {
         throw new TranslatableException("invalidHomeName");
      }
   }

   private boolean checkHomeLimit(final User user, final User usersHome, final String name) throws Exception {
      if (!user.isAuthorized("essentials.sethome.multiple.unlimited")) {
         int limit = this.ess.getSettings().getHomeLimit(user);
         if (usersHome.getHomes().size() >= limit) {
            if (usersHome.getHomes().contains(name)) {
               return false;
            } else {
               throw new TranslatableException("maxHomes", this.ess.getSettings().getHomeLimit(user));
            }
         } else {
            return limit == 1;
         }
      } else {
         return false;
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return Collections.emptyList();
   }
}
