package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import java.util.Collections;
import java.util.List;
import net.ess3.provider.DamageEventProvider;
import org.bukkit.Server;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

public class Commandsuicide extends EssentialsCommand {
   public Commandsuicide() {
      super("suicide");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      DamageEventProvider provider = this.ess.provider(DamageEventProvider.class);
      EntityDamageEvent ede = provider.callDamageEvent(user.getBase(), DamageCause.SUICIDE, Float.MAX_VALUE);
      ede.getEntity().setLastDamageCause(ede);
      user.getBase().setHealth(0.0);
      user.sendTl("suicideMessage");
      user.setDisplayNick();
      this.ess.broadcastTl(user, "suicideSuccess", new Object[]{user.getDisplayName()});
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      return Collections.emptyList();
   }
}
