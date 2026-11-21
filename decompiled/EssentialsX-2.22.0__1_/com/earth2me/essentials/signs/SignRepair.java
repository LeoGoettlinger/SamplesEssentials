package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.commands.Commandrepair;
import com.earth2me.essentials.commands.NotEnoughArgumentsException;
import net.ess3.api.IEssentials;
import net.ess3.api.TranslatableException;

public class SignRepair extends EssentialsSign {
   public SignRepair() {
      super("Repair");
   }

   @Override
   protected boolean onSignCreate(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException {
      String repairTarget = sign.getLine(1);
      if (repairTarget.isEmpty()) {
         sign.setLine(1, "Hand");
      } else if (!repairTarget.equalsIgnoreCase("all") && !repairTarget.equalsIgnoreCase("hand")) {
         sign.setLine(1, "§c<hand|all>");
         throw new SignException("invalidSignLine", 2);
      }

      this.validateTrade(sign, 2, ess);
      return true;
   }

   @Override
   protected boolean onSignInteract(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
      Trade charge = this.getTrade(sign, 2, ess);
      charge.isAffordableFor(player);
      Commandrepair command = new Commandrepair();
      command.setEssentials(ess);

      try {
         if (sign.getLine(1).equalsIgnoreCase("hand")) {
            command.repairHand(player);
         } else {
            if (!sign.getLine(1).equalsIgnoreCase("all")) {
               throw new NotEnoughArgumentsException();
            }

            command.repairAll(player);
         }
      } catch (TranslatableException var8) {
         throw new SignException(var8.getTlKey(), var8.getArgs());
      } catch (Exception var9) {
         throw new SignException(var9, "errorWithMessage", var9.getMessage());
      }

      charge.charge(player);
      Trade.log("Sign", "Repair", "Interact", username, null, username, charge, sign.getBlock().getLocation(), player.getMoney(), ess);
      return true;
   }
}
