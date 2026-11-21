package com.earth2me.essentials.commands;

import com.earth2me.essentials.OfflinePlayerStub;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.paperlib.PaperLib;
import com.earth2me.essentials.utils.StringUtil;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.ess3.api.TranslatableException;
import net.ess3.api.events.UserTeleportHomeEvent;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandhome extends EssentialsCommand {
   public Commandhome() {
      super("home");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      Trade charge = new Trade(this.getName(), this.ess);
      User player = user;
      String homeName = "";
      if (args.length > 0) {
         String[] nameParts = args[0].split(":");
         if (nameParts[0].length() != args[0].length() && user.isAuthorized("essentials.home.others")) {
            player = this.getPlayer(server, nameParts, 0, true, true);
            if (nameParts.length > 1) {
               homeName = nameParts[1];
            }
         } else {
            homeName = nameParts[0];
         }
      }

      try {
         if ("bed".equalsIgnoreCase(homeName)) {
            if (!user.isAuthorized("essentials.home.bed")) {
               throw new TranslatableException("noAccessCommand");
            }

            if (player.getBase().isOnline() && !(player.getBase() instanceof OfflinePlayerStub)) {
               PaperLib.getBedSpawnLocationAsync(player.getBase(), true).thenAccept(location -> {
                  CompletableFuture<Boolean> future = this.getNewExceptionFuture(user.getSource(), commandLabel);
                  if (location != null) {
                     UserTeleportHomeEvent event = new UserTeleportHomeEvent(user, "bed", location, UserTeleportHomeEvent.HomeType.BED);
                     server.getPluginManager().callEvent(event);
                     if (event.isCancelled()) {
                        return;
                     }

                     future.thenAccept(success -> {
                        if (success) {
                           user.sendTl("teleportHome", "bed");
                        }
                     });
                     user.getAsyncTeleport().teleport(location, charge, TeleportCause.COMMAND, future);
                  } else {
                     this.showError(user.getBase(), new TranslatableException("bedMissing"), commandLabel);
                  }
               });
               throw new NoChargeException();
            }

            throw new TranslatableException("bedOffline");
         }

         this.goHome(user, player, homeName.toLowerCase(Locale.ENGLISH), charge, this.getNewExceptionFuture(user.getSource(), commandLabel));
      } catch (NotEnoughArgumentsException var12) {
         User finalPlayer = player;
         CompletableFuture<Location> message = new CompletableFuture<>();
         message.thenAccept(
            bed -> {
               List<String> homes = finalPlayer.getHomes();
               if (homes.isEmpty() && finalPlayer.equals(user)) {
                  if (this.ess.getSettings().isSpawnIfNoHome()) {
                     boolean useBed = bed != null && user.isAuthorized("essentials.home.bed");
                     UserTeleportHomeEvent event = new UserTeleportHomeEvent(
                        user,
                        null,
                        useBed ? bed : finalPlayer.getWorld().getSpawnLocation(),
                        useBed ? UserTeleportHomeEvent.HomeType.BED : UserTeleportHomeEvent.HomeType.SPAWN
                     );
                     server.getPluginManager().callEvent(event);
                     if (!event.isCancelled()) {
                        if (useBed) {
                           user.getAsyncTeleport().respawn(charge, TeleportCause.COMMAND, this.getNewExceptionFuture(user.getSource(), commandLabel));
                        } else {
                           user.getAsyncTeleport()
                              .teleport(
                                 finalPlayer.getWorld().getSpawnLocation(),
                                 charge,
                                 TeleportCause.COMMAND,
                                 this.getNewExceptionFuture(user.getSource(), commandLabel)
                              );
                        }
                     }
                  } else {
                     this.showError(user.getBase(), new TranslatableException("noHomeSetPlayer"), commandLabel);
                  }
               } else if (!homes.isEmpty() && finalPlayer.hasValidHomes()) {
                  if (homes.size() == 1 && finalPlayer.equals(user)) {
                     try {
                        this.goHome(user, finalPlayer, homes.get(0), charge, this.getNewExceptionFuture(user.getSource(), commandLabel));
                     } catch (Exception var10x) {
                        this.showError(user.getBase(), var10x, commandLabel);
                     }
                  } else {
                     int count = homes.size();
                     if (user.isAuthorized("essentials.home.bed")) {
                        if (bed != null) {
                           homes.add(user.playerTl("bed"));
                        } else {
                           homes.add(user.playerTl("bedNull"));
                        }
                     }

                     user.sendTl("homes", AdventureUtil.parsed(StringUtil.joinList(homes)), count, this.getHomeLimit(finalPlayer));
                  }
               } else {
                  this.showError(user.getBase(), new TranslatableException("noHomeSetPlayer"), commandLabel);
               }
            }
         );
         if (!player.getBase().isOnline() || player.getBase() instanceof OfflinePlayerStub) {
            message.complete(null);
            return;
         }

         PaperLib.getBedSpawnLocationAsync(player.getBase(), true).thenAccept(message::complete);
      }

      throw new NoChargeException();
   }

   private String getHomeLimit(final User player) {
      if (!player.getBase().isOnline()) {
         return "?";
      } else {
         return player.isAuthorized("essentials.sethome.multiple.unlimited") ? "*" : Integer.toString(this.ess.getSettings().getHomeLimit(player));
      }
   }

   private void goHome(final User user, final User player, final String home, final Trade charge, final CompletableFuture<Boolean> future) throws Exception {
      if (home.length() < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         Location loc = player.getHome(home);
         if (loc == null) {
            throw new NotEnoughArgumentsException();
         } else if (user.getWorld() != loc.getWorld()
            && this.ess.getSettings().isWorldHomePermissions()
            && !user.isAuthorized("essentials.worlds." + loc.getWorld().getName())) {
            throw new TranslatableException("noPerm", "essentials.worlds." + loc.getWorld().getName());
         } else {
            UserTeleportHomeEvent event = new UserTeleportHomeEvent(user, home, loc, UserTeleportHomeEvent.HomeType.HOME);
            user.getServer().getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
               user.getAsyncTeleport().teleport(loc, charge, TeleportCause.COMMAND, future);
               future.thenAccept(success -> {
                  if (success) {
                     user.sendTl("teleportHome", home);
                  }
               });
            }
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      boolean canVisitOthers = user.isAuthorized("essentials.home.others");
      boolean canVisitBed = user.isAuthorized("essentials.home.bed");
      if (args.length == 1) {
         List<String> homes = user.getHomes();
         if (canVisitBed) {
            homes.add("bed");
         }

         if (canVisitOthers) {
            int sepIndex = args[0].indexOf(58);
            if (sepIndex < 0) {
               this.getPlayers(user).forEach(player -> homes.add(player + ":"));
            } else {
               String namePart = args[0].substring(0, sepIndex);

               User otherUser;
               try {
                  otherUser = this.getPlayer(server, new String[]{namePart}, 0, true, true);
               } catch (Exception var12) {
                  return homes;
               }

               otherUser.getHomes().forEach(home -> homes.add(namePart + ":" + home));
               if (canVisitBed) {
                  homes.add(namePart + ":bed");
               }
            }
         }

         return homes;
      } else {
         return Collections.emptyList();
      }
   }
}
