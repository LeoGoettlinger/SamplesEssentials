package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.adventure.AdventureUtil;
import com.earth2me.essentials.utils.DateUtil;
import com.earth2me.essentials.utils.EnumUtil;
import com.earth2me.essentials.utils.VersionUtil;
import java.util.Collections;
import java.util.List;
import net.ess3.api.IUser;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.Statistic;

public class Commandplaytime extends EssentialsCommand {
   private static final Statistic PLAY_ONE_TICK = EnumUtil.getStatistic("PLAY_ONE_MINUTE", "PLAY_ONE_TICK");

   public Commandplaytime() {
      super("playtime");
   }

   @Override
   protected void run(Server server, CommandSource sender, String commandLabel, String[] args) throws Exception {
      String displayName;
      long playtime;
      String key;
      if (args.length > 0 && sender.isAuthorized("essentials.playtime.others")) {
         try {
            IUser user = this.getPlayer(server, sender, args, 0);
            displayName = user.getDisplayName();
            playtime = user.getBase().getStatistic(PLAY_ONE_TICK);
         } catch (PlayerNotFoundException var11) {
            if (VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_15_2_R01)) {
               throw var11;
            }

            User userx = this.getPlayer(server, args, 0, true, true);
            displayName = userx.getName();
            playtime = Bukkit.getOfflinePlayer(userx.getBase().getUniqueId()).getStatistic(PLAY_ONE_TICK);
            if (userx.getBase().isOnline() && userx.isVanished()) {
               playtime -= (System.currentTimeMillis() - userx.getLastVanishTime()) / 50L;
            }
         }

         key = "playtimeOther";
      } else {
         if (!sender.isPlayer()) {
            throw new NotEnoughArgumentsException();
         }

         displayName = sender.getPlayer().getDisplayName();
         playtime = sender.getPlayer().getStatistic(PLAY_ONE_TICK);
         key = "playtime";
      }

      long playtimeMs = System.currentTimeMillis() - playtime * 50L;
      sender.sendTl(key, DateUtil.formatDateDiff(playtimeMs), AdventureUtil.parsed(this.ess.getAdventureFacet().legacyToMini(displayName)));
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 && sender.isAuthorized("essentials.playtime.others") ? this.getPlayers(sender) : Collections.emptyList();
   }
}
