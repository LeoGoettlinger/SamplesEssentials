package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.PlayerList;
import com.earth2me.essentials.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.bukkit.Server;

public class Commandlist extends EssentialsCommand {
   public Commandlist() {
      super("list");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      boolean showHidden = true;
      User user = null;
      if (sender.isPlayer()) {
         user = this.ess.getUser(sender.getPlayer());
         showHidden = user.isAuthorized("essentials.list.hidden") || user.canInteractVanished();
      }

      sender.sendComponent(this.ess.getAdventureFacet().deserializeMiniMessage(PlayerList.listSummary(this.ess, user, showHidden)));
      Map<String, List<User>> playerList = PlayerList.getPlayerLists(this.ess, user, showHidden);
      if (args.length > 0) {
         sender.sendComponent(this.ess.getAdventureFacet().deserializeMiniMessage(PlayerList.listGroupUsers(this.ess, playerList, args[0].toLowerCase())));
      } else {
         this.sendGroupedList(sender, commandLabel, playerList);
      }
   }

   private void sendGroupedList(final CommandSource sender, final String commandLabel, final Map<String, List<User>> playerList) {
      for (String str : PlayerList.prepareGroupedList(this.ess, sender, commandLabel, playerList)) {
         sender.sendComponent(this.ess.getAdventureFacet().deserializeMiniMessage(str));
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return (List<String>)(args.length == 1 ? new ArrayList<>(PlayerList.getPlayerLists(this.ess, sender.getUser(), false).keySet()) : Collections.emptyList());
   }
}
