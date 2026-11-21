package com.earth2me.essentials.commands;

import com.earth2me.essentials.ChargeException;
import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.VersionUtil;
import java.util.Collections;
import java.util.List;
import net.ess3.api.IUser;
import net.ess3.api.MaxMoneyException;
import org.bukkit.Server;

public class Commandice extends EssentialsLoopCommand {
   public Commandice() {
      super("ice");
   }

   @Override
   protected void run(Server server, CommandSource sender, String commandLabel, String[] args) throws Exception {
      if (VersionUtil.getServerBukkitVersion().isLowerThan(VersionUtil.v1_17_R01)) {
         sender.sendTl("unsupportedFeature");
      } else if (args.length == 0 && !sender.isPlayer()) {
         throw new NotEnoughArgumentsException();
      } else if (args.length > 0 && sender.isAuthorized("essentials.ice.others")) {
         this.loopOnlinePlayers(server, sender, false, true, args[0], null);
      } else {
         this.freezePlayer(sender.getUser());
      }
   }

   @Override
   protected void updatePlayer(Server server, CommandSource sender, User user, String[] args) throws NotEnoughArgumentsException, PlayerExemptException, ChargeException, MaxMoneyException {
      this.freezePlayer(user);
      sender.sendTl("iceOther", user.getDisplayName());
   }

   private void freezePlayer(final IUser user) {
      user.getBase().setFreezeTicks(user.getBase().getMaxFreezeTicks());
      user.sendTl("ice", new Object[0]);
   }

   @Override
   protected List<String> getTabCompleteOptions(Server server, CommandSource sender, String commandLabel, String[] args) {
      return args.length == 1 && sender.isAuthorized("essentials.ice.others") ? this.getPlayers(sender) : Collections.emptyList();
   }
}
