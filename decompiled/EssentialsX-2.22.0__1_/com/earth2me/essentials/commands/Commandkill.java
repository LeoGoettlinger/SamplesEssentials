package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import java.util.Collections;
import java.util.List;
import net.ess3.provider.DamageEventProvider;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class Commandkill extends EssentialsLoopCommand {
   public Commandkill() {
      super("kill");
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length == 0) {
         throw new NotEnoughArgumentsException();
      } else {
         this.loopOnlinePlayers(server, sender, true, true, args[0], null);
      }
   }

   @Override
   protected void updatePlayer(final Server server, final CommandSource sender, final User user, final String[] args) throws PlayerExemptException {
      Player matchPlayer = user.getBase();
      if (sender.isPlayer() && user.isAuthorized("essentials.kill.exempt") && !this.ess.getUser(sender.getPlayer()).isAuthorized("essentials.kill.force")) {
         throw new PlayerExemptException("killExempt", matchPlayer.getDisplayName());
      } else {
         DamageEventProvider provider = this.ess.provider(DamageEventProvider.class);
         EntityDamageEvent ede = provider.callDamageEvent(
            matchPlayer,
            sender.isPlayer() && sender.getPlayer().getName().equals(matchPlayer.getName()) ? DamageCause.SUICIDE : DamageCause.CUSTOM,
            Float.MAX_VALUE
         );
         if (!ede.isCancelled() || !sender.isPlayer() || this.ess.getUser(sender.getPlayer()).isAuthorized("essentials.kill.force")) {
            ede.getEntity().setLastDamageCause(ede);
            matchPlayer.setHealth(0.0);
            sender.sendTl("kill", matchPlayer.getDisplayName());
         }
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 ? this.getPlayers(sender) : Collections.emptyList();
   }
}
