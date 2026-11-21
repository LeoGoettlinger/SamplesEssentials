package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.VersionUtil;
import java.util.Collections;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.inventory.Inventory;

public class Commandinvsee extends EssentialsCommand {
   public Commandinvsee() {
      super("invsee");
   }

   @Override
   protected void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User invUser = this.getPlayer(server, user, args, 0);
         if (user == invUser) {
            user.sendTl("invseeNoSelf");
            throw new NoChargeException();
         } else {
            Inventory inv;
            if (args.length > 1 && user.isAuthorized("essentials.invsee.equip")) {
               inv = server.createInventory(invUser.getBase(), 9, user.playerTl("equipped"));
               inv.setContents(invUser.getBase().getInventory().getArmorContents());
               if (VersionUtil.getServerBukkitVersion().isHigherThanOrEqualTo(VersionUtil.v1_9_4_R01)) {
                  inv.setItem(4, invUser.getBase().getInventory().getItemInOffHand());
               }
            } else {
               inv = invUser.getBase().getInventory();
            }

            user.getBase().closeInventory();
            user.getBase().openInventory(inv);
            user.setInvSee(true);
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      if (args.length == 1) {
         List<String> suggestions = this.getPlayers(user);
         suggestions.remove(user.getName());
         return suggestions;
      } else {
         return Collections.emptyList();
      }
   }
}
