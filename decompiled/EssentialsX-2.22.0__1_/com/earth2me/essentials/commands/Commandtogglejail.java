package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.ISettings;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.EnumUtil;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import net.ess3.api.events.JailStatusChangeEvent;
import org.bukkit.Server;
import org.bukkit.Statistic;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandtogglejail extends EssentialsCommand {
   private static final Statistic PLAY_ONE_TICK = EnumUtil.getStatistic("PLAY_ONE_MINUTE", "PLAY_ONE_TICK");

   public Commandtogglejail() {
      super("togglejail");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0) {
         throw new NotEnoughArgumentsException();
      } else {
         User player;
         String jailName;
         label114: {
            player = this.getPlayer(server, args, 0, true, true);
            if (!player.isJailed()) {
               if (!player.getBase().isOnline() && sender.isPlayer() && !this.ess.getUser(sender.getPlayer()).isAuthorized("essentials.togglejail.offline")) {
                  sender.sendTl("mayNotJailOffline");
                  return;
               }

               if (player.isAuthorized("essentials.jail.exempt")) {
                  sender.sendTl("mayNotJail");
                  return;
               }

               if (args.length > 1) {
                  jailName = args[1];
                  break label114;
               }

               if (this.ess.getJails().getCount() == 1) {
                  jailName = (String)Iterables.get(this.ess.getJails().getList(), 0);
                  break label114;
               }
            }

            if (args.length >= 2 && player.isJailed() && !args[1].equalsIgnoreCase(player.getJail())) {
               sender.sendTl("jailAlreadyIncarcerated", player.getJail());
               return;
            }

            if (args.length >= 2 && player.isJailed() && args[1].equalsIgnoreCase(player.getJail())) {
               jailName = getFinalArg(args, 2);
               long displayTimeDiff = DateUtil.parseDateDiff(jailName, true);
               long timeDiff = DateUtil.parseDateDiff(jailName, true, this.ess.getSettings().isJailOnlineTime());
               player.setJailTimeout(timeDiff);
               player.setOnlineJailedTime(this.ess.getSettings().isJailOnlineTime() ? player.getOffline().getStatistic(PLAY_ONE_TICK) + timeDiff / 50L : 0L);
               sender.sendTl("jailSentenceExtended", DateUtil.formatDateDiff(displayTimeDiff));
               String tlKey = "jailNotifySentenceExtended";
               Object[] objects = new Object[]{player.getName(), DateUtil.formatDateDiff(displayTimeDiff), sender.getSender().getName()};
               this.ess.getLogger().log(Level.INFO, this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("jailNotifySentenceExtended", objects)));
               this.ess.broadcastTl(null, "essentials.jail.notify", "jailNotifySentenceExtended", objects);
               return;
            }

            if (args.length == 1 || args.length == 2 && args[1].equalsIgnoreCase(player.getJail())) {
               if (!player.isJailed()) {
                  throw new NotEnoughArgumentsException();
               }

               JailStatusChangeEvent event = new JailStatusChangeEvent(player, sender.isPlayer() ? this.ess.getUser(sender.getPlayer()) : null, false);
               this.ess.getServer().getPluginManager().callEvent(event);
               if (!event.isCancelled()) {
                  player.setJailed(false);
                  player.setJailTimeout(0L);
                  player.sendTl("jailReleasedPlayerNotify");
                  player.setJail(null);
                  if (player.getBase().isOnline()) {
                     CompletableFuture<Boolean> future = this.getNewExceptionFuture(sender, commandLabel);
                     future.thenAccept(success -> {
                        if (success) {
                           sender.sendTl("jailReleased", player.getName());
                        }
                     });
                     if (this.ess.getSettings().getTeleportWhenFreePolicy() == ISettings.TeleportWhenFreePolicy.BACK) {
                        player.getAsyncTeleport().back(future);
                        future.exceptionally(e -> {
                           player.getAsyncTeleport().respawn(null, TeleportCause.PLUGIN, new CompletableFuture<>());
                           sender.sendTl("jailReleased", player.getName());
                           return false;
                        });
                     } else if (this.ess.getSettings().getTeleportWhenFreePolicy() == ISettings.TeleportWhenFreePolicy.SPAWN) {
                        player.getAsyncTeleport().respawn(null, TeleportCause.PLUGIN, future);
                     }

                     return;
                  }

                  sender.sendTl("jailReleased", player.getName());
               }
            }

            return;
         }

         this.ess.getJails().getJail(jailName);
         JailStatusChangeEvent event = new JailStatusChangeEvent(player, sender.isPlayer() ? this.ess.getUser(sender.getPlayer()) : null, true);
         this.ess.getServer().getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            long displayTime = 0L;
            long preTimeDiff = 0L;
            if (args.length > 2) {
               String time = getFinalArg(args, 2);
               displayTime = DateUtil.parseDateDiff(time, true);
               preTimeDiff = DateUtil.parseDateDiff(time, true, this.ess.getSettings().isJailOnlineTime());
            }

            long timeDiff = preTimeDiff;
            long finalDisplayTime = displayTime;
            CompletableFuture<Boolean> future = this.getNewExceptionFuture(sender, commandLabel);
            future.thenAccept(
               success -> {
                  if (success) {
                     player.setJailed(true);
                     player.sendTl("userJailed");
                     player.setJail(null);
                     player.setJail(jailName);
                     if (args.length > 2) {
                        player.setJailTimeout(timeDiff);
                        player.setOnlineJailedTime(
                           this.ess.getSettings().isJailOnlineTime() ? player.getOffline().getStatistic(PLAY_ONE_TICK) + timeDiff / 50L : 0L
                        );
                     }

                     String tlKeyx;
                     Object[] objectsx;
                     if (timeDiff > 0L) {
                        tlKeyx = "jailNotifyJailedFor";
                        objectsx = new Object[]{player.getName(), DateUtil.formatDateDiff(finalDisplayTime), sender.getSender().getName()};
                        sender.sendTl("playerJailedFor", player.getName(), DateUtil.formatDateDiff(finalDisplayTime));
                     } else {
                        tlKeyx = "jailNotifyJailed";
                        objectsx = new Object[]{player.getName(), sender.getSender().getName(), sender.getSender().getName()};
                        sender.sendTl("playerJailed", player.getName());
                     }

                     this.ess.getLogger().log(Level.INFO, this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral(tlKeyx, objectsx)));
                     this.ess.broadcastTl(null, "essentials.jail.notify", tlKeyx, objectsx);
                  }
               }
            );
            if (player.getBase().isOnline()) {
               this.ess.getJails().sendToJail(player, jailName, future);
            } else {
               future.complete(true);
            }
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return this.getPlayers(sender);
      } else if (args.length == 2) {
         try {
            return new ArrayList<>(this.ess.getJails().getList());
         } catch (Exception var6) {
            return Collections.emptyList();
         }
      } else {
         return COMMON_DATE_DIFFS;
      }
   }
}
