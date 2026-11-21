package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.SpawnMob;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import java.util.List;
import net.ess3.api.IEssentials;

public class SignSpawnmob extends EssentialsSign {
   public SignSpawnmob() {
      super("Spawnmob");
   }

   @Override
   protected boolean onSignCreate(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
      this.validateInteger(sign, 1);
      this.validateTrade(sign, 3, ess);
      return true;
   }

   @Override
   protected boolean onSignInteract(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
      Trade charge = this.getTrade(sign, 3, ess);
      charge.isAffordableFor(player);

      try {
         List<String> mobParts = SpawnMob.mobParts(sign.getLine(2));
         List<String> mobData = SpawnMob.mobData(sign.getLine(2));
         SpawnMob.spawnmob(ess, ess.getServer(), player.getSource(), player, mobParts, mobData, Integer.parseInt(sign.getLine(1)));
      } catch (Exception var8) {
         throw new SignException(var8, "errorWithMessage", var8.getMessage());
      }

      charge.charge(player);
      Trade.log("Sign", "Spawnmob", "Interact", username, null, username, charge, sign.getBlock().getLocation(), player.getMoney(), ess);
      return true;
   }
}
