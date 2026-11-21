package com.earth2me.essentials.commands;

import com.earth2me.essentials.CommandSource;
import com.earth2me.essentials.User;
import java.util.Collections;
import java.util.List;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.potion.PotionEffect;

public class Commandheal extends EssentialsLoopCommand {
   public Commandheal() {
      super("heal");
   }

   @Override
   public void run(final Server server, final User user, final String commandLabel, final String[] args) throws Exception {
      if (!user.isAuthorized("essentials.heal.cooldown.bypass")) {
         user.healCooldown();
      }

      if (args.length > 0 && user.isAuthorized("essentials.heal.others")) {
         this.loopOnlinePlayers(server, user.getSource(), true, true, args[0], null);
      } else {
         this.updatePlayer(server, user.getSource(), user, args);
      }
   }

   @Override
   public void run(final Server server, final CommandSource sender, final String commandLabel, final String[] args) throws Exception {
      if (args.length < 1) {
         throw new NotEnoughArgumentsException();
      } else {
         this.loopOnlinePlayers(server, sender, true, true, args[0], null);
      }
   }

   @Override
   protected void updatePlayer(final Server server, final CommandSource sender, final User user, final String[] args) throws PlayerExemptException {
      try {
         Player player = user.getBase();
         if (player.getHealth() == 0.0) {
            throw new PlayerExemptException("healDead");
         }

         double amount = player.getMaxHealth() - player.getHealth();
         EntityRegainHealthEvent erhe = new EntityRegainHealthEvent(player, amount, RegainReason.CUSTOM);
         this.ess.getServer().getPluginManager().callEvent(erhe);
         if (erhe.isCancelled()) {
            throw new QuietAbortException();
         }

         double newAmount = player.getHealth() + erhe.getAmount();
         if (newAmount > player.getMaxHealth()) {
            newAmount = player.getMaxHealth();
         }

         player.setHealth(newAmount);
         player.setFoodLevel(20);
         player.setFireTicks(0);
         player.setRemainingAir(player.getMaximumAir());
         user.sendTl("heal");
         if (this.ess.getSettings().isRemovingEffectsOnHeal()) {
            for (PotionEffect effect : player.getActivePotionEffects()) {
               player.removePotionEffect(effect.getType());
            }
         }

         if (!sender.isPlayer() || !user.getBase().equals(sender.getPlayer())) {
            sender.sendTl("healOther", user.getDisplayName());
         }
      } catch (QuietAbortException var13) {
      }
   }

   @Override
   protected List<String> getTabCompleteOptions(final Server server, final CommandSource sender, final String commandLabel, final String[] args) {
      return args.length == 1 && sender.isAuthorized("essentials.heal.others") ? this.getPlayers(sender) : Collections.emptyList();
   }
}
