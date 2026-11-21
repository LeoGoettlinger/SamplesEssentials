package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.Console;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.FormatUtil;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import net.ess3.api.TranslatableException;
import net.essentialsx.api.v2.events.UserKickEvent;
import org.bukkit.Server;

public class Commandkick extends EssentialsCommand {
   public Commandkick() {
      super("kick");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         User target = this.getPlayer(server, args, 0, true, false);
         User user = sender.isPlayer() ? this.ess.getUser(sender.getPlayer()) : null;
         if (user != null) {
            if (target.isHidden(sender.getPlayer()) && !user.canInteractVanished() && target.isHiddenFrom(sender.getPlayer())) {
               throw new PlayerNotFoundException();
            }

            if (target.isAuthorized("essentials.kick.exempt")) {
               throw new TranslatableException("kickExempt");
            }
         }

         String kickReason = args.length > 1 ? getFinalArg(args, 1) : this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("kickDefault"));
         kickReason = FormatUtil.replaceFormat(kickReason.replace("\\n", "\n").replace("|", "\n"));
         UserKickEvent event = new UserKickEvent(user, target, kickReason);
         this.ess.getServer().getPluginManager().callEvent(event);
         if (!event.isCancelled()) {
            kickReason = event.getReason();
            target.getBase().kickPlayer(kickReason);
            String senderDisplayName = sender.isPlayer() ? sender.getPlayer().getDisplayName() : Console.displayName();
            String tlKey = "playerKicked";
            Object[] objects = new Object[]{senderDisplayName, target.getName(), kickReason};
            this.ess.getLogger().log(Level.INFO, this.ess.getAdventureFacet().miniToLegacy(I18n.tlLiteral("playerKicked", objects)));
            this.ess.broadcastTl(null, "essentials.kick.notify", "playerKicked", objects);
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getPlayers(sender) : Collections.emptyList();
   }
}
