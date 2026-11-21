package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.craftbukkit.BanLookup;
import com.earth2me.essentials.utils.CommonPlaceholders;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.FormatUtil;
import com.earth2me.essentials.utils.StringUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import net.ess3.api.TranslatableException;
import org.bukkit.BanEntry;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.BanList.Type;

public class Commandseen extends EssentialsCommand {
   public Commandseen() {
      super("seen");
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         final boolean showBan = sender.isAuthorized("essentials.seen.banreason");
         final boolean showIp = sender.isAuthorized("essentials.seen.ip");
         final boolean showLocation = sender.isAuthorized("essentials.seen.location");
         final boolean showWhitelist = sender.isAuthorized("essentials.seen.whitelist");
         final boolean searchAccounts = commandLabel.contains("alts") && sender.isAuthorized("essentials.seen.alts");

         User player;
         try {
            UUID uuid = UUID.fromString(args[0]);
            player = this.ess.getUser(uuid);
         } catch (IllegalArgumentException var12) {
            player = this.ess.getOfflineUser(args[0]);
         }

         if (player == null) {
            if (!searchAccounts) {
               if (sender.isAuthorized("essentials.seen.ipsearch") && FormatUtil.validIP(args[0])) {
                  if (this.ess.getServer().getBanList(Type.IP).isBanned(args[0])) {
                     sender.sendTl("isIpBanned", args[0]);
                  }

                  this.seenIP(sender, args[0], args[0]);
                  return;
               }

               if (this.ess.getServer().getBanList(Type.IP).isBanned(args[0])) {
                  sender.sendTl("isIpBanned", args[0]);
                  return;
               }

               if (BanLookup.isBanned(this.ess, args[0])) {
                  sender.sendTl("whoisBanned", showBan ? BanLookup.getBanEntry(this.ess, args[0]).getReason() : sender.tl("true"));
                  return;
               }
            }

            this.ess.getScheduler().runTaskAsynchronously(this.ess, new Runnable() {
               @Override
               public void run() {
                  User userFromBukkit = Commandseen.this.ess.getUsers().getUser(args[0]);

                  try {
                     if (userFromBukkit != null) {
                        this.showUserSeen(userFromBukkit);
                     } else {
                        try {
                           this.showUserSeen(Commandseen.this.getPlayer(server, sender, args, 0));
                        } catch (PlayerNotFoundException var3) {
                           throw new TranslatableException("playerNeverOnServer", args[0]);
                        }
                     }
                  } catch (Exception var4) {
                     Commandseen.this.ess.showError(sender, var4, commandLabel);
                  }
               }

               private void showUserSeen(final User user) {
                  Commandseen.this.showSeenMessage(sender, user, searchAccounts, showBan, showIp, showLocation, showWhitelist);
               }
            });
         } else {
            this.showSeenMessage(sender, player, searchAccounts, showBan, showIp, showLocation, showWhitelist);
         }
      }
   }

   private void showSeenMessage(
      final CommandSource sender,
      final User player,
      final boolean searchAccounts,
      final boolean showBan,
      final boolean showIp,
      final boolean showLocation,
      final boolean showWhitelist
   ) {
      if (searchAccounts) {
         this.seenIP(sender, player.getLastLoginAddress(), player.getDisplayName());
      } else if (player.getBase().isOnline() && this.canInteractWith(sender, player)) {
         this.seenOnline(sender, player, showIp);
      } else {
         this.seenOffline(sender, player, showBan, showIp, showLocation, showWhitelist);
      }
   }

   private void seenOnline(final CommandSource sender, final User user, final boolean showIp) {
      user.setDisplayNick();
      sender.sendTl("seenOnline", user.getDisplayName(), DateUtil.formatDateDiff(user.getLastLogin()));
      List<String> history = user.getPastUsernames();
      if (history != null && !history.isEmpty()) {
         sender.sendTl("seenAccounts", StringUtil.joinListSkip(", ", user.getName(), history));
      }

      if (sender.isAuthorized("essentials.seen.uuid")) {
         sender.sendTl("whoisUuid", user.getBase().getUniqueId().toString());
      }

      if (sender.isAuthorized("essentials.seen.firstlogin")) {
         sender.sendTl("whoisFirstLogin", DateUtil.formatDate(user.getBase().getFirstPlayed(), this.ess));
      }

      if (user.isAfk()) {
         sender.sendTl("whoisAFK", CommonPlaceholders.trueFalse(sender, true));
      }

      if (user.isJailed()) {
         sender.sendTl("whoisJail", user.getJailTimeout() > 0L ? user.getFormattedJailTime() : CommonPlaceholders.trueFalse(sender, true));
      }

      if (user.isMuted()) {
         long muteTimeout = user.getMuteTimeout();
         if (!user.hasMuteReason()) {
            sender.sendTl("whoisMuted", muteTimeout > 0L ? DateUtil.formatDateDiff(muteTimeout) : CommonPlaceholders.trueFalse(sender, true));
         } else {
            sender.sendTl(
               "whoisMutedReason", muteTimeout > 0L ? DateUtil.formatDateDiff(muteTimeout) : CommonPlaceholders.trueFalse(sender, true), user.getMuteReason()
            );
         }
      }

      String location = user.getGeoLocation();
      if (location != null && (!sender.isPlayer() || this.ess.getUser(sender.getPlayer()).isAuthorized("essentials.geoip.show"))) {
         sender.sendTl("whoisGeoLocation", location);
      }

      if (showIp) {
         sender.sendTl("whoisIPAddress", user.getBase().getAddress().getAddress().toString());
      }
   }

   private void seenOffline(
      final CommandSource sender, final User user, final boolean showBan, final boolean showIp, final boolean showLocation, final boolean showWhitelist
   ) {
      user.setDisplayNick();
      if (user.getLastLogout() > 0L) {
         sender.sendTl("seenOffline", user.getName(), DateUtil.formatDateDiff(user.getLastLogout()));
         List<String> history = user.getPastUsernames();
         if (history != null && history.size() > 1) {
            sender.sendTl("seenAccounts", StringUtil.joinListSkip(", ", user.getName(), history));
         }

         if (sender.isAuthorized("essentials.seen.uuid")) {
            sender.sendTl("whoisUuid", user.getBase().getUniqueId());
         }
      } else {
         sender.sendTl("userUnknown", user.getName());
      }

      if (showWhitelist) {
         sender.sendTl("whoisWhitelist", CommonPlaceholders.trueFalse(sender, user.getBase().isWhitelisted()));
      }

      if (BanLookup.isBanned(this.ess, user)) {
         BanEntry banEntry = BanLookup.getBanEntry(this.ess, user.getName());
         Object reason = showBan ? banEntry.getReason() : CommonPlaceholders.trueFalse(sender, true);
         sender.sendTl("whoisBanned", reason);
         if (banEntry.getExpiration() != null) {
            Date expiry = banEntry.getExpiration();
            Object expireString = AdventureUtil.parsed(sender.tl("now"));
            if (expiry.after(new Date())) {
               expireString = DateUtil.formatDateDiff(expiry.getTime());
            }

            sender.sendTl("whoisTempBanned", expireString);
         }
      }

      if (user.isMuted()) {
         long muteTimeout = user.getMuteTimeout();
         if (!user.hasMuteReason()) {
            sender.sendTl("whoisMuted", muteTimeout > 0L ? DateUtil.formatDateDiff(muteTimeout) : CommonPlaceholders.trueFalse(sender, true));
         } else {
            sender.sendTl(
               "whoisMutedReason", muteTimeout > 0L ? DateUtil.formatDateDiff(muteTimeout) : CommonPlaceholders.trueFalse(sender, true), user.getMuteReason()
            );
         }
      }

      String location = user.getGeoLocation();
      if (location != null && (!sender.isPlayer() || this.ess.getUser(sender.getPlayer()).isAuthorized("essentials.geoip.show"))) {
         sender.sendTl("whoisGeoLocation", location);
      }

      if (showIp && !user.getLastLoginAddress().isEmpty()) {
         sender.sendTl("whoisIPAddress", user.getLastLoginAddress());
      }

      if (showLocation) {
         Location loc = user.getLogoutLocation();
         if (loc != null) {
            sender.sendTl("whoisLocation", loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
         }
      }
   }

   private void seenIP(final CommandSource sender, final String ipAddress, final String display) {
      sender.sendTl("runningPlayerMatch", AdventureUtil.parsed(this.ess.getAdventureFacet().legacyToMini(display)));
      this.ess.runTaskAsynchronously(() -> {
         List<String> matches = new ArrayList<>();

         for (UUID u : this.ess.getUsers().getAllUserUUIDs()) {
            User user = this.ess.getUsers().loadUncachedUser(u);
            if (user != null) {
               String uIPAddress = user.getLastLoginAddress();
               if (!uIPAddress.isEmpty() && uIPAddress.equalsIgnoreCase(ipAddress)) {
                  matches.add(user.getName());
               }
            }
         }

         if (matches.size() > 0) {
            sender.sendTl("matchingIPAddress");
            sender.sendTl("matchingAccounts", StringUtil.joinList(matches));
         } else {
            sender.sendTl("noMatchingPlayers");
         }
      });
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getPlayers(sender) : Collections.emptyList();
   }
}
