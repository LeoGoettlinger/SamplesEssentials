package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.api.IWarps;
import com.earth2me.essentials.utils.NumberUtil;
import com.earth2me.essentials.utils.StringUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import net.ess3.api.IUser;
import net.ess3.api.TranslatableException;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandwarp extends EssentialsCommand {
   private static final int WARPS_PER_PAGE = 20;

   public Commandwarp() {
      super("warp");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length != 0 && !args[0].matches("[0-9]+")) {
         if (args.length != 2 || !user.isAuthorized("essentials.warp.otherplayers") && !user.isAuthorized("essentials.warp.others")) {
            this.warpUser(user, user, args[0], commandLabel);
            throw new NoChargeException();
         } else {
            User otherUser = this.getPlayer(server, user, args, 1);
            this.warpUser(user, otherUser, args[0], commandLabel);
            throw new NoChargeException();
         }
      } else if (!user.isAuthorized("essentials.warp.list")) {
         throw new TranslatableException("warpListPermission");
      } else {
         this.warpList(user.getSource(), args, user);
         throw new NoChargeException();
      }
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length >= 2 && !NumberUtil.isInt(args[0])) {
         User otherUser = this.getPlayer(server, args, 1, true, false);
         otherUser.getAsyncTeleport().warp(otherUser, args[0], null, TeleportCause.COMMAND, this.getNewExceptionFuture(sender, commandLabel));
         throw new NoChargeException();
      } else {
         this.warpList(sender, args, null);
         throw new NoChargeException();
      }
   }

   private void warpList(final CommandSource sender, final String[] args, final IUser user) throws Exception {
      IWarps warps = this.ess.getWarps();
      List<String> warpNameList = this.getAvailableWarpsFor(user);
      if (warpNameList.isEmpty()) {
         throw new TranslatableException("noWarpsDefined");
      } else {
         int page = 1;
         if (args.length > 0 && NumberUtil.isInt(args[0])) {
            page = Integer.parseInt(args[0]);
         }

         int maxPages = (int)Math.ceil(warpNameList.size() / 20.0);
         if (page > maxPages) {
            page = maxPages;
         }

         int warpPage = (page - 1) * 20;
         String warpList = StringUtil.joinList(warpNameList.subList(warpPage, warpPage + Math.min(warpNameList.size() - warpPage, 20)));
         if (warpNameList.size() > 20) {
            sender.sendTl("warpsCount", warpNameList.size(), page, maxPages);
            sender.sendTl("warpList", warpList);
         } else {
            sender.sendTl("warps", warpList);
         }
      }
   }

   private void warpUser(final User owner, final User user, final String name, final String commandLabel) throws Exception {
      Trade chargeWarp = new Trade("warp-" + name.toLowerCase(Locale.ENGLISH).replace('_', '-'), this.ess);
      Trade chargeCmd = new Trade(this.getName(), this.ess);
      BigDecimal fullCharge = chargeWarp.getCommandCost(user).add(chargeCmd.getCommandCost(user));
      Trade charge = new Trade(fullCharge, this.ess);
      charge.isAffordableFor(owner);
      if (this.ess.getSettings().getPerWarpPermission() && !owner.isAuthorized("essentials.warps." + name)) {
         throw new TranslatableException("warpUsePermission");
      } else {
         owner.getAsyncTeleport().warp(user, name, charge, TeleportCause.COMMAND, this.getNewExceptionFuture(user.getSource(), commandLabel));
      }
   }

   private List<String> getAvailableWarpsFor(final IUser user) {
      return (List<String>)(this.ess.getSettings().getPerWarpPermission() && user != null
         ? this.ess.getWarps().getList().stream().filter(warpName -> user.isAuthorized("essentials.warps." + warpName)).collect(Collectors.toList())
         : new ArrayList<>(this.ess.getWarps().getList()));
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1 && user.isAuthorized("essentials.warp.list")) {
         return this.getAvailableWarpsFor(user);
      } else {
         return args.length != 2 || !user.isAuthorized("essentials.warp.otherplayers") && !user.isAuthorized("essentials.warp.others")
            ? Collections.emptyList()
            : this.getPlayers(user);
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         return new ArrayList<>(this.ess.getWarps().getList());
      } else {
         return args.length == 2 ? this.getPlayers(sender) : Collections.emptyList();
      }
   }
}
