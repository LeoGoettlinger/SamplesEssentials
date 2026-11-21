package com.earth2me.essentials.commands;

import com.earth2me.essentials.Trade;
import com.earth2me.essentials.User;
import com.earth2me.essentials.utils.LocationUtil;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import net.ess3.api.TranslatableException;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class Commandjump extends EssentialsCommand {
   public Commandjump() {
      super("jump");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (args.length > 0 && args[0].contains("lock") && user.isAuthorized("essentials.jump.lock")) {
         if (user.isFlyClickJump()) {
            user.setRightClickJump(false);
            user.sendTl("jumpEasterDisable");
         } else {
            user.setRightClickJump(true);
            user.sendTl("jumpEasterEnable");
         }
      } else {
         Location cloc = user.getLocation();

         Location loc;
         try {
            loc = LocationUtil.getTarget(user.getBase());
            loc.setYaw(cloc.getYaw());
            loc.setPitch(cloc.getPitch());
            loc.setY(loc.getY() + 1.0);
         } catch (NullPointerException var8) {
            throw new TranslatableException(var8, "jumpError");
         }

         Trade charge = new Trade(this.getName(), this.ess);
         charge.isAffordableFor(user);
         user.getAsyncTeleport().teleport(loc, charge, TeleportCause.COMMAND, this.getNewExceptionFuture(user.getSource(), commandLabel));
         throw new NoChargeException();
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final User user, final String commandLabel, final String[] args) {
      return (List<String>)(args.length == 1 && user.isAuthorized("essentials.jump.lock")
         ? Lists.newArrayList(new String[]{"lock", "unlock"})
         : Collections.emptyList());
   }
}
