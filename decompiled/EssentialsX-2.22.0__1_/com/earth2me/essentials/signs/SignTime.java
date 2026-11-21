package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.DescParseTickFormat;
import net.ess3.api.IEssentials;

public class SignTime extends EssentialsSign {
   public SignTime() {
      super("Time");
   }

   @Override
   protected boolean onSignCreate(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException {
      if (sign.getLine(1).isEmpty() && sign.getLine(2).isEmpty() && sign.getLine(3).isEmpty()) {
         return true;
      } else {
         this.validateTrade(sign, 2, ess);
         String timeString = sign.getLine(1);
         if ("Day".equalsIgnoreCase(timeString)) {
            sign.setLine(1, "§2Day");
            return true;
         } else if ("Night".equalsIgnoreCase(timeString)) {
            sign.setLine(1, "§2Night");
            return true;
         } else {
            throw new SignException("onlyDayNight");
         }
      }
   }

   @Override
   protected boolean onSignInteract(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
      if (sign.getLine(1).isEmpty() && sign.getLine(2).isEmpty() && sign.getLine(3).isEmpty()) {
         player.sendTl("timeWorldCurrentSign", DescParseTickFormat.format(player.getWorld().getTime()));
         return true;
      } else {
         Trade charge = this.getTrade(sign, 2, ess);
         charge.isAffordableFor(player);
         String timeString = sign.getLine(1);
         long time = player.getWorld().getTime();
         time -= time % 24000L;
         if ("§2Day".equalsIgnoreCase(timeString)) {
            player.getWorld().setTime(time + 24000L);
            charge.charge(player);
            Trade.log("Sign", "TimeDay", "Interact", username, null, username, charge, sign.getBlock().getLocation(), player.getMoney(), ess);
            return true;
         } else if ("§2Night".equalsIgnoreCase(timeString)) {
            player.getWorld().setTime(time + 37700L);
            charge.charge(player);
            Trade.log("Sign", "TimeNight", "Interact", username, null, username, charge, sign.getBlock().getLocation(), player.getMoney(), ess);
            return true;
         } else {
            throw new SignException("onlyDayNight");
         }
      }
   }
}
