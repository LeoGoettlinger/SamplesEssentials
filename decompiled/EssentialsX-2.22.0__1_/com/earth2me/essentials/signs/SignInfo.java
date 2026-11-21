package com.earth2me.essentials.signs;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.textreader.IText;
import com.earth2me.essentials.textreader.KeywordReplacer;
import com.earth2me.essentials.textreader.TextInput;
import com.earth2me.essentials.textreader.TextPager;
import java.io.IOException;
import net.ess3.api.IEssentials;

public class SignInfo extends EssentialsSign {
   public SignInfo() {
      super("Info");
   }

   @Override
   protected boolean onSignCreate(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException {
      this.validateTrade(sign, 3, ess);
      return true;
   }

   @Override
   protected boolean onSignInteract(final EssentialsSign.ISign sign, final User player, final String username, final IEssentials ess) throws SignException, ChargeException {
      Trade charge = this.getTrade(sign, 3, ess);
      charge.isAffordableFor(player);
      String chapter = sign.getLine(1);
      String page = sign.getLine(2);

      try {
         player.setDisplayNick();
         IText input = new TextInput(player.getSource(), "info", true, ess);
         IText output = new KeywordReplacer(input, player.getSource(), ess);
         TextPager pager = new TextPager(output);
         pager.showPage(chapter, page, null, player.getSource());
      } catch (IOException var11) {
         throw new SignException(var11, "errorWithMessage", var11.getMessage());
      }

      charge.charge(player);
      Trade.log("Sign", "Info", "Interact", username, null, username, charge, sign.getBlock().getLocation(), player.getMoney(), ess);
      return true;
   }
}
