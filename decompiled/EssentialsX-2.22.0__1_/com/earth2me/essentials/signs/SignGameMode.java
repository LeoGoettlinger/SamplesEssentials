package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import java.util.Locale;
import net.ess3.api.IEssentials;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class SignGameMode extends EssentialsSign {
   public SignGameMode() {
      super("GameMode");
   }

   @Override
   protected boolean onSignCreate(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException {
      String gamemode = sign.getLine(1);
      if (gamemode.isEmpty()) {
         sign.setLine(1, "Survival");
      }

      this.validateTrade(sign, 2, ess);
      return true;
   }

   @Override
   protected boolean onSignInteract(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
      Trade charge = this.getTrade(sign, 2, ess);
      String mode = sign.getLine(1).trim();
      if (mode.isEmpty()) {
         throw new SignException("invalidSignLine", 2);
      } else {
         charge.isAffordableFor(player);
         this.performSetMode(mode.toLowerCase(Locale.ENGLISH), player.getBase());
         player.sendTl("gameMode", player.playerTl(player.getBase().getGameMode().toString().toLowerCase(Locale.ENGLISH)), player.getDisplayName());
         Trade.log("Sign", "gameMode", "Interact", username, null, username, charge, sign.getBlock().getLocation(), player.getMoney(), ess);
         charge.charge(player);
         return true;
      }
   }

   private void performSetMode(final String mode, final Player player) throws SignException {
      if (mode.contains("survi") || mode.equalsIgnoreCase("0")) {
         player.setGameMode(GameMode.SURVIVAL);
      } else if (mode.contains("creat") || mode.equalsIgnoreCase("1")) {
         player.setGameMode(GameMode.CREATIVE);
      } else if (!mode.contains("advent") && !mode.equalsIgnoreCase("2")) {
         if (!mode.contains("spec") && !mode.equalsIgnoreCase("3")) {
            throw new SignException("invalidSignLine", 2);
         }

         player.setGameMode(GameMode.SPECTATOR);
      } else {
         player.setGameMode(GameMode.ADVENTURE);
      }
   }
}
