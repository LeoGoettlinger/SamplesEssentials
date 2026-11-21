package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.utils.StringUtil;
import java.util.stream.Collectors;
import org.bukkit.Server;
import org.bukkit.entity.Player;

public class Commandjailedplayers extends EssentialsCommand {
   public Commandjailedplayers() {
      super("jailedplayers");
   }

   @Override
   protected void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (this.ess.getJailedPlayers() != null && !this.ess.getJailedPlayers().isEmpty()) {
         sender.sendTl("jailedPlayersList", StringUtil.joinList(", ", this.ess.getJailedPlayers().stream().map(Player::getName).collect(Collectors.toList())));
      } else {
         sender.sendTl("jailedPlayersEmpty");
      }
   }
}
