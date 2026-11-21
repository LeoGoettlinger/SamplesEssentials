package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import net.ess3.provider.ContainerProvider;
import org.bukkit.Server;

public class Commandgrindstone extends EssentialsCommand {
   public Commandgrindstone() {
      super("grindstone");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      ContainerProvider containerProvider = this.ess.provider(ContainerProvider.class);
      if (containerProvider == null) {
         user.sendTl("unsupportedBrand");
      } else {
         containerProvider.openGrindstone(user.getBase());
      }
   }
}
