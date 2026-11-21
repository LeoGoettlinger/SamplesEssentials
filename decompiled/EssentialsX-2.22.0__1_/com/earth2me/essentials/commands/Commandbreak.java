package com.earth2me.essentials.commands;

import com.earth2me.essentials.User;
import net.ess3.api.TranslatableException;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.event.block.BlockBreakEvent;

public class Commandbreak extends EssentialsCommand {
   public Commandbreak() {
      super("break");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      Block block = user.getTargetBlock(20);
      if (block.getType() == Material.AIR) {
         throw new NoChargeException();
      } else if (block.getType() == Material.BEDROCK && !user.isAuthorized("essentials.break.bedrock")) {
         throw new TranslatableException("noBreakBedrock");
      } else {
         BlockBreakEvent event = new BlockBreakEvent(block, user.getBase());
         server.getPluginManager().callEvent(event);
         if (event.isCancelled()) {
            throw new NoChargeException();
         } else {
            block.setType(Material.AIR);
         }
      }
   }
}
