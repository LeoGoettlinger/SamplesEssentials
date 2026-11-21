package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.craftbukkit.SetExpFix;
import com.earth2me.essentials.utils.CommonPlaceholders;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.EnumUtil;
import com.earth2me.essentials.utils.NumberUtil;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.bukkit.Server;
import org.bukkit.Statistic;

public class Commandwhois extends EssentialsCommand {
   private static final Statistic PLAY_ONE_TICK = EnumUtil.getStatistic("PLAY_ONE_MINUTE", "PLAY_ONE_TICK");

   public Commandwhois() {
      super("whois");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User user = this.getPlayer(server, sender, args, 0);
         sender.sendTl("whoisTop", user.getName());
         user.setDisplayNick();
         sender.sendTl("whoisNick", user.getDisplayName());
         sender.sendTl("whoisUuid", user.getBase().getUniqueId().toString());
         sender.sendTl("whoisHealth", user.getBase().getHealth());
         sender.sendTl("whoisHunger", user.getBase().getFoodLevel(), user.getBase().getSaturation());
         sender.sendTl("whoisExp", SetExpFix.getTotalExperience(user.getBase()), user.getBase().getLevel());
         sender.sendTl(
            "whoisLocation",
            user.getLocation().getWorld().getName(),
            user.getLocation().getBlockX(),
            user.getLocation().getBlockY(),
            user.getLocation().getBlockZ()
         );
         long playtimeMs = System.currentTimeMillis() - user.getBase().getStatistic(PLAY_ONE_TICK) * 50L;
         sender.sendTl("whoisPlaytime", DateUtil.formatDateDiff(playtimeMs));
         if (!this.ess.getSettings().isEcoDisabled()) {
            sender.sendTl("whoisMoney", AdventureUtil.parsed(NumberUtil.displayCurrency(user.getMoney(), this.ess)));
         }

         if (!sender.isPlayer() || this.ess.getUser(sender.getPlayer()).isAuthorized("essentials.whois.ip")) {
            sender.sendTl("whoisIPAddress", user.getBase().getAddress().getAddress().toString());
         }

         String location = user.getGeoLocation();
         if (location != null && (!sender.isPlayer() || this.ess.getUser(sender.getPlayer()).isAuthorized("essentials.geoip.show"))) {
            sender.sendTl("whoisGeoLocation", location);
         }

         sender.sendTl("whoisGamemode", sender.tl(user.getBase().getGameMode().toString().toLowerCase(Locale.ENGLISH)));
         sender.sendTl("whoisGod", CommonPlaceholders.trueFalse(sender, user.isGodModeEnabled()));
         sender.sendTl("whoisOp", CommonPlaceholders.trueFalse(sender, user.getBase().isOp()));
         sender.sendTl(
            "whoisFly",
            CommonPlaceholders.trueFalse(sender, user.getBase().getAllowFlight()),
            AdventureUtil.parsed(user.getBase().isFlying() ? sender.tl("flying") : sender.tl("notFlying"))
         );
         sender.sendTl("whoisSpeed", user.getBase().isFlying() ? user.getBase().getFlySpeed() : user.getBase().getWalkSpeed());
         sender.sendTl("whoisWhitelist", CommonPlaceholders.trueFalse(sender, user.getBase().isWhitelisted()));
         if (user.isAfk()) {
            sender.sendTl("whoisAFKSince", CommonPlaceholders.trueFalse(sender, true), DateUtil.formatDateDiff(user.getAfkSince()));
         } else {
            sender.sendTl("whoisAFK", CommonPlaceholders.trueFalse(sender, false));
         }

         sender.sendTl(
            "whoisJail",
            AdventureUtil.parsed(user.isJailed() ? (user.getJailTimeout() > 0L ? user.getFormattedJailTime() : sender.tl("true")) : sender.tl("false"))
         );
         long muteTimeout = user.getMuteTimeout();
         if (!user.hasMuteReason()) {
            sender.sendTl(
               "whoisMuted",
               AdventureUtil.parsed(user.isMuted() ? (muteTimeout > 0L ? DateUtil.formatDateDiff(muteTimeout) : sender.tl("true")) : sender.tl("false"))
            );
         } else {
            sender.sendTl(
               "whoisMutedReason",
               AdventureUtil.parsed(user.isMuted() ? (muteTimeout > 0L ? DateUtil.formatDateDiff(muteTimeout) : sender.tl("true")) : sender.tl("false")),
               user.getMuteReason()
            );
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getPlayers(sender) : Collections.emptyList();
   }
}
