package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Kit;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.commands.NoChargeException;
import java.util.Locale;
import net.ess3.api.IEssentials;

public class SignKit extends EssentialsSign {
   public SignKit() {
      super("Kit");
   }

   @Override
   protected boolean onSignCreate(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException {
      this.validateTrade(sign, 3, ess);
      String kitName = sign.getLine(1).toLowerCase(Locale.ENGLISH).trim();
      if (kitName.isEmpty()) {
         sign.setLine(1, "§dKit name!");
         return false;
      } else {
         try {
            ess.getKits().getKit(kitName);
         } catch (Exception var7) {
            throw new SignException(var7, "errorWithMessage", var7.getMessage());
         }

         String group = sign.getLine(2);
         if ("Everyone".equalsIgnoreCase(group) || "Everybody".equalsIgnoreCase(group)) {
            sign.setLine(2, "§2Everyone");
         }

         return true;
      }
   }

   @Override
   protected boolean onSignInteract(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
      String kitName = sign.getLine(1).toLowerCase(Locale.ENGLISH).trim();
      String group = sign.getLine(2).trim();
      if ((group.isEmpty() || !"§2Everyone".equals(group) && !player.inGroup(group))
         && (!group.isEmpty() || !player.isAuthorized("essentials.kits." + kitName))) {
         if (group.isEmpty()) {
            throw new SignException("noKitPermission", "essentials.kits." + kitName);
         } else {
            throw new SignException("noKitGroup", group);
         }
      } else {
         Trade charge = this.getTrade(sign, 3, ess);
         charge.isAffordableFor(player);

         try {
            Kit kit = new Kit(kitName, ess);
            kit.checkDelay(player);
            kit.setTime(player);
            kit.expandItems(player);
            charge.charge(player);
            Trade.log("Sign", "Kit", "Interact", username, null, username, charge, sign.getBlock().getLocation(), player.getMoney(), ess);
            return true;
         } catch (NoChargeException var9) {
            return false;
         } catch (Exception var10) {
            throw new SignException(var10, "errorWithMessage", var10.getMessage());
         }
      }
   }
}
